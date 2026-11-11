# Chapter 5 notes - AprilTags and the Limelight

Code baseline:  `305fb81`, rechecked 2026-10-27 against `dc4da50`.

**Progress:**  complete.  5.1-5.10, closing sections, and `book/opening_05.md`
written 2026-10-27, in one session, from scratch.  The original drafter's
research transcripts are on the home machine and weren't used.

## 1.  Sources

- `doc/FlintLessons/GettingStarted/04-apriltags-and-limelight.md`
- `RobotModel/Robots/BillyRobot.java` (Limelight setup, tracker start, `y`
  toggle);  `AutonStrategies/LimelightAutoTarget.java`;
  `Extensions/LimelightExtensions.java`;  `OpModes/BillyTeleOpBLUE.java`
- SDK samples:  `SensorLimelight3A.java`, `ConceptAprilTagEasy.java`,
  `ConceptAprilTag.java`, `RobotAutoDriveToAprilTagOmni.java`
- `doc/limelight3a/api.md`, `data-shapes.md` (checked against SDK 11.2.1;
  the repo builds 12.0.0)
- `planning/code-facts.md`, Chapter 5 section

## 2.  Where the lesson and the code disagreed

- **The `tryGetFiducial` crash** is fixed (`1a6809a`).  5.8 shows the current
  guard and describes the old shape in one sentence, as history.
- **The `rotateCW` sign** is fixed (`1a6809a`).  5.6 shows both methods as
  they are now, with no bug story.
- **The unattached `AprilTagProcessor`** in `BillyRobot` was deleted, not
  finished.  5.2 notes the unused `VisionPortal` field on `Robot`;  the
  lesson's "should Billy have both?" question survives as 5.10's Argue it.
- **The lesson's webcam loop** (`detection.id`, `detection.metadata`) won't
  compile against SDK 12.0.  5.10 points to 14.2 and 15.4, which show the
  current sample's loop, and quotes only the two setup calls.
- **The lesson's result loop** is illustrative.  5.4 quotes the SDK sample's
  loop instead.
- The lesson quotes the sample as guarding with `result != null &&
  result.isValid()`.  The sample checks only `isValid()`.  5.8 says so.

## 3.  Guesses and open questions for Jason

- `isValid()`:  the team's helpers check `null` and not `isValid()`.  An
  invalid result's fiducial list is presumably empty, so the search hands back
  `null` anyway;  unconfirmed against 12.0.0.  `cluster-targeting-plan.md`
  Task 1 asks for the check.  No `TODO` proposed.
- `doc/limelight3a/api.md` says that in 11.2.1, `getLatestResult()` never
  returns `null`, and synthesizes an empty invalid result.  The SDK sample
  comment says it returns `null` without `start()`.  5.3 quotes the sample.
  Check against 12.0.0.
- 5.9's troubleshooting list keeps the lesson's motion blur, lighting, and
  distance points.  "The SDK's webcam samples set a short exposure" is
  checked:  `ConceptAprilTagOptimizeExposure` and both `RobotAutoDriveToAprilTag`
  samples set exposure.
- 5.3's Try it sends the reader to the Limelight's web page without a
  port or address.  `api.md` gives the HTTP API port (5807), which isn't the
  web page.
- Moved **deadband** to Chapter 4 in the PROCESS ownership table, since 4.3
  explains it with the turret's `±2`.  Chapter 5 points there.

## 4.  Unverified

- The lesson's "serial number is actually an IP address."  `api.md` line 100
  agrees ("the Control Hub interface address displayed as a serial number").
- The lesson's claims about AprilTags against QR codes (5.1) are kept as the
  lesson states them.  No new outside claims added.

## 5.  Cross-references left at chapter level

- 5.1:  Chapter 14, Chapter 15
- 5.2:  Chapter 14
- 5.6:  Chapter 12
- 5.8:  Chapter 13, and "Chapter 14 shows code that checks `isValid()`"
  (14.4's sample, around line 220 of chapter 14)
- 5.9:  Chapter 15;  12.6 and 13.6 given exactly
- 5.10:  Chapter 14;  14.1, 14.2, 15.4 given exactly

## 6.  Asides used that another chapter owns

None.  `for` points to 2.6, `null` to 1.2, lambda to 1.9, the deadband to 4.3.
`Optional` and guard clause are explained in 5.7 and 5.8.  PROCESS gives guard
clause to Chapter 13;  5.8 defines it in one sentence, and Chapter 13 still
owns it.
