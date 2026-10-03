package org.firstinspires.ftc.teamcode.RobotModel.Mechs.Assemblies;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.Collections;
import java.util.Set;

public class MacroManager<R>
{
    private MechAssembly.IMacroAssemblyStrategy<R> active;   // null = no macro running

    public void request(MechAssembly.IMacroAssemblyStrategy<R> macro) 
    { 
        if (active != null) return;   // != here: something's already running, so ignore this request
        active = macro;   
        active.start();
    }
    public void cancel() 
    { 
        if (active == null)  
            return;           
        active.cancel();
        active = null;       
    }
    public void update(Gamepad gamepad) 
    {
        if (active == null)   
            return;           
        active.update(gamepad);     
        if(active.isComplete())   
            active = null;     
    }
    public Set<R> claimed() 
    {  
        if (active == null)   
            return Collections.emptySet();           
        return active.claims();      // return what active claims    
    }
    public boolean isRunning() 
    {           
        return active != null;      // true while a macro is running
    }
}
