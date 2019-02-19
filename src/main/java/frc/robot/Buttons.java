package frc.robot;

import edu.wpi.first.wpilibj.Joystick;

// this is to simplify button press mappings

public class Buttons
{

    Joystick joystick;

    public int X = 1;
    public int A = 2;
    public int B = 3;
    public int Y = 4;
    public int L1 = 5;
    public int L2 = 7;
    public int LThumb = 11;
    public int R1 = 6;
    public int R2 = 8;
    public int RThumb = 12;
    public int Select = 9;
    public int Start = 10;
    
    public int LAxisUD = 1;
    public int LAxisLR = 0;
    public int RAxisUD = 3;
    public int RAxisLR = 2;

    public int Up = 0;
    public int Down = 180;
    public int Left = 270;
    public int Right = 90;

    public Boolean isXInput = false;


    public Buttons(Joystick stick)
    {
        joystick = stick;
        try
        {
            if(joystick.getAxisCount() == 6)
            {
                isXInput = true;

                // 6 axis xinput controller
                A = 1;
                B = 2;
                X = 3;
                Y = 4;
                L1 = 5;
                R1 = 6;
                Select = 7;
                Start = 8;
                LThumb = 9;
                RThumb = 10;

                LAxisUD = 1;
                LAxisLR = 0;
                RAxisUD = 5;
                RAxisLR = 4;

                // remember that these are axes on the driver 1 controller, not buttons
                L2 = 2;
                R2 = 3;
            }
        }
        catch(Exception e)
        {
            System.out.println("Controller not connected!");
        }
    }

    public Boolean pressed(int btn)
    {
        try
        {
            if(isXInput && (btn == L2 || btn == R2))
            {
                return joystick.getRawAxis(btn) > 0.75;
            }
            else
            {
                return joystick.getRawButtonPressed(btn);
            }
        }
        catch(Exception e)
        {
            // controller not connected

            return false;
        }
    }

    public Boolean released(int btn)
    {

        try
        {
            if(isXInput && (btn == L2 || btn == R2))
            {
                return joystick.getRawAxis(btn) < 0.75;
            }
            else
            {
                return joystick.getRawButtonReleased(btn);
            }
        }
        catch(Exception e)
        {
            // controller not connected

            return false;
        }
    }

    public double getAxis(int axis)
    {
        try
        {
            return joystick.getRawAxis(axis);
        }
        catch(Exception e)
        {
            return 0;
        }
    }

    public Boolean down(int btn)
    {
        return joystick.getRawButton(btn);
    }

    public Boolean dpad(int dir)
    {
        return joystick.getPOV(0) == dir;
    }
}
