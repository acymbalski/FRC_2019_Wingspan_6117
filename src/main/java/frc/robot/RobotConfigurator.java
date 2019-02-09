package frc.robot;

import static org.junit.Assume.assumeTrue;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.wpilibj.Joystick;

public class RobotConfigurator
{

    // our robot has 3 talons, 4 victors
    private TalonSRX[] talons;
    private VictorSPX[] victors;

    // get controller (used to iterate motors/cancel motor config)
    private Joystick playerJoystick;

    // amount to spin motors
    private double motorSpinAmount = 0.25;

    // index of current motor being spun
    private int motor_index = 0;

    public RobotConfigurator()
    {

        talons = new TalonSRX[3];
        victors = new VictorSPX[4];


        talons[0] = new TalonSRX(0);
        talons[1] = new TalonSRX(1);
        talons[2] = new TalonSRX(2);

        victors[0] = new VictorSPX(2);
        victors[1] = new VictorSPX(3);
        victors[2] = new VictorSPX(4);
        victors[3] = new VictorSPX(5);

        playerJoystick = new Joystick(0);

    }

    public void discoverMotors()
    {
        int motor_index = 0;

        System.out.println();
        System.out.println("-------------------");
        System.out.println("Discovering motors.");
        System.out.println("Press 'A' to cycle between motors at +" + motorSpinAmount);
        System.out.println("Press 'B' to exit.");
        System.out.println("Take note of which motor is spinning, and in what direction.");
        System.out.println("If a motor is not spinning, verify that the Motor Controller (Victor/Talon)");
        System.out.println("is displaying lights (either green for a positive spin or red for a negative");
        System.out.println("If lights are displayed but motor is not spinning, the motor is the issue.");
        System.out.println("If lights are blinking or otherwise not solid, the CAN address is likely wrong.");
        System.out.println("-------------------");
        System.out.println();

        if(motor_index < 3)
        {
            System.out.println("Spinning Talon " + motor_index);
        }
        else
        {
            System.out.println("Spinning Victor " + (motor_index - 3));
        }

        while(true)
        {
            switch(motor_index)
            {
                case 0:
                case 1:
                case 2:
                    talons[motor_index].set(ControlMode.PercentOutput, motorSpinAmount);
                    break;
                case 3:
                case 4:
                case 5:
                case 6:
                    victors[motor_index - 3].set(ControlMode.PercentOutput, motorSpinAmount);
                    break;
            }

            if(playerJoystick.getRawButtonPressed(1))
            {
                motor_index++;
                if(motor_index > 6)
                {
                    motor_index = 0;
                }

                System.out.println("Button 0 pressed. Iterating to motor_index " + motor_index);
                if(motor_index < 3)
                {
                    System.out.println("Spinning Talon " + motor_index);
                }
                else
                {
                    System.out.println("Spinning Victor " + (motor_index - 3));
                }
            }
            if(playerJoystick.getRawButtonPressed(2))
            {
                System.out.println("Button 1 pressed. Exiting discoverMotors...");
                return;
            }
        }


    }

}
