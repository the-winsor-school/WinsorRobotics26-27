Billy's four drive motors answer to the names somebody typed for them, spelled exactly the way they typed them.

```java
        LB = hardwareMap.get(DcMotor.class, "lb");
        LB.setDirection(orientationConfiguration.getLb());
        LF = hardwareMap.get(DcMotor.class, "lf");
        LF.setDirection(orientationConfiguration.getLf());
```
