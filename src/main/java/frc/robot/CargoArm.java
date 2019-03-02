package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;

public class CargoArm
{

    TalonSRX moTalBallArm;

    VictorSPX moVicBallRoll;

    Encoder encCargoArm;

    Solenoid solHandExtend;
    Solenoid solArmBrake;

    DigitalInput limBallPresent;

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
    
    // in order to properly calculate the sin of the angle we need to know what angle we're starting at in the 0 position
    // in that position, the ENCODER value is 0, but we will adjust the angle value in here by this amount
    double ZERO_ANGLE = 21;


    public CargoArm()
    {
        moTalBallArm = new TalonSRX(2);
        

        moVicBallRoll = new VictorSPX(4);
        encCargoArm = new Encoder(moTalBallArm, 2);


        solHandExtend = new Solenoid(2);
        solArmBrake = new Solenoid(1);

        limBallPresent = new DigitalInput(1);

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
        //solArmBrake.set(true);
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
        
        double target = 200;
        
        if (encCargoArm.velocity() < (target - (target / 5)))
        {
            
            motorPower += 0.001;
            
        }
        else if (encCargoArm.velocity() > (target + (target / 5)))
        {
            motorPower -= 0.001;
        }
        
        rotateArm(motorPower);
        System.out.printf("Velocity: %f Motor Power: %f%n", encCargoArm.velocity(), motorPower);
        
    }

    public void toggleBrake()
    {
        solArmBrake.set(!solArmBrake.get());
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
        
        double armCurAngle = encCargoArm.angle() + ZERO_ANGLE;
        armCurAngle = Math.toRadians(armCurAngle);
        // this should be put in the Encoder code but it's here temporarily
        double armGoalAngle = armPositionTarget / 56.9 + ZERO_ANGLE;
        armGoalAngle = Math.toRadians(armGoalAngle);
        
        double distToTarget = armGoalAngle - armCurAngle;
        
        // old (theoretical)
        //double amtToMove = (Math.cos(curArmAngle) * GRAV_CONSTANT + (armPositionTarget - curArmAngle) / armPosRange) / (GRAV_CONSTANT + 1);
        // new
        // make sure Math.sin works in degrees
        //double amtToMove = -1 * ((Math.sin(armCurAngle) * GRAV_CONSTANT) - (armGoalAngle - armCurAngle) * (1 - GRAV_CONSTANT));
        
        
        // below explained:
        // -1 times the force of gravity at this angle minus the percentage of the distance we are to the target times a limiting factor (to ensure we never go beyond [-1.0, 1.0])
        // the latter part is multiplied by the direction in which we must move the arm (the sign of the distance we have to the target. positive = we must go up, negative = we must go down).
        
        double amtToMove = 0;
        if(armLockEnabled)
        {
            amtToMove = -1 * (Math.sin(armCurAngle) * GRAV_CONSTANT);


            // if we are moving positive and we're beyond 5 degrees, don't try to push further
            if(Math.signum(amtToMove) > 0)
            {
                if(armCurAngle >= 0.09)
                {
                    System.out.println("Angle is beyond 5 degrees and moving upward. No force will be applied.");
                    amtToMove = 0;
                }
            }
            // similarly, if we're moving negative and we're beyond -95 degrees, don't try to push further...?
            if(Math.signum(amtToMove) < 0)
            {
                if(armCurAngle <= -1.57)
                {
                    System.out.println("Angle is beyond -90 degrees and moving downward. No force will be applied.");
                    amtToMove = 0;
                }
            }
        }
        else
        {
            //System.out.println("Distance to target is: " + distToTarget);

            amtToMove =  -1 * ((Math.sin(armCurAngle) * GRAV_CONSTANT) - Math.signum(distToTarget) * (Math.toDegrees(distToTarget) / (armPosRange / 56.9)) * (1 - GRAV_CONSTANT));
            // if we are moving positive and we're beyond 5 degrees, don't try to push further
            if(Math.signum(distToTarget) > 0)
            {
                if(armCurAngle >= 0.09)
                {
                    System.out.println("Angle is beyond 5 degrees and moving upward. No force will be applied.");
                    amtToMove = 0;
                }
            }
            // similarly, if we're moving negative and we're beyond -95 degrees, don't try to push further...?
            if(Math.signum(distToTarget) < 0)
            {
                if(armCurAngle <= -1.57)
                {
                    System.out.println("Angle is beyond -90 degrees and moving downward. No force will be applied.");
                    amtToMove = 0;
                }
            }
        }
        // drewnote: if the above is too slow, you can adjust it by mulitplying the percentage check on the right by a factor.
        // you can determine the factor by finding the above equation's maximum peak value, which is dependent on the value of sin(a)*G at the arm's zero position
        
        
        // disabled for now (check calculations first)
        //return amtToMove;
        //System.out.println("" + armCurAngle + "," + GRAV_CONSTANT + "," + armGoalAngle + "," + armPosRange);
        
        return amtToMove;
        
        
        // check for cargo arm position
        // double curArmPos = encCargoArm.position();

        // double distToTarget = curArmPos - armGoalAngle;


        //double amtToMove = Math.signum(distToTarget) * (1 - ((0.5) * curArmPos / armPosRange));
        //double amtToMove = -0.25;

        // if(distToTarget < 0)
        // {
            // amtToMove *= -1;
        // }
        // if(Math.abs(distToTarget) > 1000)// && Math.signum())
        // {
            // amtToMove *= 2.5;
        // }

        // // System.out.println("Moving cargo arm by " + amtToMove);
        // // System.out.println("Distance to move: " + distToTarget);
        // // System.out.println("Current position: " + curArmPos);
        // // System.out.println("---");

        // // the encoder is only so accurate - even manually putting it at the zeroed position
        // // we can read a value from -20 to about +20.
        // // so let's not move the arm if we're "close enough" since we may be there already
        // if((curArmPos < -100) || (curArmPos > -100 && Math.signum(amtToMove) < 0) && (distToTarget > 25 || distToTarget < -25))
        // {
            // // move by the percentage of the way there we are
            // // ex. if we are at 0 and we need to be 100% of the way there, move at (100 - 0)% speed
            // // ex. if we are at 250 and we need to be at enc 500, move at (100 - 50)% speed...?
            // // and multiply by the sign of the distance i think.

            // // compensate for gravity by moving 2/3s as much downward than upward
            // if(curArmPos < -250 && Math.signum(amtToMove) < 0)
            // {
                // amtToMove *= 0.67;
            // }

            // return amtToMove;
        // }
        // else
        // {
            // return 0;
        // }

    }

    public void requestMove(double amt)
    {
        requestedMove = amt;
    }

    public void manuallyRotateArm(double amt)
    {
        
        double armCurAngle = encCargoArm.angle() + ZERO_ANGLE;
        armCurAngle = Math.toRadians(armCurAngle);
        
        if(amt != 0)
        {
            moTalBallArm.set(ControlMode.PercentOutput, amt + -1 * Math.sin(armCurAngle) * GRAV_CONSTANT);
            
            //System.out.println("Rotating arm.");
        }
        else
        {
            //moTalBallArm.set(ControlMode.PercentOutput, amt);

            if(!solArmBrake.get())
            {
                //System.out.println("Engaging brake.");
            }
        }
        //armLockEnabled = true;
    }

    public void rotateArm(double amt)
    {
        
        //moTalBallArm.set(ControlMode.PercentOutput, amt);
        // prevent arm from going too low unless its arm is pulled in ("not extended")
        // prevent arm from going too low in general (i.e. smashing the hand into the metal frame)
        // these checks only apply when the arm is moving down
        // note: cannot put in similar check for upward movement against the Zeroing Bar
        // b/c the arm cannot be moved without motor power (too strong)
        // preventing reach of the zero bar with necessary tolerance will prevent us
        // from actually zeroing ever
        if((encCargoArm.position() < -6800 && amt < 0) || (encCargoArm.position() < -6500 && handIsExtended && amt < 0))
        {
            System.out.println("Arm not moving to prevent crushing!");
            moTalBallArm.set(ControlMode.PercentOutput, 0);
        }
        else
        {
            moTalBallArm.set(ControlMode.PercentOutput, amt);
        }
    }

    // snap cargo arm to set positions
    // the actual motor movement takes place in periodic()
    public void setArmDown()
    {
        armLockEnabled = false;
        // set hand to some known position that means "down"
        System.out.println("Setting cargo arm position to 'down'");
        armPositionTarget = armPositions[armPosDown];
    }
    
    public void setArmLow()
    {
        armLockEnabled = false;
        System.out.println("Setting cargo arm position to 'low'");
        armPositionTarget = armPositions[armPosLow];
    }
    
    public void setArmMid()
    {
        armLockEnabled = false;
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
        armLockEnabled = false;
        System.out.println("Setting cargo arm position to 'up'");
        armPositionTarget = armPositions[armPosUp];
    }

    public void spinBallMotor(double amt)
    {
        // if(amt < 0.25)
        // {
        //     System.out.println("Ball is being pulled...? Amt is pos");
        // }
        // if(!limBallPresent.get())
        // {
        //     System.out.println("Limit switch pulled.");
        // }

        // if the above is true, then you don't need to modify this
        // otherwise you might need to reverse it
        if(amt > 0.25)
        {
            // if we have a ball present AND we're pulling inward, stop pulling!
            if(!limBallPresent.get())
            {
                amt = 0;
            }
        }

        
        moVicBallRoll.set(ControlMode.PercentOutput, amt);
    }

    public void toggleArmLock()
    {
        armLockEnabled = !armLockEnabled;
        System.out.println("Cargo arm lock set to: " + armLockEnabled);
        // if(armLockEnabled)
        // {
        //     armPositionTarget = encCargoArm.position();
        //     System.out.println("Locking cargo arm at: " + armPositionTarget);
        // }
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
