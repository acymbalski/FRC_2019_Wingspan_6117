package frc.robot;

import edu.wpi.first.wpilibj.Solenoid;

public class Ramp
{
    
    Solenoid solRamp;

    public Ramp()
    {
        solRamp = new Solenoid(0);
    }

    public void deploy()
    {
        solRamp.set(true);
    }

    public void undeploy()
    {
        solRamp.set(false);
    }
}