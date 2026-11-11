All that design, and the OpMode's whole job is to say `update` until somebody
presses stop.

```java
        waitForStart();
        while(opModeIsActive()){
            robot.update(gamepad1, gamepad2);
            robot.updateTelemetry();
        }
```
