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

    double armPositionTarget = 0;

    double[] armPositions;

    int armPosUp = 0;
    int armPosMid = 1;
    int armPosLow = 2;
    int armPosDown = 3;

    double armPosRange;

    public CargoArm()
    {
        moTalBallArm = new TalonSRX(2);
        

        moVicBallRoll = new VictorSPX(4);
        encBallRoll = new Encoder(moTalBallArm, 2);


        solHandExtend = new Solenoid(2);
        solArmBrake = new Solenoid(1);

        armPositions = new double[4];

        armPositions[0] = 0;
        armPositions[1] = -5000;
        armPositions[2] = -5500;
        armPositions[3] = -6600;


        // get max (abs) value for the range of our arm movement
        // (in reality this value will probably always be the same but it never hurts to be careful)
        for(int i = 0; i < armPositions.length; i++)
        {
            if(Math.abs(armPositions[i]) > armPosRange)
            {
                armPosRange = Math.abs(armPositions[i]);
            }
        }
    
    }

    public void init()
    {
        //encVicBallRoll.reset();
        encBallRoll.initQuad();
        System.out.println("Ball roller zeroed.");
        System.out.println("Ball roller value: " + encBallRoll.position());

        armPositionTarget = 0;
    }

    public void periodic()
    {
        if(Constants.DEBUG)
        {
            System.out.println("---<CargoArm>---");
            System.out.println("Ball roller:  " + encBallRoll.position());
            System.out.println();
        }

        // check for cargo arm position
        double curArmPos = encBallRoll.position();

        double distToTarget = curArmPos - armPositionTarget;

        // the encoder is only so accurate - even manually putting it at the zeroed position
        // we can read a value from -20 to about +20.
        // so let's not move the arm if we're "close enough" since we may be there already
        if(distToTarget > 50)
        {
            // move by the percentage of the way there we are
            // ex. if we are at 0 and we need to be 100% of the way there, move at (100 - 0)% speed
            // ex. if we are at 250 and we need to be at enc 500, move at (100 - 50)% speed...?
            // and multiply by the sign of the distance i think.
            moTalBallArm.set(ControlMode.PercentOutput, Math.signum(distToTarget) * (1 - (curArmPos / armPosRange)));
        }
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

    // snap cargo arm to set positions
    // the actual motor movement takes place in periodic()
    public void setArmDown()
    {
        // set hand to some known position that means "down"
        System.out.println("Setting cargo arm position to 'down'");
        armPositionTarget = armPositions[armPosDown];
    }
    public void setArmUp()
    {
        System.out.println("Setting cargo arm position to 'up'");
        armPositionTarget = armPositions[armPosUp];
    }
    public void setArmMid()
    {
        System.out.println("Setting cargo arm position to 'mid'");
        armPositionTarget = armPositions[armPosMid];
    }
    public void setArmLow()
    {
        System.out.println("Setting cargo arm position to 'low'");
        armPositionTarget = armPositions[armPosLow];
    }

    public void setArmTarget(double target)
    {
        System.out.println("Setting arm target to: " + target);
        armPositionTarget = target;
    }

    public double currentPosition()
    {
        return encBallRoll.position();
    }
}