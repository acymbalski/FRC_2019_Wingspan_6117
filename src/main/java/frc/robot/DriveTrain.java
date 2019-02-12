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

    // so we can flip forwards/backwards driving
    private int orientation;

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
        // moTalWhlR.setInverted(true);
        // moVicWhlR.setInverted(false);

        orientation = 1;

        // stop all motors
        stop();
    }

    public void drive(double left_amt, double right_amt)
    {
        // if driving forward
        if(orientation == 1)
        {
            set_left_motors(left_amt);// * speedFactor);
            set_right_motors(right_amt);// * speedFactor);
        }
        // otherwise, if driving backwards, flip the controls
        // (and reverse which joystick controls which motor)
        else if(orientation == -1)
        {
            set_left_motors(right_amt);
            set_right_motors(left_amt);
        }
    }

    public void stop()
    {
        set_left_motors(0);
        set_right_motors(0);

        System.out.println("Motors stopped.");
    }

    private void set_left_motors(double amt)
    {
        moTalWhlL.set(ControlMode.PercentOutput, amt * orientation);
        moVicWhlL.set(ControlMode.PercentOutput, amt * orientation);
    }

    private void set_right_motors(double amt)
    {
        moTalWhlR.set(ControlMode.PercentOutput, -amt * orientation);
        moVicWhlR.set(ControlMode.PercentOutput, -amt * orientation);
    }

    public void flip_orientation()
    {
        System.out.println("Flipping orientation!");
        orientation *= -1;
    }

    public boolean isFacingForward()
    {
        return orientation == 1;
    }

}
