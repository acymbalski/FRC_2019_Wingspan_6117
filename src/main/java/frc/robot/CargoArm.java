package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.wpilibj.Solenoid;

public class CargoArm
{

    TalonSRX moTalBallArm;

    VictorSPX moVicBallRoll;

    Encoder encBallRoll;

    Solenoid solHandExtend;
    Solenoid solArmBrake;

    Boolean handIsExtended = false;

    public CargoArm()
    {
        moTalBallArm = new TalonSRX(2);
        

        moVicBallRoll = new VictorSPX(4);
        encBallRoll = new Encoder(moTalBallArm, 2);


        solHandExtend = new Solenoid(2);
        solArmBrake = new Solenoid(1);
    
    }

    public void init()
    {
        //encVicBallRoll.reset();
        encBallRoll.initQuad();
        System.out.println("Ball roller zeroed.");
        System.out.println("Ball roller value: " + encBallRoll.position());
    }

    public void periodic()
    {
        System.out.println("---<CargoArm>---");
        System.out.println("Ball roller:  " + encBallRoll.position());
        System.out.println();
    }

    public void rotateArm(double amt)
    {
        if(amt != 0)
        {
        solArmBrake.set(false);
        moTalBallArm.set(ControlMode.PercentOutput, amt);
        System.out.println("Rotating arm.");
        }
        else
        {
            moTalBallArm.set(ControlMode.PercentOutput, amt);
            solArmBrake.set(true);
        }
    }

    public void ballPull(double amt)
    {
        moVicBallRoll.set(ControlMode.PercentOutput, amt);
    }

    public void ballPush(double amt)
    {
        ballPull(-amt);
    }

    public void spinBallMotor(double amt)
    {
        moVicBallRoll.set(ControlMode.PercentOutput, amt);
    }
    
    public void toggleHand()
    {
        handIsExtended = !handIsExtended;

        solHandExtend.set(handIsExtended);
    }

    public void setArmDown()
    {
        // set hand to some known position that means "down"
        
    }
}