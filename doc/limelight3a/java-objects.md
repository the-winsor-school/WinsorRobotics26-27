# Limelight 3A Java objects and behavior

This guide describes the **FTC Hardware 11.2.1 Java library**, matching this repository. Verified on 2026-09-12 against the [published SDK source archive](https://repo.maven.apache.org/maven2/org/firstinspires/ftc/Hardware/11.2.1/Hardware-11.2.1-sources.jar). Implementation details below are version-specific. Camera firmware and hardware were not exercised.

All Limelight classes use `com.qualcomm.hardware.limelightvision`. The [Java API index](https://javadoc.io/static/org.firstinspires.ftc/Hardware/11.2.1/com/qualcomm/hardware/limelightvision/package-summary.html) provides upstream navigation. See [driver signatures and complete OpMode example](api.md) and [getter/type tables](data-shapes.md) for companion references.

## Object relationships

Here, **property** means a value exposed by a Java getter unless a public field is explicitly named. Getter calls do not generally change the camera; device commands do. Tables cover the public Limelight API, not private parser fields or inherited `Object` methods. Units and frames belong to the property, not just its numeric Java type.

| Object | Role | How your code obtains it |
| --- | --- | --- |
| `Limelight3A` | Hardware device, polling, camera commands | `hardwareMap.get(Limelight3A.class, "limelight")` |
| `LLResult` | One parsed pipeline response | `camera.getLatestResult()` |
| `LLResultTypes.FiducialResult` | One detected AprilTag | `result.getFiducialResults()` |
| `LLResultTypes.ColorResult` | One color/retroreflective target | `result.getColorResults()` |
| `LLResultTypes.DetectorResult` | One neural-network object detection | `result.getDetectorResults()` |
| `LLResultTypes.ClassifierResult` | One neural-network classification | `result.getClassifierResults()` |
| `LLResultTypes.BarcodeResult` | One decoded barcode | `result.getBarcodeResults()` |
| `LLStatus` | Device diagnostics | `camera.getStatus()` |
| `LLFieldMap` | Known field-tag configuration | Construct from a list of map fiducials |
| `LLFieldMap.Fiducial` | One known tag's map definition | Construct from verified field-map values |
| `LLResultTypes.CalibrationResult` | Camera calibration report | `camera.getCalDefault()`, `getCalFile()`, `getCalEEPROM()`, or `getCalLatest()` |

`LLResultTypes` groups static nested classes; the detection classes do **not** share a public target base class or interface. A `FiducialResult` is an observation. An `LLFieldMap.Fiducial` is configuration describing where a tag belongs. They are not interchangeable.

## Limelight3A: lifecycle and I/O

| Property / method | Java type | Meaning | Typical use / limits |
| --- | --- | --- | --- |
| `getLatestResult()` | `LLResult` | Most recently parsed pipeline response. | Read once per control iteration; validate before acting. |
| `getStatus()` | `LLStatus` | Fresh diagnostic request. | Telemetry and troubleshooting; blocks for network I/O. |
| `isRunning()` | `boolean` | Whether polling is enabled. | Check lifecycle state, not camera health. |
| `isConnected()` | `boolean` | Whether a successful result response arrived within 250 ms. | Detect communication loss; valid targets are a separate check. |
| `getTimeSinceLastUpdate()` | `long`, ms | Hub time elapsed since last successful response. | Choose an application-specific disconnect threshold; before any update this is not a meaningful measured interval. |
| `getDeviceName()` | `String` | Hardware configuration name. | Identify the device in logs; may differ from the camera hostname in `LLStatus`. |
| `getManufacturer()` | `HardwareDevice.Manufacturer` | Returns `LimelightVision`. | Generic hardware inventories. |
| `getConnectionInfo()` | `String` | SDK connection/serial description. | Diagnose robot configuration; not a vision measurement. |
| `getVersion()` | `int` | Always 0 in this driver. | Do not use as the camera firmware version. |
| `getCalDefault()` | `CalibrationResult` | Default calibration report. | Inspect baseline camera calibration; synchronous request. |
| `getCalFile()` | `CalibrationResult` | User calibration file report. | Inspect saved calibration; check report validity. |
| `getCalEEPROM()` | `CalibrationResult` | EEPROM calibration report. | Inspect device calibration; check report validity. |
| `getCalLatest()` | `CalibrationResult` | Most recently generated calibration report. | Evaluate calibration work; not necessarily the active calibration. |


Initialize once per OpMode, select the pipeline, set the poll rate, and call `start()`. Keep one hardware device reference. The public constructor takes SDK connection objects; normal team code should use `hardwareMap` so robot configuration supplies those values.

| Operation | Behavior in 11.2.1 |
| --- | --- |
| `start()` | Schedules background polling; repeated calls while starting/running do nothing. Can recreate an executor after `stop()`. |
| `pause()` | Disables polling work but leaves the scheduled executor in place. `start()` resumes it. |
| `stop()` | Disables polling and shuts down the executor. Does not clear the cached result. |
| `shutdown()`, `close()` | Delegate to `stop()`. |
| `setPollRateHz(int)` | Clamps to 1–250; default 100. Uses integer millisecond intervals. Changes while running are ignored. |
| `isRunning()` | Reports polling state, not a healthy camera connection. |
| `isConnected()` | Tests whether the last successful result response arrived within 250 ms. Does not test target visibility. |

To change an already scheduled rate, use `stop()`, `setPollRateHz(...)`, then `start()`. Merely pausing does not rebuild the existing schedule. Polling at 100 Hz does not make the camera process 100 distinct images per second.

`getLatestResult()` returns a cached object and does not perform network I/O. `getStatus()`, calibration getters, and camera commands perform synchronous HTTP requests on the calling thread. In this version, the connection timeout is 100 ms, GET read timeout is 100 ms, and POST/DELETE read timeout is 15 seconds. These are individual timeouts, not an overall execution-time guarantee. Avoid configuration uploads in the driving loop; throttle diagnostic reads.

Boolean commands return true for HTTP 200 and false on request failure. That does not guarantee that the next cached result uses the requested pipeline. After `pipelineSwitch(index)`, accept results only when `getPipelineIndex()` matches. The Java wrapper does not range-check the pipeline index; use configured slots 0–9.

HardwareDevice metadata is separate from vision data: `getManufacturer()` returns `LimelightVision`; `getDeviceName()` returns the configured device name; `getConnectionInfo()` returns the serial-number representation. `getVersion()` returns **0**, not LimelightOS version. `resetDeviceConfigurationForOpMode()` does nothing in this implementation.

## LLResult: a received response, not a live view

| Property / method | Java type | Meaning | Typical use / limits |
| --- | --- | --- | --- |
| `isValid()` | `boolean` | Pipeline reports valid targeting. | First targeting gate; not proof that a particular tag or pose exists. |
| `getPipelineIndex()` | `int` | Pipeline slot that produced the response. | Reject old-pipeline data after a switch. |
| `getPipelineType()` | `String` | Pipeline kind text. | Diagnostic labeling; 11.2.1 reads a different key from documented firmware `pTYPE`, so it can be empty. |
| `getTx()` | `double`, degrees | Selected target horizontal offset from the configured crosshair. | Horizontal aim/turn error; verify motor direction on the robot. |
| `getTy()` | `double`, degrees | Selected target vertical offset from the configured crosshair. | Vertical alignment or calibrated geometric ranging. |
| `getTxNC()` | `double`, degrees | Horizontal offset from the optical principal pixel. | Geometry independent of the user crosshair offset. |
| `getTyNC()` | `double`, degrees | Vertical offset from the optical principal pixel. | Optical geometry; use the matching vertical reference in calculations. |
| `getTa()` | `double`, percent | Selected target image area. | Filter tiny detections or compare apparent size; not a distance in meters. |
| `getBotpose()` | `Pose3D` | MegaTag1 robot pose in field space. | Field localization with the configured map and camera mounting transform. |
| `getBotpose_MT2()` | `Pose3D` | MegaTag2 robot pose in field space. | Localization using externally supplied field-aligned yaw. |
| `getBotposeTagCount()` | `int` | Number of tags used by the pose solution. | Reject a missing solution; more tags are supporting evidence, not a guarantee. |
| `getBotposeSpan()` | `double`, meters | Maximum separation of tags used in the solution. | Assess spatial spread of the observations; not robot travel distance. |
| `getBotposeAvgDist()` | `double`, meters | Average camera-to-tag distance for tags used. | Assess observation range; not distance to a selected goal. |
| `getBotposeAvgArea()` | `double` | Average image area of tags used. | Assess apparent tag size; verify firmware scale before thresholds. |
| `getStddevMt1()` | `double[]` | MT1 deviations in x, y, z, roll, pitch, yaw order. | Quality weighting; translations use meters, rotations degrees. Missing arrays become zeros, not perfect certainty. |
| `getStddevMt2()` | `double[]` | MT2 deviations in the same component order. | Quality weighting for MT2; validate availability and length. |
| `getCaptureLatency()` | `double`, ms | Capture-stage delay reported by the camera. | Latency accounting; not total observation age. |
| `getTargetingLatency()` | `double`, ms | Camera pipeline-processing duration. | Compare pipeline cost and latency. |
| `getParseLatency()` | `double`, ms | Hub time spent parsing target lists. | Diagnose Java-side processing overhead. |
| `getTimestamp()` | `double`, ms | Camera-local result timestamp. | Identify repeated camera frames; separate clock from Hub time. |
| `getControlHubTimeStamp()` | `long`, ms | Hub clock recorded at result construction. | Track local receipt/parse age. |
| `getControlHubTimeStampNanos()` | `long`, ns | Same stored timestamp multiplied into nanoseconds. | Unit compatibility; no extra precision or camera synchronization. |
| `getStaleness()` | `long`, ms | Hub clock minus stored construction timestamp. | Reject an old cached response, subject to Hub clock changes. |
| `getFocusMetric()` | `double` | Focus-pipeline quality metric. | Compare focus while using that pipeline; no universal valid threshold. |
| `getPythonOutput()` | `double[32]` | Script-defined numeric outputs. | Read agreed slots; padded zeros do not prove a script produced a measurement. |
| `getFiducialResults()` | `List<FiducialResult>` | Individual observed tags. | Find an ID and inspect its angles or relative pose. |
| `getColorResults()` | `List<ColorResult>` | Individual color/retroreflective targets. | Choose the blob appropriate to the task. |
| `getDetectorResults()` | `List<DetectorResult>` | Individual neural detections. | Filter by category, score, and image location. |
| `getClassifierResults()` | `List<ClassifierResult>` | Image classification outputs. | Decide which category an image matches; no target position. |
| `getBarcodeResults()` | `List<BarcodeResult>` | Decoded barcode observations. | Read text and associated image location. |
| `toString()` | `String` | Underlying response serialized as JSON. | Logging/debugging; getters are the normal Java interface. |


Capture one result reference at the beginning of a control iteration. Background polling replaces the driver's cached reference; an older result object does not update itself. Repeated calls may return the same object, or different responses if polling completes between calls.

The constructor and parser are not public team APIs. Obtain results through the driver. Before the first response, 11.2.1 creates an empty invalid result if its cache is null. Older examples describe null, and failed parsing can also return null internally, so retaining a null guard is sensible.

Use separate checks for separate questions:

| Check | What it establishes | What it does not establish |
| --- | --- | --- |
| `result != null` | An object is available | Valid target or fresh data |
| `result.isValid()` | Response reports valid targeting | Your desired tag exists, or a usable field pose exists |
| `camera.isConnected()` | Recent successful polling | A new camera frame or target |
| `result.getStaleness()` | Milliseconds since the Hub parsed this result | Time since camera exposure |
| `result.getTimestamp()` | Camera-local result timestamp | A timestamp in the Hub's clock domain |
| `result.getPipelineIndex()` | Pipeline that produced this result | Successful selection of an arbitrary tag |

Failed polling can leave an old valid result cached. Repeated polling of the same frame can produce newly parsed objects with low staleness. For once-per-frame processing, compare camera timestamps across iterations and reset your comparison state after a camera restart. Do not subtract a camera timestamp from a Hub timestamp.

`getCaptureLatency()`, `getTargetingLatency()`, and `getParseLatency()` describe different stages, in milliseconds. Their sum does not include every transport/scheduling delay. `getControlHubTimeStampNanos()` converts the stored millisecond clock to nanoseconds; it adds no timing precision.

`getTx()/getTy()` describe the pipeline's selected target. They do not accept a tag ID. `getFocusMetric()` applies to the focus pipeline. Missing scalar values often default to zero; a zero angle alone does not prove a centered target.

### Example: select a tag from one response

Add these imports and method to your own helper class. Check connection, age, and pipeline at the caller before passing the response if it will drive motion.

```java
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;

public static LLResultTypes.FiducialResult findTag(LLResult result, int tagId) {
    if (result == null || !result.isValid()) {
        return null;
    }
    for (LLResultTypes.FiducialResult tag : result.getFiducialResults()) {
        if (tag.getFiducialId() == tagId) {
            return tag;
        }
    }
    return null;
}
```

An empty list or null helper return means this response did not supply the requested observation. Choose the robot's no-target behavior explicitly instead of reusing an old steering command indefinitely.

## LLResultTypes: nested type container

`LLResultTypes` has no public vision properties or camera-control behaviors of its own. It names the six nested types documented below: five observation types and `CalibrationResult`. Use imports such as `LLResultTypes.FiducialResult`; creating the container does not create a detector or configure a pipeline.

## Detection objects

The five detection classes have protected JSON constructors and public getters. Normal team code reads them from `LLResult`; it does not instantiate them. Their getters do not send commands or update the camera.

### FiducialResult

| Property / method | Java type | Meaning | Typical use / limits |
| --- | --- | --- | --- |
| `getFiducialId()` | `int` | Decoded AprilTag ID. | Select a known field tag; absent IDs default to zero during parsing. |
| `getFamily()` | `String` | Detected tag coding family. | Diagnose family mismatch; it does not specify physical tag size. |
| `getSkew()` | `double` | Exposed legacy/unused skew value. | Diagnostic only; use a valid pose orientation for 3D alignment. |
| `getTargetXDegrees()` | `double`, degrees | This target horizontal offset from the crosshair. | Aim toward the selected observation; validate steering sign. |
| `getTargetYDegrees()` | `double`, degrees | This target vertical offset from the crosshair. | Vertical alignment or geometry with known camera/target heights. |
| `getTargetXDegreesNoCrosshair()` | `double`, degrees | Horizontal offset from the principal pixel. | Optical calculations that should not depend on crosshair tuning. |
| `getTargetYDegreesNoCrosshair()` | `double`, degrees | Vertical offset from the principal pixel. | Use with the matching optical reference in vertical calculations. |
| `getTargetXPixels()` | `double`, pixels | Reported horizontal image location. | Image overlays and image-space comparisons; confirm pixel origin with firmware. |
| `getTargetYPixels()` | `double`, pixels | Reported vertical image location. | Image overlays; not an angle or field coordinate. |
| `getTargetArea()` | `double` | Apparent image area, passed through unchanged. | Reject tiny observations; vendor per-target scale descriptions differ from top-level area. |
| `getTargetCorners()` | `List<List<Double>>` | Corner coordinates as nested x/y pairs. | Inspect shape or draw an outline. Enable corner output; may be empty. The returned list is mutable. |
| `getCameraPoseTargetSpace()` | `Pose3D` | Camera position/orientation expressed in the target frame. | Inspect camera-to-target geometry and mounting assumptions. |
| `getRobotPoseTargetSpace()` | `Pose3D` | Robot position/orientation expressed in the target frame. | Approach or align relative to a target, using correct mounting configuration. |
| `getRobotPoseFieldSpace()` | `Pose3D` | Robot field pose derived from this target. | Compare single-target localization against the aggregate solution. |
| `getTargetPoseCameraSpace()` | `Pose3D` | Target position/orientation in the camera frame. | Compute camera-relative target distance when the pose is available. |
| `getTargetPoseRobotSpace()` | `Pose3D` | Target position/orientation in the robot frame. | Choose robot-relative motion toward the target. |

**Behavior:** Read from a valid parent result, then select by ID. There is no per-tag validity flag or automatic refresh. Pose getters can return default values when no pose was supplied.

Use `getFiducialId(): int` and `getFamily(): String` to identify the tag. `getTargetXDegrees()/getTargetYDegrees(): double` provide aiming errors. The corresponding `NoCrosshair` methods use the principal pixel; `getTargetXPixels()/getTargetYPixels()` return pixel coordinates. `getTargetArea(): double` and `getTargetCorners(): List<List<Double>>` provide image geometry. `getSkew(): double` exposes a field but should not be treated as a replacement for 3D orientation.

Five getters return `Pose3D`:

| Getter | Meaning |
| --- | --- |
| `getCameraPoseTargetSpace()` | Camera pose relative to this tag |
| `getRobotPoseTargetSpace()` | Robot pose relative to this tag |
| `getRobotPoseFieldSpace()` | Robot field pose estimated using this tag |
| `getTargetPoseCameraSpace()` | Tag pose relative to the camera |
| `getTargetPoseRobotSpace()` | Tag pose relative to the robot |

The per-tag field pose is distinct from `LLResult.getBotpose()`, which represents the pipeline's MegaTag solution. The nested object has no separate `isValid()` method. A recognized ID does not establish that every pose field is available.

### ColorResult

| Property / method | Java type | Meaning | Typical use / limits |
| --- | --- | --- | --- |
| `getTargetXDegrees()` | `double`, degrees | This target horizontal offset from the crosshair. | Aim toward the selected observation; validate steering sign. |
| `getTargetYDegrees()` | `double`, degrees | This target vertical offset from the crosshair. | Vertical alignment or geometry with known camera/target heights. |
| `getTargetXDegreesNoCrosshair()` | `double`, degrees | Horizontal offset from the principal pixel. | Optical calculations that should not depend on crosshair tuning. |
| `getTargetYDegreesNoCrosshair()` | `double`, degrees | Vertical offset from the principal pixel. | Use with the matching optical reference in vertical calculations. |
| `getTargetXPixels()` | `double`, pixels | Reported horizontal image location. | Image overlays and image-space comparisons; confirm pixel origin with firmware. |
| `getTargetYPixels()` | `double`, pixels | Reported vertical image location. | Image overlays; not an angle or field coordinate. |
| `getTargetArea()` | `double` | Apparent image area, passed through unchanged. | Reject tiny observations; vendor per-target scale descriptions differ from top-level area. |
| `getTargetCorners()` | `List<List<Double>>` | Corner coordinates as nested x/y pairs. | Inspect shape or draw an outline. Enable corner output; may be empty. The returned list is mutable. |
| `getCameraPoseTargetSpace()` | `Pose3D` | Camera position/orientation expressed in the target frame. | Inspect camera-to-target geometry and mounting assumptions. |
| `getRobotPoseTargetSpace()` | `Pose3D` | Robot position/orientation expressed in the target frame. | Approach or align relative to a target, using correct mounting configuration. |
| `getRobotPoseFieldSpace()` | `Pose3D` | Robot field pose derived from this target. | Compare single-target localization against the aggregate solution. |
| `getTargetPoseCameraSpace()` | `Pose3D` | Target position/orientation in the camera frame. | Compute camera-relative target distance when the pose is available. |
| `getTargetPoseRobotSpace()` | `Pose3D` | Target position/orientation in the robot frame. | Choose robot-relative motion toward the target. |

**Behavior:** Select the desired color target from the list. A pipeline must actually compute 3D geometry for pose getters to be useful; no ID distinguishes persistent blobs.

Exposes the same angle, pixel, area, corner, and five pose getters as the fiducial class, but no tag ID, family, or skew getters. Use angles for color-target alignment. Do not assume every color pipeline supplies 3D pose just because its result class has pose methods.

### DetectorResult

| Property / method | Java type | Meaning | Typical use / limits |
| --- | --- | --- | --- |
| `getClassName()` | `String` | Human-readable model category. | Log or select a desired object category; depends on the deployed model. |
| `getClassId()` | `int` | Model category index. | Efficient category matching; not a tracked-object identity. |
| `getConfidence()` | `double` | Model prediction score, passed through unchanged. | Reject weak predictions using a model-tested threshold; do not label as percent without scale conversion. |
| `getTargetXDegrees()` | `double`, degrees | This target horizontal offset from the crosshair. | Aim toward the selected observation; validate steering sign. |
| `getTargetYDegrees()` | `double`, degrees | This target vertical offset from the crosshair. | Vertical alignment or geometry with known camera/target heights. |
| `getTargetXDegreesNoCrosshair()` | `double`, degrees | Horizontal offset from the principal pixel. | Optical calculations that should not depend on crosshair tuning. |
| `getTargetYDegreesNoCrosshair()` | `double`, degrees | Vertical offset from the principal pixel. | Use with the matching optical reference in vertical calculations. |
| `getTargetXPixels()` | `double`, pixels | Reported horizontal image location. | Image overlays and image-space comparisons; confirm pixel origin with firmware. |
| `getTargetYPixels()` | `double`, pixels | Reported vertical image location. | Image overlays; not an angle or field coordinate. |
| `getTargetArea()` | `double` | Apparent image area, passed through unchanged. | Reject tiny observations; vendor per-target scale descriptions differ from top-level area. |
| `getTargetCorners()` | `List<List<Double>>` | Corner coordinates as nested x/y pairs. | Inspect shape or draw an outline. Enable corner output; may be empty. The returned list is mutable. |

**Behavior:** Each object is one detection from the current response. Multiple detections may share the same class ID. The SDK does not track identity across frames.

Exposes the angle, pixel, area, and corner getters plus `getClassName(): String`, `getClassId(): int`, and `getConfidence(): double`. It has no pose getters. A class ID identifies a model category, not a persistent tracked object or field tag. Select by class and application-specific confidence/geometry criteria instead of assuming the first list entry is your target.

### ClassifierResult

| Property / method | Java type | Meaning | Typical use / limits |
| --- | --- | --- | --- |
| `getClassName()` | `String` | Human-readable model category. | Log or select a desired object category; depends on the deployed model. |
| `getClassId()` | `int` | Model category index. | Efficient category matching; not a tracked-object identity. |
| `getConfidence()` | `double` | Model prediction score, passed through unchanged. | Reject weak predictions using a model-tested threshold; do not label as percent without scale conversion. |

**Behavior:** This object provides category and score only. It has no image geometry or pose methods; do not use it as a located object.

Exposes only `getClassName(): String`, `getClassId(): int`, and `getConfidence(): double`. It classifies an image; it supplies no target coordinates, corners, or pose. Classification cannot directly supply a steering offset.

### BarcodeResult

| Property / method | Java type | Meaning | Typical use / limits |
| --- | --- | --- | --- |
| `getFamily()` | `String` | Barcode symbology/family. | Distinguish the kind of code decoded. |
| `getData()` | `String` | Decoded text payload. | Match an application-defined label or identifier; treat it as text, not a fiducial ID. |
| `getTargetXDegrees()` | `double`, degrees | This target horizontal offset from the crosshair. | Aim toward the selected observation; validate steering sign. |
| `getTargetYDegrees()` | `double`, degrees | This target vertical offset from the crosshair. | Vertical alignment or geometry with known camera/target heights. |
| `getTargetXDegreesNoCrosshair()` | `double`, degrees | Horizontal offset from the principal pixel. | Optical calculations that should not depend on crosshair tuning. |
| `getTargetYDegreesNoCrosshair()` | `double`, degrees | Vertical offset from the principal pixel. | Use with the matching optical reference in vertical calculations. |
| `getTargetXPixels()` | `double`, pixels | Reported horizontal image location. | Image overlays and image-space comparisons; confirm pixel origin with firmware. |
| `getTargetYPixels()` | `double`, pixels | Reported vertical image location. | Image overlays; not an angle or field coordinate. |
| `getTargetArea()` | `double` | Apparent image area, passed through unchanged. | Reject tiny observations; vendor per-target scale descriptions differ from top-level area. |
| `getTargetCorners()` | `List<List<Double>>` | Corner coordinates as nested x/y pairs. | Inspect shape or draw an outline. Enable corner output; may be empty. The returned list is mutable. |

**Behavior:** Read decoded text and geometry from the same observation. Missing strings become empty text; there is no pose or per-barcode validity method.

Exposes the angle, pixel, area, and corner getters plus `getFamily(): String` and `getData(): String`. `getData()` is decoded text, not an AprilTag ID. There are no 3D pose getters.

## Pose3D and Java navigation types

These supporting classes belong to **RobotCore**, rather than the Limelight package. The following properties and behaviors were checked against the [RobotCore 11.2.1 sources](https://repo.maven.apache.org/maven2/org/firstinspires/ftc/RobotCore/11.2.1/RobotCore-11.2.1-sources.jar). Coverage here focuses on interpreting values returned by Limelight.

### Pose3D properties and behavior

| Property / method | Java type | Meaning and use |
| --- | --- | --- |
| `getPosition()` | `Position` | Translation in the pose's reference frame. Use for field location or relative displacement, according to the originating getter. |
| `getOrientation()` | `YawPitchRollAngles` | Rotation in that frame. Use for heading or alignment; axis mapping comes from the producer, not `Pose3D` itself. |
| `toString()` | `String` | Combined position/orientation text for telemetry. |

`new Pose3D(Position, YawPitchRollAngles)` stores the supplied references without copying. The getters return those references. There is no frame-name property, pose-validity method, automatic unit conversion, or automatic transformation between frames. Keep the frame meaning with the value in your own code.

### Position properties and behavior

| Public field / method | Java type | Meaning and use |
| --- | --- | --- |
| `x`, `y`, `z` | `double` | Coordinates along the reference frame's axes. They are mutable public fields; don't modify a shared observation. |
| `unit` | `DistanceUnit` | Unit applying to all three coordinates. Check it before combining positions from different APIs. |
| `acquisitionTime` | `long`, ns | Acquisition timestamp, or zero when unavailable. Limelight-created poses use zero; this cannot replace result freshness checks. |
| `toUnit(DistanceUnit)` | `Position` | Converts all coordinates to a requested distance unit. Returns a new position if units differ, but returns the same object if they already match. |
| `toString()` | `String` | Coordinates and unit for logging. |

`new Position()` is zero in **millimeters**. The value constructor accepts `(DistanceUnit unit, double x, double y, double z, long acquisitionTime)`. Normal Limelight pose parsing explicitly supplies meters. Never change only the `unit` field to perform conversion: that relabels unchanged numbers.

### YawPitchRollAngles properties and behavior

| Property / method | Java type | Meaning and use |
| --- | --- | --- |
| `getYaw(AngleUnit)` | `double` | Heading-like rotation, returned in the requested angle unit. Use for planar heading alignment in the appropriate frame. |
| `getPitch(AngleUnit)` | `double` | Pitch component of orientation. Use to inspect target/camera tilt; interpret axes according to the producing API. |
| `getRoll(AngleUnit)` | `double` | Roll component of orientation. Use to inspect tilt about the remaining rotation axis. |
| `getYaw()`, `getPitch()`, `getRoll()` | `double` | Stored component values in the constructor's unit. Prefer explicit-unit overloads when combining APIs. |
| `getAcquisitionTime()` | `long`, ns | Acquisition timestamp; zero in Limelight-created poses. |
| `toString()` | `String` | Formats the three angles in degrees for telemetry. |

Construct with `(AngleUnit, double yaw, double pitch, double roll, long acquisitionTime)`. This object has no public setters. Its constructor stores values as supplied; explicit-unit getters perform unit conversion. Constructor argument order is **yaw, pitch, roll**, unlike the roll/pitch/yaw order in Limelight's raw six-component pose. The driver already handles that rearrangement.

### Quaternion properties and behavior

| Public field / method | Java type | Meaning and use |
| --- | --- | --- |
| `w` | `float` | Scalar component of a quaternion rotation. Not a heading angle. |
| `x`, `y`, `z` | `float` | Vector components of that rotation. Not position coordinates or Euler angles. |
| `acquisitionTime` | `long`, ns | Timestamp, or zero when unavailable. The Limelight status parser leaves it zero. |
| `magnitude()` | `float` | Quaternion norm. Useful when checking a rotation representation. |
| `normalized()` | `Quaternion` | Produces a normalized quaternion. Check for nonzero magnitude first. |
| `toOrientation(AxesReference, AxesOrder, AngleUnit)` | `Orientation` | Converts to Euler orientation with explicit axis conventions. Choose conventions appropriate to the camera's frame. |
| `toString()` | `String` | Component values for diagnostics. |

`LLStatus.getCameraQuat()` returns its stored mutable object. Defaults are identity (`w=1`, other components zero), which also occurs when no orientation was supplied; identity is not proof of an actual orientation measurement. For a distinct object, copy the components with `new Quaternion(w, x, y, z, acquisitionTime)`. This is a rotation representation, not a second position estimate.

### Reading a pose in Java

Limelight returns FTC navigation classes from `org.firstinspires.ftc.robotcore.external.navigation`:

```java
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

// After accepting a result with a usable field-pose solution:
Pose3D pose = result.getBotpose();
Position position = pose.getPosition();
YawPitchRollAngles orientation = pose.getOrientation();
double x = position.x;
double y = position.y;
double z = position.z;
double yawDegrees = orientation.getYaw(AngleUnit.DEGREES);
double pitchDegrees = orientation.getPitch(AngleUnit.DEGREES);
double rollDegrees = orientation.getRoll(AngleUnit.DEGREES);
```

Normal six-component Limelight poses use meters and degrees. Inspect `position.unit` if handling default/fallback values. These are not VisionPortal's `AprilTagDetection.ftcPose` fields, and do not provide a `range` property. Compute a Euclidean distance only after choosing a pose in the appropriate relative frame; field-position magnitude is distance from the field origin, not distance to a tag.

The SDK supplies zero acquisition times for these poses and substitutes default/zero poses for missing values. Non-null pose and all-zero deviations do not imply a measured pose. Use `getBotposeTagCount()`, `getBotposeSpan()`, `getBotposeAvgDist()`, and `getStddevMt1()/getStddevMt2()` as supporting information, together with pipeline configuration and freshness.

`getBotpose_MT2()` requires external field-aligned robot yaw supplied with `updateRobotOrientation(double)`. This method sends yaw and five zeros; it is not a full six-component orientation API. It does not initialize the robot's IMU or choose its zero heading. Camera mounting and field-map configuration remain necessary.

## Object ownership and mutation

The following behaviors are verified from the SDK implementation, rather than inferred from getter names:

| Getter or constructor | Copy behavior |
| --- | --- |
| `LLResult.get...Results()` | Returns the internal mutable list |
| Detection `getTargetCorners()` | Returns the internal nested mutable list |
| Detection pose getters | Return stored pose objects |
| `LLResult.getBotpose()` / `getBotpose_MT2()` | Construct a pose on each call |
| `LLResult.getStddevMt1()/getStddevMt2()/getPythonOutput()` | Return newly allocated arrays |
| `LLStatus.getCameraQuat()` | Returns its stored `Quaternion` |
| `LLFieldMap` list constructor / `getFiducials()` | Copy the outer list; not a deep copy of tags |
| Map fiducial constructor / `getTransform()` | Copy the transform list |
| Calibration array constructor / array getters | Clone the arrays |

Treat observations as read-only. Sorting or clearing a returned detection list modifies the cached result seen by other callers. For your own sorting, use `new ArrayList<>(result.getFiducialResults())`; nested objects are still shared. Copy corner pairs too if you need to mutate them.

## LLStatus: diagnostics, not target validity

| Property / method | Java type | Meaning | Typical use / limits |
| --- | --- | --- | --- |
| `getName()` | `String` | Camera-reported hostname. | Distinguish devices in telemetry; not necessarily the hardwareMap name. |
| `getPipelineIndex()` | `int` | Currently reported pipeline slot. | Confirm configuration; a separate status request is not atomic with an earlier result. |
| `getPipelineType()` | `String` | Pipeline-kind text. | Identify the current mode in diagnostics. |
| `getTemp()` | `double`, Celsius | CPU temperature. | Investigate thermal conditions and performance changes. |
| `getCpu()` | `double`, percent | CPU utilization. | Assess pipeline processing load. |
| `getRam()` | `double`, percent | Memory utilization. | Diagnose resource use. |
| `getFps()` | `double`, frames/s | Camera processing rate. | Measure pipeline throughput; distinct from SDK poll rate. |
| `getCid()` | `int` | Camera sensor identifier. | Identify sensor hardware during diagnostics; not a tag ID. |
| `getHwType()` | `int` | Device hardware-type code. | Hardware diagnostics; SDK offers no named enum mapping here. |
| `getPipeImgCount()` | `int` | Image count associated with the pipeline. | Inspect pipeline image resources; not a frame sequence number. |
| `getSnapshotMode()` | `int` | Raw snapshot-mode flag. | Inspect snapshot configuration; do not invent enum meanings absent from the API. |
| `getFinalYaw()` | `double`, degrees | Reported final yaw value. | Orientation diagnostics; shared firmware property is not proof of a built-in 3A IMU. |
| `getCameraQuat()` | `Quaternion` | Reported camera orientation as w/x/y/z. | Orientation diagnostics; defaults to identity if missing and returns the stored object. |
| `toString()` | `String` | Readable diagnostic summary. | Log status; output is not a JSON serialization. |


Read `getName(): String`, `getPipelineIndex(): int`, `getPipelineType(): String`, `getTemp(): double` (Celsius), `getCpu()/getRam(): double` (percent), and `getFps(): double` (processing rate) for telemetry. Additional getters are `getCid(): int`, `getHwType(): int`, `getPipeImgCount(): int`, `getSnapshotMode(): int`, `getFinalYaw(): double` (degrees), and `getCameraQuat(): Quaternion`.

There is a public default constructor but no public `isValid()`. A failed `getStatus()` returns an object with empty strings, numeric zeros, and identity quaternion. Default diagnostics must not be interpreted as real measurements. `getCameraQuat()` returns a **Quaternion**, despite an upstream comment describing a double array. Its component fields are `w`, `x`, `y`, `z`. `toString()` is diagnostic text, unlike the JSON string returned by `LLResult.toString()`.

## LLFieldMap and LLFieldMap.Fiducial: configuration objects

### LLFieldMap properties

| Property / method | Java type | Meaning | Typical use / limits |
| --- | --- | --- | --- |
| `getFiducials()` | `List<LLFieldMap.Fiducial>` | Known tags in this map. | Enumerate configuration; modifying this copied list does not modify the map. |
| `getType()` | `String` | Competition/map category, such as `"ftc"`. | Pass the literal `"ftc"` for FTC because of the validation bug below. |
| `getNumberOfTags()` | `int` | Number of map entries. | Check configuration completeness; not the number of currently visible tags. |
| `isValid()` | `boolean` | Limited nonempty/type check. | Pre-upload gate only; does not validate field geometry. |


### LLFieldMap.Fiducial properties

| Property / method | Java type | Meaning | Typical use / limits |
| --- | --- | --- | --- |
| `getId()` | `int` | Tag ID expected at this mapped location. | Associate an observation with known field geometry. |
| `getSize()` | `double`, mm | Configured physical fiducial size. | Establish metric scale for pose estimation; wrong size corrupts distance. |
| `getFamily()` | `String` | Configured fiducial coding family. | Match the map to the actual tag family. |
| `getTransform()` | `List<Double>` | Flattened 4×4 tag transform. | Define position and orientation in the map; use verified map conventions, not six pose values. |
| `isUnique()` | `boolean` | Map flag marking the fiducial as unique. | Preserve the verified map setting; this does not inspect the field for duplicate IDs. |

**Behavior:** Both classes copy input lists and expose no public setters. Construct replacements for changed configuration. A map fiducial has no `isValid()` method of its own.

### Construction and upload

`new LLFieldMap()` creates an empty map with empty type. There are no public setters or `addFiducial()` method. Editing the list returned by `getFiducials()` does not edit the map. Build a new map from the desired list:

```java
import com.qualcomm.hardware.limelightvision.LLFieldMap;
import java.util.ArrayList;
import java.util.List;

// existingMap and verifiedTag are supplied by your configuration code.
List<LLFieldMap.Fiducial> tags = new ArrayList<>(existingMap.getFiducials());
tags.add(verifiedTag);
LLFieldMap updated = new LLFieldMap(tags, "ftc");
boolean uploaded = camera.uploadFieldmap(updated, null);
```

Construct a tag with `new LLFieldMap.Fiducial(int id, double size, String family, List<Double> transform, boolean isUnique)`. Size is **millimeters**, not the meters used by observed poses. The transform describes a 4×4 matrix; it is not a six-number pose array. Use verified map-export conventions for matrix ordering and translation units. The SDK does not validate them. The family example in its source is `apriltag3_36h11_classic`.

`new LLFieldMap.Fiducial()` uses ID −1, size 165.1, that family, `unique=true`, and an **empty** transform list. Capacity 16 does not mean the list contains 16 elements. This default object is not a ready-to-upload field definition.

Map getters are `getFiducials()`, `getType()`, `getNumberOfTags()`, and `isValid()`. Tag getters are `getId()`, `getSize()`, `getFamily()`, `getTransform()`, and `isUnique()`. JSON constructors and `toJson()` are protected; ordinary TeamCode cannot call them as public import/export utilities.

**11.2.1 validity bug:** `LLFieldMap.isValid()` compares type strings to `"ftc"` and `"frc"` using Java reference inequality (`!=`), not `.equals()`. Thus a nonempty map made with the literal `"ftc"` passes the type test, while an equal string loaded or constructed at runtime may fail it. When constructing an FTC map, pass the literal `"ftc"`. The method also does not validate tag IDs, sizes, families, duplicate IDs, or matrix contents. `uploadFieldmap()` rejects maps that fail this limited check; do not pass null.

## CalibrationResult: reading camera calibration

| Property / method | Java type | Meaning | Typical use / limits |
| --- | --- | --- | --- |
| `getDisplayName()` | `String` | Human-readable calibration label. | Choose or log a report; label does not prove it is active. |
| `getResX()` | `double`, pixels | Calibration image width. | Check resolution compatibility before using intrinsics. |
| `getResY()` | `double`, pixels | Calibration image height. | Check resolution/aspect compatibility. |
| `getReprojectionError()` | `double` | Reported fit error when projecting calibration points back into images. | Compare calibration fit quality under the same procedure; SDK does not define a universal threshold or document this getter’s unit. |
| `getCamMatVector()` | `double[]`, intended length 9 | Flattened 3×3 camera intrinsic matrix. | Describes image projection, including focal parameters and principal point; copied on return. Confirm layout with calibration export before indexing. |
| `getDistortionCoefficients()` | `double[]`, intended length 5 | Lens-distortion model coefficients. | Correct image distortion in compatible geometry code; copied on return. Preserve export coefficient ordering. |
| `isValid()` | `boolean` | Basic structural/nonzero checks. | Reject unusable defaults; not a guarantee of good calibration or correct resolution. |


| Camera getter | Report |
| --- | --- |
| `getCalDefault()` | Default camera calibration |
| `getCalFile()` | User-generated calibration file |
| `getCalEEPROM()` | EEPROM calibration |
| `getCalLatest()` | Latest calibration result; not necessarily active in the camera |

All return `LLResultTypes.CalibrationResult`. Its getters expose display name, X/Y resolution, reprojection error, a camera-matrix vector, and distortion coefficients. `getCamMatVector()` returns a `double[]` intended to hold 9 elements; `getDistortionCoefficients()` returns a `double[]` intended to hold 5. Both return copies.

`new CalibrationResult()` creates zero/default values and fails `isValid()`. A public value constructor takes `(String displayName, double resX, double resY, double reprojectionError, double[] camMatVector, double[] distortionCoefficients)` and clones the arrays. Supply non-null arrays of the documented sizes: this constructor does not enforce their lengths, and an empty matrix can make `isValid()` throw when it accesses element zero.

The JSON report parser checks matrix/coefficient lengths and nonzero resolution. `isValid()` additionally checks its internal flag and a nonzero first camera-matrix element; it does not establish numerical quality or a good reprojection error. Check validity and application-specific quality before using a report. A failed fetch produces default invalid calibration. Calibration update/delete methods in this driver are private, so constructing a report does not upload it.

## Python values and other commands

### Device command behaviors

All methods below belong to `Limelight3A`. Boolean results report request success, not a newly processed frame or geometric correctness.

| Method | Meaning / use | Effect and limits |
| --- | --- | --- |
| `pipelineSwitch(int index)` | Select the saved vision task. | Changes camera pipeline; wait for matching result index. |
| `reloadPipeline()` | Reload the current saved pipeline. | Camera command; returns `boolean`. |
| `updateRobotOrientation(double yaw)` | Supply field-aligned yaw for MegaTag2. | Degrees; sends five other orientation fields as zero. |
| `updatePythonInputs(double[])` / eight-double overload | Send application values to a SnapScript. | Arrays require 1–32 values; the scalar overload sends eight. |
| `uploadPipeline(String jsonString, Integer index)` | Save a pipeline definition to the camera. | Synchronous upload; null index selects default. |
| `uploadFieldmap(LLFieldMap fieldmap, Integer index)` | Install known field-tag geometry. | Checks limited map validity; null map is not accepted. |
| `uploadPython(String pythonString, Integer index)` | Install script source. | Synchronous upload; does not mean outputs are already available. |
| `captureSnapshot(String snapname)` | Save an image for later tuning. | Changes camera storage; returns success flag. |
| `deleteSnapshot(String snapname)` | Remove one saved image. | Deletes camera data. |
| `deleteSnapshots()` | Remove all saved snapshots. | Deletes camera data. |
| `resetDeviceConfigurationForOpMode()` | SDK lifecycle hook. | No-op in 11.2.1; does not reset pipeline or clear observations. |

`updatePythonInputs(double[])` accepts 1–32 values and returns false for null, empty, or longer arrays. An overload takes exactly eight scalar doubles. The outgoing values have no built-in meanings; define a slot contract with your SnapScript.

`LLResult.getPythonOutput()` always allocates **32 doubles** in this SDK, padding short firmware output with zeros and truncating long output. The array's length cannot tell you how many script values were actually produced. Include a script-defined validity or sequence value when your application needs one.

Pipeline uploads accept JSON text, Python uploads accept script text, and field-map uploads accept an `LLFieldMap`. Each accepts an `Integer` slot with null selecting the default. These methods perform I/O when called; modifying a Java object alone does not configure the camera. Snapshot capture/deletion likewise acts on the camera. Refer to the [command signature table](api.md#driver-methods) for names and parameter types.
