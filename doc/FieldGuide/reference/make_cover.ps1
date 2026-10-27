# Draws the field guide's cover art:  a gear on a hex bore, above the one line
# an OpMode in this codebase needs.
#
#     powershell -ExecutionPolicy Bypass -File doc/FieldGuide/reference/make_cover.ps1
#
# Writes doc/FieldGuide/build/book/cover.png, which build_book.lua puts on the
# title page.  Plain line art in the book's blue, the same as the C# book's
# cover.  Kept ASCII, because Windows PowerShell 5.1 misreads UTF-8 source
# without a BOM.

Add-Type -AssemblyName System.Drawing

$out = Join-Path $PSScriptRoot '..\build\book\cover.png'
New-Item -ItemType Directory -Force (Split-Path $out) | Out-Null

$size = 1200
$bmp = New-Object System.Drawing.Bitmap $size, $size
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = 'AntiAlias'
$g.TextRenderingHint = 'AntiAliasGridFit'
$g.Clear([System.Drawing.Color]::Transparent)

$blue  = [System.Drawing.ColorTranslator]::FromHtml('#1F4E79')
$pale  = [System.Drawing.ColorTranslator]::FromHtml('#EAF2FB')
$light = [System.Drawing.ColorTranslator]::FromHtml('#CFE0F3')
$pen = New-Object System.Drawing.Pen $blue, 12
$pen.LineJoin = 'Round'

# A spur gear:  each tooth climbs from the root circle to the tip circle and
# back.  Twenty teeth, like the 20T gear the REV DUO center-distance table
# starts from.
$cx = 600.0; $cy = 500.0; $tipR = 400.0; $rootR = 340.0; $teeth = 20
function Polar([double]$radius, [double]$angle) {
  New-Object System.Drawing.PointF ([float]($cx + $radius * [Math]::Cos($angle))), ([float]($cy + $radius * [Math]::Sin($angle)))
}
$step = 2 * [Math]::PI / $teeth
$pts = @()
for ($t = 0; $t -lt $teeth; $t++) {
  $a = $t * $step - [Math]::PI / 2
  $pts += Polar $rootR ($a - 0.5 * $step)
  $pts += Polar $rootR ($a - 0.28 * $step)
  $pts += Polar $tipR ($a - 0.16 * $step)
  $pts += Polar $tipR ($a + 0.16 * $step)
  $pts += Polar $rootR ($a + 0.28 * $step)
}
$g.FillPolygon((New-Object System.Drawing.SolidBrush $pale), [System.Drawing.PointF[]]$pts)
$g.DrawPolygon($pen, [System.Drawing.PointF[]]$pts)

# The hub, and the hex bore every REV gear shares.
$g.FillEllipse((New-Object System.Drawing.SolidBrush $light), ($cx - 170), ($cy - 170), 340, 340)
$g.DrawEllipse((New-Object System.Drawing.Pen $blue, 9), ($cx - 170), ($cy - 170), 340, 340)
$hex = @(); foreach ($i in 0..5) { $hex += Polar 90 ($i * [Math]::PI / 3) }
$g.FillPolygon([System.Drawing.Brushes]::White, [System.Drawing.PointF[]]$hex)
$g.DrawPolygon($pen, [System.Drawing.PointF[]]$hex)
foreach ($k in 0..3) {
  $a = $k * [Math]::PI / 2 + [Math]::PI / 4
  $p = Polar 255 $a
  $g.FillEllipse([System.Drawing.Brushes]::White, ($p.X - 28), ($p.Y - 28), 56, 56)
  $g.DrawEllipse((New-Object System.Drawing.Pen $blue, 7), ($p.X - 28), ($p.Y - 28), 56, 56)
}

# And the line, from every TeleOp in the repository.
$fmt = New-Object System.Drawing.StringFormat
$fmt.Alignment = 'Center'; $fmt.LineAlignment = 'Center'
$codeFont = New-Object System.Drawing.Font 'Consolas', 56, ([System.Drawing.FontStyle]::Regular), ([System.Drawing.GraphicsUnit]::Pixel)
$brush = New-Object System.Drawing.SolidBrush $blue
$g.DrawString('robot.update(gamepad1, gamepad2);', $codeFont, $brush, (New-Object System.Drawing.RectangleF 0, 990, $size, 140), $fmt)

$bmp.Save($out, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $bmp.Dispose()
"wrote $out"
