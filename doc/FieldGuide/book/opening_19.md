The switch has reported the problem.  Somebody still has to listen.

```java
        if ((power > 0 && !canGoForward() )|| (power < 0 && !canGoReverse())) {
            power = 0;
        }
```
