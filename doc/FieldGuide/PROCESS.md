# The Field Guide Process

How the Flint lessons become a reference book.  Written 2026-10-26, adapted
from the chapter process in `OODP_Lessons/Course/chapters/PROCESS.md`, which
made the C# course book.  Read this file first, then
[`planning/code-facts.md`](planning/code-facts.md), then the lessons your
chapters come from.

---

## What this book is

The lessons in [`doc/FlintLessons/`](../FlintLessons/README.md) are tutor
prompts.  They're written to a chatbot ("Ask the student...") and they run one
session each, in order.  This book takes the same material and turns it into
something a student reads alone, in any order, to look up one big idea at a
time.

So three things change, and everything else in this file follows from them.

1. **The reader is alone.**  Every question a tutor would ask becomes the
   explanation it was asking for.  Nobody is there to say whether a guess was
   right, so the book says it.
2. **The reader jumps in anywhere.**  A student arrives at 13.4 from the
   glossary with a question about the turret.  Every entry has to make sense
   to that reader, or point them in one line to the entry that makes it make
   sense.
3. **The youngest reader is a middle schooler.**  The team has 6th to 8th
   graders who are new to programming.  Write for a capable one.

## Where things live

```
doc/FieldGuide/
  README.md                 what this is, and how to build it
  PROCESS.md                this file
  chapters/                 one Markdown file per chapter
  book/opening_NN.md        each chapter's title-page opening
  planning/
    code-facts.md           the current code, checked against every lesson
    BACKLOG.md              what's done, who has what, and what's next
    chapter_NN_notes.md     each drafter's notes:  guesses, unverified claims, open questions
  reference/                the build:  build_book.lua, finish_book.ps1, make_art.ps1, make_cover.ps1
  build/
    word/                   one .docx per chapter, for Flint
    book/                   the printed book, its art and cover
```

### Leaving work another session can finish

A session can stop partway, and the first run of this book did.  So
`planning/BACKLOG.md` says what's done, who has what, and what's next.  Read
it after this file, and update it when you take a chapter or hand one back.

Save the chapter and its notes after each finished entry, and have the notes
name the next entry.  Then an interrupted draft loses one entry, at most.  A
build's output proves something ran;  open it to see what it made.

Before starting a script that may outlive its writer's session, add a short
run entry to `planning/BACKLOG.md`:  its goal, exact command, start time,
expected outputs, and log path.  Record the process id and tool session id
as soon as they are available.  Include the machine/session context:  a
reused process id alone doesn't identify an old task.

When the script finishes, record its exit status separately from the review
result.  Name the output you opened and what you checked.  Until someone
has inspected it, mark it **finished, awaiting review**.  On recovery, read
the log and inspect existing outputs before deciding whether to rerun.
This record can be a few bullets;  it doesn't need a separate task runner.

## The chapters

Numbers are global, so `7.3` names one entry in the whole book.  Cross-reference
by those numbers.

| Part | Ch | Title | Comes from |
|---|---|---|---|
| | 0 | How to Use This Book | written last, by hand |
| 1 - Reading the Code | 1 | Reading Java in This Codebase | GettingStarted/01 |
| | 2 | Extension Methods | Advanced/extension-methods |
| 2 - Hardware and Sensing | 3 | The FTC Hardware Toolbox | GettingStarted/02, up to *Reuse first* |
| | 4 | Sensors, and Where They Live | GettingStarted/03 |
| | 5 | AprilTags and the Limelight | GettingStarted/04 |
| 3 - The Robot Model | 6 | The Layered Robot Model | 01 |
| | 7 | Components | 02, and GettingStarted/02 from *Reuse first* on |
| | 8 | Control Strategies | 03 |
| | 9 | Assemblies | 04 |
| | 10 | The Robot Layer | 06 |
| | 11 | Autonomous Verbs | 08 |
| 4 - Behavior Over Time | 12 | State Machines | 09, Advanced/state-machines |
| | 13 | Control Ownership | 05, 07, Advanced/control-ownership, and the arbitration code |
| 5 - Seeing the Field | 14 | Pose and Navigation | Advanced/apriltags-and-navigation |
| | 15 | AprilTag Clusters | Advanced/apriltag-clusters |
| 6 - Building the Robot | 16 | Structure, Fasteners, and Shafts | Mechanical/structure-and-fasteners |
| | 17 | Actuators, Gearing, and Sizing | Mechanical/actuators-and-mechanism-sizing |
| | 18 | Power Transmission and Wheels | Mechanical/power-transmission-and-wheels |
| | 19 | Linear Motion and Control Electronics | Mechanical/linear-motion-and-control-electronics |
| | 20 | Learning From Kits and Starter Bots | Mechanical/kits-and-starter-bots |
| 7 - Designing a Robot | 21 | Designing a Robot, End to End | 10, and `doc/NewRobotDesignWorkflow.md` |
| | 22 | Common Pitfalls | Advanced/common-pitfalls |
| | 98 | Glossary | gathered from every chapter's Vocabulary |
| | 99 | Postscript | written last, by hand |

File names:  `chapters/chapter_NN_short_name.md`, two-digit `NN`.

---

## The rule:  every line of code is real

### Writing about student work

This codebase was written by students.  Treat it as student work throughout
the book, planning notes, reviews, and handoffs.  Describe code from its
actual behavior, and distinguish implemented features from design examples.

The book helps students understand and build on their work.  Describe the
behavior and the idea it illustrates in respectful, concrete language.
Minor bugs may be ignored.  Finding a bug is not by itself a reason to put
it in the book or make it a lesson about what a student did wrong.

When a confirmed bug prevents a feature from working, the next step belongs
in a neutral `TODO` comment in the source.  Drafters propose the comment, word
for word with its file and line, in their notes file, and it goes into the
code after review, so several writers working at once never edit the robot's
code.  Say what the feature needs to do,
with enough context to act on it.  Avoid judgments about the work or its
author.  Distinguish a feature that fails from an optional capability that
has not been added.  Keep uncertain observations provisional until checked.

Technical accuracy still matters.  Explain how to use the current code
accurately, and give necessary operating instructions.  Keep any discussion
of a source TODO brief and useful to the reader's task.

Every line of code in a chapter is copied from the repository at the source
revision recorded in the backlog and chapter notes.  Start with `master`'s
checked revision, and recheck if the code changes.  `planning/code-facts.md`
is where to start, and the file
itself is the authority.  Open it and copy from it.

- **The code wins.**  The lessons were written in September, and the code has
  moved since.  Where a lesson and the code disagree, write what the code
  does.  `code-facts.md` lists every place they disagree.
- **Historical changes need a teaching purpose.**  Use an earlier version
  only when it helps explain the idea.  Describe the behavior and the change
  neutrally.  Leave minor bugs out.
- **A feature that needs work gets a TODO.**  For a confirmed feature-breaking
  bug, propose a neutral source comment naming the next step (above).  Mention it in
  the chapter only where the reader needs it to use that feature.  Leave
  implementation changes to separate coding work.
- **`...` may stand for lines left out** of a block.  Nothing inside a code
  block is ever paraphrased.
- **Invented code says it's invented.**  A counter-example, a sketch, or a
  template gets introduced as one, in prose, every time:  "Here's a made-up
  method that would break the rule."  The templates in
  `doc/NewRobotDesignWorkflow.md` are cited as templates.
- **Old code comes from git, with its commit.**  A fixed bug can be shown as
  it was:  copy it from `git show <commit>:<path>` and say which version it
  is ("`BillyMA.java` as it was before commit `1a6809a`").  That's real code
  too.
- **The current design leads.**  The ownership system the lessons call unbuilt
  is partly built (`claims()`, `MacroManager`, `BillyMA.Part`).  Explain what
  the code does now, starting with the student's working design and the idea
  it illustrates.  The audit supports accurate writing;  it is not a list
  of faults to present to students.
- **Don't state a count you haven't checked.**  The lessons say eighteen
  `TODO`s.  There are seventeen now.  Count again before you write a number.

Facts that aren't code come from the team's own references, which were checked
against the exact versions this repository builds against:

| Topic | Source |
|---|---|
| REV parts, ratios, speeds, torques, part numbers | `doc/rev-duo/*.md` |
| Limelight methods and behavior | `doc/limelight3a/*.md` |
| FTC SDK types | the samples in `FtcRobotController/.../external/samples/`, and the SDK release notes at the top of the root `README.md` |
| The game | `doc/game-manual/` |

Those references can contain mistakes too.  When their numbers contradict
each other, check the arithmetic and the linked manufacturer's documentation.
Record the discrepancy, source URL, date checked, and the decision in the
chapter notes.  Give a corrected specification its source in the chapter.
The code audit already identifies a version mismatch in the Limelight
reference;  check the relevant SDK version before relying on it.

A claim about the world outside this repository (who invented AprilTags, what
NASA uses) stays only if the lesson already makes it.  Add no new ones.  Put
any you doubt in your notes file as **unverified**, so they get checked.

Invent nothing about people.  No team history, no events, no student names.

---

## The shape of a chapter

```
# Chapter N - Title
*one-line tagline*
---
Opening:  what the big idea is, and when you'd look here.  Two or three short
paragraphs.  Then a "Read first" line, if the chapter leans on another.
Then the entry table:  | Entry | The question it answers |
---
## N.1  Entry title
*Where it lives:*  `RobotModel/Mechs/Components/SpinnyIntake.java`
...the entry...
*See also:*  9.2, Chapter 13
## N.2  ...
...
## Chapter summary              the big idea in a few sentences, then a table
### Java in this chapter        only where the chapter brings in Java:  | Syntax | Where |
### Vocabulary                  alphabetical:  **term** -- definition.  (N.x)
### Check your understanding    numbered questions, each ending with where the answer is:  (N.x)
## Where to go next             the neighbouring ideas, by chapter
```

**An entry answers one question.**  Title it with the words a student would
look for:  the name of the thing (`## 12.4  doAndWait`), or the question they
have (`## 13.2  Who controls the turret?`).  Aim for 300 to 900 words an entry,
and 6 to 10 entries a chapter.  A chapter runs 12 to 25 KB.

**Every entry stands on its own.**  When an entry needs an idea from somewhere
else, give it in one sentence and point:  "A lambda is a chunk of code handed
over to run later (1.9)."  Don't make a reader go back three entries to follow
this one.

**Billy first, the pattern name after.**  Show the real code doing the thing,
then name what it is.  The team's robots are `BillyRobot` (Billy) and
`Wildbots2025`.

**The tagline** is one plain line, taken from the lesson where a line fits:
OODP's were things like *One deck, many shuffles, and who gets to decide.*

### The marks

The build turns these into colored boxes.  Use these and nothing else.

| Mark | Written as | Means |
|---|---|---|
| Aside | `> **Aside:  topic**` | a piece of Java, or a term, explained where it's first needed |
| Try it | `> **Try it.**  ...` | something to do at a laptop or at the robot, and what you should see |
| Watch out | `> **Watch out.**  ...` | a trap that bites:  a bug, a safety rule, a mistake people make |
| Predict | `> **Predict.**  ...` | stop and guess before reading on.  The answer is the next paragraph |
| Argue it | `> **Argue it.**  ...` | a design question with more than one good answer, for the team |

And two plain lines, which stay out of boxes:

- `*Where it lives:*  \`path\`` under an entry heading, when the entry is
  about one file or class.  Paths start at the `teamcode` package
  (`RobotModel/...`, `AutonStrategies/...`) or at the repository root
  (`doc/...`).
- `*See also:*  ...` as the last line of an entry, pointing at other entries.
  Within your own chapters, use exact numbers.  For another drafter's chapter,
  write the chapter (`Chapter 12`), and the cross-reference pass will sharpen
  it to an entry.

**Keep the questions few.**  The OODP run let tutor questions creep back in,
and its review asked for a cap.  At most three **Predict** boxes a chapter, and
two **Argue it**.  Every other question in the lesson becomes an explanation.
The lesson's guided practice becomes **Try it** boxes or Check your
understanding questions.  Its wrap-up check becomes Check your understanding.

**Asides belong to one chapter.**  The table below says which chapter owns
each term.  The owner explains it.  Everybody else uses it with a pointer:
"`@Override` (Aside in 1.7)".

### The title-page opening

Each chapter gets `book/opening_NN.md`, printed on its title page unlabeled.
One mildly amusing line of prose, then a moment quoted from the chapter
itself, where its problem first shows:  real code, or real output.  OODP's
Chapter 2 opening was *The museum of bad dice is open.  Please don't roll the
exhibits.* above the program's own output.  The joke has to be about something
true in the chapter.  Never name the device.

For a mechanical chapter, the quoted moment may be a passage, calculation,
or table from the chapter.  Use the actual chapter text.  An opening needs
something true to point at;  leave a forced joke out and flag it in the notes.

### Your notes file

Each drafter writes `planning/chapter_NN_notes.md` for each chapter:

1. **Sources** used, with the file paths.
2. **Where the lesson and the code disagreed**, and what you wrote.
3. **Guesses and open questions** for Jason.  Anything you had to infer.
4. **Unverified** claims, especially about the world outside the repository.
5. **Cross-references** you left at chapter level for the pass to sharpen.
6. **Asides you used** that the table says another chapter owns, if any.

---

## Who explains what

The owner explains it, in an Aside or an entry.  Every other chapter points.

| Ch | Owns |
|---|---|
| 1 | class, object, field, type, `int`/`double`/`boolean`/`String`, `public`/`private`/`protected`, `final`, method, signature, parameter, return type, `void`, constructor, `new`, the dot and chaining, `extends` and subclass, `super(...)`, `abstract`, `interface`, `@Override`, `if`/`else if`/`else`, `!` `&&` `\|\|`, lambda (enough to recognize one), `null`, comments, `TODO`, configuration name |
| 2 | `static`, extension method, utility class, the three-question test for a helper |
| 3 | hardware map, `DcMotor`, `DcMotorEx`, run modes, encoder and ticks, `Servo`, `CRServo`, `TouchSensor`, `DigitalChannel`, `DistanceSensor`, color sensor and hue, IMU, yaw, heading, webcam, `VisionPortal`, Driver Station configuration |
| 4 | sensor against actuator, reading against meaning, noise, threshold, hysteresis, deadzone, out-of-range values, a missing sensor, the sensor ownership rule |
| 5 | AprilTag, fiducial, tag ID, `tx` and `ty`, pipeline, polling, proportional control, gain, deadband, `ftcPose`, `NullPointerException`, `stream()`/`filter`/`findFirst`/`orElse` |
| 6 | layer, OpMode, `Robot`, `DriveTrain`, `MechAssembly`, `MechComponent`, TeleOp and autonomous, instructions flow down, capabilities flow up, composition |
| 7 | the component contract, two-phase construction, the telemetry buffer (write against flush), inner class, generics (`<T extends ...>`), cast, the two kinds of component, reuse first |
| 8 | control strategy, Strategy pattern, functional interface, decorator, the rule for which layer a rule belongs to |
| 9 | assembly, Composite pattern, `giveInstructions`, passing telemetry down, the aggregate autonomous object, `updateTelemetry` as a checklist |
| 10 | template method, Facade pattern, the single flush point, robot-wide sensors |
| 11 | autonomous verb, intention against hardware, leaky abstraction, the dot-chain as narrowing |
| 12 | state, `IState`, `StateMachine`, tick, loop, blocking and non-blocking, `Thread.sleep`, `doAndWait`, closure, `IAutonStrategy`, abort and reset |
| 13 | resource, ownership, arbitration, default/assist/macro/mode strategies, `MacroManager`, guard clause, and whatever the arbitration code names (newest request wins, stop all, end reasons) |
| 14 | bearing against pose, six degrees of freedom, field coordinates, `robotPose`, botpose, camera pose, MegaTag1 and MegaTag2, sensor fusion, standard deviation, data association, SLAM |
| 15 | cluster, cluster origin, rigid body, rotating an offset, pose ambiguity, weighted average, `instanceof` |
| 16 | REV DUO, extrusion, channel, Extended Motion Pattern, M3, nyloc, Studs Up, T-nut, hex shaft, collar, spacer, bearing, deburring |
| 17 | HD Hex Motor, Core Hex Motor, UltraPlanetary, gear ratio, reduction, nominal and exact ratios, stall torque, free speed, Smart Robot Servo, brownout, Servo Power Module, work, power, safety factor, surface speed |
| 18 | 0.5 Mod, pitch diameter, center distance, idler, chain, sprocket, chain tension, belt, traction/omni/mecanum/compliant wheels, the mecanum X, durometer |
| 19 | linear slide, cascading and continuous rigging, active retraction, constant-force spring, limit switch, zeroing, Control Hub, Expansion Hub, Driver Hub, service loop, I2C, interference |
| 20 | kit guide, Kickoff Starter Bot, Class Bot, decomposing a design |
| 21 | top-down design, bottom-up building, build order, dependency injection, the design checklist |
| 22 | bug shape, the tell, the five categories |

---

## The voice

Read [`APPALACHIAN_ACADEMIC_STYLE.md`](../../../ClaudeWritingProject/style-guide/APPALACHIAN_ACADEMIC_STYLE.md)
and [`WRITING_COMMON_ERRORS.md`](../../../ClaudeWritingProject/style-guide/WRITING_COMMON_ERRORS.md)
in full before you write a sentence.  They live in
`C:\Users\kyrok\OneDrive\Documents\GitHub\ClaudeWritingProject\style-guide\`.
Then read one OODP chapter for the register:
`C:\Users\kyrok\OneDrive\Documents\GitHub\OODP_Lessons\Course\chapters\chapter_04_collections_and_strategies.md`.

**The register is OODP's chapters, at a middle schooler's reading level.**
Second person.  Contractions are welcome.  Instructions are written as
instructions.  Jason's own "I" belongs to Chapter 0 and the Postscript, so
chapters 1 to 22 leave it out.

**For a middle schooler:**  short sentences, one idea to a paragraph, every
term defined where it first appears in the chapter (or pointed to), and
concrete before abstract.  Technical words are welcome when they do real work.
Explain them;  never water them down.  Keep the trade-offs and the unsolved
design questions.  Apply the student-work guidance above when deciding
whether a bug belongs in a chapter or a source TODO.

**Design choices are trade-offs.**  Jason's word, hyphenated.  Introduce a
choice with its cost first, then what it buys, and call it a trade-off once.
The big one is complexity against reusability.  Webcam against Limelight,
cascading against continuous, averaging against one big solve:  all
trade-offs.  Never "bet".

**The mechanical rules**, which get a grep before a chapter is done:

| Rule | Find it |
|---|---|
| two spaces after every sentence, and after a colon, in prose, boxes, lists, and table cells | `grep -nE '[a-z)][.?!:] ["A-Z]'` and `grep -nE '[a-z)][.?!:](\*\*\|\*) [^ ]'` |
| no typographic dashes or curly quotes;  `--` with a space each side | `grep -n '—\|–\|“\|”\|’'` |
| no paired `-- ... --` brackets | `grep -nE ' -- [^.]{5,120} -- '` |
| headings take a single hyphen | `grep -nE '^#{1,6} .* -- '` |
| no definition by negation | read for `not`, `never`, `nothing`, `without`, `rather than`, `instead of` doing setup-and-knockdown work |
| no abstract "stays" (entry 29) | `grep -niE '\bstay(s\|ed\|ing)?\b'`, and ask whether you could point at it |
| no "landed" meaning got through (entry 37) | `grep -niE '\bland(s\|ed\|ing)?\b'` |
| no "sit with", no "holds still" | `grep -niE 'sit(ting)? with\|holds? still'` |
| no commentary lines (entry 36) | read every paragraph's last sentence |
| no beat that only completes a shape (entry 27) | for each comma-tail, each third item, each run of matched clauses:  delete it and reread |
| prefix hyphens vary (entry 26) | `re-read` and `reread` both, in no pattern |

And the rules with no grep:

- Concede first.  The unflattering part, then the case.
- The sentence carrying the most weight is the plainest one in its paragraph.
- "Data is".  `indexes`, `formulas`, singular `they`.  `grey`.
- No giant asterisks.  Jason places those himself, rarely.  If you think a
  facade over something fundamental deserves one, say so in your notes.
- His conventions stay out of the book.  Never explain the double space, the
  dashes, or any of the rest.
- Straight quotes, no emoji.

---

## Build

```
pandoc lua doc/FieldGuide/reference/build_book.lua 01 02 ... 22   # one .docx per chapter, for Flint
powershell -ExecutionPolicy Bypass -File doc/FieldGuide/reference/make_cover.ps1
powershell -ExecutionPolicy Bypass -File doc/FieldGuide/reference/make_art.ps1
pandoc lua doc/FieldGuide/reference/build_book.lua book            # the printed book, as Word
powershell -ExecutionPolicy Bypass -File doc/FieldGuide/reference/finish_book.ps1
```

`finish_book.ps1` lays the book out in Word, fills in the contents and page
numbers, writes `build/book/Field_Guide_Draft.pdf`, and cuts one PDF per
chapter into `build/pdf`, so a printed chapter matches the book page for page.
Word through COM hangs easily;  `ClaudeWritingProject/COMMON_ERRORS.md` entry
1 says what to do about it.
