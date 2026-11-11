Ask this state what comes next and it names itself.  It keeps that up until
the clock runs out.

```java
            if(elapsedTime.milliseconds() < millis)
                return doAndWait(action, millis, nextState);
```
