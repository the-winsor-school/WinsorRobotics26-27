# Field Guide backlog

What's done, who has what, and what's next.  Read `../PROCESS.md` first.
Code baseline for every quotation:  `305fb81` (master at the audit), plus the
one `TODO` comment Codex added to `DoublyLimitedMotor.java`, which is
uncommitted and waiting on Jason.

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

## Chapters

Snapshot updated 2026-10-26 after Codex's Part 3 continuation.  Original
Claude drafter ids are historical; no Codex sub-agents were launched.

| Ch | State at snapshot | Drafter (agent id, resumable with SendMessage) |
|---|---|---|
| 0, 98, 99 | not started | Claude, last |
| 1 | 10 entries, notes, opening | `a4dc877b00902bbee` (Ch 1-2) |
| 2 | 6 entries, notes; unfinished closing sections, no opening | same |
| 3 | 10 entries, notes, opening | `a1f767dabdb891fb0` (Ch 3-5) |
| 4 | 2 entries, partial notes | same |
| 5 | not started | same |
| 6 | 8 entries, notes, opening | `ab5d5d829aeb4c81f` (Ch 6-8) |
| 7 | 9 entries, notes, opening; checked and rendered | Claude draft, Codex completion |
| 8 | 8 entries, notes, opening; checked and rendered | Codex |
| 9 | 9 entries, notes, opening | `ad9a24408abdf710c` (Ch 9-11) |
| 10 | 8 entries, notes, opening; checked and rendered | Claude draft, Codex completion |
| 11 | 8 entries, notes, opening; checked and rendered | Codex |
| 12 | 9 entries, notes, opening | `a805554bac31d3f33` (Ch 12-13) |
| 13 | 1 entry; no notes or opening | same |
| 14 | 9 entries, notes, opening | `a7a5e9fc94915d1a5` (Ch 14-15) |
| 15 | 9 entries, notes;  no opening | same |
| 16-20 | drafted;  voice pass done;  checks pass | done |
| 21 | 9 entries, notes, opening | `a750d834c5b6943fd` (Ch 21-22) |
| 22 | 3 entries, partial notes; no opening | same |

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

### Next writing checkpoint

Chapter 13 is the natural next continuation after the completed 11/12 drafts:
read its lesson sources and current code, create notes, and continue from
13.2 after checking what 13.1 already covers.  Then finish 2, 4, 5, 15's
supporting files, and 22.  Use the process's run record for long scripts.

1. Finish drafting 1-15 and 21-22.
2. Chapter 0, glossary (98), postscript (99).
3. Cross-reference pass, style pass, `check_chapters.ps1` over everything.
4. Build the book, `finish_book.ps1`, look at the pages.
