# Chapter 7 notes

Drafted 2026-10-26 by Claude (drafter for Chapters 6-8).
Robot code baseline:  `305fb81`, plus the uncommitted `TODO` in
`DoublyLimitedMotor.java` recorded in BACKLOG.  Java blocks are copied from
the working tree by line range.

Progress:  7.1-7.9, closing sections, and opening saved.  Codex completed
the handoff notes and scoped the reuse and concrete-return-type explanations.
Source checks and rendered review completed.  Next:  Jason's editorial review.

## Sources

- `doc/FlintLessons/02-modeling-a-component.md`
- `doc/FlintLessons/GettingStarted/02-ftc-hardware-toolbox.md`, from *Reuse
  first* to the end of *Sensors belong to a layer, too*, and *Adding a device*
- `doc/TelemetryContract.md`, `doc/AbstractRobotObjectModel.md`
- `planning/code-facts.md`, Chapter 7, sections A-D
- every file in `RobotModel/Mechs/Components/`
- `RobotModel/Mechs/Assemblies/BillyMA.java`, `CascadeArm.java`,
  `ExampleIntakeAssembly.java`

## Source discrepancies and decisions

- Component lifecycle and telemetry descriptions follow current code.
- The future sensors in 7.8 are proposals already recorded in source TODOs,
  not features currently implemented or a request to rename student work.
- Scoped the constructor discussion to SpinnyIntake; other components can
  accept additional choices such as telemetry strategies.
- Concrete return types are explained through the actual overrides rather
  than implying that any caller-selected generic type is safe.

## Guesses and questions for Jason

The device-wrapper naming convention remains an open design choice, as
recorded in MechComponent.  No renaming or implementation change made.

## Unverified claims

Hardware travel, gripping, and sensor placement weren't physically tested.
The chapter distinguishes current components from proposed sensor additions.

## Unresolved cross-references

Chapters 8 and 11 now have complete drafts.  No new future entry number
introduced by this continuation.

## Foreign-owned asides

Strategies, autonomous verbs, and assembly checklists point to Chapters
8, 11, and 9.  This chapter owns the component contract and lifecycle.

## Next checkpoint

Selected source checks passed.  Built the Word draft and reviewed the
20-page contact sheet; no clipping seen.  The 31.6 KB draft exceeds the
chapter-size guideline because of source excerpts and closing reference
tables.  A later editorial pass can assess duplication with Chapters 8/11.
Next:  Jason's editorial review and the final whole-book cross-reference pass.
