# Chapter 20 notes

Reviewed 2026-10-26 by Codex.  Inherited draft from `8f9a46d`.
Status:  local-source/prose pass and 12-page chapter preview review complete.
Full-book layout and title-page art verification remain pending.
Robot code baseline:  `305fb81`, unchanged at `8f9a46d`.

## Sources

- `doc/FlintLessons/Mechanical/kits-and-starter-bots.md`
- `doc/rev-duo/kits-and-starter-bots.md`, README
- `doc/NewRobotDesignWorkflow.md`
- `RobotModel/Mechs/Components/MechComponent.java` javadoc
- `planning/code-facts.md`, mechanical audit, Chapter 20
- [2026-27 starter bot](https://docs.revrobotics.com/ftc-kickoff-concepts/home/2026-27-rev-duo-ftc-starter-bot.md),
  checked 2026-10-26.  The local preview URL returns Page Not Found;  it points
  to this current page, which includes build, BOM, and programming resources.

## Differences and decisions

Retained the inherited draft's four prerequisite chapters and all five
drivetrain kits.  It had already corrected the lesson's count and its omitted
Basic Channel Drivetrain.  Reuse of existing hardware-shape components takes
priority over the source's instruction to write a dedicated class every time.

Updated the current-season link rather than asserting that a preview is the
current resource.  Historical starter mechanisms remain the local reference's
summaries, introduced as such.  Removed blanket legality claims;  the chapter
is about reading a design, not establishing its compliance in a given event.

Removed unsupported generalizations about every robot needing counterbalancing
and the Class Bot providing no progress toward the season.  Describe skills
that transfer.  Reworded definition-by-negation and unsupported claims about
team goals and typical end-of-season robots.

The imaginary spring-loaded ramp is explicitly invented, as in the lesson.
Its missing latch/release is treated as a design question rather than filled
in with invented hardware.  The Java comment is copied from MechComponent.

## Guesses and questions for Jason

The Class Bot's value and the time available are design choices for the team,
so the discussion questions remain open.  There is no invented team history.

## Unverified

Historical bot mechanism summaries and individual kit architecture were
compared with the local source, not re-audited against every manufacturer's
build manual.  The local preview URL is confirmed stale.  Verify a selected
kit's actual guide before building from its summarized row.

## Cross-references

19.2-19.6 resolve.  Chapters 7-9, 11 and 21 remain chapter-level pointers.

## Asides owned elsewhere

Short reminders for MechComponent and MechAssembly point to Chapters 7 and 9.
No Java Aside was introduced.

## Next step

Rendered tables and the quoted comment were inspected in the chapter contact
sheet.  Check the title-page opening in the full book.  Sharpen references once
the software and design chapters are drafted.
