package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

public class DriveTrain
{

    // motors
    private TalonSRX moTalWhlL, moTalWhlR;
    private VictorSPX moVicWhlL, moVicWhlR;

    // so we can flip forwards/backwards driving
    private int orientation;

    private Encoder encLeft, encRight;

    public boolean fastSpeed = false;
    
    public DriveTrain()
    {
        
        // init motors
        
        // left motors
        // (must be inverted)
        moTalWhlL = new TalonSRX(1);
        moVicWhlL = new VictorSPX(3);
        moTalWhlL.configFactoryDefault();

        // right motors
        moTalWhlR = new TalonSRX(0);
        moVicWhlR = new VictorSPX(7);
        moTalWhlR.configFactoryDefault();

        encLeft = new Encoder(moTalWhlL, 1);
        encRight = new Encoder(moTalWhlR, 0);

        orientation = 1;

        // stop all motors
        stop();
        encLeft.initQuad();
        encRight.initQuad();
        
        System.out.println("Left and right wheels zeroed.");
        System.out.println("Left wheel value value: " + encLeft.position());
        System.out.println("Right wheel value value: " + encRight.position());
    }

    public void drive(double left_amt, double right_amt)
    {
        // if driving forward
        if(orientation == 1)
        {
            set_left_motors(left_amt);
            set_right_motors(right_amt);
        }
        // otherwise, if driving backwards, flip the controls
        // (and reverse which joystick controls which motor)
        else if(orientation == -1)
        {
            set_left_motors(right_amt);
            set_right_motors(left_amt);
        }
    }

    public void flip_orientation()
    {
        System.out.println("Flipping orientation!");
        orientation *= -1;
    }

    // public void init()
    // {
    //     encLeft.initQuad();
    //     encRight.initQuad();
        
    //     System.out.println("Left and right wheels zeroed.");
    //     System.out.println("Left wheel value value: " + encLeft.position());
    //     System.out.println("Right wheel value value: " + encRight.position());
    // }

    public boolean isFacingForward()
    {
        return orientation == 1;
    }

    public void set_left_motors(double amt)
    {
        moTalWhlL.set(ControlMode.PercentOutput, amt * orientation * (fastSpeed ? 2 : 1));
        moVicWhlL.set(ControlMode.PercentOutput, amt * orientation * (fastSpeed ? 2 : 1));
    }

    public void set_right_motors(double amt)
    {
        moTalWhlR.set(ControlMode.PercentOutput, -amt * orientation * (fastSpeed ? 2 : 1));
        moVicWhlR.set(ControlMode.PercentOutput, -amt * orientation * (fastSpeed ? 2 : 1));
    }

    public void stop()
    {
        set_left_motors(0);
        set_right_motors(0);

        System.out.println("Motors stopped.");
    }

}
