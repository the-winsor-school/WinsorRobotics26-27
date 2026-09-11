# Goal 
The tank must follow an april tag, which may be moving around or away from the robot. It should be able to follow different aprilTags, which we will place. The tank should be able to spin itself around to find the aprilTag. 


## What Components are Required? (Hardware level implies Abstraction level requirement.)
The components that are required are Standard Tank DriveTrain and Limelight.

## What Are we extending (Scope)
We are extending the Robot Strategy. It should not be modifying any existing files. It should be a new IRobotStrategy that uses a StateMachine. 

## How should it happen? (broad strokes of Milestones)
The program will be a mode and run in the background while chasing the april tags. It should use the preexisting stateMachine. 

# Rules 
> What is Codex allowed to touch, what should it NOT touch
> what are conventions/styles it should follow.

- It should not be modifiying any existing files.
- Codex should log all of it's process in the working document.

# Structure (Step by Step Plan)
1. Take `InitialPlanning.md` and build a formal process Plan with Codex.
2. Refine the plan in dialog with Codex
3. Enforce checkpoints during the execution of the plan.
4. ...  Coding Milestones:

## Codex Comments Before Formal Project Plan
1. The goal is clear at a concept level, but acceptance criteria are not yet measurable.
**developer note** Acceptance means explicit approval by the developer.
2. There is a likely scope conflict between "do not modify existing files" and "use preexisting stateMachine" if integration/registration is required.
**developer note** model based off of the preexisting stateMachine in a new file, thereby not modifying existing files
3. "Run in the background while chasing AprilTags" is ambiguous and should be tied to a specific OpMode and activation model. **developer note:** It should run in autonomous
4. Multi-tag behavior is currently underspecified and needs an explicit selection/switching policy.
**developer note:**
5. Search behavior ("spin to find tag") needs safety bounds and timeout/fallback behavior.
6. Hardware assumptions need to be explicit (camera mount, offset, FOV expectations, calibration status). **developer note:** It is a Limelight3A limelight

## Open Questions To Resolve Before Planning
1. What exact success metrics define "follow"?  
**developer note:** These should be tested empirically, so use static variables to configure.
   - Target stand-off distance
   **developer note:** 1 meter  
   - Heading/lateral tolerance
   **developer note:** 5 degrees
   - Reacquisition time after losing tag
   **developer note:** 5 seconds  
2. Which mode(s) should support this strategy (`TeleOp`, `Autonomous`, or both)?
**developer note:** Autonomous
3. Is "no existing file changes" absolute, or are minimal integration edits allowed?
**developer note:** Absolute
4. Which specific state machine implementation should be reused, and what interface constraints does it impose?
5. How should the system choose between multiple visible tags?
   - Closest
   - Fixed priority list
   - Driver-selected target
6. What should happen when no tag is visible?
   - Spin search immediately
   - Hold position first
   - Timeout then abort/fallback
7. What control limits are required for safety and drivability?
   - Max translational speed
   - Max rotational speed
   - Acceleration/jerk constraints
8. Which Limelight signals should drive control (`tx/ty`, pose estimate, distance estimate, confidence)?
9. What environmental assumptions are expected (lighting, tag size, expected range)?
10. What telemetry is required for tuning and debugging?
11. What are the required override/abort behaviors?
12. What testing stages and pass/fail checkpoints are expected before competition use?
