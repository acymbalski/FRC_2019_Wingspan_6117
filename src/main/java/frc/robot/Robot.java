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
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.I2C;

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
  private static final int intDriver1Port = 0;
  private static final int intDriver2Port = 1;

  // joysticks
  private Joystick joyDriver1, joyDriver2;

  // drewnote: This will alter all speeds!
  // typically we will set it to half to be safe until we're all set
  public double speedModifier = 0.5;

  // USB cameras
  UsbCamera camFront, camBack;
  VideoSink camServ;

  // gyro
  private AHRS gyro;

  private double yaw;
  private double pitch;
  private double roll;

  private double velX;
  private double velY;
  private double velZ;

  DriveTrain driveTrain;
  CargoArm cargoArm;
  HatchArm hatchArm;

  Buttons buttons;


  // drewnote: used for debug
  // has a method to spin all motors individually to check who is who
  //RobotConfigurator configgy;

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
    camServ = CameraServer.getInstance().getServer();

    // init joysticks
    joyDriver1 = new Joystick(intDriver1Port);
    joyDriver2 = new Joystick(intDriver2Port);

    driveTrain = new DriveTrain();
    hatchArm = new HatchArm();
    cargoArm = new CargoArm();

    buttons = new Buttons();

    //configgy = new RobotConfigurator();

    // init gyro
    try
    {
      gyro = new AHRS(I2C.Port.kOnboard);
    }
    catch (RuntimeException ex)
    {
      // DriverStation.reportError("Error instantiating navX-MXP: " + ex.getMessage(),
      // true);
      System.out.println("Error initializing gyro!");
    }

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


    System.out.println("X channel: " + joyDriver1.getXChannel());
    System.out.println("Y channel: " + joyDriver1.getYChannel());
    
    System.out.println(joyDriver1.getAxisCount());

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

    
    updateShuffleboard();

    // configgy.discoverMotors();
    // configgy.discoverPneumatics();

    System.out.println("Teleop initialization complete.");
  }

  /**
   * This function is called periodically during teleoperated mode.
   */
  @Override
  public void teleopPeriodic()
  {
    // drive robot
    double leftJoystick = joyDriver1.getRawAxis(buttons.LAxisUD);
    double rightJoystick = joyDriver1.getRawAxis(buttons.RAxisUD);

    driveTrain.drive(leftJoystick * speedModifier, rightJoystick * speedModifier);

    // control hatch arm
    if(joyDriver1.getRawButtonPressed(buttons.A))
    {
      hatchArm.togglePistons();
    }

    // open/close hatch grabber arms
    if(joyDriver1.getRawButtonPressed(buttons.B))
    {
      hatchArm.toggleGrabber();
    }

    // flip drive orientation
    if(joyDriver1.getRawButtonPressed(buttons.Select))
    {
      driveTrain.flip_orientation();
    }

    // button 7: L2
    // button 8: R2
    // L2 will reverse the finger
    // R2 will rotate it forward
    if(joyDriver1.getRawButton(buttons.L2))
    {
      hatchArm.rotateFinger(-1 * speedModifier);
    }
    else
    {
      if(joyDriver1.getRawButton(buttons.R2))
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
    if(joyDriver1.getRawButton(buttons.L1))
    {
      hatchArm.rotateArm(-1 * speedModifier * 0.75);
    }
    else
    {
      if(joyDriver1.getRawButton(buttons.R1))
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
    if(joyDriver1.getRawButton(buttons.X))
    {
      cargoArm.rotateArm(-1 * speedModifier);
    }
    else
    {
      if(joyDriver1.getRawButton(buttons.Y))
      {
        cargoArm.rotateArm(1 * speedModifier);
      }
      else
      {
        cargoArm.rotateArm(0);
      }
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
    yaw = gyro.getYaw();
    pitch = gyro.getPitch();
    roll = gyro.getRoll();

    velX = gyro.getVelocityX();
    velY = gyro.getVelocityY();
    velZ = gyro.getVelocityZ();

    // adjust for range 0 - 360
    yaw += 180;
    pitch += 180;
    roll += 180;

  }

  private void updateShuffleboard()
  {

    // update gyro info on smart dashboard
    SmartDashboard.putNumber("X angle", yaw);//gyro.getYaw() + 180);
    SmartDashboard.putNumber("Y angle", pitch);//gyro.getPitch() + 180);
    SmartDashboard.putNumber("Z angle", roll);//gyro.getRoll() + 180);
    SmartDashboard.putNumber("X vel", velX);//gyro.getVelocityX());
    SmartDashboard.putNumber("Y vel", velY);//gyro.getVelocityY());
    SmartDashboard.putNumber("Z vel", velZ);//gyro.getVelocityZ());
    SmartDashboard.putData(gyro);
  }
}
