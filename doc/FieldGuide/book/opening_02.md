One class in this folder has a constructor whose whole job is to make sure it never runs.

```java
public final class GamepadExtensions
{
    /**
     * Java is so gross.... why can't I just have an explicitly static class ;_;
     */
    private GamepadExtensions() { }
```
