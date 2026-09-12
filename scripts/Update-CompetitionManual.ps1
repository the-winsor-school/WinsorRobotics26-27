[CmdletBinding()]
param(
    [string] $OutputDirectory = (Join-Path $PSScriptRoot '..\doc\game-manual\rendered'),
    [ValidateRange(1, 99)]
    [int] $MaximumVersion = 20
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

Add-Type -AssemblyName System.Net.Http
$client = [System.Net.Http.HttpClient]::new()
$client.DefaultRequestHeaders.UserAgent.ParseAdd('WinsorRobotics-competition-manual-mirror/1.0')

function Get-PinnedPandoc {
    $version = '3.10.2'
    $isWindowsPlatform = [Environment]::OSVersion.Platform -eq [PlatformID]::Win32NT

    if ($isWindowsPlatform) {
        $platform = 'windows-x86_64'
        $archiveName = "pandoc-$version-windows-x86_64.zip"
        $expectedHash = '52487faaa63f8cef5363d5a771097da001228d61c6f44f32ed41b27a98c0278c'
        $executableName = 'pandoc.exe'
    }
    elseif ([Runtime.InteropServices.RuntimeInformation]::IsOSPlatform([Runtime.InteropServices.OSPlatform]::Linux)) {
        $platform = 'linux-amd64'
        $archiveName = "pandoc-$version-linux-amd64.tar.gz"
        $expectedHash = 'c7edd535941c48be6a362081a748272837de81ae11777202d9c341d3d8261c9a'
        $executableName = 'pandoc'
    }
    else {
        throw 'Automatic Markdown conversion currently supports 64-bit Windows and Linux.'
    }

    $toolRoot = Join-Path ([IO.Path]::GetTempPath()) "WinsorRobotics-tools\pandoc-$version-$platform"
    $pandocPath = Join-Path $toolRoot "pandoc-$version\$executableName"
    if (Test-Path -LiteralPath $pandocPath) {
        return $pandocPath
    }

    New-Item -ItemType Directory -Path $toolRoot -Force | Out-Null
    $archivePath = Join-Path $toolRoot $archiveName
    $downloadUrl = "https://github.com/jgm/pandoc/releases/download/$version/$archiveName"
    $downloadResponse = $client.GetAsync($downloadUrl).GetAwaiter().GetResult()
    try {
        if (-not $downloadResponse.IsSuccessStatusCode) {
            throw "Unable to download Pandoc $version (HTTP $([int]$downloadResponse.StatusCode))."
        }
        [IO.File]::WriteAllBytes(
            $archivePath,
            $downloadResponse.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
        )
    }
    finally {
        $downloadResponse.Dispose()
    }

    $actualHash = (Get-FileHash -LiteralPath $archivePath -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualHash -ne $expectedHash) {
        throw "Pandoc archive checksum mismatch. Expected $expectedHash but received $actualHash."
    }

    if ($isWindowsPlatform) {
        Expand-Archive -LiteralPath $archivePath -DestinationPath $toolRoot -Force
    }
    else {
        & tar -xzf $archivePath -C $toolRoot
        if ($LASTEXITCODE -ne 0) {
            throw "Unable to extract $archivePath."
        }
    }

    if (-not (Test-Path -LiteralPath $pandocPath)) {
        throw "Pandoc executable was not found after extracting $archivePath."
    }
    return $pandocPath
}

try {
    $manualTemplate = 'https://ftc-resources.firstinspires.org/ftc/game/cm-html/BIOBUZZ%20Competition%20Manual%20-%20V{0}.htm'
    $latest = $null

    # FIRST publishes revisions as V1, V2, and so on. Probe ahead so a new URL is
    # found even when the previous revision remains unchanged.
    foreach ($version in 1..$MaximumVersion) {
        $sourceUrl = $manualTemplate -f $version
        $response = $client.GetAsync($sourceUrl).GetAwaiter().GetResult()
        try {
            if ($response.IsSuccessStatusCode) {
                $latest = [pscustomobject]@{
                    Version      = $version
                    Url          = $sourceUrl
                    Bytes        = $response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
                    ETag         = if ($response.Headers.ETag) { $response.Headers.ETag.Tag } else { $null }
                    LastModified = if ($response.Content.Headers.LastModified) {
                        $response.Content.Headers.LastModified.UtcDateTime.ToString('yyyy-MM-ddTHH:mm:ssZ')
                    } else { $null }
                }
            }
        }
        finally {
            $response.Dispose()
        }
    }

    if (-not $latest) {
        throw "No BIOBUZZ Competition Manual was found in versions 1 through $MaximumVersion."
    }

    $outputPath = [IO.Path]::GetFullPath($OutputDirectory)
    $parentPath = Split-Path -Parent $outputPath
    $stagingPath = Join-Path $parentPath ('.rendered-staging-{0}' -f $PID)

    if (Test-Path -LiteralPath $stagingPath) {
        Remove-Item -LiteralPath $stagingPath -Recurse -Force
    }
    New-Item -ItemType Directory -Path $stagingPath | Out-Null

    try {
        $encoding = [Text.Encoding]::GetEncoding(1252)
        $html = $encoding.GetString($latest.Bytes)
        $referencePattern = '(?i)(?:href|src)\s*=\s*(?:"([^"]+)"|''([^'']+)''|([^\s>]+))'
        $references = [regex]::Matches($html, $referencePattern) | ForEach-Object {
            if ($_.Groups[1].Success) { $_.Groups[1].Value }
            elseif ($_.Groups[2].Success) { $_.Groups[2].Value }
            else { $_.Groups[3].Value }
        } | Where-Object {
            $_ -notmatch '^(?:#|https?:|mailto:|javascript:)' -and $_ -notmatch '#'
        } | Sort-Object -Unique

        $downloadedReferences = @()
        foreach ($reference in $references) {
            $decodedReference = [Uri]::UnescapeDataString($reference).Replace('/', [IO.Path]::DirectorySeparatorChar)
            if ([IO.Path]::IsPathRooted($decodedReference) -or $decodedReference -match '(^|[\\/])\.\.([\\/]|$)') {
                throw "Unsafe relative asset path in the manual: $reference"
            }

            $destination = Join-Path $stagingPath $decodedReference
            $destinationDirectory = Split-Path -Parent $destination
            New-Item -ItemType Directory -Path $destinationDirectory -Force | Out-Null

            $assetUri = [Uri]::new([Uri]$latest.Url, $reference)
            $assetResponse = $client.GetAsync($assetUri).GetAwaiter().GetResult()
            try {
                if (-not $assetResponse.IsSuccessStatusCode) {
                    $extension = [IO.Path]::GetExtension($decodedReference)
                    if ($extension -match '^(?i:\.png|\.jpe?g|\.gif|\.svg|\.webp)$') {
                        throw "Unable to download required image $assetUri (HTTP $([int]$assetResponse.StatusCode))."
                    }
                    Write-Warning "Skipping unavailable optional Word metadata $assetUri (HTTP $([int]$assetResponse.StatusCode))."
                    continue
                }
                [IO.File]::WriteAllBytes(
                    $destination,
                    $assetResponse.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
                )
                $downloadedReferences += $reference
            }
            finally {
                $assetResponse.Dispose()
            }
        }

        [IO.File]::WriteAllBytes((Join-Path $stagingPath 'index.htm'), $latest.Bytes)

        $pandocPath = Get-PinnedPandoc
        $markdownPath = Join-Path $stagingPath 'manual.md'
        $wordStyleFilter = Join-Path $PSScriptRoot 'strip-word-styles.lua'
        & $pandocPath `
            --from=html `
            --to=gfm-raw_html `
            --lua-filter=$wordStyleFilter `
            --wrap=none `
            --markdown-headings=atx `
            --output=$markdownPath `
            (Join-Path $stagingPath 'index.htm')
        if ($LASTEXITCODE -ne 0) {
            throw "Pandoc failed to render the Competition Manual as Markdown (exit code $LASTEXITCODE)."
        }

        # Pandoc uses the host platform's line endings. Normalize them so the
        # scheduled Linux check and Windows refreshes produce identical files.
        $markdown = [IO.File]::ReadAllText($markdownPath, [Text.Encoding]::UTF8)
        $markdown = $markdown.Replace("`r`n", "`n").Replace("`r", "`n")
        [IO.File]::WriteAllText($markdownPath, $markdown, [Text.UTF8Encoding]::new($false))

        $sha256 = [Security.Cryptography.SHA256]::Create()
        try {
            $hash = [BitConverter]::ToString($sha256.ComputeHash($latest.Bytes)).Replace('-', '').ToLowerInvariant()
        }
        finally {
            $sha256.Dispose()
        }

        $metadata = [ordered]@{
            source_url    = $latest.Url
            source_version = $latest.Version
            source_etag   = $latest.ETag
            source_last_modified_utc = $latest.LastModified
            source_sha256 = $hash
            asset_count   = @($downloadedReferences).Count
            markdown_converter = 'pandoc 3.10.2'
        }
        $metadataJson = ($metadata | ConvertTo-Json -Compress) + "`n"
        [IO.File]::WriteAllText(
            (Join-Path $stagingPath 'source.json'),
            $metadataJson,
            [Text.UTF8Encoding]::new($false)
        )

        if (Test-Path -LiteralPath $outputPath) {
            Remove-Item -LiteralPath $outputPath -Recurse -Force
        }
        Move-Item -LiteralPath $stagingPath -Destination $outputPath
    }
    catch {
        if (Test-Path -LiteralPath $stagingPath) {
            Remove-Item -LiteralPath $stagingPath -Recurse -Force
        }
        throw
    }

    Write-Host "Mirrored BIOBUZZ Competition Manual V$($latest.Version) to $outputPath"
}
finally {
    $client.Dispose()
}
