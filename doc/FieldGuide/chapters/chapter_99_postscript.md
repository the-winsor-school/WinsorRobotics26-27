# Postscript

*How this book was made, and how to keep it true.*

---

This book was drafted with two AI writing assistants, Claude and Codex,
working from the Flint lessons and from this repository.  It has one rule
above the rest, the one in the subtitle:  the code on the page is the code on
the robot.

The Flint lessons were written in September, and the code kept moving after
that.  Before a single chapter was
drafted, every code block and every claim in the lessons was checked against
the repository as it stood.  A lot of the bugs the lessons were built around
had already been fixed by the team, in one commit:  the intake's two `if`
statements, the Limelight crash, the turret's backward sign, the pusher's
direction.

So this book describes the code you actually have.  Where a lesson taught a
bug, the chapter teaches the fix, and mentions the bug only where the history
helps you understand the idea.  Where something is still waiting to be built,
the code has a `TODO` that says what's next.

The other rule was about who wrote the code.  Students did, and this book
treats it that way.  It describes what the code does, names the idea behind
it, and points at the next step.  If you're reading about code you wrote,
I hope it reads like somebody took your work seriously, because they did.

## Keeping it true

A book about code goes out of date the day the code changes.  This one is
useful only as long as the team keeps it in step with the code.

- When you rename a class or change a method, search
  `doc/FieldGuide/chapters/` for it, and fix the pages that mention it.
- Run `doc/FieldGuide/reference/check_chapters.ps1`.  It checks that every
  code block in the book still matches the repository.
- Rebuild the book (the steps are in `doc/FieldGuide/README.md`), and look at
  the pages you changed.
