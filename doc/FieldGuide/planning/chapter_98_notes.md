# Chapter 98 notes - Glossary

Generated 2026-10-27 from every chapter's `### Vocabulary` section:  252
entries, merged to 246.  The scripts were scratch tools (a PowerShell
extractor and an awk formatter);  to regenerate after vocabulary changes,
re-run the same extraction, or edit this file by hand.

## Decisions

- Same term, same meaning, two chapters:  the owner's definition (PROCESS
  table) with both entry numbers.  actuator (4.1, 17.1), deadzone (2.5, 4.3),
  fiducial (2.7, 5.1), guard clause (5.8, 13.5), torque (16.6, 17.1), tick of
  the loop (12.1, 22.2).
- Same word, two meanings:  **bearing, a direction** (14.1) and **bearing, a
  part** (16.2);  **tick, of an encoder** (3.2) and **tick, of the loop**.
- Chapters 8 and 11 give no entry numbers in their vocabulary, so their terms
  point to the chapter (`(Chapter 8)`).  The cross-reference pass could sharpen
  them.
- Chapters 8, 11 and 13 capitalize their terms and definitions;  the glossary
  lowercases them to match the rest of the book.  Proper names (REV parts,
  SDK types) keep their capitals.
- "Chapter N owns it" pointers in a definition were dropped, since the
  glossary already names the entry.

## Open

- Sorting ignores backticks and a leading "the", so `ftcPose` files under F
  and "the five categories" under F.
