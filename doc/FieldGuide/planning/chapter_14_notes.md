# Chapter 14 notes

Drafted 2026-10-26.  Robot code baseline:  `305fb81` (`master`), rechecked
against the working tree the same day;  no Java file in this chapter's scope
had changed.
Status:  all nine entries, summary, opening drafted;  mechanical checks run.
Next:  cross-reference pass and review.

## Sources

- Lesson:  `doc/FlintLessons/Advanced/apriltags-and-navigation.md`
- `planning/code-facts.md`, Chapter 14 and sections A-D
- Team code (paths from the `teamcode` package):
  `AutonStrategies/LimelightAutoTarget.java:108-126` (14.1, abridged with `...`);
  `RobotModel/Robots/BillyRobot.java:101-104` (14.4), `:106-116` (14.5)
- SDK samples (`FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples/`):
  `ConceptAprilTagLocalization.java:79-102` (comment and camera pose), `:187`,
  `:250-263` (14.2, 14.3);  `SensorLimelight3A.java:102-105`, `:150` (14.4)
- `README.md:62-103`, SDK 12.0 release notes (the bold BIOBUZZ localization line)
- `doc/limelight3a/api.md:94-96`, `data-shapes.md:25-30, 64-82`,
  `java-objects.md:78-85, 172, 340-344`
- `doc/game-manual/sections/09-9-arena.md`, sections 9.2, 9.6, 9.9
- `build.dependencies.gradle:7-14` (SDK 12.0.0)

Every Java block was copied from the files above;  the `!= null` line in 14.6
is introduced as made up.

## Where the lesson and the code or docs disagreed

1. **SDK version.**  The lesson says `java-objects.md` was checked against
   11.2.1, "the exact SDK version this repo builds against."  The repo builds
   against 12.0.0.  The chapter cites the Limelight docs by path and repeats
   neither version claim.  Their Limelight facts are used as the team's
   reference;  whether 12.0.0 changed any of them is unverified.
2. **The `robotPose` block.**  The lesson's block reads `"XYZ ..."` and
   `"PRY ..."`.  The 12.0 sample reads `"Robot XYZ ..."` / `"Robot PRY ..."`
   and sits inside `if (detection instanceof AprilTagSingleDetection)` and
   `if (singleDet.metadata != null)`.  The chapter quotes the current sample
   and points to 15.4 for `instanceof`.
3. **`getBotpose_MT2()` code line.**  The lesson shows
   `Pose3D botpose_mt2 = result.getBotpose_MT2();` as code.  No file in the
   repository contains that line (it's in prose in `api.md:96`), so the
   chapter names the method in prose and quotes the doc.
4. **Billy's unused `AprilTagProcessor`.**  The lesson says Billy builds one,
   marked `TODO`.  Commit `1a6809a` removed it.  Left out:  it teaches nothing
   the current Limelight setup doesn't, and PROCESS asks for historical
   changes only with a teaching purpose.
5. **"Every tag's field position is already known."**  The lesson's guided
   practice says the field map is fixed and published.  For BIOBUZZ it isn't:
   the tags are on the CELLS, each HIVE pivots (manual 9.6), and the 12.0
   release notes say in bold the tags "are not suitable for absolute Field
   Localization."  The chapter teaches field pose as the general technique
   and states the BIOBUZZ limit in 14.2, 14.5 and 14.8.
6. **MegaTag2 quote source.**  The lesson attributes it (by position) to
   `java-objects.md`;  it's in `api.md:96`.  Cited correctly.
7. **"No remaining ambiguity" with one tag.**  The lesson's 14.7 material says
   four corners leave "no remaining ambiguity";  its sister lesson (Chapter
   15) says small planar targets have a real pose-ambiguity problem.  The
   chapter says one tag is enough to *solve*, and points to 15.3 for when it
   isn't enough to solve *well*.
8. **Sample guard.**  Chapter 5's lesson says `SensorLimelight3A` checks
   `result != null && result.isValid()`.  The sample checks only
   `result.isValid()` (`:103`).  This chapter quotes the sample as it is.

## Guesses and open questions for Jason

1. Is MegaTag2 for Billy worth listing as a project, given that BIOBUZZ tags
   move?  The chapter calls it "a project waiting for someone to take it on"
   and attaches the BIOBUZZ caution.  Cut if it reads as a dead end.
2. The Try it in 14.4 says the sample prints `Limelight` / `No data available`
   with no tag in view.  That is what the code does when `isValid()` is false;
   it was not run on the robot.
3. 14.5 says `resetYaw()` makes zero "whichever way the robot faced when the
   code started" and that MegaTag2 needs the zero lined up with the field map.
   That reading of `api.md:96` ("an IMU heading aligned to the loaded field
   map") is an inference;  worth a mentor's check.
4. Length:  about 25.8 KB, a little over PROCESS's 25 KB.  The next cuts would
   be the summary table or the 14.9 list.
5. No giant-asterisk candidates.

## Unverified

Outside-world claims, kept only as the lesson states them, some softened:

- AprilTags came from the University of Michigan's APRIL Robotics Lab, around
  2011.
- NASA's Astrobee robots on the ISS use AprilTags.  The lesson says "for
  localization and docking";  softened to "use markers like these where
  there's no GPS."
- University labs use AprilTags as a cheap substitute for motion capture
  (lesson:  "routinely";  chapter:  "often").
- Indoor drones and warehouse robots use floor, ceiling or shelf fiducials;
  Amazon Robotics's warehouse drive units read floor markers.
- ROS ships an `apriltag_ros` package (lesson:  "official";  chapter drops
  "official").
- ArUco is a sibling marker system built into OpenCV.
- A self-driving car blends GPS, IMU and wheel odometry (the lesson says "an
  Extended Kalman Filter";  the chapter drops the filter's name).
- GPS needs several satellites;  one satellite gives a distance, a sphere.
- A surveyor trusts a fix from four landmarks more than one (lesson's
  analogy).

Repository facts not checked against SDK 12.0.0 itself (the jars and sources
aren't in the repo or gradle cache):  that `getBotpose()` returns a zeroed
`Pose3D` with no tags;  the zero-filled standard deviations;  the meaning of
`getBotposeTagCount/Span/AvgDist`;  `getRobotPoseFieldSpace()`.  All come from
`doc/limelight3a/`, written against 11.2.1.

## Cross-references left at chapter level

Chapter 3 (IMU, yaw), Chapter 4 (14.5 see-also, sensors), Chapter 5 (tags,
`tx`, `ftcPose`, fiducial), Chapter 10 (robot-wide sensors, in the Argue it),
Chapter 12 (14.1 see-also, `IState`), Chapter 13 (claims), Chapter 22
(silent-failure trap).

## Asides used that another chapter owns

None.  `String.format` is explained in two sentences of prose in 14.2;  no
chapter owns it in the table.  `instanceof` is used in 14.2 with a pointer to
its owner, 15.4.

## Proposed source TODOs

None.  Nothing in this chapter's scope is a broken feature;  pose reading is a
capability the code hasn't added.
