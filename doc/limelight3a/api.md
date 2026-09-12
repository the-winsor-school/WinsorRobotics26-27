# Limelight 3A API (FTC SDK 11.2.1)

For class responsibilities, result ownership, lifecycle details, field-map construction, and calibration, see [Java objects and runtime behavior](java-objects.md).

All camera classes below use `com.qualcomm.hardware.limelightvision`. Signatures and implementation notes were checked against the [published 11.2.1 source archive](https://repo.maven.apache.org/maven2/org/firstinspires/ftc/Hardware/11.2.1/Hardware-11.2.1-sources.jar). See also the [Limelight3A Javadoc](https://javadoc.io/static/org.firstinspires.ftc/Hardware/11.2.1/com/qualcomm/hardware/limelightvision/Limelight3A.html).

## Driver methods

| Signature | Behavior |
| --- | --- |
| `void setPollRateHz(int rateHz)` | Call before `start()`; clamps to 1–250 Hz; ignored while running. Default 100 Hz. Polling frequency is not camera frame rate. |
| `void start()` | Start/resume background result polling. |
| `void pause()` | Suspend polling. |
| `void stop()` | Stop polling and shut down its executor. |
| `boolean isRunning()` | Polling state, not evidence of target detection. |
| `boolean isConnected()` | Last successful update was at most 250 ms ago. |
| `long getTimeSinceLastUpdate()` | Milliseconds since last successful update. |
| `LLResult getLatestResult()` | Cached parsed result; does not request a fresh frame. |
| `LLStatus getStatus()` | Fetch diagnostics; returns a default object if request fails. |
| `boolean pipelineSwitch(int index)` | Request pipeline 0–9; inspect subsequent result index before using it. |
| `boolean reloadPipeline()` | Reload the active pipeline. |
| `boolean updateRobotOrientation(double yaw)` | Send field-aligned robot yaw in degrees for MegaTag2. |
| `boolean updatePythonInputs(double[] inputs)` | Accepts 1–32 numbers; rejects null/empty/oversize arrays. |
| `boolean updatePythonInputs(double input1, double input2, double input3, double input4, double input5, double input6, double input7, double input8)` | Eight-value convenience overload. |
| `boolean captureSnapshot(String snapname)` | Save a camera image. |
| `boolean deleteSnapshot(String snapname)` | Delete one snapshot. |
| `boolean deleteSnapshots()` | Delete all snapshots. |
| `boolean uploadPipeline(String jsonString, Integer index)` | Upload pipeline JSON. |
| `boolean uploadFieldmap(LLFieldMap fieldmap, Integer index)` | Upload a field map; rejects a map failing `isValid()`. |
| `boolean uploadPython(String pythonString, Integer index)` | Upload script text. |
| `LLResultTypes.CalibrationResult getCalDefault()` | Default calibration. |
| `LLResultTypes.CalibrationResult getCalFile()` | Calibration file. |
| `LLResultTypes.CalibrationResult getCalEEPROM()` | EEPROM calibration. |
| `LLResultTypes.CalibrationResult getCalLatest()` | Latest calibration. |

Upload methods accept `null` for the default slot. Boolean command returns report request success; they do not prove that a subsequent image has been processed with the new settings. Commands perform HTTP requests and can block; avoid uploads or snapshot management in a timing-sensitive driving loop.

## Read a specific AprilTag

Original example: copy into a TeamCode Java file named `LimelightReadTag.java`, configure an AprilTag pipeline in slot 0, and replace the illustrative tag ID. The example only reports telemetry.

```java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Limelight Read Tag")
public class LimelightReadTag extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        final int pipeline = 0;
        final int targetId = 1; // Replace with the desired field tag.
        final long maxAgeMs = 100; // Example policy; tune for your application.
        Limelight3A camera = hardwareMap.get(Limelight3A.class, "limelight");
        camera.setPollRateHz(100);
        if (!camera.pipelineSwitch(pipeline)) {
            telemetry.addLine("Pipeline switch failed");
            telemetry.update();
            return;
        }
        camera.start();
        try {
            waitForStart();
            while (opModeIsActive()) {
                LLResult result = camera.getLatestResult();
                boolean found = false;
                if (camera.isConnected() && result != null && result.isValid()
                        && result.getStaleness() <= maxAgeMs
                        && result.getPipelineIndex() == pipeline) {
                    for (LLResultTypes.FiducialResult tag : result.getFiducialResults()) {
                        if (tag.getFiducialId() == targetId) {
                            telemetry.addData("tx degrees", tag.getTargetXDegrees());
                            telemetry.addData("ty degrees", tag.getTargetYDegrees());
                            found = true;
                            break;
                        }
                    }
                }
                telemetry.addData("Requested tag visible", found);
                telemetry.update();
                idle();
            }
        } finally {
            camera.stop();
        }
    }
}
```

In 11.2.1, `getLatestResult()` synthesizes an empty invalid result when its cache is null. Older samples describe null before startup. Keep the null guard, but never use non-null alone to establish validity. A failed poll can leave an old valid result cached. `getStaleness()` measures time since SDK parsing, not image exposure; repeated responses containing the same camera frame can look newly received. Compare `getTimestamp()` across iterations when a control algorithm must process each camera frame only once.

MegaTag2 additionally requires ongoing `updateRobotOrientation(yawDegrees)` calls using an IMU heading aligned to the loaded field map, followed by `result.getBotpose_MT2()`. Configure camera mounting pose and the correct field map before interpreting either MegaTag pose. See the [FTC programming guide](https://docs.limelightvision.io/docs/docs-limelight/apis/ftc-programming).

## HTTP interface

The camera serves HTTP at `http://<camera-ip>:5807`. Use the camera's IP, not the Control Hub interface address displayed as a serial number. These routes are documented in the [official HTTP reference](https://docs.limelightvision.io/docs/docs-limelight/apis/rest-http-api) and used by the SDK source.

| Method and route | Request / response |
| --- | --- |
| `GET /results` | Current targeting JSON; see [data shapes](data-shapes.md). |
| `GET /status` | Diagnostic JSON. |
| `GET /hwreport` | Hardware/calibration reports. |
| `POST /pipeline-switch?index=0` | No body; select pipeline. |
| `POST /update-pythoninputs` | JSON numeric array, such as `[1, 0, 0, 0, 0, 0, 0, 0]`. |
| `POST /update-robotorientation` | SDK sends `[yaw, 0, 0, 0, 0, 0]`. |

From a computer that can reach the camera, a read-only diagnostic request is:

```sh
curl --max-time 2 http://limelight.local:5807/results
curl --max-time 2 http://limelight.local:5807/status
```
