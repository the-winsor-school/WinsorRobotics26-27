Even the autonomous twin needs to be introduced to telemetry.

```java
    @Override
    public void initializeTelemetry(Telemetry telemetry) {
        super.initializeTelemetry(telemetry);
        auton = new AutonomousIntakeBehaviors(telemetry);
    }
```
