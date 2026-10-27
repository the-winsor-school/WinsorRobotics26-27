Roll call, every loop.  Four names, and every one gets called.

```java
@Override
public void updateTelemetry() {
    telemetry.addLine("--- BillyMA ---");
    intake.update();
    ballPusher.update();
    flywheel.update();
    turret.update();
}
```
