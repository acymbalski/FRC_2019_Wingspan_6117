package frc.robot;

// this is to simplify button press mappings

// 2-11-2019 I think the right way to make sure the controls are mapped correctly
// is to utilize the XInput switch on the back of ONE the controllers
// b/c it will add extra axes by changing L2/R2 from buttons to axes
// so if we check a controller's axis count or button count... we'll know
// TODO

// this class should maybe also handle the buttonPress stuff too
// TODO

public class Buttons
{

    public final int X = 1;
    public final int A = 2;
    public final int B = 3;
    public final int Y = 4;
    public final int L1 = 5;
    public final int L2 = 7;
    public final int LThumb = 11;
    public final int R1 = 6;
    public final int R2 = 8;
    public final int RThumb = 12;
    public final int Select = 9;
    public final int Start = 10;
    
    public final int LAxisUD = 1;
    public final int LAxisLR = 0;
    public final int RAxisUD = 3;
    public final int RAxisLR = 2;


    public Buttons()
    {

    }
}