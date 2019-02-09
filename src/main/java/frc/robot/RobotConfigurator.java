package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;

public class RobotConfigurator
{

    // our robot has 3 talons, 5 victors
    private TalonSRX[] talons;
    private VictorSPX[] victors;

    // get controller (used to iterate motors/cancel motor config)
    private Joystick playerJoystick;

    // amount to spin motors
    private double motorSpinAmount = 0.25;

    // index of current motor being spun
    private int motor_index = 0;
    private int pneu_index = 0;

    private Solenoid[] solenoids;

    public RobotConfigurator()
    {

        talons = new TalonSRX[3];
        victors = new VictorSPX[5];


        talons[0] = new TalonSRX(0);
        talons[1] = new TalonSRX(1);
        talons[2] = new TalonSRX(2);

        victors[0] = new VictorSPX(3);
        victors[1] = new VictorSPX(4);
        victors[2] = new VictorSPX(5);
        victors[3] = new VictorSPX(6);
        victors[4] = new VictorSPX(7);

        
        // solenoids will go in their respective class (CargoArm or HatchArm)
        // we will likely have the solenoid controlled with two routes
        // they are controlled from PWM channels 0 and 1, CAN channel 0
        solenoids = new Solenoid[7];// = new Solenoid(0 /* can channel */, 0 /*PWM channel*/);

        for(int i = 0; i < solenoids.length; i++)
        {
            solenoids[i] = new Solenoid(0 /* can channel */, i /*PWM channel*/);
        }


        playerJoystick = new Joystick(0);

    }

    public void discoverMotors()
    {
        motor_index = 0;

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
                case 7:
                    victors[motor_index - 3].set(ControlMode.PercentOutput, motorSpinAmount);
                    break;
            }

            if(playerJoystick.getRawButtonPressed(1))
            {
                motor_index++;
                if(motor_index > 7)
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

                // stop all motors
                stop();
            }
            if(playerJoystick.getRawButtonPressed(2))
            {
                System.out.println("Button 1 pressed. Exiting discoverMotors...");
                stop();
                return;
            }
        }


    }

    
    public void discoverPneumatics()
    {
        pneu_index = 0;

        System.out.println();
        System.out.println("-------------------");
        System.out.println("Discovering pneumatics.");
        System.out.println("Press 'A' to cycle between pneumatics");// at +" + motorSpinAmount);
        System.out.println("Press 'B' to exit.");
        System.out.println("-------------------");
        System.out.println();
        System.out.println("Firing pneumatic #" + pneu_index);

        while(true)
        {
            
            solenoids[pneu_index].set(true);

            if(playerJoystick.getRawButtonPressed(1))
            {
                // turn off old solenoid
                solenoids[pneu_index].set(false);

                pneu_index++;
                if(pneu_index >= solenoids.length)
                {
                    pneu_index = 0;
                }

                System.out.println("Button 0 pressed. Iterating to pneu_index " + pneu_index);

                System.out.println("Solenoid setting to true: " + pneu_index);
                

            }
            if(playerJoystick.getRawButtonPressed(2))
            {
                System.out.println("Button 1 pressed. Exiting discoverPneumatics...");
                return;
            }
        }


    }

    private void stop()
    {
        
        // stop all motors
        for(int i = 0; i < talons.length; i++)
        {
            talons[i].set(ControlMode.PercentOutput, 0);
        }
        for(int i = 0; i < victors.length; i++)
        {
            victors[i].set(ControlMode.PercentOutput, 0);
        }
    }

}
