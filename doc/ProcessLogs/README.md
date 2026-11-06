# Process logs

Each student records how their work was done, one log per task, in
`doc/ProcessLogs/<firstname-lastname>/YYYY-MM-DD-<short-topic>.md`.
The rules are in the repo's [AGENTS.md](../../AGENTS.md). Every AI assistant follows them,
and `CLAUDE.md` imports the same file.

**Why:** Git shows *what* changed. This log shows *how* and *why*: what you asked for,
what plan you followed, what you tested and what you understand. Judges, mentors and next
year's team will ask about the *why*.

Copy the template below to start a new log.

---

```markdown
# <Short topic>

Student: <firstname lastname>
Started: YYYY-MM-DD
Branch: <branch name>
Assistant: <tool and model, e.g. Claude Code (Opus) or Codex>; note it here if you switch

## Goal
What I'm trying to do and why, in one or two sentences.

## What I asked for
My requests to Claude, copied word for word, in order. Include follow-ups and corrections.

> first request

> follow-up

## Plan
Numbered steps, written down before any code changed.

1. Step one: files it touches
2. Step two: files it touches

## What changed
One line per change: file, what changed, why.

- `path/to/File.java`: what changed, and why

## Decisions
Choices made along the way, and what else we considered.

## Deviations and approvals
Places where this work breaks a rule in AGENTS.md, and why. Also mentor approvals for new
dependencies, patterns or base-class changes: who approved, and when.

## Testing
For each change: run on hardware / compiled only / not tested, and what I saw.

## In my own words
Written by me, not by Claude.

**Q (from Claude):** a question about the trickiest part
**A:** my answer

**Q:**
**A:**

## Still unknown
Open questions, things not checked on the robot, settings copied from somewhere else.

## Commits
A commit can't contain its own hash, so add each hash the next time you update this log.

- `abc1234`: message
```
