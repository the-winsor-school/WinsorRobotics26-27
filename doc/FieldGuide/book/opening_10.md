Everyone adds a line.  One method sends the report.

```java
    public void updateTelemetry() {
        if (driveTrain != null) driveTrain.updateTelemetry();
        if (mechAssembly != null) mechAssembly.updateTelemetry();
        telemetry.update();
    }
```
