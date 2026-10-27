# Source checks for drafted chapters.  Editorial
# candidates are printed for reading; they are not automatic rewrite rules.
param([string[]]$Keys = @())
$ErrorActionPreference = 'Stop'
$guideRoot = Split-Path $PSScriptRoot
$repoRoot = (Resolve-Path (Join-Path $guideRoot '../..')).Path
$javaRoot = Join-Path $repoRoot 'TeamCode/src/main/java/org/firstinspires/ftc/teamcode'
function Normalize-Excerpt([string]$value) {
    # Existing chapters dedent copied blocks.  Ignore only line-edge whitespace;
    # preserve all tokens, comments, line breaks, and internal spacing.
    (($value.Replace("`r`n", "`n").Split("`n") | ForEach-Object { $_.Trim() }) -join "`n").Trim()
}
$sampleRoot = Join-Path $repoRoot 'FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples'
$javaSources = @(foreach ($root in @($javaRoot, $sampleRoot)) {
    Get-ChildItem -LiteralPath $root -Recurse -Filter '*.java' | ForEach-Object {
        [pscustomobject]@{ Path = $_.FullName.Substring($repoRoot.Length + 1); Text = Normalize-Excerpt ([IO.File]::ReadAllText($_.FullName)) }
    }
})
$historical = @(
    @{ Chapter = '09'; Label = '1a6809a'; Revision = '1a6809a^'; File = 'TeamCode/src/main/java/org/firstinspires/ftc/teamcode/RobotModel/Mechs/Assemblies/BillyMA.java' },
    @{ Chapter = '10'; Label = '34c22ea'; Revision = '34c22ea'; File = 'TeamCode/src/main/java/org/firstinspires/ftc/teamcode/RobotModel/Robots/BillyRobot.java' }
)
function Test-Excerpt([string]$source, [string[]]$chunks) {
    $offset = 0
    foreach ($chunk in $chunks) {
        $index = $source.IndexOf($chunk, $offset, [StringComparison]::Ordinal)
        if ($index -lt 0) { return $false }
        $offset = $index + $chunk.Length
    }
    return $true
}
$problems = New-Object 'Collections.Generic.List[string]'
$fence = [string][char]96 * 3
$entries = @{}
$allFiles = @(Get-ChildItem (Join-Path $guideRoot 'chapters') -Filter 'chapter_*.md')
$files = @($allFiles | Where-Object { -not $Keys.Count -or $Keys -contains ([regex]::Match($_.Name, 'chapter_(\d\d)_').Groups[1].Value) })
foreach ($key in $Keys) {
    if (-not @($files | Where-Object { $_.Name -like "chapter_${key}_*" }).Count) { $problems.Add("No chapter file for key $key") }
}
foreach ($file in $files) {
    $text = [IO.File]::ReadAllText($file.FullName)
    foreach ($m in [regex]::Matches($text, '(?m)^## (\d+\.\d+)\s')) { $entries[$m.Groups[1].Value] = $true }
}
foreach ($file in $files) {
    $key = [regex]::Match($file.Name, 'chapter_(\d\d)_').Groups[1].Value
    $text = [IO.File]::ReadAllText($file.FullName).Replace("`r`n", "`n")
    $count = [regex]::Matches($text, '(?m)^## \d+\.\d+\s').Count
    if ($count -lt 6 -or $count -gt 10) { $problems.Add("$($file.Name): $count entries") }
    foreach ($suffix in @("planning/chapter_${key}_notes.md", "book/opening_${key}.md")) {
        if (-not (Test-Path (Join-Path $guideRoot $suffix))) { $problems.Add("Missing $suffix") }
    }
    foreach ($mark in @(@('Predict',3), @('Argue it',2))) {
        $countMark = [regex]::Matches($text, '(?m)^> \*\*' + [regex]::Escape($mark[0]) + '\.\*\*').Count
        if ($countMark -gt $mark[1]) { $problems.Add("$($file.Name): too many $($mark[0]) boxes ($countMark)") }
    }
    # Check entry numbers within the selected section.  Other chapters may
    # still be partial; their references require the later whole-book review.
    $referenceKeys = @($files | ForEach-Object { [int]([regex]::Match($_.Name, 'chapter_(\d\d)_').Groups[1].Value) })
    foreach ($m in [regex]::Matches($text, '(?<![\d.])\d{1,2}\.\d+(?![\d.])')) {
        if ($referenceKeys -notcontains [int]($m.Value.Split('.')[0])) { continue }
        # Ratios such as 18.93:1 are numerical data.  PROCESS caps entries at 10.
        if ([int]($m.Value.Split('.')[1]) -gt 10) { continue }
        if (-not $entries.ContainsKey($m.Value)) { $problems.Add("$($file.Name): unresolved entry $($m.Value)") }
    }
    Write-Output "$($file.Name): $count entries, $([Text.Encoding]::UTF8.GetByteCount($text)) bytes"
}
$allMarkdown = $files + @(Get-ChildItem (Join-Path $guideRoot 'book') -Filter 'opening_*.md' | Where-Object { -not $Keys.Count -or $Keys -contains ([regex]::Match($_.Name, 'opening_(\d\d)').Groups[1].Value) })
foreach ($file in $allMarkdown) {
    $text = [IO.File]::ReadAllText($file.FullName).Replace("`r`n", "`n")
    foreach ($m in [regex]::Matches($text, '(?ms)^' + $fence + 'java\n(.*?)\n' + $fence)) {
        $code = $m.Groups[1].Value
        $chunks = @([regex]::Split($code, '(?m)^\s*\.\.\.\s*$') | ForEach-Object { Normalize-Excerpt $_ } | Where-Object { $_.Length })
        if (-not $chunks.Count) { $problems.Add("$($file.Name): empty Java excerpt"); continue }
        $source = @($javaSources | Where-Object { Test-Excerpt $_.Text $chunks })
        $line = $text.Substring(0, $m.Index).Split("`n").Count
        if ($source.Count) { Write-Output "$($file.Name):${line}: source verified (line-edge whitespace ignored) in $($source[0].Path)"; continue }
        $key = [regex]::Match($file.Name, 'chapter_(\d\d)_').Groups[1].Value
        $prefix = $text.Substring([Math]::Max(0, $m.Index - 1000), [Math]::Min(1000, $m.Index))
        $matchedHistory = $false
        foreach ($old in $historical) {
            if ($old.Chapter -ne $key -or -not $prefix.Contains($old.Label)) { continue }
            $oldText = & git -C $repoRoot show "$($old.Revision):$($old.File)"
            if ($LASTEXITCODE -ne 0) { throw "Cannot read historical source $($old.Revision)" }
            if (Test-Excerpt (Normalize-Excerpt ($oldText -join "`n")) $chunks) {
                Write-Output "$($file.Name):${line}: labelled historical source verified at $($old.Revision)"
                $matchedHistory = $true
                break
            }
        }
        if (-not $matchedHistory) { $problems.Add("$($file.Name):${line}: Java excerpt differs from current or labelled historical source") }
    }
    $prose = [regex]::Replace($text, '(?ms)^' + $fence + '.*?^' + $fence + '\s*$', '')
    if ($prose -match '[\u2013\u2014\u2018\u2019\u201c\u201d\x00-\x08\x0b\x0c\x0e-\x1f]') {
        $problems.Add("$($file.Name): forbidden typography or control character")
    }
    foreach ($line in $prose.Split("`n")) {
        if ($line -match '[a-z)][.?!:] ["A-Z]|[a-z)][.?!:](\*\*|\*) [^ ]| -- [^.]{5,120} -- |^#{1,6} .* -- ') {
            Write-Output "READ punctuation $($file.Name): $line"
        }
    }
}
$ratio = (68.0/13)*(76.0/21)
$three = $ratio*(84.0/29)
$work = 2.0*9.81*1.2
$designPower = ($work/1.5)*2
$speed = [Math]::PI*.090*(6000/$ratio)/60
Write-Output ('Arithmetic: R2={0:F5}, R3={1:F5}, work={2:F3} J, design={3:F3} W, rim={4:F4} m/s' -f $ratio,$three,$work,$designPower,$speed)
if ([Math]::Round($ratio,2) -ne 18.93 -or [Math]::Round($three,2) -ne 54.83) { $problems.Add('Ratio calculation disagrees with draft') }
if ($problems.Count) { $problems | ForEach-Object { Write-Output "FAIL $_" }; exit 1 }
Write-Output 'Structural and source-text checks passed (line-edge whitespace ignored; standalone ellipses mark omissions). Read punctuation candidates and review prose separately.'
