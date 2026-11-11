# Chapter 0 - How to Use This Book

*Look up one idea, read its entry, then go try it on the robot.*

---

This is a reference book for the team.  You don't have to read it in order.
Most of the time you'll arrive with one question, like what a `MechComponent`
is for, or why the turret ignores its bumpers, and leave with the answer.

The book is made from the Flint lessons in `doc/FlintLessons/`.  Those lessons
are tutor sessions:  a chatbot asks you questions, one session at a time, in
order.  This book has the same ideas rewritten for a reader with nobody to
ask.  Every question a tutor would have asked, the book answers.

I wrote it for the youngest programmer on the team as much as the oldest.  If
you're in middle school and new to Java, it's for you.  Start at Chapter 1,
and take your time.

## Finding what you need

Every entry in the book has a number, like **7.3**.  The first part is the
chapter, and the second is the entry within it.  So `7.3` names one entry in
the whole book, and the book points at other entries that way:  "(see 7.3)."

There are four ways in.

- **The contents**, at the front, lists every entry by its title.  Entry
  titles are written the way you'd search for them:  the name of the thing
  (`12.4  doAndWait`), or the question you have (`13.6  Who controls the
  turret?`).
- **Each chapter's opening** has a table of its entries, each with the
  question it answers.
- **The glossary**, at the back, lists every term the book defines, and the
  entry that explains it.
- **The *See also* line** at the end of every entry points at the entries
  closest to it.

Every entry is written to make sense on its own.  When it needs an idea from
somewhere else, it gives you that idea in a sentence and points to where it's
explained.

## How an entry works

Most entries start with a line like this one:

*Where it lives:*  `RobotModel/Mechs/Components/SpinnyIntake.java`

That's the file the entry is about.  Paths start at the team's code folder,
`teamcode`, or at the top of the repository (`doc/...`).  Open that file
beside the book.

Then the entry shows you real code doing the thing, and names it afterward.
Along the way you'll meet five kinds of box:

| Box | What it's for |
|---|---|
| **Aside** | a piece of Java, or a term, explained where you first need it |
| **Try it** | something to do at a laptop or at the robot, and what you should see |
| **Watch out** | a trap:  a bug that bites, a safety rule, a mistake people make |
| **Predict** | stop and guess before you read on.  The answer is the next paragraph |
| **Argue it** | a design question with more than one good answer, for the team to talk over |

A **Predict** box works only if you actually guess.  Cover the next paragraph,
decide, then read.

Every chapter ends the same way:  a summary, a table of the Java the chapter
used, the chapter's vocabulary, questions to check yourself with, and where to
go next.  Each question names the entry with its answer.

## The code on the page is the code on the robot

Every line of code in this book was copied from this repository.  A few rules
make that work.

- **Three dots on a line of their own** (`...`) mean some lines were left out.
  Everything else inside a code box is exactly what's in the file.
- **Made-up code says so.**  Sometimes an idea is clearest in a small example
  that isn't in the codebase.  The text always tells you, in so many words:
  "here's a made-up method."
- **Old code names its commit.**  When the book shows how something used to
  be, it says which version:  "before commit `1a6809a`."  You can see it
  yourself with `git show`.

The code keeps changing, and the book has to keep up.  When a page and the
code disagree, the code wins.  Tell me, and we'll fix the page.  When you
rename a class or change a method, search `doc/FieldGuide/chapters/` for it,
the same way you'd search the Flint lessons.

The code in this book was written by students on this team.  The book
describes what it does and the ideas behind it.  Where something is still
waiting to be built, the code usually has a `TODO` comment saying what comes
next, and the book points you to it.  Those `TODO`s are good places to start a
project, and 21.10 lists them by size.

## Where to start

| If you want to... | Start with |
|---|---|
| read the team's Java for the first time | Chapter 1, then Chapter 6 |
| add a motor, servo, or sensor | Chapter 3, then 7.9 |
| understand how Billy is put together | Part 3, Chapters 6 to 11 |
| make the robot do something over time, in autonomous | Chapter 12 |
| find out why a button or the turret does nothing | 13.6, then Chapter 22 |
| aim at an AprilTag | Chapter 5, then Chapters 14 and 15 |
| build or fix the robot itself | Part 6, Chapters 16 to 20 |
| design next season's robot | Chapter 21 |
| hunt down a bug | Chapter 22 |

## The parts

| Part | Chapters | What it's about |
|---|---|---|
| 1 - Reading the Code | 1, 2 | Java as this codebase uses it, and the shared helpers |
| 2 - Hardware and Sensing | 3, 4, 5 | the SDK's devices, sensors, and AprilTags |
| 3 - The Robot Model | 6 to 11 | the layers Billy is built from, from component up to robot |
| 4 - Behavior Over Time | 12, 13 | state machines, and who controls each part |
| 5 - Seeing the Field | 14, 15 | where the robot is, and aiming at a group of tags |
| 6 - Building the Robot | 16 to 20 | structure, motors and gearing, wheels, slides, electronics, kits |
| 7 - Designing a Robot | 21, 22 | a robot from first idea to working code, and the bugs to watch for |
