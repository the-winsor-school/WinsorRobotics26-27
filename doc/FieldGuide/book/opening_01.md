Hold up and down at the same time, and the flywheel politely splits the difference.

```java
                    double power = 0.45;
                    if (gamepad.dpad_up) {
                        power += 0.05;
                    }
                    if (gamepad.dpad_down) {
                        power -= 0.05;
                    }
```
