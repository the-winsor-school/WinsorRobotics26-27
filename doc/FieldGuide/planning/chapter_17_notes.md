# Chapter 17 notes

Drafted 2026-10-26 by Codex.  Status:  draft and source review complete for
this pass;  12-page chapter preview inspected.  Full-book layout and title-page
art verification remain pending.  Robot code baseline:  `305fb81`.

## Sources

- `doc/FlintLessons/Mechanical/actuators-and-mechanism-sizing.md`
- `doc/rev-duo/actuators-and-gearboxes.md`, `design-decision-guide.md`, README
- `planning/code-facts.md`, mechanical audit, Chapter 17
- Manufacturer checks below, read 2026-10-26:
  [HD Hex](https://docs.revrobotics.com/duo-build/motion/motors/hd-hex-motor),
  [Core Hex](https://docs.revrobotics.com/duo-build/motion/motors/core-hex-motor),
  [cartridges](https://docs.revrobotics.com/rev-crossover-products/ultraplanetary/cartridge-details),
  [load ratings](https://docs.revrobotics.com/rev-crossover-products/ultraplanetary/load-ratings),
  [Control Hub specifications](https://docs.revrobotics.com/duo-control/control-system-overview/control-hub-basics),
  [Servo Power Module](https://docs.revrobotics.com/rev-crossover-products/servo/spm).

## Differences and decisions

| Local source claim | What the chapter does |
|---|---|
| Core Hex has a 20:1 internal gearbox | Manufacturer specifies 72:1.  Corrected. |
| Core Hex encoder is physically on output shaft | Manufacturer gives 4 counts at motor, 288 at output.  Explain count reference, without asserting physical placement. |
| Rounded decimals are exact | Use fractions 84/29, 76/21, 68/13.  Compound reductions are about 18.93 and 54.83. |
| 18.88:1, 54.68:1, 2,076 RPM | Recalculate from fractions and 6,000 RPM:  about 18.93, 54.83 and 2,071 RPM. |
| HD Hex produces about 40 W;  one comfortably handles example lift | Manufacturer specifies 15 W max output.  Keep the 31.4 W design calculation and reject one motor for that requirement. |
| 40 N-m is a gearbox maximum to work toward | Manufacturer labels a failure threshold and warns about shock/cantilever loads.  Present as failure threshold. |
| Hub has 2A total for all six servo ports | Manufacturer specifies 2A per pair and 5A shared total with auxiliary 5V ports.  Corrected. |
| SPM input is XT30 | Manufacturer shows screw terminals.  Point to its wiring guide.  Include 3A/channel alongside 15A total. |
| 1.4-1.8m/s is universal sweet spot | Attribute as local guide's starting design choice;  calculate unloaded speed, then measure. |

The made-up exact 20:1 gearbox in 17.2 is explicitly labeled.  No Java
code blocks are used.  The 1.5kg practice load remains the lesson's
hypothetical exercise, not a claim about a team mechanism.

## Guesses and questions for Jason

Which SRS generation is in the team's inventory?  The local reference mixes
the older part number and a V2 manual link.  The chapter scopes the 270-degree
description to the older servo and asks the reader to check the model.

Should the local REV reference files get a separate corrective pass?  This
continuation corrects the book and records errors here, preserving lesson and
reference source files for a reviewable follow-up.

## Unverified

The local explanation of sun/planet tooth size is retained with attribution.
The manufacturer confirms the stacking order;  its linked page does not
independently establish that detailed tooth-strength explanation.
No published peak rating is treated as a sustained operating guarantee.

## Cross-references

17.4 preserves the inherited Chapter 16 pointer.  18.3, 18.6, 18.7 and the
19.2/19.3/19.5/19.6/19.8/19.9 pointers resolve in the mechanical section.
Pointers to Chapters 3 and 7 await those drafts.

## Asides owned elsewhere

Encoders and SDK servo types get a brief reminder and a Chapter 3 pointer.
No Java asides were added.

## Next step

Formulas and tables were checked in rendered pages, including a detailed view
of the power example.  Check the title-page opening in the full book.  The power
example's changed conclusion is an editorial decision for Jason to review.
