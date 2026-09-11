package org.firstinspires.ftc.teamcode.Extensions;


import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * Define some extension methods for the Gamepad class to do some basic stuff in one place
 * particularly, handle the Zero Deadzone and inversion of the Y axis output.
 */
public final class GamepadExtensions
{
    /**
     * Java is so gross.... why can't I just have an explicitly static class ;_;
     */
    private GamepadExtensions() { }

    /**
     * Defines the size of the dead-zone for Zero on
     * a gamepad.
     */
    public static final float DeadZoneRadius = 0.05f;

    /**
     * Helper Method for Applying a standard Dead-Zone calculation to a Gamepad Axis reading.
     * @param input pass any gamepad axis here.
     * @return 0 if abs(input) < DeadZoneRadius, otherwise, input is unchanged.
     */
    public static float ApplyDeadZone(float input)
    {
        if(Math.abs(input) < DeadZoneRadius)
            return 0.0f;
        return input;
    }

    /**
     * apply dead zone to `gamepad.left_stick_x`
     * @param gamepad
     * @return value between -1 and 1 with dead zone applied.
     */
    public static float GetLeftStickX(Gamepad gamepad)
    {
        if(gamepad == null)
            return 0f;
        return ApplyDeadZone(gamepad.left_stick_x);
    }

    /**
     * apply dead zone to `gamepad.left_stick_y` and invert (because Up is negative on the joystick.)
     * @param gamepad
     * @return value between -1 and 1 with dead zone applied.
     */
    public static float GetLeftStickY(Gamepad gamepad)
    {
        if(gamepad == null)
            return 0f;
        // negative to account for inverted y on gamepads!
        return -ApplyDeadZone(gamepad.left_stick_y);
    }

    /**
     * apply dead zone to `gamepad.right_stick_x`
     * @param gamepad
     * @return value between -1 and 1 with dead zone applied.
     */
    public static float GetRightStickX(Gamepad gamepad)
    {
        if(gamepad == null)
            return 0f;
        return ApplyDeadZone(gamepad.right_stick_x);
    }

    /**
     * apply dead zone to `gamepad.right_stick_y` and invert (because Up is negative on the joystick.)
     * @param gamepad
     * @return value between -1 and 1 with dead zone applied.
     */
    public static float GetRightStickY(Gamepad gamepad)
    {
        if(gamepad == null)
            return 0f;
        // negative to account for inverted y on gamepads!
        return -ApplyDeadZone(gamepad.right_stick_y);
    }
}
