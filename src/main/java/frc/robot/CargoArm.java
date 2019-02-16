package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Solenoid;

public class CargoArm
{

    TalonSRX moTalBallArm;

    VictorSPX moVicBallRoll;

    Encoder encVicBallRoll;

    Solenoid solHandExtend;
    Solenoid solArmBrake;

    Boolean handIsExtended = false;

    public CargoArm()
    {
        moTalBallArm = new TalonSRX(2);

        moVicBallRoll = new VictorSPX(4);
        encVicBallRoll = new Encoder(0, 1, false, Encoder.EncodingType.k4X);
        encVicBallRoll.setMaxPeriod(1);
        encVicBallRoll.setMinRate(10);
        encVicBallRoll.setDistancePerPulse(5);
        encVicBallRoll.setSamplesToAverage(7);


        solHandExtend = new Solenoid(2);
        solArmBrake = new Solenoid(1);
    
    }

    public void init()
    {
        encVicBallRoll.reset();
        moTalBallArm.setSelectedSensorPosition(0);
    }

    public void periodic()
    {
        
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
}