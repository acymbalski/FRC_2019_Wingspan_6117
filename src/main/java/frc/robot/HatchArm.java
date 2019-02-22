package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

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
    }

    public HatchArm(int pcmPushPort, int canPushPort, int pcmRetractPort, int canRetractPort)
    {
        solHatchPush = new Solenoid(canPushPort, pcmPushPort);
        solHatchRetract = new Solenoid(canRetractPort, pcmRetractPort);
        
        retractPistons();
        pistonsOut = false;
    }

    public void raiseArm()
    {
        // raise arm
        // when arm is at top, move finger to hold

        // when finger is holding (for sure), open arm (this step maybe should go to "release hatch")

    }

    public void openGrabber()
    {
        solHatchClose.set(false);
        solHatchOpen.set(true);
    }

    public void closeGrabber()
    {
        solHatchOpen.set(false);
        solHatchClose.set(true);
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

    public void retractPistons()
    {
        solHatchPush.set(false);
        solHatchRetract.set(true);
        pistonsOut = true;
    }

    public void togglePistons()
    {
        solHatchPush.set(!pistonsOut);
        solHatchRetract.set(pistonsOut);
        pistonsOut = !pistonsOut;
    }

    /**
	 * Rotates the hatch panel holding finger.
	 * @param amt The speed to rotate with
	 */
    public void rotateFinger(double amt)
    {
        moVicHatFin.set(ControlMode.PercentOutput, amt);
    }

    
    /**
	 * Rotates the hatch panel grabbing arm.
	 * @param amt The speed to rotate with
	 */
    public void rotateArm(double amt)
    {
        moVicHatArm.set(ControlMode.PercentOutput, amt);
    }

}
