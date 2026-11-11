# Draws a title-page picture for each chapter of the printed field guide, and
# the spot pictures finish_book.ps1 uses to fill large gaps.
#
#     powershell -ExecutionPolicy Bypass -File doc/FieldGuide/reference/make_art.ps1
#
# Writes doc/FieldGuide/build/book/art/chNN.png and spot_*.png.  Line art in
# the book's blue, one picture per chapter, each built from what that chapter
# is about, in the style of the C# book's art.  Every label is a real name
# from the team's code or the REV DUO references.  Kept ASCII, because Windows
# PowerShell 5.1 misreads UTF-8 source without a BOM.
#
# PowerShell ignores the case of variable names, so $R and $r are the same
# variable.  Every name here differs by more than case.

Add-Type -AssemblyName System.Drawing

$outDir = Join-Path $PSScriptRoot '..\build\book\art'
New-Item -ItemType Directory -Force $outDir | Out-Null

$W = 1600; $Hgt = 900
$blue  = [System.Drawing.ColorTranslator]::FromHtml('#1F4E79')
$mid   = [System.Drawing.ColorTranslator]::FromHtml('#2B6CB0')
$pale  = [System.Drawing.ColorTranslator]::FromHtml('#EAF2FB')
$light = [System.Drawing.ColorTranslator]::FromHtml('#CFE0F3')
$red   = [System.Drawing.ColorTranslator]::FromHtml('#B03A2E')
$gray  = [System.Drawing.ColorTranslator]::FromHtml('#8A9AAA')
$white = [System.Drawing.Color]::White

function New-Canvas {
  $script:bmp = New-Object System.Drawing.Bitmap $W, $Hgt
  $script:g = [System.Drawing.Graphics]::FromImage($script:bmp)
  $script:g.SmoothingMode = 'AntiAlias'
  $script:g.TextRenderingHint = 'AntiAliasGridFit'
  $script:g.Clear([System.Drawing.Color]::Transparent)
}
function Save-Canvas([string]$name) {
  $path = Join-Path $outDir $name
  $script:bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
  $script:g.Dispose(); $script:bmp.Dispose()
  "wrote $path"
}
function Pen([System.Drawing.Color]$c, [float]$wid) {
  $p = New-Object System.Drawing.Pen $c, $wid
  $p.LineJoin = 'Round'; $p.StartCap = 'Round'; $p.EndCap = 'Round'
  $p
}
function Brush([System.Drawing.Color]$c) { New-Object System.Drawing.SolidBrush $c }
function Font([string]$family, [float]$px, [string]$style = 'Regular') {
  New-Object System.Drawing.Font $family, $px, ([System.Drawing.FontStyle]$style), ([System.Drawing.GraphicsUnit]::Pixel)
}
function Center-Text([string]$text, $font, [System.Drawing.Color]$c, [float]$x, [float]$y, [float]$wid, [float]$hei) {
  $fmt = New-Object System.Drawing.StringFormat
  $fmt.Alignment = 'Center'; $fmt.LineAlignment = 'Center'
  $script:g.DrawString($text, $font, (Brush $c), (New-Object System.Drawing.RectangleF $x, $y, $wid, $hei), $fmt)
}
function Rounded([float]$x, [float]$y, [float]$wid, [float]$hei, [float]$rad) {
  $path = New-Object System.Drawing.Drawing2D.GraphicsPath
  $d = 2 * $rad
  $path.AddArc($x, $y, $d, $d, 180, 90)
  $path.AddArc($x + $wid - $d, $y, $d, $d, 270, 90)
  $path.AddArc($x + $wid - $d, $y + $hei - $d, $d, $d, 0, 90)
  $path.AddArc($x, $y + $hei - $d, $d, $d, 90, 90)
  $path.CloseFigure()
  $path
}
function Box([float]$x, [float]$y, [float]$wid, [float]$hei, [System.Drawing.Color]$fill, [float]$stroke = 8, [float]$rad = 24) {
  $path = Rounded $x $y $wid $hei $rad
  $script:g.FillPath((Brush $fill), $path)
  $script:g.DrawPath((Pen $blue $stroke), $path)
}
function Label-Box([float]$x, [float]$y, [float]$wid, [float]$hei, [string]$text, [System.Drawing.Color]$fill, [float]$px = 50) {
  Box $x $y $wid $hei $fill 7 22
  $ink = if ($fill -eq $blue) { $white } else { $blue }
  Center-Text $text (Font 'Consolas' $px) $ink $x $y $wid $hei
}
function Arrow([float]$x1, [float]$y1, [float]$x2, [float]$y2, [float]$wid = 8) {
  $p = Pen $blue $wid
  $p.CustomEndCap = New-Object System.Drawing.Drawing2D.AdjustableArrowCap 4, 5
  $script:g.DrawLine($p, $x1, $y1, $x2, $y2)
}
function Dashed([float]$x1, [float]$y1, [float]$x2, [float]$y2, [System.Drawing.Color]$c, [float]$wid = 6) {
  $p = Pen $c $wid
  $p.DashStyle = 'Dash'
  $script:g.DrawLine($p, $x1, $y1, $x2, $y2)
}
function Pt([float]$x, [float]$y) { New-Object System.Drawing.PointF $x, $y }
function Polar([float]$cx, [float]$cy, [double]$radius, [double]$angle) {
  Pt ([float]($cx + $radius * [Math]::Cos($angle))) ([float]($cy + $radius * [Math]::Sin($angle)))
}
function Hexagon([float]$cx, [float]$cy, [float]$radius, [System.Drawing.Color]$fill, [float]$stroke = 8) {
  $hex = @(); foreach ($i in 0..5) { $hex += Polar $cx $cy $radius ($i * [Math]::PI / 3) }
  $script:g.FillPolygon((Brush $fill), [System.Drawing.PointF[]]$hex)
  $script:g.DrawPolygon((Pen $blue $stroke), [System.Drawing.PointF[]]$hex)
}
# A spur gear on a hex bore.  $turn rotates it, in fractions of a tooth, so two
# gears can mesh.
function Gear([float]$cx, [float]$cy, [float]$tipR, [int]$teeth, [System.Drawing.Color]$fill, [float]$stroke = 8, [double]$turn = 0) {
  $rootR = $tipR - [Math]::Max(22, $tipR * 0.12)
  $step = 2 * [Math]::PI / $teeth
  $pts = @()
  for ($t = 0; $t -lt $teeth; $t++) {
    $a = ($t + $turn) * $step - [Math]::PI / 2
    $pts += Polar $cx $cy $rootR ($a - 0.5 * $step)
    $pts += Polar $cx $cy $rootR ($a - 0.28 * $step)
    $pts += Polar $cx $cy $tipR ($a - 0.16 * $step)
    $pts += Polar $cx $cy $tipR ($a + 0.16 * $step)
    $pts += Polar $cx $cy $rootR ($a + 0.28 * $step)
  }
  $script:g.FillPolygon((Brush $fill), [System.Drawing.PointF[]]$pts)
  $script:g.DrawPolygon((Pen $blue $stroke), [System.Drawing.PointF[]]$pts)
  Hexagon $cx $cy ([Math]::Max(18, $tipR * 0.18)) $white ($stroke * 0.8)
}
# An AprilTag-style square:  a dark border one cell wide around a 6-by-6 field
# of cells, inside a white margin.  The pattern is fixed, and decorative.
$TAG_BITS = '101100011010110101001110100011011001'
function Tag([float]$x, [float]$y, [float]$size) {
  $cell = $size / 10
  $script:g.FillRectangle((Brush $white), $x, $y, $size, $size)
  $script:g.DrawRectangle((Pen $gray 3), $x, $y, $size, $size)
  $script:g.FillRectangle((Brush $blue), ($x + $cell), ($y + $cell), (8 * $cell), (8 * $cell))
  for ($i = 0; $i -lt 36; $i++) {
    if ($TAG_BITS[$i] -eq '0') {
      $col = $i % 6; $row = [Math]::Floor($i / 6)
      $script:g.FillRectangle((Brush $white), ($x + (2 + $col) * $cell), ($y + (2 + $row) * $cell), ($cell + 0.5), ($cell + 0.5))
    }
  }
}
function Magnifier([float]$cx, [float]$cy, [float]$radius, [float]$stroke = 16) {
  $a = [Math]::PI / 4
  $x1 = $cx + $radius * [Math]::Cos($a); $y1 = $cy + $radius * [Math]::Sin($a)
  $script:g.DrawLine((Pen $blue ($stroke * 2.2)), $x1, $y1, ($x1 + 0.9 * $radius), ($y1 + 0.9 * $radius))
  $script:g.DrawEllipse((Pen $blue $stroke), ($cx - $radius), ($cy - $radius), (2 * $radius), (2 * $radius))
}
# A gamepad, seen from above.
function Gamepad([float]$cx, [float]$cy, [float]$scale) {
  $body = New-Object System.Drawing.Drawing2D.GraphicsPath
  $body.AddEllipse(($cx - 330 * $scale), ($cy - 60 * $scale), (260 * $scale), (300 * $scale))
  $body.AddEllipse(($cx + 70 * $scale), ($cy - 60 * $scale), (260 * $scale), (300 * $scale))
  $body.AddPath((Rounded ($cx - 260 * $scale) ($cy - 150 * $scale) (520 * $scale) (240 * $scale) (90 * $scale)), $false)
  $region = New-Object System.Drawing.Region $body
  $script:g.FillRegion((Brush $pale), $region)
  foreach ($k in 0..1) {
    $sx = $cx + (-160 + 320 * $k) * $scale
    $script:g.FillEllipse((Brush $light), ($sx - 45 * $scale), ($cy + 30 * $scale), (90 * $scale), (90 * $scale))
    $script:g.DrawEllipse((Pen $blue (7 * $scale)), ($sx - 45 * $scale), ($cy + 30 * $scale), (90 * $scale), (90 * $scale))
  }
  $dx = $cx - 200 * $scale; $dy = $cy - 70 * $scale
  $script:g.FillRectangle((Brush $blue), ($dx - 14 * $scale), ($dy - 40 * $scale), (28 * $scale), (80 * $scale))
  $script:g.FillRectangle((Brush $blue), ($dx - 40 * $scale), ($dy - 14 * $scale), (80 * $scale), (28 * $scale))
  foreach ($b in @(@(0, -38), @(38, 0), @(0, 38), @(-38, 0))) {
    $bx = $cx + 200 * $scale + $b[0] * $scale; $by = $cy - 70 * $scale + $b[1] * $scale
    $script:g.FillEllipse((Brush $mid), ($bx - 16 * $scale), ($by - 16 * $scale), (32 * $scale), (32 * $scale))
  }
  $outline = Pen $blue (9 * $scale)
  $script:g.DrawEllipse($outline, ($cx - 330 * $scale), ($cy - 60 * $scale), (260 * $scale), (300 * $scale))
  $script:g.DrawEllipse($outline, ($cx + 70 * $scale), ($cy - 60 * $scale), (260 * $scale), (300 * $scale))
  $script:g.FillPath((Brush $pale), (Rounded ($cx - 250 * $scale) ($cy - 140 * $scale) (500 * $scale) (220 * $scale) (85 * $scale)))
  $script:g.DrawPath($outline, (Rounded ($cx - 260 * $scale) ($cy - 150 * $scale) (520 * $scale) (240 * $scale) (90 * $scale)))
  # Redraw the controls the body fill just covered.
  foreach ($k in 0..1) {
    $sx = $cx + (-160 + 320 * $k) * $scale
    $script:g.FillEllipse((Brush $light), ($sx - 45 * $scale), ($cy + 30 * $scale), (90 * $scale), (90 * $scale))
    $script:g.DrawEllipse((Pen $blue (7 * $scale)), ($sx - 45 * $scale), ($cy + 30 * $scale), (90 * $scale), (90 * $scale))
  }
  $script:g.FillRectangle((Brush $blue), ($dx - 14 * $scale), ($dy - 40 * $scale), (28 * $scale), (80 * $scale))
  $script:g.FillRectangle((Brush $blue), ($dx - 40 * $scale), ($dy - 14 * $scale), (80 * $scale), (28 * $scale))
  foreach ($b in @(@(0, -38), @(38, 0), @(0, 38), @(-38, 0))) {
    $bx = $cx + 200 * $scale + $b[0] * $scale; $by = $cy - 70 * $scale + $b[1] * $scale
    $script:g.FillEllipse((Brush $mid), ($bx - 16 * $scale), ($by - 16 * $scale), (32 * $scale), (32 * $scale))
  }
}
function Bubble([float]$x, [float]$y, [float]$wid, [float]$hei, [bool]$tailLeft, [System.Drawing.Color]$fill) {
  $path = Rounded $x $y $wid $hei 50
  $script:g.FillPath((Brush $fill), $path)
  $script:g.DrawPath((Pen $blue 9), $path)
  if ($tailLeft) { $pts = @(@(($x + 90), ($y + $hei - 4)), @(($x + 40), ($y + $hei + 90)), @(($x + 190), ($y + $hei - 4))) }
  else { $pts = @(@(($x + $wid - 190), ($y + $hei - 4)), @(($x + $wid - 40), ($y + $hei + 90)), @(($x + $wid - 90), ($y + $hei - 4))) }
  $tail = [System.Drawing.PointF[]]($pts | ForEach-Object { Pt $_[0] $_[1] })
  $script:g.FillPolygon((Brush $fill), $tail)
  $script:g.DrawLine((Pen $blue 9), $tail[0], $tail[1]); $script:g.DrawLine((Pen $blue 9), $tail[1], $tail[2])
}

# ---------------------------------------------------------------------------
# Chapter 0:  how to use this book.  A page, and a lens over one entry number.
New-Canvas
Box 250 90 900 720 $white 9 20
for ($i = 0; $i -lt 9; $i++) { $g.FillRectangle((Brush $light), 330, (170 + 70 * $i), (740 - 160 * ($i % 3)), 26) }
$g.FillEllipse((Brush $pale), 830, 270, 440, 440)
Center-Text '7.3' (Font 'Georgia' 170 'Bold') $blue 830 270 440 440
Magnifier 1050 490 220 16
Save-Canvas 'ch00.png'

# Chapter 1:  reading Java.  One method signature, its four parts underlined.
New-Canvas
Box 100 230 1400 260 $white 9 26
$sigFont = Font 'Consolas' 66
# Consolas is monospaced, so every character is one cell wide.
$typo = [System.Drawing.StringFormat]::GenericTypographic
$cellW = $g.MeasureString(('M' * 20), $sigFont, 2000, $typo).Width / 20
$parts = @('public', 'void', 'move', '(Gamepad gamepad)')
$x = 150
for ($i = 0; $i -lt 4; $i++) {
  $wid = $cellW * $parts[$i].Length
  $g.DrawString($parts[$i], $sigFont, (Brush $blue), $x, 300, $typo)
  $ink = if ($i % 2 -eq 0) { $blue } else { $mid }
  $g.DrawLine((Pen $ink 10), ($x + 4), 400, ($x + $wid - 4), 400)
  $x += $wid + $cellW
}
$g.DrawString('intake.setPower(0.75);', (Font 'Consolas' 58), (Brush $gray), 300, 620)
Save-Canvas 'ch01.png'

# Chapter 2:  extension methods.  The gamepad, and the helper bolted to it.
New-Canvas
Gamepad 520 470 1.15
Label-Box 980 140 520 130 'GamepadExtensions' $blue 46
Label-Box 980 330 520 110 'GetLeftStickX()' $white 46
Arrow 1240 270 1240 325 7
Arrow 980 400 400 540 8
Save-Canvas 'ch02.png'

# Chapter 3:  the hardware toolbox.  A motor and a servo, and the line that
# finds them.
New-Canvas
Box 180 180 460 300 $pale 9 40
$g.FillRectangle((Brush $light), 640, 290, 120, 80); $g.DrawRectangle((Pen $blue 8), 640, 290, 120, 80)
Hexagon 800 330 34 $white 7
Center-Text 'DcMotor' (Font 'Consolas' 54 'Bold') $blue 180 180 460 300
Box 980 220 340 220 $pale 9 18
$g.FillRectangle((Brush $pale), 930, 300, 50, 60); $g.DrawRectangle((Pen $blue 7), 930, 300, 50, 60)
$g.FillRectangle((Brush $pale), 1320, 300, 50, 60); $g.DrawRectangle((Pen $blue 7), 1320, 300, 50, 60)
$g.FillEllipse((Brush $light), 1100, 120, 100, 100); $g.DrawEllipse((Pen $blue 8), 1100, 120, 100, 100)
$g.DrawLine((Pen $blue 18), 1150, 170, 1290, 110)
Center-Text 'Servo' (Font 'Consolas' 54 'Bold') $blue 980 220 340 220
Center-Text 'hardwareMap.get(DcMotor.class, "intakeMotor")' (Font 'Consolas' 44) $gray 0 640 $W 120
Save-Canvas 'ch03.png'

# Chapter 4:  sensors.  A switch becomes a reading, and the reading becomes a
# meaning.
New-Canvas
Box 120 320 260 220 $pale 9 18
$g.FillRectangle((Brush $mid), 220, 250, 60, 80)
$g.FillEllipse((Brush $light), 200, 220, 100, 50); $g.DrawEllipse((Pen $blue 7), 200, 220, 100, 50)
Arrow 400 430 500 430 9
Label-Box 520 360 400 140 'isPressed()' $white 52
Arrow 940 430 1030 430 9
Label-Box 1050 360 480 140 'canGoForward()' $light 50
Save-Canvas 'ch04.png'

# Chapter 5:  AprilTags.  A tag off to one side of the camera's view, and the
# angle between them.
New-Canvas
Box 160 90 1280 720 $pale 8 30
Dashed 800 120 800 780 $gray 6
Tag 940 260 380
$g.DrawLine((Pen $blue 6), 1130, 200, 1130, 700)
Arrow 810 730 1120 730 8
Center-Text 'tx' (Font 'Consolas' 64 'Bold') $blue 860 610 200 110
$g.DrawEllipse((Pen $mid 6), 770, 420, 60, 60)
Save-Canvas 'ch05.png'

# Chapter 6:  the layered robot model.
New-Canvas
Label-Box 590 40 420 120 'OpMode' $blue 54
Arrow 800 160 800 215 7
Label-Box 590 220 420 120 'Robot' $light 54
Arrow 700 340 420 420 7
Arrow 900 340 1140 420 7
Label-Box 180 425 480 120 'DriveTrain' $white 52
Label-Box 880 425 540 120 'MechAssembly' $white 52
foreach ($k in 0..2) {
  $bx = 600 + 330 * $k
  Arrow (1150) 545 ($bx + 150) 635 6
  Label-Box $bx 640 310 110 'MechComponent' $pale 36
}
Save-Canvas 'ch06.png'

# Chapter 7:  a component, and the four things every component does.
New-Canvas
Box 380 200 840 500 $pale 10 36
Center-Text 'SpinnyIntake' (Font 'Consolas' 60 'Bold') $blue 380 230 840 100
Label-Box 600 380 400 120 'DcMotor' $light 52
Label-Box 470 70 660 100 'initializeTelemetry()' $white 42
Label-Box 60 400 300 100 'move()' $white 44
Label-Box 1240 400 300 100 'update()' $white 44
Label-Box 420 740 760 100 'getAutonomousBehaviors()' $white 42
Save-Canvas 'ch07.png'

# Chapter 8:  control strategies.  The button mapping arrives from outside.
New-Canvas
Box 900 200 560 500 $pale 10 36
Center-Text 'SpinnyIntake' (Font 'Consolas' 56 'Bold') $blue 900 230 560 100
$g.FillRectangle((Brush $white), 900, 400, 120, 160); $g.DrawRectangle((Pen $blue 8), 900, 400, 120, 160)
Label-Box 1060 590 360 90 'DcMotor' $light 44
Box 100 380 640 200 $white 9 26
Center-Text '(motor, gamepad) ->' (Font 'Consolas' 50) $blue 100 400 640 80
Center-Text '{ ... }' (Font 'Consolas' 50) $blue 100 480 640 80
Arrow 750 480 890 480 10
Save-Canvas 'ch08.png'

# Chapter 9:  an assembly of components.
New-Canvas
Box 140 90 1320 720 $pale 10 40
Center-Text 'BillyMA' (Font 'Consolas' 70 'Bold') $blue 140 110 1320 110
$fields = @(@(230, 260, 'intake'), @(830, 260, 'ballPusher'), @(230, 520, 'flywheel'), @(830, 520, 'turret'))
foreach ($f in $fields) { Label-Box $f[0] $f[1] 540 190 $f[2] $light 56 }
Save-Canvas 'ch09.png'

# Chapter 10:  the robot layer, and the one place telemetry gets flushed.
New-Canvas
Box 120 60 1360 480 $pale 10 40
Center-Text 'BillyRobot' (Font 'Consolas' 64 'Bold') $blue 120 75 1360 100
Label-Box 190 200 560 130 'MecanumDrive' $light 50
Label-Box 850 200 560 130 'BillyMA' $light 50
Label-Box 190 370 560 120 'limelight' $white 48
Label-Box 850 370 560 120 'imu' $white 48
$funnel = [System.Drawing.PointF[]]@((Pt 520 560), (Pt 1080 560), (Pt 880 700), (Pt 720 700))
$g.FillPolygon((Brush $light), $funnel); $g.DrawPolygon((Pen $blue 8), $funnel)
Label-Box 520 730 560 120 'telemetry.update()' $blue 46
Save-Canvas 'ch10.png'

# Chapter 11:  autonomous verbs.  Each dot in the chain narrows the way down.
New-Canvas
$chain = @('robot.getAutonomousRobot()', '.mechAssembly', '.autonBallPusher', '.pushBalls()')
for ($i = 0; $i -lt 4; $i++) {
  $wid = 1200 - 260 * $i
  $bx = (1600 - $wid) / 2
  $by = 60 + 205 * $i
  $fill = if ($i -eq 3) { $blue } else { if ($i -eq 0) { $pale } else { $white } }
  Label-Box $bx $by $wid 140 $chain[$i] $fill 48
  if ($i -lt 3) { Arrow 800 ($by + 140) 800 ($by + 200) 7 }
}
Save-Canvas 'ch11.png'

# Chapter 12:  a state machine.  Each state hands back the next one.
New-Canvas
$states = @(@(230, 300, 'startShooter'), @(640, 300, 'fire'), @(1050, 300, 'retract'), @(1050, 690, 'stopShooter'))
foreach ($s in $states) {
  $fill = if ($s[2] -eq 'stopShooter') { $light } else { $pale }
  $g.FillEllipse((Brush $fill), ($s[0] - 170), ($s[1] - 110), 340, 220)
  $g.DrawEllipse((Pen $blue 9), ($s[0] - 170), ($s[1] - 110), 340, 220)
  Center-Text $s[2] (Font 'Consolas' 44) $blue ($s[0] - 170) ($s[1] - 110) 340 220
}
Arrow 405 300 465 300 8
Arrow 815 300 875 300 8
Arrow 1050 415 1050 575 8
$loop = Pen $blue 8
$loop.CustomEndCap = New-Object System.Drawing.Drawing2D.AdjustableArrowCap 4, 5
$g.DrawArc($loop, 640, 60, 410, 260, 340, -140)      # retract, back to fire
Save-Canvas 'ch12.png'

# Chapter 13:  control ownership.  Two voices, one turret, and the claim that
# decides which one it hears.
New-Canvas
$g.FillEllipse((Brush $pale), 1020, 330, 320, 320); $g.DrawEllipse((Pen $blue 10), 1020, 330, 320, 320)
$g.FillRectangle((Brush $light), 1150, 170, 60, 200); $g.DrawRectangle((Pen $blue 9), 1150, 170, 60, 200)
Center-Text 'turret' (Font 'Consolas' 54 'Bold') $blue 1020 430 320 120
Label-Box 980 700 400 100 'Part.TURRET' $blue 44
Label-Box 100 170 520 130 'gamepad' $white 54
Label-Box 100 600 620 130 'LimelightAutoTarget' $light 46
Dashed 630 240 860 350 $gray 8
$g.DrawLine((Pen $red 14), 840, 300, 900, 410)
Arrow 730 660 1010 560 10
Save-Canvas 'ch13.png'

# Chapter 14:  pose.  Where the robot is on the whole field, worked out from
# one tag on the wall.
New-Canvas
Box 300 60 1000 780 $pale 9 10
for ($i = 1; $i -lt 6; $i++) {
  $g.DrawLine((Pen $light 4), (300 + 1000 * $i / 6), 60, (300 + 1000 * $i / 6), 840)
  $g.DrawLine((Pen $light 4), 300, (60 + 780 * $i / 6), 1300, (60 + 780 * $i / 6))
}
Tag 960 20 120
Dashed 680 560 1010 130 $mid 6
$g.TranslateTransform(640, 600); $g.RotateTransform(-35)
Box -80 -80 160 160 $white 9 14
$g.FillPolygon((Brush $blue), [System.Drawing.PointF[]]@((Pt 0 -70), (Pt 40 -10), (Pt -40 -10)))
$g.ResetTransform()
Arrow 200 840 200 640 8
Arrow 200 840 400 840 8
Center-Text 'y' (Font 'Consolas' 54 'Bold') $blue 130 580 140 80
Center-Text 'x' (Font 'Consolas' 54 'Bold') $blue 400 800 100 80
Save-Canvas 'ch14.png'

# Chapter 15:  a cluster.  Four tags, and the point they share, where no tag
# sits.
New-Canvas
for ($k = 0; $k -lt 4; $k++) { Tag (230 + 300 * $k) 120 200 }
$g.FillPath((Brush $light), (Rounded 450 520 700 300 40)); $g.DrawPath((Pen $blue 10), (Rounded 450 520 700 300 40))
foreach ($k in 0..3) { Dashed (330 + 300 * $k) 330 800 670 $gray 5 }
$g.DrawEllipse((Pen $red 8), 750, 620, 100, 100)
$g.DrawLine((Pen $red 6), 800, 590, 800, 750); $g.DrawLine((Pen $red 6), 720, 670, 880, 670)
Save-Canvas 'ch15.png'

# Chapter 16:  structure.  The extrusion's T-slots, a hex shaft, and an M3
# screw.
New-Canvas
$ex = New-Object System.Drawing.Drawing2D.GraphicsPath
$s = 420; $ox = 170; $oy = 240; $slot = 110; $lip = 50
$ex.AddLines([System.Drawing.PointF[]]@(
  (Pt $ox $oy), (Pt ($ox + ($s - $slot) / 2) $oy), (Pt ($ox + ($s - $slot) / 2) ($oy + $lip)), (Pt ($ox + ($s + $slot) / 2) ($oy + $lip)), (Pt ($ox + ($s + $slot) / 2) $oy),
  (Pt ($ox + $s) $oy), (Pt ($ox + $s) ($oy + ($s - $slot) / 2)), (Pt ($ox + $s - $lip) ($oy + ($s - $slot) / 2)), (Pt ($ox + $s - $lip) ($oy + ($s + $slot) / 2)), (Pt ($ox + $s) ($oy + ($s + $slot) / 2)),
  (Pt ($ox + $s) ($oy + $s)), (Pt ($ox + ($s + $slot) / 2) ($oy + $s)), (Pt ($ox + ($s + $slot) / 2) ($oy + $s - $lip)), (Pt ($ox + ($s - $slot) / 2) ($oy + $s - $lip)), (Pt ($ox + ($s - $slot) / 2) ($oy + $s)),
  (Pt $ox ($oy + $s)), (Pt $ox ($oy + ($s + $slot) / 2)), (Pt ($ox + $lip) ($oy + ($s + $slot) / 2)), (Pt ($ox + $lip) ($oy + ($s - $slot) / 2)), (Pt $ox ($oy + ($s - $slot) / 2))))
$ex.CloseFigure()
$g.FillPath((Brush $pale), $ex); $g.DrawPath((Pen $blue 9), $ex)
$g.FillEllipse((Brush $white), ($ox + $s / 2 - 40), ($oy + $s / 2 - 40), 80, 80); $g.DrawEllipse((Pen $blue 8), ($ox + $s / 2 - 40), ($oy + $s / 2 - 40), 80, 80)
Center-Text '15mm' (Font 'Consolas' 50 'Bold') $blue $ox ($oy + $s + 30) $s 80
Hexagon 830 450 150 $light 9
Center-Text '5mm hex' (Font 'Consolas' 50 'Bold') $blue 680 690 300 80
$g.FillRectangle((Brush $light), 1150, 250, 260, 90); $g.DrawRectangle((Pen $blue 9), 1150, 250, 260, 90)
$g.FillRectangle((Brush $pale), 1240, 340, 80, 330); $g.DrawRectangle((Pen $blue 8), 1240, 340, 80, 330)
for ($i = 0; $i -lt 9; $i++) { $g.DrawLine((Pen $blue 5), 1240, (360 + 34 * $i), 1320, (380 + 34 * $i)) }
Center-Text 'M3' (Font 'Consolas' 50 'Bold') $blue 1180 690 200 80
Save-Canvas 'ch16.png'

# Chapter 17:  actuators.  An HD Hex Motor with two UltraPlanetary stages,
# the biggest reduction against the motor.
New-Canvas
Box 120 300 520 300 $pale 9 40
Center-Text 'HD Hex' (Font 'Consolas' 60 'Bold') $blue 120 300 520 300
Label-Box 640 280 260 340 '5:1' $light 70
Label-Box 900 280 260 340 '4:1' $white 70
$g.FillRectangle((Brush $light), 1160, 400, 160, 100); $g.DrawRectangle((Pen $blue 8), 1160, 400, 160, 100)
Hexagon 1390 450 60 $white 8
Arrow 650 720 1300 720 8
Center-Text 'torque' (Font 'Georgia' 48 'Italic') $mid 800 730 380 90
Save-Canvas 'ch17.png'

# Chapter 18:  power transmission.  A 20T driving a 40T, 15.0mm apart.
New-Canvas
Gear 430 450 150 20 $light 8 0
Gear 840 450 290 40 $pale 8 0.5
$g.DrawLine((Pen $red 5), 430, 830, 840, 830)
$g.DrawLine((Pen $red 5), 430, 800, 430, 860); $g.DrawLine((Pen $red 5), 840, 800, 840, 860)
Center-Text '15.0mm' (Font 'Consolas' 46 'Bold') $red 480 760 300 60
Center-Text '20T' (Font 'Consolas' 46 'Bold') $blue 330 200 200 80
Center-Text '40T' (Font 'Consolas' 46 'Bold') $blue 1150 120 200 80
Save-Canvas 'ch18.png'

# Chapter 19:  linear motion.  Three slide stages, a cord over their pulleys,
# and the spool that pulls it.
New-Canvas
$stages = @(@(560, 230, 240, 620), @(620, 150, 180, 560), @(670, 70, 130, 500))
foreach ($st in $stages) { Box $st[0] $st[1] $st[2] $st[3] $(if ($st[0] -eq 560) { $pale } elseif ($st[0] -eq 620) { $white } else { $light }) 8 12 }
foreach ($p in @(@(790, 250), @(720, 170))) {
  $g.FillEllipse((Brush $white), ($p[0] - 28), ($p[1] - 28), 56, 56); $g.DrawEllipse((Pen $blue 6), ($p[0] - 28), ($p[1] - 28), 56, 56)
}
# The spool pulls a cord over the first stage's pulley, down to the second
# stage;  a second cord, tied to the base, rides over the second stage's
# pulley down to the third.
$cord = Pen $red 5
$g.DrawLine($cord, 1000, 820, 818, 250); $g.DrawLine($cord, 762, 250, 762, 700)
$g.DrawLine($cord, 692, 840, 692, 170); $g.DrawLine($cord, 748, 170, 748, 560)
$g.FillEllipse((Brush $light), 940, 760, 120, 120); $g.DrawEllipse((Pen $blue 8), 940, 760, 120, 120)
Hexagon 1000 820 26 $white 6
Save-Canvas 'ch19.png'

# Chapter 20:  kits and starter bots.  Parts from a box, becoming the team's
# own component.
New-Canvas
$boxFront = [System.Drawing.PointF[]]@((Pt 160 430), (Pt 640 430), (Pt 640 780), (Pt 160 780))
$flapL = [System.Drawing.PointF[]]@((Pt 160 430), (Pt 400 430), (Pt 300 320), (Pt 80 330))
$flapR = [System.Drawing.PointF[]]@((Pt 400 430), (Pt 640 430), (Pt 720 330), (Pt 500 320))
$g.FillPolygon((Brush $light), $flapL); $g.DrawPolygon((Pen $blue 8), $flapL)
$g.FillPolygon((Brush $light), $flapR); $g.DrawPolygon((Pen $blue 8), $flapR)
Gear 300 360 90 14 $pale 7 0
$g.FillEllipse((Brush $white), 410, 250, 180, 180); $g.DrawEllipse((Pen $blue 8), 410, 250, 180, 180)
Hexagon 500 340 24 $light 6
$g.FillPolygon((Brush $pale), $boxFront); $g.DrawPolygon((Pen $blue 9), $boxFront)
Arrow 690 600 990 600 10
Label-Box 1000 500 500 200 'MechComponent' $blue 52
Save-Canvas 'ch20.png'

# Chapter 21:  designing a robot.  Decide from the top, build from the
# bottom:  the folders, in build order.
New-Canvas
Box 260 40 1080 820 $pale 9 20
for ($i = 1; $i -lt 10; $i++) { $g.DrawLine((Pen $light 3), 260, (40 + 82 * $i), 1340, (40 + 82 * $i)) }
$order = @('Components/', 'Assemblies/', 'DriveTrain/', 'Robots/', 'AutonStrategies/', 'OpModes/')
for ($i = 0; $i -lt 6; $i++) {
  $by = 720 - 125 * $i
  Label-Box 470 $by 760 100 $order[$i] $(if ($i -eq 0) { $light } else { $white }) 46
}
Arrow 380 800 380 90 10
Save-Canvas 'ch21.png'

# Chapter 22:  common pitfalls.  A bug, under the lens.
New-Canvas
$g.FillEllipse((Brush $pale), 560, 180, 560, 560)
foreach ($side in -1, 1) {
  foreach ($k in 0..2) {
    $y0 = 400 + 90 * $k
    $g.DrawLine((Pen $blue 9), (840 + 80 * $side), $y0, (840 + 210 * $side), ($y0 - 40 + 40 * $k))
  }
  $g.DrawLine((Pen $blue 7), (840 + 20 * $side), 310, (840 + 90 * $side), 240)
}
$g.FillEllipse((Brush $mid), 760, 360, 160, 230); $g.DrawEllipse((Pen $blue 9), 760, 360, 160, 230)
$g.DrawLine((Pen $blue 6), 840, 370, 840, 585)
$g.FillEllipse((Brush $blue), 790, 290, 100, 90)
Magnifier 840 460 280 18
Save-Canvas 'ch22.png'

# Glossary:  index tabs.
New-Canvas
$tabs = @('A', 'D', 'M', 'S', 'T')
for ($k = 0; $k -le 4; $k++) {        # back card first, so each tab shows above the card in front
  $x0 = 300 + 40 * $k; $y0 = 200 + 70 * $k
  Box $x0 $y0 900 560 $(if ($k % 2 -eq 0) { $white } else { $pale }) 8 20
  $tx = $x0 + 120 + 150 * $k
  $g.FillPath((Brush $light), (Rounded $tx ($y0 - 70) 130 90 18)); $g.DrawPath((Pen $blue 7), (Rounded $tx ($y0 - 70) 130 90 18))
  Center-Text $tabs[$k] (Font 'Georgia' 56 'Bold') $blue $tx ($y0 - 70) 130 90
}
Save-Canvas 'ch98.png'

# Postscript:  the conversation the book came out of.
New-Canvas
Bubble 120 90 820 380 $true $white
foreach ($i in 0..3) { $g.FillRectangle((Brush $light), 190, (160 + 70 * $i), (680 - 120 * ($i % 2)), 34) }
Bubble 660 400 820 360 $false $pale
foreach ($i in 0..3) { $g.FillRectangle((Brush $gray), 730, (465 + 66 * $i), (660 - 140 * ($i % 3)), 30) }
Save-Canvas 'ch99.png'

# ---------------------------------------------------------------------------
# Spot pictures:  small things from the shop and the field, for filling a
# large gap at the foot of a page.  finish_book.ps1 places them, sparingly.
# No words.

$W = 900; $Hgt = 900

New-Canvas
Gear 450 450 340 24 $pale 10 0
Save-Canvas 'spot_gear.png'

# A combination wrench.
New-Canvas
$g.TranslateTransform(450, 450); $g.RotateTransform(-40)
$g.FillPath((Brush $light), (Rounded -320 -45 640 90 40)); $g.DrawPath((Pen $blue 10), (Rounded -320 -45 640 90 40))
$g.FillEllipse((Brush $pale), -420, -110, 220, 220); $g.DrawEllipse((Pen $blue 10), -420, -110, 220, 220)
$g.FillEllipse((Brush $white), -360, -50, 100, 100); $g.DrawEllipse((Pen $blue 8), -360, -50, 100, 100)
$g.FillPie((Brush $pale), 200, -120, 240, 240, 0, 360); $g.DrawEllipse((Pen $blue 10), 200, -120, 240, 240)
$g.FillRectangle((Brush $white), 330, -45, 140, 90)
$g.DrawLine((Pen $blue 9), 330, -45, 430, -45); $g.DrawLine((Pen $blue 9), 330, 45, 430, 45); $g.DrawLine((Pen $blue 9), 330, -45, 330, 45)
$g.ResetTransform()
Save-Canvas 'spot_wrench.png'

New-Canvas
Tag 150 150 600
Save-Canvas 'spot_tag.png'

# A mecanum wheel, seen from the side:  rollers at 45 degrees.
New-Canvas
$rim = New-Object System.Drawing.Drawing2D.GraphicsPath
$rim.AddEllipse(110, 110, 680, 680)
$g.FillPath((Brush $pale), $rim)
$g.SetClip($rim)
for ($k = -6; $k -le 6; $k++) {
  $g.TranslateTransform((450 + 110 * $k), 450); $g.RotateTransform(45)
  $g.FillPath((Brush $light), (Rounded -45 -420 90 840 40)); $g.DrawPath((Pen $blue 7), (Rounded -45 -420 90 840 40))
  $g.ResetTransform()
}
$g.ResetClip()
$g.DrawEllipse((Pen $blue 12), 110, 110, 680, 680)
$g.FillEllipse((Brush $white), 360, 360, 180, 180); $g.DrawEllipse((Pen $blue 10), 360, 360, 180, 180)
Hexagon 450 450 40 $light 7
Save-Canvas 'spot_wheel.png'

# A battery pack.
New-Canvas
Box 170 260 560 420 $pale 12 30
$g.FillRectangle((Brush $blue), 290, 200, 90, 60); $g.FillRectangle((Brush $blue), 520, 200, 90, 60)
$bolt = [System.Drawing.PointF[]]@((Pt 480 320), (Pt 380 490), (Pt 450 490), (Pt 410 620), (Pt 530 440), (Pt 460 440), (Pt 510 320))
$g.FillPolygon((Brush $mid), $bolt); $g.DrawPolygon((Pen $blue 7), $bolt)
Save-Canvas 'spot_battery.png'

# A servo with its horn.
New-Canvas
Box 200 330 500 340 $pale 12 26
$g.FillRectangle((Brush $pale), 120, 450, 80, 100); $g.DrawRectangle((Pen $blue 10), 120, 450, 80, 100)
$g.FillRectangle((Brush $pale), 700, 450, 80, 100); $g.DrawRectangle((Pen $blue 10), 700, 450, 80, 100)
$g.FillEllipse((Brush $light), 310, 230, 140, 140); $g.DrawEllipse((Pen $blue 10), 310, 230, 140, 140)
$g.DrawLine((Pen $blue 30), 380, 300, 600, 190)
$g.FillEllipse((Brush $white), 360, 280, 40, 40)
Save-Canvas 'spot_servo.png'

# A bolt and a nyloc nut.
New-Canvas
Hexagon 300 300 150 $light 10
$g.FillRectangle((Brush $pale), 260, 440, 80, 380); $g.DrawRectangle((Pen $blue 9), 260, 440, 80, 380)
for ($i = 0; $i -lt 10; $i++) { $g.DrawLine((Pen $blue 5), 260, (460 + 36 * $i), 340, (482 + 36 * $i)) }
Hexagon 640 560 150 $pale 10
$g.FillEllipse((Brush $white), 570, 490, 140, 140); $g.DrawEllipse((Pen $blue 9), 570, 490, 140, 140)
$g.DrawEllipse((Pen $mid 8), 545, 465, 190, 190)
Save-Canvas 'spot_bolt.png'

# A game ball, mid-roll.
New-Canvas
$g.FillEllipse((Brush $light), 200, 180, 520, 520); $g.DrawEllipse((Pen $blue 12), 200, 180, 520, 520)
$g.DrawArc((Pen $blue 8), 260, 180, 400, 520, 270, 180)
$g.DrawArc((Pen $blue 8), 200, 300, 520, 280, 180, 180)
foreach ($k in 0..2) { $g.DrawLine((Pen $gray 10), (60 + 30 * $k), (560 + 50 * $k), (170 + 10 * $k), (560 + 50 * $k)) }
Save-Canvas 'spot_ball.png'
