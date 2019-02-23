package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.wpilibj.Solenoid;

public class CargoArm
{

    TalonSRX moTalBallArm;

    VictorSPX moVicBallRoll;

    Encoder encCargoArm;

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

    double requestedMove = 0;

    Boolean armLockEnabled = true;

    double motorPower = 0.0;
    
    // amount to consider the cargo arm "locked" by
    // ex. a value of 50 will let us be in the "correct" position if
    // we're within +/- 50 encoder counts
    int ENCODER_TOLERANCE = 50;
    
    double GRAV_CONSTANT = 0.4;


    public CargoArm()
    {
        moTalBallArm = new TalonSRX(2);
        

        moVicBallRoll = new VictorSPX(4);
        encCargoArm = new Encoder(moTalBallArm, 2);


        solHandExtend = new Solenoid(2);
        solArmBrake = new Solenoid(1);

        armPositions = new double[4];

        armPositions[0] = 0;
        armPositions[1] = -2835;
        armPositions[2] = -3880;
        armPositions[3] = -6000;


        // get max (abs) value for the range of our arm movement
        // (in reality this value will probably always be the same but it never hurts to be careful)
        for(int i = 0; i < armPositions.length; i++)
        {
            if(Math.abs(armPositions[i]) > armPosRange)
            {
                armPosRange = Math.abs(armPositions[i]);
            }
        }

        //encVicBallRoll.reset();
        System.out.println("Ball roller zeroed.");
        System.out.println("Ball roller value: " + encCargoArm.position());

        armPositionTarget = 0;
        solArmBrake.set(false);
    
    }

    public void armDown()
    {

        double target = -100; //This is slow, measured in encoder ticks per cycle

        if (encCargoArm.velocity() < (target - (target / 5)))
        {
            motorPower += 0.003; //Arbitary constant to tune
        }
        else if (encCargoArm.velocity() > (target + (target / 5)))
        {
            motorPower -= 0.003;
        }

        System.out.println("Motor Power: " + motorPower);
        rotateArm(motorPower);

    }
    
    public void armUp()
    {
        
        double target = 100;
        
        if (encCargoArm.velocity() < (target - (target / 5)))
        {
            
            motorPower += 0.003;
            
        }
        else if (encCargoArm.velocity() > (target + (target / 5)))
        {
            motorPower -= 0.003;
        }
        
        rotateArm(motorPower);
        
    }

    public void ballPull(double amt)
    {
        moVicBallRoll.set(ControlMode.PercentOutput, amt);
    }

    public void ballPush(double amt)
    {
        ballPull(-amt);
    }

    public double currentPosition()
    {
        return encCargoArm.position();
    }

    public double currentVelocity()
    {
        return encCargoArm.velocity();
    }

    public double getArmCalculation()
    {
        
        // cos(a_t)*G + (a_t - a_f)/a

        // G = gravitational constant
        // a_t = our current angular location

        // a_f = our destination location

        // a = range of the entire angle
        
        double curArmAngle = encCargoArm.angle();
        
        double amtToMove = (Math.cos(curArmAngle) * GRAV_CONSTANT + (armPositionTarget - curArmAngle) / armPosRange) / (GRAV_CONSTANT + 1);
        
        // disabled for now (check calculations first)
        //return amtToMove;
        System.out.println("" + curArmAngle + "," + GRAV_CONSTANT + "," + armPositionTarget + "," + armPosRange);
        
        // check for cargo arm position
        double curArmPos = encCargoArm.position();

        double distToTarget = curArmPos - armPositionTarget;

        //double amtToMove = Math.signum(distToTarget) * (1 - ((0.5) * curArmPos / armPosRange));
        //double amtToMove = -0.25;

        if(distToTarget < 0)
        {
            amtToMove *= -1;
        }
        if(Math.abs(distToTarget) > 1000)// && Math.signum())
        {
            amtToMove *= 2.5;
        }

        // System.out.println("Moving cargo arm by " + amtToMove);
        // System.out.println("Distance to move: " + distToTarget);
        // System.out.println("Current position: " + curArmPos);
        // System.out.println("---");

        // the encoder is only so accurate - even manually putting it at the zeroed position
        // we can read a value from -20 to about +20.
        // so let's not move the arm if we're "close enough" since we may be there already
        if((curArmPos < -100) || (curArmPos > -100 && Math.signum(amtToMove) < 0) && (distToTarget > 25 || distToTarget < -25))
        {
            // move by the percentage of the way there we are
            // ex. if we are at 0 and we need to be 100% of the way there, move at (100 - 0)% speed
            // ex. if we are at 250 and we need to be at enc 500, move at (100 - 50)% speed...?
            // and multiply by the sign of the distance i think.

            // compensate for gravity by moving 2/3s as much downward than upward
            if(curArmPos < -250 && Math.signum(amtToMove) < 0)
            {
                amtToMove *= 0.67;
            }

            return amtToMove;
        }
        else
        {
            return 0;
        }

    }

    public void requestMove(double amt)
    {
        requestedMove = amt;
    }

    public void rotateArm(double amt)
    {
        if(amt != 0)
        {
            moTalBallArm.set(ControlMode.PercentOutput, amt);
            System.out.println("Rotating arm.");
        }
        else
        {
            moTalBallArm.set(ControlMode.PercentOutput, amt);

            if(!solArmBrake.get())
            {
                System.out.println("Engaging brake.");
            }
        }
    }

    // snap cargo arm to set positions
    // the actual motor movement takes place in periodic()
    public void setArmDown()
    {
        // set hand to some known position that means "down"
        System.out.println("Setting cargo arm position to 'down'");
        armPositionTarget = armPositions[armPosDown];
    }
    
    public void setArmLow()
    {
        System.out.println("Setting cargo arm position to 'low'");
        armPositionTarget = armPositions[armPosLow];
    }
    
    public void setArmMid()
    {
        System.out.println("Setting cargo arm position to 'mid'");
        armPositionTarget = armPositions[armPosMid];
    }

    public void setArmTarget(double target)
    {
        System.out.println("Setting arm target to: " + target);
        armPositionTarget = target;
    }
    
    public void setArmUp()
    {
        System.out.println("Setting cargo arm position to 'up'");
        armPositionTarget = armPositions[armPosUp];
    }

    public void spinBallMotor(double amt)
    {
        moVicBallRoll.set(ControlMode.PercentOutput, amt);
    }

    public void toggleArmLock()
    {
        armLockEnabled = !armLockEnabled;
        System.out.println("Cargo arm lock set to: " + armLockEnabled);
        if(armLockEnabled)
        {
            armPositionTarget = encCargoArm.position();
            System.out.println("Locking cargo arm at: " + armPositionTarget);
        }
    }
    
    public void toggleHand()
    {
        handIsExtended = !handIsExtended;

        solHandExtend.set(handIsExtended);
    }

    public void zeroEncoder()
    {
        encCargoArm.initQuad();
    }
}
