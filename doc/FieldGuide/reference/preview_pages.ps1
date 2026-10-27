# Page previews of chapter Word files, so a page layout can be looked at as
# images.  The Read tool can't render a PDF's pages on this machine, and this
# can:  Word exports each chapter to PDF, and the Windows PDF renderer turns
# every page into a PNG, plus a contact sheet per chapter.
#
# From a PowerShell session that permits local scripts:
#     & ./doc/FieldGuide/reference/preview_pages.ps1 -Keys @('16','17')
#
# Build the chapters first (build_book.lua).  The source .docx files are
# opened read-only.  Written by Codex, 2026-10-26.  Kept ASCII.
param([string[]]$Keys = @('16','17','18','19','20'))
$ErrorActionPreference = 'Stop'
$guideRoot = Split-Path $PSScriptRoot
$previewDir = Join-Path $guideRoot 'build/preview'
New-Item -ItemType Directory -Force -Path $previewDir | Out-Null
$word = New-Object -ComObject Word.Application
try {
    $word.Visible = $false
    $word.DisplayAlerts = 0
    foreach ($key in $Keys) {
        $path = (Resolve-Path (Join-Path $guideRoot "build/word/FieldGuide_Chapter$key.docx")).Path
        $doc = $word.Documents.Open($path, $false, $true)
        try {
            $null = $doc.Paragraphs.Count      # touch it before exporting;  see COMMON_ERRORS entry 1
            $pdfPath = Join-Path $previewDir "chapter_$key.pdf"
            $doc.ExportAsFixedFormat([string]$pdfPath, 17)
            Write-Output "Chapter $key exported"
        } finally { $doc.Close(0); [void][Runtime.InteropServices.Marshal]::ReleaseComObject($doc) }
    }
} finally {
    $word.Quit()
    [void][Runtime.InteropServices.Marshal]::ReleaseComObject($word)
}

Add-Type -AssemblyName System.Runtime.WindowsRuntime
Add-Type -AssemblyName System.Drawing
$null = [Windows.Storage.StorageFile,Windows.Storage,ContentType=WindowsRuntime]
$null = [Windows.Data.Pdf.PdfDocument,Windows.Data.Pdf,ContentType=WindowsRuntime]
$null = [Windows.Storage.Streams.InMemoryRandomAccessStream,Windows.Storage.Streams,ContentType=WindowsRuntime]
$asTask = [System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
    $_.Name -eq 'AsTask' -and $_.IsGenericMethod -and $_.GetParameters().Count -eq 1 -and
    $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1'
} | Select-Object -First 1
$asAction = [System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
    $_.Name -eq 'AsTask' -and -not $_.IsGenericMethod -and $_.GetParameters().Count -eq 1 -and
    $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncAction'
} | Select-Object -First 1
function Await-Operation($Operation, [Type]$ResultType) {
    $task = $asTask.MakeGenericMethod($ResultType).Invoke($null, @($Operation))
    $task.Wait()
    $task.Result
}
foreach ($key in $Keys) {
    $pdfPath = Join-Path $previewDir "chapter_$key.pdf"
    $storageFile = Await-Operation ([Windows.Storage.StorageFile]::GetFileFromPathAsync($pdfPath)) ([Windows.Storage.StorageFile])
    $pdf = Await-Operation ([Windows.Data.Pdf.PdfDocument]::LoadFromFileAsync($storageFile)) ([Windows.Data.Pdf.PdfDocument])
    $rows = [int][Math]::Ceiling($pdf.PageCount / 3.0)
    $sheet = New-Object Drawing.Bitmap 1530, ($rows * 700)
    $graphics = [Drawing.Graphics]::FromImage($sheet)
    $graphics.Clear([Drawing.Color]::LightGray)
    $font = New-Object Drawing.Font 'Calibri', 12
    try {
        for ($index = 0; $index -lt $pdf.PageCount; $index++) {
            $page = $pdf.GetPage([uint32]$index)
            $stream = New-Object Windows.Storage.Streams.InMemoryRandomAccessStream
            try {
                $options = New-Object Windows.Data.Pdf.PdfPageRenderOptions
                $options.DestinationWidth = 1275
                $action = $asAction.Invoke($null, @($page.RenderToStreamAsync($stream,$options)))
                $action.Wait()
                $stream.Seek(0)
                $reader = New-Object Windows.Storage.Streams.DataReader ($stream.GetInputStreamAt(0))
                $null = Await-Operation ($reader.LoadAsync([uint32]$stream.Size)) ([uint32])
                $bytes = New-Object byte[] ([int]$stream.Size)
                $reader.ReadBytes($bytes)
                $pngPath = Join-Path $previewDir ('chapter_{0}_page_{1:D2}.png' -f $key,($index+1))
                [IO.File]::WriteAllBytes($pngPath,$bytes)
                $reader.Dispose()
                $img = [Drawing.Image]::FromFile($pngPath)
                try {
                    $x = ($index % 3) * 510
                    $y = [int][Math]::Floor($index / 3.0) * 700
                    $graphics.DrawString("Chapter $key / page $($index+1)",$font,[Drawing.Brushes]::Black,$x+8,$y+4)
                    $graphics.DrawImage($img,$x+5,$y+25,500,647)
                } finally { $img.Dispose() }
            } finally { $stream.Dispose(); $page.Dispose() }
        }
        $sheet.Save((Join-Path $previewDir "chapter_${key}_contact.png"), [Drawing.Imaging.ImageFormat]::Png)
    } finally { $font.Dispose(); $graphics.Dispose(); $sheet.Dispose() }
    Write-Output "Chapter $key rendered: $($pdf.PageCount) page PNGs and a contact sheet"
}
