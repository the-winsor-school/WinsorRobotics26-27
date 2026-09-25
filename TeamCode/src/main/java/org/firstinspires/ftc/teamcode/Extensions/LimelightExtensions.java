package org.firstinspires.ftc.teamcode.Extensions;

import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import java.util.Arrays;

public class LimelightExtensions {
    // TODO (students): BIOBUZZ goals are AprilTag *clusters* (4 tags sharing one origin).
    // Add cluster search + origin estimation here - see doc/limelight3a/cluster-targeting-plan.md
    /**
     * *look for a specific AprilTag Id using the limelight camera
     * If the tag is not found, this method returns NULL
     * @param limelight camera configured to read Fiducials
     * @param tagId which tag id are you looking for.
     * @return The desired FiducialResult or NULL if not found.
     */

    // TODO: getLatestResult() can return null before the Limelight has produced its
    // first frame (e.g. right after limelight.start()), or if it drops out mid-match.
    // Neither this method nor tryFindOneOf() below null-checks it before calling
    // .getFiducialResults(), so that's an uncaught NullPointerException waiting to
    // happen - and since LimelightAutoTarget.lookForTag() calls this every single
    // teleop loop, it would crash the whole OpMode, not just this one lookup.
    public static LLResultTypes.FiducialResult tryGetFiducial(
        Limelight3A limelight,
        int tagId)
    {
        return limelight
            .getLatestResult()
            .getFiducialResults()
            .stream()
            .filter(fr -> fr.getFiducialId() == tagId)
            .findFirst()
            .orElse(null);
    }

    public static LLResultTypes.FiducialResult tryFindOneOf(
            Limelight3A limelight,
            int... tags)
    {


        return limelight
                .getLatestResult()
                .getFiducialResults()
                .stream()
                .filter(fr ->  Arrays.stream(tags).anyMatch(i -> i == fr.getFiducialId()))
                .findFirst()
                .orElse(null);
    }
}