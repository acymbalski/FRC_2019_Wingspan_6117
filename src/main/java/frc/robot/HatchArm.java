package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;

public class HatchArm
{

    // solenoids to control the pushing pistons
    Solenoid solHatchPush;
    Solenoid solHatchRetract;

    // solenoids to control the hatch grabber
    Solenoid solHatchOpen;
    Solenoid solHatchClose;

    // hatch pinching finger
    VictorSPX moVicHatFin;

    // hatch arm
    VictorSPX moVicHatArm;

    public boolean pistonsOut;
    public boolean grabberOpen;

    //DigitalInput limFingerUp;
    Counter fingerCounter;
    int dirMoving = 0;
    int fingerEncCount = 0;
    int finger_target = 0; 
    int FINGER_UP_ENC = -100; // broad assumption! check later.
    int FINGER_GRAB_ENC = -40; // broad assumption! check later.
    int ENC_TOLERANCE = 1;//5;
    Boolean finger_moving = false;
    int dirToMove = 0;

    public HatchArm()
    {
        solHatchPush = new Solenoid(0, 5);
        solHatchRetract = new Solenoid(0, 4);

        solHatchOpen = new Solenoid(0, 3);
        solHatchClose = new Solenoid(0, 6);

        moVicHatFin = new VictorSPX(5);
        moVicHatArm = new VictorSPX(6);

        retractPistons();
        pistonsOut = false;
        grabberOpen = false;

        //limFingerUp = new DigitalInput(3);
        fingerCounter = new Counter(new DigitalInput(3));
        fingerEncCount = 0;
        finger_moving = false;
        finger_target = 0;
    }

    public HatchArm(int pcmPushPort, int canPushPort, int pcmRetractPort, int canRetractPort)
    {
        solHatchPush = new Solenoid(canPushPort, pcmPushPort);
        solHatchRetract = new Solenoid(canRetractPort, pcmRetractPort);
        
        retractPistons();
        pistonsOut = false;
        
        //limFingerUp = new DigitalInput(3);
        fingerCounter = new Counter(new DigitalInput(3));
        fingerEncCount = 0;
        finger_moving = false;
        finger_target = 0;
    }

    public void closeGrabber()
    {
        solHatchOpen.set(false);
        solHatchClose.set(true);
    }

    public void openGrabber()
    {
        solHatchClose.set(false);
        solHatchOpen.set(true);
    }

    public int fingerEncValue()
    {
        return fingerEncCount;
    }

    public void zero()
    {
        fingerEncCount = 0;
    }

    /**
	 * Pushes the hatch panel off.
	 */
    public void pushPanel()
    {
        // open arm
        //TODO

        // push hatch panel
        pushPistons();

        // this won't work... something has to go in between here... right?
        // otherwise you'll slam the panel on the finger?
        //TODO?
    }

    public void pushPistons()
    {
        solHatchPush.set(true);
        solHatchRetract.set(false);
        pistonsOut = false;
    }

    public void raiseArm()
    {
        // raise arm
        // when arm is at top, move finger to hold

        // when finger is holding (for sure), open arm (this step maybe should go to "release hatch")

    }

    public void retractPistons()
    {
        solHatchPush.set(false);
        solHatchRetract.set(true);
        pistonsOut = true;
    }
    
    /**
	 * Rotates the hatch panel grabbing arm.
	 * @param amt The speed to rotate with
	 */
    public void rotateArm(double amt)
    {
        moVicHatArm.set(ControlMode.PercentOutput, amt);
    }

    /**
	 * Rotates the hatch panel holding finger.
	 * @param amt The speed to rotate with
	 */
    public void rotateFinger(double amt)
    {
        moVicHatFin.set(ControlMode.PercentOutput, amt);


        if(amt == 0)
        {
            dirMoving = 0;
        }
        if(amt > 0)
        {
            dirMoving = 1;
        }
        if(amt < 0)
        {
            dirMoving = -1;
        }
        //dirMoving = amt < 0 ? -1 : 1;//(int) Math.signum((float) amt);

    }

    public void fingerUp()
    {
        //dirToMove = -1;
        finger_moving = true;
        finger_target = FINGER_UP_ENC;
    }
    public void fingerGrab()
    {
        //dirToMove = 1;
        finger_moving = true;
        finger_target = FINGER_GRAB_ENC;
    }

    public void periodic()
    {
        //moVicHatFin.set(ControlMode.PercentOutput, dirToMove);
        //rotateFinger(dirToMove);
        System.out.println("Finger at: " + fingerEncCount);

        if(dirMoving > 0)
        {
            System.out.println("Finger moving POSITIVE");
            fingerEncCount += fingerCounter.get();
            fingerCounter.reset();
        }
        if(dirMoving < 0)
        {
            System.out.println("Finger moving NEGATIVE");
            fingerEncCount -= fingerCounter.get();
            fingerCounter.reset();
        }

        if(finger_moving)
        {
            // if we are at our target, we're good!
            if(Math.abs(fingerEncCount - finger_target) <= ENC_TOLERANCE)
            {
                System.out.println("finger at target!");
                rotateFinger(0);
                finger_target = 0;
                finger_moving = false;
            }
            // else, move towards target
            else
            {
                if(fingerEncCount > finger_target)
                {
                    
                    System.out.println("Finger rotating NEGATIVE toward target...");
                    // fingerEncCount -= fingerCounter.get();
                    // fingerCounter.reset();
                    
            fingerEncCount -= fingerCounter.get();
            fingerCounter.reset();
                    rotateFinger(-1);
                }
                else
                {
                    System.out.println("Finger rotating POSITIVE toward target...");
                    
            fingerEncCount += fingerCounter.get();
            fingerCounter.reset();
                    rotateFinger(1);
                }
            }
        }
        // // if finger is Up, zero & stop
        // if(fingerEncCount <= 10)
        // {
        //     //fingerEncCount = 0;
        //     dirToMove = 0;
        // }
        // // if encoder is at Down, stop
        // else
        // {
        //     if(fingerEncCount <= FINGER_GRAB_ENC + ENC_TOLERANCE && fingerEncCount >= FINGER_GRAB_ENC - ENC_TOLERANCE)
        //     {
        //         dirToMove = 0;
        //     }
        // }
    }

    public void toggleGrabber()
    {
        System.out.println("Toggling grabber to: " + grabberOpen);
        if(grabberOpen)
        {
            closeGrabber();
        }
        else
        {
            openGrabber();
        }
        grabberOpen = !grabberOpen;
    }

    public void togglePistons()
    {
        solHatchPush.set(!pistonsOut);
        solHatchRetract.set(pistonsOut);
        pistonsOut = !pistonsOut;
    }

}
