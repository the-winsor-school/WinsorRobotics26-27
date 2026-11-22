package org.firstinspires.ftc.teamcode.AutonStrategies;

import org.firstinspires.ftc.teamcode.Extensions.IState;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a macro as a list of timed steps, top to bottom.
 */
public class SequenceBuilder
{
    private final StateMachine machine;  
    
    public SequenceBuilder(StateMachine machine)
    {
        // TODO: store it - idk bro
    }

    public SequenceBuilder doAndWait(Runnable action, int millis)
    {
        return () ->
        {
            if (elapsedTime == null) // if we are STARTING this state
            {   // this is an initialization step.
                elapsedTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
                // DO THE THING!
                action.run();
            }


            // if we are still waiting... go let other things happen!!
            // this is the non blocking step.
            if(elapsedTime.milliseconds() < millis)
                return doAndWait(action, millis, nextState);
            // if we have waited long enough, clear the clock
            // and go on over to the next state!
            elapsedTime = null;
            return nextState;
        };
    }

    public IState build()
    {
        IState next = null;   
        //idk bro
    }
}
