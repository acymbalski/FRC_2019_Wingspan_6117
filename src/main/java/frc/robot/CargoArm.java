package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class CargoArm
{

    TalonSRX moTalBallArm;

    public CargoArm()
    {
        moTalBallArm = new TalonSRX(2);
    }

    public void rotateArm(double amt)
    {
        moTalBallArm.set(ControlMode.PercentOutput, amt);
    }
    
}