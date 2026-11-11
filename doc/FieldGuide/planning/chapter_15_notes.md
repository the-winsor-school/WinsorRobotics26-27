# Chapter 15 notes - AprilTag Clusters

Drafted 2026-10-26.  Robot code baseline:  `305fb81`, rechecked 2026-10-27
against `dc4da50`.

**Status:**  complete.  15.1-15.9 and the closing sections from 2026-10-26;
`book/opening_15.md` and these notes filled in 2026-10-27 by Claude, who
didn't draft the entries.  Sections 2-4 below are reconstructed from the
chapter text, so the original drafter's own guesses may be missing.

## 1.  Sources (from the chapter's `Where it lives` lines)

- `doc/game-manual/sections/09-9-arena.md`, section 9.6 (15.1)
- the SDK 12.0 release notes in the root `README.md` (15.2)
- SDK sample `ConceptAprilTag.java` (15.3, 15.4)
- `Extensions/LimelightExtensions.java` (15.5);  `OpModes/BillyTeleOpRED.java`
  and `BillyTeleOpBLUE.java` (15.1)
- `doc/limelight3a/cluster-targeting-plan.md`, Tasks 2-5 and 7 (15.5-15.9);
  `doc/limelight3a/data-shapes.md` (15.5)

## 2.  Where the plan and the code disagreed

- 15.9 says Tasks 1 and 7 of the cluster plan each point at a `TODO` that has
  since been resolved.  Task 1 still asks for an `isValid()` check, which the
  helpers don't have (Chapter 5 notes).

## 3.  Open questions for Jason

- The cluster plan's offsets are copied from the SDK by hand (15.5).  The
  chapter asks the team to record where and when;  nobody has done that yet,
  because the copy doesn't exist yet.
- 15.8's **Argue it** (one field map covering both goals) is left open.
- Chapter 2's 2.7 now has an **Argue it** about whether cluster search
  belongs in `LimelightExtensions`, as the `TODO` says.  15.9 could point to
  it in the cross-reference pass.
- The opening is new.  Its joke is 15.1's point:  the four tags surround the
  opening, and none of them is on it.

## 4.  Unverified

Nothing flagged in the chapter text.  The game-manual figures in 15.1 (20 by
14 inches, four tags a CELL) are quoted from `doc/game-manual/`.

## 5.  Cross-references left at chapter level

- Chapter 5:  15.1 (the tracker centers its tag, now 5.5-5.6), 15.5
  (`FiducialResult`, now 5.4), 15.7, and "Read first"
- Chapter 1 (`instanceof` background), Chapter 6, Chapter 7 (casts) in 15.4
- Chapter 13 in 15.1 and 15.9;  Chapter 14 throughout;  Chapter 2 in 15.5

## 6.  Asides used that another chapter owns

None found.  The 15.4 Aside explains `instanceof`, which Chapter 15 owns, and
points to Chapter 7 for casts.
