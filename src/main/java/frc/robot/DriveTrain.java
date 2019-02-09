package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

public class DriveTrain
{

    // CAN motors
    private TalonSRX moRightTal, moLeftTal;
    private VictorSPX moLeftVic, moRightVic;


    // set speed limitations
    // 0.5 = half speed
    // ...
    // 1.0 = full speed
    // etc.
    private double speedFactor = 0.5;

    public DriveTrain()
    {
        
        // init motors: CAN
        moRightTal = new TalonSRX(0);
        moLeftTal = new TalonSRX(1);
        moLeftVic = new VictorSPX(2);
        moRightVic = new VictorSPX(3);

        // set CAN motors to 0%
        moRightTal.set(ControlMode.PercentOutput, 0);
        moLeftTal.set(ControlMode.PercentOutput, 0);
        moLeftVic.set(ControlMode.PercentOutput, 0);
        moRightVic.set(ControlMode.PercentOutput, 0);
    }

    public void drive(double left_amt, double right_amt)
    {
        set_left_motors(left_amt * speedFactor);
        set_right_motors(right_amt * speedFactor);
    }

    public void stop()
    {
        set_left_motors(0);
        set_right_motors(0);
    }

    private void set_left_motors(double amt)
    {
        moLeftTal.set(ControlMode.PercentOutput, amt);
        moLeftVic.set(ControlMode.PercentOutput, amt);
    }

    private void set_right_motors(double amt)
    {
        moRightTal.set(ControlMode.PercentOutput, amt);
        moRightVic.set(ControlMode.PercentOutput, amt);
    }

}