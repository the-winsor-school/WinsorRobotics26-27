# Limelight 3A data shapes

For Java object semantics and usage examples, start with [Java objects and runtime behavior](java-objects.md). This page is the compact getter/type and wire-format companion.

This is an FTC SDK **11.2.1** consumer reference. Mappings and fallback behavior come from `LLResult.java`, `LLResultTypes.java`, `LLStatus.java`, and `LLFieldMap.java` in the [published SDK sources](https://repo.maven.apache.org/maven2/org/firstinspires/ftc/Hardware/11.2.1/Hardware-11.2.1-sources.jar). Raw firmware fields can exceed what this SDK exposes.

## LLResult: one pipeline result

| Getter | Java type | JSON key / meaning |
| --- | --- | --- |
| `isValid()` | `boolean` | `v == 1`; target validity |
| `getPipelineIndex()` | `int` | `pID` |
| `getPipelineType()` | `String` | SDK reads `pipelineType`; see discrepancy below |
| `getTx()`, `getTy()` | `double` | `tx`, `ty`; crosshair-relative degrees |
| `getTxNC()`, `getTyNC()` | `double` | `txnc`, `tync`; principal-pixel-relative degrees |
| `getTa()` | `double` | `ta`; top-level target area, percent of image |
| `getTimestamp()` | `double` | `ts`; camera-local milliseconds |
| `getCaptureLatency()` | `double` | `cl`; milliseconds |
| `getTargetingLatency()` | `double` | `tl`; milliseconds |
| `getParseLatency()` | `double` | SDK parsing duration, milliseconds |
| `getControlHubTimeStamp()` | `long` | SDK parse-time clock, milliseconds |
| `getControlHubTimeStampNanos()` | `long` | Same millisecond clock converted to nanoseconds; no added precision |
| `getStaleness()` | `long` | Current Hub clock minus parse-time clock, milliseconds |
| `getFocusMetric()` | `double` | `focus_metric`; focus pipeline metric |
| `getBotpose()` | `Pose3D` | `botpose`; MegaTag1 field pose |
| `getBotpose_MT2()` | `Pose3D` | `botpose_orb`; MegaTag2 field pose |
| `getStddevMt1()`, `getStddevMt2()` | `double[]` | `stdev_mt1`, `stdev_mt2`; six pose deviations |
| `getBotposeTagCount()` | `int` | `botpose_tagcount` |
| `getBotposeSpan()` | `double` | `botpose_span`; meters |
| `getBotposeAvgDist()` | `double` | `botpose_avgdist`; meters |
| `getBotposeAvgArea()` | `double` | `botpose_avgarea` |
| `getPythonOutput()` | `double[]` | `PythonOut`; script-defined values |
| `getFiducialResults()` | `List<LLResultTypes.FiducialResult>` | `Fiducial` |
| `getColorResults()` | `List<LLResultTypes.ColorResult>` | `Retro` |
| `getDetectorResults()` | `List<LLResultTypes.DetectorResult>` | `Detector` |
| `getClassifierResults()` | `List<LLResultTypes.ClassifierResult>` | `Classifier` |
| `getBarcodeResults()` | `List<LLResultTypes.BarcodeResult>` | `Barcode` |
| `toString()` | `String` | Underlying JSON serialization |

Missing lists become empty lists. Many missing numeric keys become zero; missing text becomes empty text. Missing standard-deviation arrays become six zeros; a supplied array keeps its supplied length. In 11.2.1, `getPythonOutput()` always returns **32 elements**, zero-padding shorter firmware arrays and truncating longer ones. Define which slots your script populates; zero padding is not evidence of a script measurement. Zero values do not establish that a measurement exists.

## Per-target objects

Barcode, color, detector, and fiducial objects each expose:

| Getter | Type | JSON key |
| --- | --- | --- |
| `getTargetXDegrees()`, `getTargetYDegrees()` | `double` | `tx`, `ty` |
| `getTargetXDegreesNoCrosshair()`, `getTargetYDegreesNoCrosshair()` | `double` | `tx_nocross`, `ty_nocross` |
| `getTargetXPixels()`, `getTargetYPixels()` | `double` | `txp`, `typ` |
| `getTargetArea()` | `double` | `ta`, returned without rescaling |
| `getTargetCorners()` | `List<List<Double>>` | `pts`; SDK expects nested `[x, y]` pairs |

| Object | Additional getters |
| --- | --- |
| `FiducialResult` | `int getFiducialId()` (`fID`), `String getFamily()` (`fam`), `double getSkew()` (`skew`), five poses below |
| `ColorResult` | Five poses below; only meaningful with the appropriate 3D pipeline configuration |
| `DetectorResult` | `String getClassName()` (`class`), `int getClassId()` (`classID`), `double getConfidence()` (`conf`) |
| `ClassifierResult` | Same class/confidence getters; no target location getters |
| `BarcodeResult` | `String getFamily()` (`fam`), `String getData()` (`data`) |

Treat confidence as the returned score; do not append a percent sign without an explicit scale conversion. The top-level target need not be the AprilTag ID you want: search `getFiducialResults()` by ID.

## Pose shape and reference frames

| Getter on fiducial/color result | JSON key | Object positioned / reference frame |
| --- | --- | --- |
| `getCameraPoseTargetSpace()` | `t6c_ts` | Camera / target |
| `getRobotPoseFieldSpace()` | `t6r_fs` | Robot / field, from this target |
| `getRobotPoseTargetSpace()` | `t6r_ts` | Robot / target |
| `getTargetPoseCameraSpace()` | `t6t_cs` | Target / camera |
| `getTargetPoseRobotSpace()` | `t6t_rs` | Target / robot |

All return `org.firstinspires.ftc.robotcore.external.navigation.Pose3D`. The SDK consumes six-number arrays as `[x, y, z, roll, pitch, yaw]`, with translation in meters and angles in degrees. It constructs `YawPitchRollAngles` with indices **5, 4, 3**, in that order. Standard-deviation arrays follow the same component order. Field, robot, camera, and target frames are distinct; confirm the loaded field map and camera mounting transform before combining coordinates.

```java
Pose3D pose = result.getBotpose();
double xMeters = pose.getPosition().x;
double yawDegrees = pose.getOrientation().getYaw(AngleUnit.DEGREES);
```

Import `Pose3D` and `AngleUnit` from `org.firstinspires.ftc.robotcore.external.navigation`. `Pose3D` does not expose `getX()` or `getY()` directly. SDK-created pose acquisition times are zero. Missing/short pose arrays can produce zero/default poses, so non-null pose is not proof of localization. Check target validity, tag count, age, and application-specific quality limits first.

## Minimal raw result example

Illustrative payload, not a recorded frame or complete firmware schema. Keys are case-sensitive. Optional firmware metadata is omitted; a tag ID does not imply a valid field pose.

```json
{
  "v": 1,
  "pID": 0,
  "pTYPE": "pipe_fiducial",
  "ts": 1000.0,
  "cl": 8.0,
  "tl": 12.0,
  "tx": -3.0,
  "ty": 1.0,
  "Fiducial": [
    {
      "fID": 1,
      "fam": "36H11C",
      "tx": -3.0,
      "ty": 1.0,
      "pts": [[100, 100], [120, 100], [120, 120], [100, 120]]
    }
  ],
  "Retro": [],
  "Detector": [],
  "Classifier": [],
  "Barcode": [],
  "PythonOut": []
}
```

## LLStatus and raw status

The [status specification](https://docs.limelightvision.io/docs/docs-limelight/apis/json-status-specification) describes `GET /status`. The SDK exposes these mappings:

| Java getters | Types | Wire keys / units |
| --- | --- | --- |
| `getName()`, `getPipelineType()` | `String` | `name`, `pipelineType` |
| `getPipelineIndex()`, `getCid()`, `getHwType()` | `int` | `pipelineIndex`, `cid`, `hwType` |
| `getPipeImgCount()`, `getSnapshotMode()` | `int` | `pipeImgCount`, `snapshotMode` |
| `getTemp()` | `double` | `temp`, Celsius |
| `getCpu()`, `getRam()` | `double` | `cpu`, `ram`, percent |
| `getFps()` | `double` | `fps`, processed frames/second |
| `getFinalYaw()` | `double` | `finalYaw`, degrees |
| `getCameraQuat()` | `Quaternion` | `cameraQuat`: object with `w`, `x`, `y`, `z` |

Failed status requests return a default `LLStatus`, so zero diagnostics are not a connection test. Shared firmware IMU fields do not establish that a 3A has an onboard IMU.

## Other SDK shapes

`LLFieldMap` exposes `getFiducials(): List<LLFieldMap.Fiducial>`, `getType(): String`, `getNumberOfTags(): int`, and `isValid(): boolean`. Construct it with `LLFieldMap(List<Fiducial>, String type)`. Each fiducial exposes `getId(): int`, `getSize(): double`, `getFamily(): String`, `getTransform(): List<Double>`, and `isUnique(): boolean`. The constructor is `Fiducial(int id, double size, String family, List<Double> transform, boolean isUnique)`. Use a verified camera field-map export for size and transform conventions; this SDK's basic validity check does not establish geometric correctness.

`LLResultTypes.CalibrationResult` exposes `isValid(): boolean`, `getDisplayName(): String`, `getResX()/getResY(): double`, `getReprojectionError(): double`, and `getCamMatVector()/getDistortionCoefficients(): double[]`. Validate before using the calibration.

## Vendor documentation discrepancies

The shared [JSON specification](https://docs.limelightvision.io/docs/docs-limelight/apis/json-results-specification) differs from the 11.2.1 consumer in several places:

- Firmware documents `pTYPE`; `LLResult.getPipelineType()` reads `pipelineType`, so it can return empty text. `LLStatus.getPipelineType()` reads the documented status key correctly.
- Some per-tag pose descriptions list pitch/yaw/roll; the SDK interprets indices 3–5 as roll/pitch/yaw.
- Corner descriptions include flat arrays; the SDK parses nested pairs.
- Top-level area is documented on a 0–100 scale, while per-target area descriptions say 0–1. The SDK passes both through unchanged. Verify actual camera output before applying area thresholds.

Newer shared fields such as accelerator or rewind diagnostics are not guarantees of 3A support. These notes prioritize verified SDK behavior and leave firmware-dependent ambiguities explicit.
