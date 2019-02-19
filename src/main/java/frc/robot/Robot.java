/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

// robot
import edu.wpi.first.wpilibj.TimedRobot;

// joystick
import edu.wpi.first.wpilibj.Joystick;

// smart dashboard
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.shuffleboard.*;

// cameras
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSink;
import edu.wpi.first.cameraserver.CameraServer;

// gyro
// import com.kauailabs.navx.frc.AHRS;
// import edu.wpi.first.wpilibj.I2C;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends TimedRobot
{

  // joystick ports
  private int joystickPorts = 2;

  // drewnote: This will alter all speeds!
  // typically we will set it to half to be safe until we're all set
  public double speedModifier = 0.5;

  // USB cameras
  UsbCamera camFront, camBack;
  VideoSink camServForward, camServReverse;

  // gyro
  // private AHRS gyro;

  // private double yaw;
  // private double pitch;
  // private double roll;

  // private double velX;
  // private double velY;
  // private double velZ;

  DriveTrain driveTrain;
  CargoArm cargoArm;
  HatchArm hatchArm;
  Ramp ramp;

  Buttons driver1, driver2;

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */
  @Override
  public void robotInit() {
    System.out.println("RoboRIO initializing...");

    // initialize cameras
    camFront = CameraServer.getInstance().startAutomaticCapture(0);
    camBack = CameraServer.getInstance().startAutomaticCapture(1);
    camServForward = CameraServer.getInstance().getServer();
    camServReverse = CameraServer.getInstance().getServer();

    camServForward.setSource(camFront);
    //camServReverse.setSource(camBack);

    // init joysticks
    Joystick[] joysticks = getControllers();

    driveTrain = new DriveTrain();
    hatchArm = new HatchArm();
    cargoArm = new CargoArm();
    ramp = new Ramp();

    driver1 = new Buttons(joysticks[0]);
    driver2 = new Buttons(joysticks[1]);

    // init gyro
    // try
    // {
    //   gyro = new AHRS(I2C.Port.kOnboard);
    // }
    // catch (RuntimeException ex)
    // {
    //   // DriverStation.reportError("Error instantiating navX-MXP: " + ex.getMessage(),
    //   // true);
    //   System.out.println("Error initializing gyro!");
    // }

    // // update gyro info on smart dashboard
    // SmartDashboard.putNumber("X angle", gyro.getYaw() + 180);
    // SmartDashboard.putNumber("Y angle", gyro.getPitch() + 180);
    // SmartDashboard.putNumber("Z angle", gyro.getRoll() + 180);
    
    // SmartDashboard.putNumber("X vel", gyro.getVelocityX());
    // SmartDashboard.putNumber("Y vel", gyro.getVelocityY());
    // SmartDashboard.putNumber("Z vel", gyro.getVelocityZ());

    // SmartDashboard.putData(gyro);
    updateVars();
    updateShuffleboard();

    System.out.println("RoboRIO initialization complete.");
  }

  /**
   * This function is run once each time the robot enters autonomous mode.
   */
  @Override
  public void autonomousInit()
  {
    System.out.println("Initializing autonomous mode...");

    driveTrain.stop();

    System.out.println("Autonomous initialization complete.");
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic()
  {

  }

  /**
   * This function is called once each time the robot enters teleoperated mode.
   */
  @Override
  public void teleopInit()
  {
    System.out.println("Initializing teleop mode...");


    driveTrain.stop();
    System.out.println("Motors stopped.");
    driveTrain.init();
    System.out.println("Drive Train encoders zeroed.");

    
    updateShuffleboard();

    cargoArm.init();

    System.out.println("Teleop initialization complete.");
  }

  /**
   * This function is called periodically during teleoperated mode.
   */
  @Override
  public void teleopPeriodic()
  {

    // debug for now
    cargoArm.periodic();
    driveTrain.periodic();
 
    // drive robot
    double d1LeftJoystick = driver1.getAxis(driver1.LAxisUD);
    double d1RightJoystick = driver1.getAxis(driver1.RAxisUD);

    double d2LeftJoystick = driver2.getAxis(driver2.LAxisUD);
    double d2RightJoystick = driver2.getAxis(driver2.RAxisUD);


    // -------------------- DRIVER 1


    driveTrain.drive(d1LeftJoystick * speedModifier, d1RightJoystick * speedModifier);

    // driver1 controls ramp
    // press start and select together to deploy
    if(driver1.pressed(driver1.Start) && driver1.pressed(driver1.Select))
    {
      ramp.deploy();
    }

    // driver 1 can double speed by holding L2
    //driveTrain.fastSpeed = driver1.down(driver1.L2);
    if(driver1.pressed(driver1.L2))
    {
      driveTrain.fastSpeed = !driveTrain.fastSpeed;
      System.out.println("Fast speed toggled to: " + driveTrain.fastSpeed);
    }

    if(driver1.down(driver1.L1))
    {
      driveTrain.set_right_motors(speedModifier);
    }
    if(driver1.down(driver1.R1))
    {
      driveTrain.set_left_motors(speedModifier);
    }

    // flip drive orientation
    if(driver1.pressed(driver1.Select))
    {
      driveTrain.flip_orientation();
      
      if(driveTrain.isFacingForward())
      {
        camServForward.setSource(camFront);
        //camServReverse.setSource(camBack);
        //     camBack.free();
        //     camFront.close();
        // camFront = CameraServer.getInstance().startAutomaticCapture(0);
        // camBack = CameraServer.getInstance().startAutomaticCapture(1);
      }
      else
      {
        camServForward.setSource(camBack);
        //camServReverse.setSource(camFront);
        //     camBack.close();
        //     camFront.close();
        // camFront = CameraServer.getInstance().startAutomaticCapture(1);
        // camBack = CameraServer.getInstance().startAutomaticCapture(0);
      }
    }


    // -------------------- DRIVER 2


    // up is out
    // down is in
    // (when it's negated)
    cargoArm.spinBallMotor(Math.round(d2LeftJoystick));

    // control hatch arm
    if(driver2.pressed(driver2.A))
    {
      hatchArm.togglePistons();
    }

    // open/close hatch grabber arms
    if(driver2.pressed(driver2.B))
    {
      hatchArm.toggleGrabber();
    }

    if(driver2.pressed(driver2.Select))
    {
      cargoArm.toggleArmLock();
    }

    // extend/de-extend cargo hand
    if(driver2.pressed(driver2.Start))
    {
      cargoArm.toggleHand();
    }

    // button 7: L2
    // button 8: R2
    // L2 will reverse the finger
    // R2 will rotate it forward
    if(driver2.down(driver2.L2))
    {
      hatchArm.rotateFinger(-1 * speedModifier);
    }
    else
    {
      if(driver2.down(driver2.R2))
      {
        hatchArm.rotateFinger(1 * speedModifier);
      }
      else
      {
        hatchArm.rotateFinger(0);
      }
    }

    
    // button 5: L1
    // button 6: R1
    // L1 will move the hatch arm one way
    // R1 will move it the other way
    if(driver2.down(driver2.L1))
    {
      hatchArm.rotateArm(-1 * speedModifier * 0.75);
    }
    else
    {
      if(driver2.down(driver2.R1))
      {
        hatchArm.rotateArm(1 * speedModifier * 0.75);
      }
      else
      {
        hatchArm.rotateArm(0);
      }
    }

    
    // button 1: x
    // button 4: y
    // button 1 will move the ball arm one way
    // button 4 will move it the other way
    if(driver2.down(driver2.X))
    {
      cargoArm.requestMove(-1 * speedModifier);
    }
    else
    {
      if(driver2.down(driver2.Y))
      {
        cargoArm.requestMove(1 * speedModifier);
      }
      else
      {
        //cargoArm.rotateArm(0);
      }
    }
    if(driver2.released(driver2.X) || driver2.released(driver2.Y))
    {
      cargoArm.setArmTarget(cargoArm.currentPosition());
    }

    if(driver2.dpad(driver2.Up))
    {
      cargoArm.setArmUp();
    }
    if(driver2.dpad(driver2.Down))
    {
      cargoArm.setArmDown();
    }
    if(driver2.dpad(driver2.Left))
    {
      cargoArm.setArmMid();
    }
    if(driver2.dpad(driver2.Right))
    {
      cargoArm.setArmLow();
    }

  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic()
  {

  }

  private void updateVars()
  {
    // yaw = gyro.getYaw();
    // pitch = gyro.getPitch();
    // roll = gyro.getRoll();

    // velX = gyro.getVelocityX();
    // velY = gyro.getVelocityY();
    // velZ = gyro.getVelocityZ();

    // // adjust for range 0 - 360
    // yaw += 180;
    // pitch += 180;
    // roll += 180;

  }

  private void updateShuffleboard()
  {

    // update gyro info on smart dashboard
    // SmartDashboard.putNumber("X angle", yaw);//gyro.getYaw() + 180);
    // SmartDashboard.putNumber("Y angle", pitch);//gyro.getPitch() + 180);
    // SmartDashboard.putNumber("Z angle", roll);//gyro.getRoll() + 180);
    // SmartDashboard.putNumber("X vel", velX);//gyro.getVelocityX());
    // SmartDashboard.putNumber("Y vel", velY);//gyro.getVelocityY());
    // SmartDashboard.putNumber("Z vel", velZ);//gyro.getVelocityZ());
    // SmartDashboard.putData(gyro);
  }

  private Joystick[] getControllers()
  {
    Joystick[] joysticks = new Joystick[joystickPorts];


    Joystick tmpJoystick;
    for(int i = 0; i < joystickPorts; i++)
    {
      try
      {
        tmpJoystick = new Joystick(i);

        if(tmpJoystick.getAxisCount() == 6)
        {
          System.out.println("Found 6 axis joystick in port " + i + ".");
          System.out.println("Setting to driver 1...");
          joysticks[0] = tmpJoystick;
        }
        else
        {
          System.out.println("Found 4 axis joystick in port " + i + ".");
          System.out.println("Setting to driver 2...");
          joysticks[1] = tmpJoystick;
        }
      }
      catch(Exception e)
      {
        System.out.println("No joystick found in port " + i + "...");
      }
    }

    return joysticks;
  }
}
