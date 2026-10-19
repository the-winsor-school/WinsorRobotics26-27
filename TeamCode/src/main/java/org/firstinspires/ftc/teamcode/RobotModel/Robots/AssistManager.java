package org.firstinspires.ftc.teamcode.RobotModel.Robots;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.Collections;
import java.util.Set;

public class AssistManager<R>
{
    private Robot.IAssistRobotStrategy<R> active;  
    private String lastEnd = "None";  
    public String lastEnd() { return lastEnd; } //"X replaced by Y"

    public void start(Robot.IAssistRobotStrategy<R> macro) 
    { 
       if (active != null) {
        lastEnd = active.name() + " replaced by " + macro.name();
        active.stop();      // stop the OLD one
       }
        active = macro;
        active.start();         // start the NEW one
    }
    public void stop(String reason)
    {
        if (active == null)
            return;
        active.stop();
        lastEnd = active.name() + " " + reason;
        active = null;
    }

    public Set<R> claimed() 
    {  
        if (active == null)   
            return Collections.emptySet();           
        return active.claims();      // return what active claims    
    }  
    

    public String activeName()
    {           
        if (active == null)   
            return "None";           
        
        return active.name();       
    }

    public boolean isRunning() 
    {           
        return active != null;      
    }

    public void update(Gamepad gamepad1, Gamepad gamepad2)
    {
        if (active == null)
            return;
        active.update(gamepad1, gamepad2);
    }

}
