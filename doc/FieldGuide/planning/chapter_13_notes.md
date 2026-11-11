# Chapter 13 notes - Control Ownership

Claude saved 13.1 and an entry outline.  Codex continuation, 2026-10-26.
Code baseline:  `305fb81`, checked against current files at branch checkpoint
`eab2e14`.  No robot-code modifications planned.

## Sources

- `doc/FlintLessons/05-assembly-control-strategies.md`
- `doc/FlintLessons/07-robot-control-strategies.md`
- `doc/FlintLessons/Advanced/control-ownership.md`
- `doc/ControlStrategyExpansionPlan.md`
- `planning/code-facts.md`, Chapter 13 and arbitration audit
- `RobotModel/Mechs/Assemblies/BillyMA.java`, `MacroManager.java`,
  `MechAssembly.java`
- `RobotModel/Robots/BillyRobot.java`, `Robot.java`
- `AutonStrategies/BillyRapidFire.java`, `LimelightAutoTarget.java`
- `RobotModel/Mechs/Components/Turret.java`

## Progress

13.1-13.10, closing sections, and opening saved.  Source checks and rendered
review completed.  Next:  editorial review and whole-book references.

## Lesson discrepancies and decisions

- Older lessons describe the ownership system as unbuilt.  Explained the
  current claims and selection code.  Used the expansion document only for
  the mode-strategy design example, distinguished from implemented features.
- Rapid fire claims PUSHER/FLYWHEEL, targeting TURRET.  They can coexist.
- Macro requests are first-active-wins; cross-category overlaps use the
  replacement branches in BillyRobot.  Avoid a universal newest-wins claim.
- stopAll cancels the macro and assist; manual dispatch follows in the same
  tick.  It is not a latched all-actuator stop.

## Guesses and questions for Jason

No personal history inferred.  Any design extension will be labelled as a
proposal, not presented as an implemented feature.

## Unverified claims

No physical robot run.  Future overlapping macro/assist examples can only
illustrate code paths: the current strategies' claims are disjoint.

## Cross-references left at chapter level

Chapters 1, 3, 7-12, and 21; sharpen only where the target entry was checked.

## Foreign-owned asides

None used.  This chapter explains enum/Set/EnumSet only as needed to read
the newly introduced claims machinery; generics point to Chapter 7.

## Proposed source TODOs

None.  Current scope is explaining existing ownership behavior.

## Verification and next checkpoint

- Selected chapter check passed for all Java blocks and the opening, with
  line-edge whitespace ignored.  Removed trailing source whitespace from
  quoted lines; tokens, comments, and internal spacing are preserved.
- Ten entries; two Predict boxes and one Argue it.  Prose typography check
  passed.  The roughly 28 KB chapter is above the 25 KB guideline because
  it includes the manager, dispatch, replacement, and cancellation excerpts.
- Built Word/PDF and inspected the contact sheet and large pages.  Trimmed
  repeated prose to keep 13.7/13.8 cross-reference lines with their entries.
  Final output:  13 pages; inspected final contact sheet and pages 8/9.
  No clipping seen in the code boxes or tables.
- Build/run details and log paths are in BACKLOG.  No physical robot run.
  The standalone chapter build doesn't include the book title-page opening;
  its layout awaits the combined-book pass.
- Next:  editorial review and the final cross-reference pass.  The next
  unfinished writing task is Chapter 2 from 2.7, LimelightExtensions.
