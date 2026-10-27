# A Field Guide to the Robot

*The code on the page is the code on the robot.*  (A working title.)

A reference book for the team, made from the Flint lessons in
[`doc/FlintLessons/`](../FlintLessons/README.md).  The lessons are tutor
prompts, run one session at a time, in order.  This book has the same material
rewritten for a student reading alone, in any order, looking up one big idea:
what a `MechComponent` is for, why `doAndWait` never sleeps, which way a
mecanum wheel's rollers go.

It's built the way the C# course book in `OODP_Lessons` is built, and it looks
the same on the page:  the same boxes, the same palette, title pages with line
art, mirrored margins for a three-hole punch.

```
doc/FieldGuide/
  README.md          this file
  PROCESS.md         how a lesson becomes a reference chapter -- read this before writing
  chapters/          the book, one Markdown file per chapter
  book/              each chapter's title-page opening
  planning/          code-facts.md, and each chapter's notes
  reference/         the build scripts
  build/             the output:  word/ for Flint, book/ for print, pdf/ for single chapters
```

## The chapters

| Part | Chapters |
|---|---|
| | 0 How to Use This Book |
| 1 Reading the Code | 1 Reading Java in This Codebase, 2 Extension Methods |
| 2 Hardware and Sensing | 3 The FTC Hardware Toolbox, 4 Sensors, 5 AprilTags and the Limelight |
| 3 The Robot Model | 6 Layers, 7 Components, 8 Control Strategies, 9 Assemblies, 10 The Robot Layer, 11 Autonomous Verbs |
| 4 Behavior Over Time | 12 State Machines, 13 Control Ownership |
| 5 Seeing the Field | 14 Pose and Navigation, 15 AprilTag Clusters |
| 6 Building the Robot | 16 Structure, 17 Actuators and Gearing, 18 Transmission and Wheels, 19 Linear Motion and Electronics, 20 Kits and Starter Bots |
| 7 Designing a Robot | 21 Designing a Robot, End to End, 22 Common Pitfalls |
| | 98 Glossary, 99 Postscript |

## Building it

Needs pandoc 3 and Word.

```
pandoc lua doc/FieldGuide/reference/build_book.lua all          # a .docx per chapter, for Flint
powershell -ExecutionPolicy Bypass -File doc/FieldGuide/reference/make_cover.ps1
powershell -ExecutionPolicy Bypass -File doc/FieldGuide/reference/make_art.ps1
pandoc lua doc/FieldGuide/reference/build_book.lua book         # the printed book, as Word
powershell -ExecutionPolicy Bypass -File doc/FieldGuide/reference/finish_book.ps1
```

The last step writes `build/book/Field_Guide_Draft.pdf`, and one PDF per
chapter in `build/pdf/`, cut from the book so the page numbers match.

## Keeping it accurate

Every line of code in the book is copied from this repository.  When you
rename a class or change a signature, search `chapters/` for it, the same way
the Flint lessons' README asks you to search `doc/FlintLessons/`.  Then rebuild.
