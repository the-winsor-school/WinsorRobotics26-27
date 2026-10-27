package org.firstinspires.ftc.teamcode.AutonStrategies;


import com.qualcomm.robotcore.util.ElapsedTime;


import org.firstinspires.ftc.teamcode.Extensions.IState;


public abstract class StateMachine {
    /* Every State Machine must be aware of its Current State */
    protected IState currentState;


    /* if this state machine can terminate, it does so by setting currentState to NULL. ...*/
    public boolean isComplete() {
        return currentState == null;
    }


    /* Abort the Current execution - no questions asked! */
    public void abort() {
        currentState = null;
        elapsedTime = null;
    }


    /* Every State Machine must be able to update its state.*/
    public void updateState() {
        if (currentState != null)
            currentState = currentState.execute();
    }


    private ElapsedTime elapsedTime;


    /**
     * helper method for non-blocking, time based states!
     * @param action the thing you're doing!
     * @param millis how long to wait before proceeding
     * @param nextState the next thing to do!
     * @return the appropriate state.
     */
    protected IState doAndWait(Runnable action, int millis, IState nextState) {
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
}
