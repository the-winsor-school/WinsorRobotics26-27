package org.firstinspires.ftc.teamcode.Extensions;

public class AngleExtensions {
    /**
     * used when u give bad data bc ur bad
     * converts number outside of (-180, 180) range to equivalent angle within that range
     * @param degrees
     * @return angle in range (-180, 180)
     */
    public static double mapToIMURange (double degrees) {
        while(Math.abs(degrees)>180) {
            if (degrees<0)
                degrees+=360;
            else {
                degrees-=360;
            }
        }
        return degrees;
    }

    /**
     * used to find the other "name" of the same angle; helps in getSmol Calculatioins
     * @param degrees
     * @return angle in range (-360, 360)
     */
    public static double getDoppleganger(double degrees) {
        if (degrees < 0)
            return degrees+360;

        return degrees-360;
    }

    /**
     * looks at all the possible ways to get from angle A to Angle B and finds the shortest
     * possible path, aka angle with smallest magnitutde
     * @param degrees
     * @param yaw
     * @return angle in range (-180, 180);
     */

    public static double getSmol(double degrees, double yaw) {
        double yeeyaw = getDoppleganger(yaw);
        double dees = getDoppleganger(degrees);
        double[] possibleValues = new double[]{
                degrees-yaw,
                degrees-yeeyaw,
                dees-yaw,
                dees-yeeyaw
        };
        double smol=possibleValues[0];
        for (int i =1; i<4; i++) {
            if (Math.abs(possibleValues[i])<Math.abs(smol)) {
                smol = possibleValues[i];
            }
        }
        return smol;
    }
}

