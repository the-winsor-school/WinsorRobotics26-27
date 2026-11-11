# Chapter 18 notes

Drafted 2026-10-26 by Codex.  Status:  draft and source review complete for
this pass;  11-page chapter preview inspected.  Full-book layout and title-page
art verification remain pending.

## Sources

- `doc/FlintLessons/Mechanical/power-transmission-and-wheels.md`
- `doc/rev-duo/power-transmission-and-motion.md`, `design-decision-guide.md`
- `planning/code-facts.md`, mechanical audit, Chapter 18
- Manufacturer checks, read 2026-10-26:
  [timing belts](https://docs.revrobotics.com/duo-build/motion/timing-belts-and-pulleys),
  [mecanum setup](https://docs.revrobotics.com/duo-build/mecanum-drivetrain-v2/mecanum-wheel-setup-and-behavior).

## Differences and decisions

The lesson's new chain pin became the reference's retained original pin.
Idler ratio is explained by cancellation of tooth-count ratios, rather than
the unrelated center-distance formula.  The formula and examples were
recalculated.  The 18/35-tooth example is explicitly hypothetical.

Avoided the claim that every tooth sum outside multiples of 60 misses every
standard mount.  The actual plate and channel patterns must be checked.
The hub protects a plastic hex bore;  adequate shaft support and spacing
keep gears engaged.  Removed the implication that a hub alone prevents
tooth deflection or climbing.

The local reference says carpet-contact rollers form X while top rollers
form O.  The manufacturer page specifically says to check X from above.
Use that instruction and link its pictures.  The original local ASCII
diagram labels and central diagonals also disagree;  the new sketch clearly
labels the top-view roller lines.  Avoided predictions of a specific diagonal
drift/spin after an unspecified wheel swap.

Softened absolute claims about zero slip, zero backlash, unbreakable belts,
and impossible-to-drive wrong layouts.  Keep the engineering reason and
describe the conditions it depends on.  Kept REV's 3mm-pitch GT2 terminology;
GT2 alone is not a sufficient belt specification.

## Guesses and questions for Jason

Please compare the top-view sketch with a real assembled drivetrain.
The 6mm chain-flex check comes from the local guide;  it is presented as a
starting workshop check, not a span-independent specification.

## Unverified

Local material choices, grease advice, and 9mm belt width were retained as
local reference guidance.  The 2,000-6,000 RPM range is a suggested use case,
not a tested limit for an unspecified belt or chain.
No part catalog or inventory validation was attempted.

## Cross-references

Mechanical entry pointers resolve across Chapters 16-20.  Chapters 3 and 6
remain chapter-level pointers until their drafts exist.

## Asides owned elsewhere

No Java asides.  Gear reduction points to 17.3;  encoder math points to 17.2.

## Next step

The mecanum sketch was shortened after visual review, until its See also
line fit on the same page.  Equations and table widths were checked in the
chapter preview.  Inspect the title-page opening in the full book.
