package org.firstinspires.ftc.teamcode.Extensions;

public class ThreadExtensions
{
    /**
     * Encapsulate Thread.sleep(...) in a try catch block.
     * @param millis try to sleep this long
     * @return returns false if sleep was interrupted.
     */
    public static boolean TrySleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            return false;
        }
        return true;
    }
}
