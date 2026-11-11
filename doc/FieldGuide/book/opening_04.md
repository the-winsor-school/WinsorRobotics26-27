Somebody timed this flywheel once, and the robot has trusted that stopwatch ever since.

```java
    public IState startShooter() {
        mechAssembly.reportStatus("startShooter");
        return
                doAndWait(
                        () -> mechAssembly
                                .autonFlywheel
                                .setPower(0.6),
                        2200,
                        fire()
                );
```
