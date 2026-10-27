# Finishes the compiled field guide in Word:  page layout, the table of
# contents, and the PDF.
#
#     pandoc lua doc/FieldGuide/reference/build_book.lua book
#     powershell -ExecutionPolicy Bypass -File doc/FieldGuide/reference/finish_book.ps1
#
# Adapted from OODP_Lessons' Course/reference/finish_book.ps1.  In order:
#   1. Margins mirror for double-sided printing, with the wide 1.5-inch margin
#      on the inside of every page.  Page numbers go centered in the footer,
#      and the title page has none.
#      a. A code block that just overflows a page shrinks until it fits.
#      b. A page break before a numbered entry goes when the entry fits in the
#         space left on the page, or the page is less than half full.  Part
#         and chapter breaks stay.
#   2. A large gap at the foot of a page, 4 inches or more, gets one of the
#      spot pictures from make_art.ps1, sized to fit the gap.  Sparingly:
#      each picture once, in the largest gaps, at least 15 pages apart.  Most
#      gaps stay white.
#      a. Chapters open on a right-hand page, and a part page sits on the
#         left, facing its first chapter.  A blank page goes in where needed.
#   3. Word fills in the contents.  The file is saved over itself and exported
#      beside it as a PDF.
#   4. Each chapter's PDF in build/pdf is cut from the finished book, so a
#      printed chapter matches the book page for page.  The book build's
#      chapters.tsv says where each chapter starts.
#
# Word through COM hangs easily.  If it does, ClaudeWritingProject's
# COMMON_ERRORS.md entry 1 says how to find and kill the automation instance.
# Kept ASCII, for PowerShell 5.1.

$dir = Join-Path $PSScriptRoot '..\build\book'
$docx = (Resolve-Path (Join-Path $dir 'Field_Guide_Draft.docx')).Path
$pdf = Join-Path (Split-Path $docx) 'Field_Guide_Draft.pdf'
$art = Join-Path $dir 'art'

$GAP_POINTS = 288          # 4 inches
$PAGE_BOTTOM = 720         # 11 inches less the 1-inch bottom margin, in points
$spots = @('spot_gear.png', 'spot_wrench.png', 'spot_tag.png', 'spot_wheel.png',
           'spot_battery.png', 'spot_servo.png', 'spot_bolt.png', 'spot_ball.png') |
  Where-Object { Test-Path (Join-Path $art $_) }

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
try {
  $doc = $word.Documents.Open($docx, $false, $false)
  $null = $doc.Paragraphs.Count

  # 1.  Margins and page numbers.
  foreach ($section in $doc.Sections) {
    $section.PageSetup.MirrorMargins = -1
    $section.PageSetup.LeftMargin = 108     # inside, in points:  1.5 inches
    $section.PageSetup.RightMargin = 72     # outside:  1 inch
    $section.PageSetup.DifferentFirstPageHeaderFooter = -1
    $null = $section.Footers.Item(1).PageNumbers.Add(1, $false)
  }
  foreach ($toc in $doc.TablesOfContents) { $toc.Update() }
  $doc.Repaginate()

  # 1a.  A code block that just overflows a page shrinks, half a point at a
  #      time down to 8 points, until it fits on one.  A block that still
  #      won't fit at 8 points breaks as before, at its normal size.
  $shrunk = 0
  foreach ($p in $doc.Paragraphs) {
    if ($p.Style.NameLocal -ne 'Source Code') { continue }
    $r = $p.Range
    $span = { $doc.Range($r.End - 1, $r.End - 1).Information(3) - $doc.Range($r.Start, $r.Start).Information(3) }
    if ((& $span) -lt 1) { continue }
    $original = $r.Font.Size
    $size = 9.5
    while ((& $span) -ge 1 -and $size -gt 8) { $size -= 0.5; $r.Font.Size = $size; $doc.Repaginate() }
    if ((& $span) -ge 1) { $r.Font.Size = $original; $doc.Repaginate() } else { $shrunk++ }
  }
  "code blocks shrunk to fit:  $shrunk"

  # 1b.  The page breaks before numbered entries bend when the page is better
  #      for it.  A break is our hairline paragraph, with a level-2 heading (a
  #      numbered entry) right after it;  part and chapter breaks belong to the
  #      Heading 1 style and stay.  A break goes when the next entry fits in the
  #      space left on this page, or when this page is less than half full.
  #      Worked front to back, measuring the live layout each time.
  $HALF_PAGE = 324
  $dropped = 0
  $all = @($doc.Paragraphs)
  for ($i = 1; $i -lt $all.Count - 1; $i++) {
    $b = $all[$i]
    if (-not $b.Format.PageBreakBefore -or $b.Range.Text.Length -gt 1) { continue }
    if ($all[$i + 1].OutlineLevel -ne 2) { continue }
    $prev = $all[$i - 1]
    $room = $PAGE_BOTTOM - ($doc.Range($prev.Range.End - 1, $prev.Range.End - 1).Information(6) + 16)
    # The entry runs to the next forced break, or the next heading of level 1.
    $j = $i + 1
    while ($j -lt $all.Count -and -not $all[$j].Format.PageBreakBefore -and $all[$j].OutlineLevel -ne 1) { $j++ }
    $first = $all[$i + 1].Range; $last = $all[$j - 1].Range
    $p1 = $doc.Range($first.Start, $first.Start).Information(3)
    $p2 = $doc.Range($last.End - 1, $last.End - 1).Information(3)
    $fits = $false
    if ($p1 -eq $p2) {
      $height = ($doc.Range($last.End - 1, $last.End - 1).Information(6) + 16) - $doc.Range($first.Start, $first.Start).Information(6)
      $fits = $height + 12 -le $room
    }
    if ($fits -or $room -gt $HALF_PAGE) {
      $b.Format.PageBreakBefore = 0
      $dropped++
      $doc.Repaginate()
    }
  }
  "entry page breaks bent:  $dropped"

  # 2.  Fill large gaps, working backward so a picture can't move a page that
  #     hasn't been checked yet.
  $doc.Repaginate()
  $paras = New-Object System.Collections.ArrayList
  foreach ($p in $doc.Paragraphs) {
    [void]$paras.Add(@{ start = $p.Range.Start; end = $p.Range.End;
      page = $p.Range.Information(3); inTable = [bool]$p.Range.Information(12) })
  }
  # Every gap that qualifies, first.  Then the biggest of them, at least
  # $SPREAD pages apart, one per picture, so the pictures spread through the
  # book and go where the white space is worst.
  $SPREAD = 15
  $gaps = New-Object System.Collections.ArrayList
  for ($k = 1; $k -lt $paras.Count; $k++) {
    $here = $paras[$k]; $prev = $paras[$k - 1]
    if ($here.page -le $prev.page -or $here.inTable -or $prev.page -lt 3) { continue }
    $bottom = $doc.Range($prev.end - 1, $prev.end - 1).Information(6) + 16
    $gap = $PAGE_BOTTOM - $bottom
    if ($gap -ge $GAP_POINTS) { [void]$gaps.Add(@{ k = $k; page = $prev.page; gap = $gap }) }
  }
  $chosen = New-Object System.Collections.ArrayList
  foreach ($c in ($gaps | Sort-Object { $_.gap } -Descending)) {
    if ($chosen.Count -ge @($spots).Count) { break }
    $near = $chosen | Where-Object { [Math]::Abs($_.page - $c.page) -lt $SPREAD }
    if (-not $near) { [void]$chosen.Add($c) }
  }
  "large gaps:  $($gaps.Count), pictures placed in $($chosen.Count) of them"

  # Insert from the back, so each insertion leaves the earlier positions alone.
  # The pictures go in book order:  the first gap gets the first picture.
  $order = @($chosen | Sort-Object { $_.page })
  for ($n = $order.Count - 1; $n -ge 0; $n--) {
    $c = $order[$n]; $here = $paras[$c.k]; $gap = $c.gap
    $pick = @($spots)[$n]
    $at = $doc.Range($here.start, $here.start)
    $at.InsertParagraphBefore()
    $para = $doc.Range($here.start, $here.start).Paragraphs.Item(1)
    $para.Style = $doc.Styles.Item('Normal')
    $f = $para.Format
    $f.PageBreakBefore = 0; $f.KeepWithNext = 0; $f.KeepTogether = 0
    $f.LineSpacingRule = 0; $f.SpaceBefore = 12; $f.SpaceAfter = 0; $f.Alignment = 1
    $shape = $doc.InlineShapes.AddPicture((Join-Path $art $pick), $false, $true, $para.Range)
    $shape.LockAspectRatio = -1
    $shape.Height = [Math]::Min($gap - 60, 200)
  }

  # 2a.  Chapters open on a right-hand page, and a part page sits on the left,
  #      facing its first chapter.  Printed double-sided, a right-hand page is
  #      an odd one.  Where a top-level heading lands on the wrong side, a blank
  #      page goes in ahead of it.  Front to back, since each blank page moves
  #      everything after it.
  $doc.Repaginate()
  $blanks = 0
  $tops = @($doc.Paragraphs | Where-Object { $_.OutlineLevel -eq 1 })
  $afterPart = $false
  foreach ($h in $tops) {
    $isPart = $h.Range.Text.StartsWith('Part ')
    if (-not $isPart -and $afterPart) { $afterPart = $false; continue }   # faces its part page
    $page = $h.Range.Information(3)
    $wantOdd = -not $isPart
    if (($page % 2 -eq 1) -ne $wantOdd) {
      $start = $h.Range.Start
      $doc.Range($start, $start).InsertParagraphBefore()
      $blank = $doc.Range($start, $start).Paragraphs.Item(1)
      $blank.Style = $doc.Styles.Item('Normal')
      $f = $blank.Format
      $f.PageBreakBefore = -1; $f.KeepWithNext = 0; $f.SpaceBefore = 0; $f.SpaceAfter = 0
      $f.LineSpacingRule = 4; $f.LineSpacing = 1       # exactly 1 point tall
      $doc.Repaginate()
      $blanks++
    }
    $afterPart = $isPart
  }
  "blank pages added for right-hand chapters:  $blanks"

  # 3.  Contents, save, export.
  $doc.Repaginate()
  foreach ($toc in $doc.TablesOfContents) { $toc.Update() }
  $doc.Save()
  $null = $doc.Paragraphs.Count
  $doc.ExportAsFixedFormat([string]$pdf, 17)
  "wrote $pdf"

  # 4.  Each chapter's PDF, cut from the finished book.  chapters.tsv, from the
  #     book build, lists the heading each chapter starts on, in book order;  a
  #     chapter runs to the page before the next one starts.  A part page has
  #     no file.
  $list = Join-Path $dir 'chapters.tsv'
  if (Test-Path $list) {
    $out = Join-Path $PSScriptRoot '..\build\pdf'
    New-Item -ItemType Directory -Force $out | Out-Null
    $out = (Resolve-Path $out).Path
    $starts = @()
    $norm = { param($t) (($t -replace '[\s ]+', ' ').Trim()) }
    $paras = @($doc.Paragraphs)
    $at = 0
    foreach ($line in (Get-Content $list -Encoding UTF8)) {
      $name, $heading = $line -split "`t", 2
      $want = & $norm $heading
      while ($at -lt $paras.Count) {
        $p = $paras[$at]; $at++
        if ($p.OutlineLevel -eq 1 -and (& $norm $p.Range.Text).StartsWith($want)) {
          $starts += @{ name = $name; page = $p.Range.Information(3) }
          break
        }
      }
      if ($starts.Count -eq 0 -or $starts[-1].name -ne $name) { throw "no heading found for: $heading" }
    }
    $last = $doc.Content.Information(3)
    for ($i = 0; $i -lt $starts.Count; $i++) {
      if (-not $starts[$i].name) { continue }
      $from = $starts[$i].page
      $to = if ($i + 1 -lt $starts.Count) { $starts[$i + 1].page - 1 } else { $last }
      $file = Join-Path $out $starts[$i].name
      $doc.ExportAsFixedFormat([string]$file, 17, $false, 0, 3, $from, $to)
      "wrote {0}  pages {1}-{2}" -f $starts[$i].name, $from, $to
    }
  }
  $doc.Close([ref]0)
} finally {
  $word.Quit()
}
