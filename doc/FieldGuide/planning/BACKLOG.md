# Field Guide backlog

What's done, who has what, and what's next.  Read `../PROCESS.md` first.
Code baseline for every quotation:  `305fb81` (master at the audit), plus the
one `TODO` comment Codex added to `DoublyLimitedMotor.java`, committed in
`eab2e14` and still waiting on Jason's keep, reword, or drop.

## History, briefly

- **2026-10-26, Claude.**  Brief (`PROCESS.md`), build, art, and the code audit
  (`code-facts.md`).  Nine drafters launched;  a shared usage limit stopped
  all of them.  Chapters 16, 19 and 20 were written before it did.  Jason
  committed that state as `8f9a46d`.
- **2026-10-26, Codex**, recovering.  Wrote Chapters 17 and 18, notes and
  openings for 16-20, and corrected the REV facts against REV's own
  documentation (`recovery_2026-10-26.md` has the list:  HD Hex is 15 W, the
  Core Hex is 72:1, the UltraPlanetary's exact fractions, 40 N-m is a failure
  threshold).  Added Jason's student-work guidance to PROCESS, and
  `check_chapters.ps1` and the page-preview script.
- **2026-10-26, Claude**, reviewing Codex.  Kept its content, its corrections,
  and its two checking scripts.  Removed its task-runner and run records.
  Restored the concrete explanations its edits had hedged away in 16, 19 and
  20, keeping its factual fixes, and gave 17 and 18 a voice pass.
- **2026-10-26, Codex**, continuing Part 3 without sub-agents.  Drafted
  Chapters 8 and 11, completed 10, finished 7's opening and notes, extended
  the source checker, and built and visually reviewed Chapters 6-11.
  Added the short script-run record protocol to PROCESS.
- **2026-10-26, Codex**, Chapter 13 continuation.  Completed 13.2-13.10,
  replaced 13.1's source placeholder, added notes and opening, source-checked
  the chapter, and built and visually reviewed the 13-page standalone draft.

## Chapters

Snapshot updated 2026-10-26 after Codex's Chapter 13 continuation.  Original
Claude drafter ids are historical; no Codex sub-agents were launched.

| Ch | State at snapshot | Drafter (agent id, resumable with SendMessage) |
|---|---|---|
| 0, 98, 99 | drafted 2026-10-27;  0 and 99 await Jason's own voice | Claude |
| 1 | 10 entries, notes, opening | `a4dc877b00902bbee` (Ch 1-2) |
| 2 | 9 entries, closing sections, notes, opening; checked and rendered (2026-10-27) | Claude draft, Claude completion |
| 3 | 10 entries, notes, opening | `a1f767dabdb891fb0` (Ch 3-5) |
| 4 | 10 entries, closing sections, notes, opening; checked and rendered (2026-10-27) | Claude draft, Claude completion |
| 5 | 10 entries, closing sections, notes, opening; checked and rendered (2026-10-27) | Claude |
| 6 | 8 entries, notes, opening | `ab5d5d829aeb4c81f` (Ch 6-8) |
| 7 | 9 entries, notes, opening; checked and rendered | Claude draft, Codex completion |
| 8 | 8 entries, notes, opening; checked and rendered | Codex |
| 9 | 9 entries, notes, opening | `ad9a24408abdf710c` (Ch 9-11) |
| 10 | 8 entries, notes, opening; checked and rendered | Claude draft, Codex completion |
| 11 | 8 entries, notes, opening; checked and rendered | Codex |
| 12 | 9 entries, notes, opening | `a805554bac31d3f33` (Ch 12-13) |
| 13 | 10 entries, notes, opening; checked and rendered | Claude start, Codex completion |
| 14 | 9 entries, notes, opening | `a7a5e9fc94915d1a5` (Ch 14-15) |
| 15 | 9 entries, closing sections, notes, opening; checked and rendered (2026-10-27) | Claude draft, Claude completion |
| 16-20 | drafted;  voice pass done;  checks pass | done |
| 21 | 9 entries, notes, opening | `a750d834c5b6943fd` (Ch 21-22) |
| 22 | 6 entries, closing sections, notes, opening; checked and rendered (2026-10-27) | Claude |

**If the session dies again:**  agent ids only work inside the session that
made them.  In a new session, start a fresh drafter per unfinished chapter
with this brief:  read `PROCESS.md` (all of it, including "Writing about
student work"), the two style files in `ClaudeWritingProject/style-guide/`
in full, `code-facts.md`, the chapter's source lesson (PROCESS table), and the
partial chapter and its notes file;  continue from the entry the notes name
as next;  save after each entry;  propose `TODO`s in notes, never edit code;
write `book/opening_NN.md` last.  Check with `reference/check_chapters.ps1`.

**Checker scope:**  `check_chapters.ps1` now searches TeamCode and the SDK
samples.  It supports `-Keys`, copied blocks with line-edge whitespace
changes, standalone ellipses, and the explicitly labelled historical
examples in 9/10.  Part 3 passes.  Other partial drafts have not received
this continuation's full source and visual review.  Cross-references outside
a selection still need the later whole-book check.

## Open for Jason

- The `TODO` Codex added to `DoublyLimitedMotor.java` (keep checking limits
  during autonomous moves):  keep, reword, or drop.
- `doc/rev-duo/` has the wrong HD Hex power (40 W), Core Hex reduction (20:1),
  servo current limit, and a `DoublyLimitedMotor` example that doesn't
  compile.  The book uses the corrected facts;  the reference itself is
  unchanged.
- `doc/limelight3a/` says it was checked against SDK 11.2.1;  the repo builds
  against 12.0.0.
- Each chapter's notes file ends with questions for Jason.

## Next

**Completed continuation, Codex, 2026-10-26:**  Part 3 (Chapters 6-11) now has
complete chapter drafts, notes, and openings.  Chapters 6/9 preserved;
7 received supporting files and two scoped explanations; 10 continued from
10.7; 8/11 drafted.  Selected source checks and rendered review completed.
These remain drafts for Jason's editorial review.  No student code changed
in this continuation.  No build or preview process remains pending.

The snapshot above predates the last saved entries.  At this continuation's
start, 2 had six entries;  4 had two;  7 had nine and closing sections;
10 had six;  13 had one;  15 had nine and closing sections;  22 had three.

### Part 3 build and review - Codex, 2026-10-26

- Source checks completed:  selected Chapters 06-11 pass, including the two
  explicitly labelled historical excerpts.  Log:  `build/part3-check.txt`.
- Commands from the repository root:
  `pandoc lua doc/FieldGuide/reference/build_book.lua 06 07 08 09 10 11`, then
  `& ./doc/FieldGuide/reference/preview_pages.ps1 -Keys @('06','07','08','09','10','11')`.
- Goal:  build six Word drafts, export PDF/page images, inspect the results.
  Outputs are under ignored `build/word` and `build/preview`.
- Start:  2026-10-26 21:24 America/New_York, this Windows workspace/session.
- Status:  **finished and reviewed**.  See results below.
- Build completed, exit 0 (PowerShell PID 10408); six Word drafts written.
- Preview completed, exit 0:  PowerShell PID 31740, tool session 13653.  Log:
  `build/part3-preview.txt`.  This process owns its Word automation instance;
  the script closes its documents and quits that instance in `finally`.
- Output page counts, Chapters 06-11 respectively:  15, 20, 10, 17, 16, 10.
- Reviewed all six `build/preview/chapter_NN_contact.png` files, plus larger
  pages 08/04, 08/07, 10/14, and 11/05 for code, tables, and callouts.
- Fixed Chapter 11's split Java list by using a short table.  Rebuilt 11,
  exported/rendered it again (PID 10516, exit 0, 21:26; logs
  `build/part3-rebuild.txt` and `build/part3-preview-final.txt`), and inspected
  final page 11/10.  Code boxes and tables fit; no clipping seen in review.
- Checker helper checks rejected changed command values and reversed excerpt
  order, and accepted whitespace changes and ordered omissions.  Final
  selected source/structure check passed.  Punctuation candidates were
  literal status strings and needed no rewrite.
- No physical robot tests.  Title-page openings are saved and source-checked;
  the standalone chapter build doesn't include book title-page layout.

### Chapter 13 continuation

### Chapter 13 build and review - Codex, 2026-10-26

- Goal:  build the completed ownership chapter, render its pages, and inspect
  code boxes, tables, and closing sections.
- Start:  2026-10-26 22:44 America/New_York, this Windows workspace/session.
- Commands from the repo root:
  `pandoc lua doc/FieldGuide/reference/build_book.lua 13`, then
  `& ./doc/FieldGuide/reference/preview_pages.ps1 -Keys @('13')`.
- Logs:  `build/chapter13-build.txt`, `build/chapter13-preview.txt`.
  Source checks already passed:  `build/chapter13-check.txt`.
- Expected outputs:  `build/word/FieldGuide_Chapter13.docx` and
  `build/preview/chapter_13.pdf`, page PNGs, and contact sheet.
- First build and preview finished, exit 0:  build PID 20364, preview PID
  23916.  Output:  15 pages.  Contact sheet and pages 8, 10, 14 inspected.
  Two trailing cross-reference lines occupied otherwise empty pages.
- Trimmed repeated prose in 13.7/13.8; rerunning the same commands to check
  those breaks.  Logs:  `build/chapter13-build-final.txt` and
  `build/chapter13-preview-final.txt`.
- Final build and preview completed synchronously, PID 18552, exit 0.
  No persistent tool session was needed.  Final output:  13 pages.
- **Finished and reviewed:**  final contact sheet and large pages 8/9 checked.
  Cross-reference lines now fit with their entries; code and tables fit.
  Final selected source/structure check passed.  No physical robot test.
- No build or preview process remains pending.  Book title-page opening is
  saved and source-checked; its combined-book layout awaits that later pass.

**Completed continuation, Codex:**  Chapter 13 has ten entries, closing
sections, notes, and opening.  It explains claims, selection, manual dispatch,
cleanup, and telemetry using current source.  No robot code changed.

### Chapter 2 continuation - Claude, 2026-10-27, school machine

- Rebuilt the ignored `build/` art here:  `make_cover.ps1` and `make_art.ps1`
  both exit 0, 07:48;  cover and 33 art PNGs written.  Cover and `ch13.png`
  opened and checked.  The home machine's build logs were never in Git.
- Wrote 2.7-2.9, the closing sections, and `book/opening_02.md`.  Notes
  updated.  `check_chapters.ps1 -Keys @('02')` passes.
- Build and preview, from the repo root:
  `pandoc lua doc/FieldGuide/reference/build_book.lua 02`, then
  `& ./doc/FieldGuide/reference/preview_pages.ps1 -Keys @('02')`.
  Expected outputs:  `build/word/FieldGuide_Chapter02.docx`,
  `build/preview/chapter_02.pdf`, page PNGs, contact sheet.
- Build exit 0, preview exit 0, PowerShell PID 36512, 07:53;  19 pages.
- **Finished and reviewed:**  contact sheet, and page 14 at full size (the
  Argue it and Try it boxes, the wrapped `TODO` comment).  Code, tables and
  boxes fit.  Page 7 holds only 2.4's `See also` line;  that entry predates
  this session and was left for Jason's edit.  The standalone build has no
  book title page, so `opening_02.md` waits on the book pass.
- Chapter 2 is 33 KB, above PROCESS's 12-25 KB guide.  Jason to decide on
  trimming;  2.7 overlaps Chapter 15's 15.1 and 15.5 a little.

### Chapter 4 continuation - Claude, 2026-10-27, school machine

- Wrote 4.3-4.10, closing sections, `book/opening_04.md`;  notes updated.
  `check_chapters.ps1 -Keys @('04')` passes.
- Build and preview, same commands as Chapter 2 with key `04`.  Expected:
  `build/word/FieldGuide_Chapter04.docx`, `build/preview/chapter_04*`.
- First build and preview exit 0 (PID 75996, 08:03), 15 pages.  Page 12's
  quoted IMU `TODO` wrapped badly, and page 9 held only 4.6's `See also`.
  Replaced the quote with prose and tightened 4.6;  rebuilt (PID 15976,
  08:04), 14 pages, checks pass.
- **Finished and reviewed:**  final contact sheet and page 12.  Page 4 holds
  only 4.2's `See also` line;  4.2 predates this session, left for Jason.

### Chapter 5 - Claude, 2026-10-27, school machine

- Wrote the whole chapter:  5.1-5.10, closing sections, notes,
  `book/opening_05.md`.  Moved deadband to Chapter 4 in PROCESS's table.
- Build and preview, same commands with key `05`.  Build and preview exit 0,
  PID 33428, 08:08;  14 pages.  Checks pass.
- **Finished and reviewed:**  contact sheet.  Code, tables and boxes fit;  the
  SDK sample's long telemetry line in 5.4 wraps once.  No orphan pages.

### Chapters 15 and 22 - Claude, 2026-10-27, school machine

- 15:  wrote `book/opening_15.md`, and filled in the notes file from the
  chapter text.  Checks pass.
- 22:  wrote 22.4-22.6, closing sections, notes, `book/opening_22.md`.  Added
  a chapter-22 historical entry (`1a6809a^`, `BillyMA.java`) to
  `check_chapters.ps1`.  Checks pass.
- Build and preview, keys `15` and `22`.  Both exit 0, PID 63696, 08:11.
  15:  16 pages;  22:  11 pages.
- **Finished and reviewed:**  both contact sheets.  Code, tables and boxes
  fit.  Chapter 15's page 4 holds only 15.1's `See also` line;  15.1 predates
  this session, left for Jason.

### First full-book build - Claude, 2026-10-27, school machine

- Goal:  the whole draft as one PDF, Chapters 1-22 (0, 98, 99 not written,
  so the build skips them).
- Commands from the repo root:
  `pandoc lua doc/FieldGuide/reference/build_book.lua book`, then
  `powershell -ExecutionPolicy Bypass -File doc/FieldGuide/reference/finish_book.ps1`.
- Expected:  `build/book/Field_Guide_Draft.docx`, `chapters.tsv`,
  `Field_Guide_Draft.pdf`, and one PDF per chapter in `build/pdf/`.
- Log:  `build/fullbook-build.txt`.  Started 09:00:02, PowerShell PID 27252,
  Word PID 43548.
- `build_book` exit 0 at 09:00:04;  skipped 00, 98, 99.  `finish_book` exit 0
  at 09:28:06, and Word quit.  The gap-finding pass took most of the time.
  64 entry page breaks bent, 36 large gaps with pictures in 8, 7 blank pages
  for right-hand chapters.  Book PDF:  347 pages.  22 chapter PDFs in
  `build/pdf/`, Chapter 1 from page 11 to Chapter 22 ending on page 347.
- **Finished and reviewed:**  rendered pages 1-4, 9-12, 31-34, 80-82, 95-97,
  346-347 (the Read tool can't open PDFs on this machine;  rendered with the
  Windows PDF renderer, as `preview_pages.ps1` does).  Cover, contents with
  page numbers, part pages, chapter title pages with art and openings all
  present.  Pages 9 and 95 are the intended blanks before part pages.  The
  other 327 pages weren't inspected one by one.

### Missing sections, file names, style pass - Claude, 2026-10-27

- Built files are now named for their chapter's title
  (`FieldGuide_Chapter02_Extension_Methods.docx`, and the same for the cut
  PDFs):  `word_name()` in `build_book.lua`.  `preview_pages.ps1` finds the
  newest file for a key.  Old untitled outputs deleted.
- Wrote `chapter_00_how_to_use.md`, `chapter_98_glossary.md` (246 terms,
  generated from every chapter's Vocabulary), `chapter_99_postscript.md`, and
  their notes.  0 and 99 are drafts in Jason's voice, for him to rewrite.
- `check_chapters.ps1` treats 00/98/99 as front and back matter (no entry
  count, no opening), and skips `0.x` values.  Whole book passes.
- Style pass, whole book:  every PROCESS grep clean.  Fixed possession-sense
  `hold` in 3, 7, 9 (twice) and 10;  abstract `stays` and a slogan closing in
  99.  Read the last line of every section.  Chapter 22's negations describe
  bugs, and stayed.  Not done:  a full read-aloud of every paragraph;  the
  Codex-drafted chapters (8, 11, 13, 17, 18) read stiffer than the rest and
  would benefit from Jason's ear.
- Full rebuild:  same commands as the first full build, plus `all` first.
  Log `build/fullbook-build-2.txt`.  Started 09:43;  `build_book` exit 0 at
  09:44;  `finish_book` exit 0 at 10:42 (58 minutes, Word PID 33344).  38
  large gaps, pictures in 8;  8 blank pages.  Book:  368 pages, Chapter 0
  through the Postscript.  25 titled chapter PDFs in `build/pdf/`.
- **Finished and reviewed:**  rendered pages 2, 9-13, 353-355, 366-368.
  Contents lists Chapter 0;  Chapter 0, glossary and Postscript title pages
  have their art;  tables and letter headings lay out cleanly.  Page 13 is the
  intended blank before Part 1.

### 21.10 and the RobotBuilder TODO - Claude, 2026-10-27

- Added the `RobotBuilder` `TODO` to `Robot.java` (comment only) and entry
  21.10.  Whole-book check passes;  Chapter 21 rendered and reviewed alone.
- Full rebuild, same commands as before.  Log `build/fullbook-build-3.txt`.
  Started 11:15;  `build_book` exit 0 at 11:15.  `finish_book` laid the book
  out, then **failed at the PDF export at 11:41**:  the book PDF was open in
  Adobe Acrobat, which locks it.  It still reported exit 0, so the failure
  only showed in the log.  No PDFs were updated;  the 10:34 PDF predates 21.10.
- Fixed `finish_book.ps1`:  it now checks every output PDF before starting
  Word, stops at once naming any that are locked, and exits 1 on any error.
  Tested:  with Acrobat open, it stops in about a second and exits 1.
- Next:  close the PDF in Acrobat, then rerun `finish_book.ps1` alone (the
  `.docx` from 11:15 is current).

### Next writing checkpoint

Chapters 1-22 are all drafted, as of 2026-10-27.  Next:  Jason's editorial
read, then Chapter 0, the glossary (98) and the postscript (99).  Chapter 0
and 99 are in Jason's own voice.  Use the process's run record for long
scripts.

Orphaned `See also` pages in the standalone renders, all in entries from
the first session:  2.4, 4.2, 15.1.

Whole-book `check_chapters.ps1` (no `-Keys`) passes, 2026-10-27, after two
fixes:  the checker now skips `N.0` numbers (`20.0`, SDK `12.0`), which it
was reading as entry references, and 14.6's invented `getBotpose` line moved
to a plain fence.  The style pass and Jason's read are still ahead.

1. Finish drafting 1-15 and 21-22.
2. Chapter 0, glossary (98), postscript (99).
3. Cross-reference pass, style pass, `check_chapters.ps1` over everything.
4. Build the book, `finish_book.ps1`, look at the pages.
