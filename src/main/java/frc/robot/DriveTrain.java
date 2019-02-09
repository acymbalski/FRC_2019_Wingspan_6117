package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

public class DriveTrain
{

    // motors
    private TalonSRX moTalWhlL, moTalWhlR;
    private VictorSPX moVicWhlL, moVicWhlR;


    // set speed limitations
    // 0.5 = half speed
    // ...
    // 1.0 = full speed
    // etc.
    private double speedFactor = 0.5;

    public DriveTrain()
    {
        
        // init motors
        
        // left motors
        // (must be inverted)
        moTalWhlL = new TalonSRX(1);
        moVicWhlL = new VictorSPX(3);
        moTalWhlL.setInverted(false);
        moVicWhlL.setInverted(false);

        // right motors
        moTalWhlR = new TalonSRX(0);
        moVicWhlR = new VictorSPX(7);
        moTalWhlR.setInverted(true);
        moVicWhlR.setInverted(true);

        // stop all motors
        stop();
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

        System.out.println("Motors stopped.");
    }

    private void set_left_motors(double amt)
    {
        moTalWhlL.set(ControlMode.PercentOutput, amt);
        moVicWhlL.set(ControlMode.PercentOutput, amt);
    }

    private void set_right_motors(double amt)
    {
        moTalWhlR.set(ControlMode.PercentOutput, amt);
        moVicWhlR.set(ControlMode.PercentOutput, amt);
    }

}