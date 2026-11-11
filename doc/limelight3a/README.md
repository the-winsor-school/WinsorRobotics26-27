# Limelight 3A reference

Researched on **2026-09-12** against FTC Hardware 11.2.1, and re-verified on **2026-11-11** against **FTC Hardware 12.0.0**, matching this repository's `build.dependencies.gradle`. The 12.0.0 check diffed the published source archives: every file in `com/qualcomm/hardware/limelightvision/`, and `Pose3D`, `Position`, `YawPitchRollAngles`, and `Quaternion` in RobotCore, is identical in 11.2.1 and 12.0.0, so every behavior described here holds for both. These are locally readable reference notes, not a full mirror of the vendor website. Camera firmware was not available for verification; the vendor's online firmware documentation is unversioned.

- [Java objects and runtime behavior](java-objects.md) — start here for the Java library
- [API reference and Java example](api.md)
- [Java objects, JSON keys, units, and compatibility notes](data-shapes.md)
- [Student TODO plan: aiming at BIOBUZZ AprilTag clusters](cluster-targeting-plan.md)

## Camera setup

The **3A** connects to the Control Hub's blue USB 3.0 port using USB-C to USB-A. It exposes an Ethernet-over-USB interface. Scan for it in Driver Station robot configuration and name it `limelight` to match the example. Configure pipelines through the browser UI at `http://limelight.local:5801` when connected to your computer, or use Limelight Hardware Manager to discover the device. Pipeline slots are 0–9.

This model has no built-in illumination or RJ45 connector. Neural pipelines run on its CPU; Google Coral is unsupported. MegaTag2 uses externally supplied robot orientation. See the official [3A quick-start](https://docs.limelightvision.io/docs/docs-limelight/getting-started/limelight-3a).

## Sources and refresh

| Source | Purpose |
| --- | --- |
| [3A quick-start](https://docs.limelightvision.io/docs/docs-limelight/getting-started/limelight-3a) | Model-specific wiring and setup |
| [FTC programming guide](https://docs.limelightvision.io/docs/docs-limelight/apis/ftc-programming) | Pipelines, targeting, and localization |
| [12.0.0 Java API](https://javadoc.io/static/org.firstinspires.ftc/Hardware/12.0.0/com/qualcomm/hardware/limelightvision/package-summary.html) | Class and method reference |
| [12.0.0 source archive](https://repo.maven.apache.org/maven2/org/firstinspires/ftc/Hardware/12.0.0/Hardware-12.0.0-sources.jar) | Exact signatures, JSON parsing, defaults, and driver behavior |
| [HTTP API](https://docs.limelightvision.io/docs/docs-limelight/apis/rest-http-api) | Direct camera requests |
| [JSON results](https://docs.limelightvision.io/docs/docs-limelight/apis/json-results-specification) | Firmware wire format |
| [JSON status](https://docs.limelightvision.io/docs/docs-limelight/apis/json-status-specification) | Diagnostic wire format |

When upgrading the SDK, download its matching source archive and compare `com/qualcomm/hardware/limelightvision/{Limelight3A,LLResult,LLResultTypes,LLStatus,LLFieldMap}.java`. Recheck the vendor pages against the installed camera firmware. In particular, retest pipeline switching, missing results, array lengths, and units with camera output.

## Repository examples

- [SDK Limelight sample](../../FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples/SensorLimelight3A.java)
- [Team tag lookup helpers](../../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Extensions/LimelightExtensions.java)
- [AprilTags and Limelight lesson](../FlintLessons/GettingStarted/04-apriltags-and-limelight.md)

`ConceptAprilTagLocalization` uses the FTC VisionPortal/AprilTagProcessor path. Its detection objects are different from Limelight's `LLResultTypes.FiducialResult`.
