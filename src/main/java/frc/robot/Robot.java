/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

// robot
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
// air compressor
import edu.wpi.first.wpilibj.Compressor;

// joystick
import edu.wpi.first.wpilibj.Joystick;

// smart dashboard
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
//import edu.wpi.first.wpilibj.smartdashboard.gui;
import edu.wpi.first.wpilibj.shuffleboard.*;

// cameras
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoSink;
import edu.wpi.first.cameraserver.CameraServer;

// CAN support
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

// PWM motors
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.SpeedController;

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
public class Robot extends TimedRobot {

  // this will allow us to switch autonomous modes without a code change
  // private static final String autLeft = "Left";
  // private static final String autCenter = "Center";
  // private static final String autRight = "Right";
  // private final SendableChooser<String> auton_mode = new SendableChooser<>();

  // joystick ports
  private static final int intDriver1Port = 0;
  private static final int intDriver2Port = 1;

  // joysticks
  private Joystick joyDriver1, joyDriver2;

  // PWM motor ports
  private static final int motorPortPwm0 = 0;
  private static final int motorPortPwm1 = 1;

  // PWM motors
  private SpeedController motorPwm0, motorPwm1;


  private VictorSPX victor4, victor5, victor6, victor7;


  // air compressor
  //private Compressor airCompressor0;

  // USB cameras
  UsbCamera camFront, camBack;
  VideoSink camServ;

  // gyro
  private AHRS gyro;

  private Boolean drive_tank = true;
  private Boolean turning_right = false;
  private Boolean turning_left = false;
  private double turning_init_x;

  private double yaw;
  private double pitch;
  private double roll;

  private double velX;
  private double velY;
  private double velZ;

  private Solenoid solen;
  boolean sol = false;


  DriveTrain driveTrain;
  CargoArm cargoArm;
  HatchArm hatchArm;


  // drewnote: used for debug
  // has a method to spin all motors individually to check who is who
  RobotConfigurator configgy;

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

    // init motors: PWM
    motorPwm0 = new PWMVictorSPX(2);
    motorPwm1 = new PWMVictorSPX(3);
    
    driveTrain = new DriveTrain();
    hatchArm = new HatchArm();
    cargoArm = new CargoArm();

    victor4 = new VictorSPX(4);
    victor5 = new VictorSPX(5);
    victor6 = new VictorSPX(6);
    victor7 = new VictorSPX(7);

    configgy = new RobotConfigurator();


    // solenoids will go in their respective class (CargoArm or HatchArm)
    // we will likely have the solenoid controlled with two routes
    // they are controlled from PWM channels 0 and 1, CAN channel 0
    //solen = new Solenoid(0 /* can channel */, 0 /*PWM channel*/);

    // air compressor
    // airCompressor0 = new Compressor();
    // closed loop control: air compressor will automatically kick on when pressure
    // is too low (<120psi)
    // drewnote: disabled for now
    // airCompressor0.setClosedLoopControl(true);

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
    //solen.set(sol);

    //sol = !sol;

    //victor3.set
    //motorPwm0.set(1);
    //motorPwm1.set(1);

    

    // victor4.set(ControlMode.PercentOutput, 1);
    // victor5.set(ControlMode.PercentOutput, 1);
    // victor6.set(ControlMode.PercentOutput, 1);
    //victor7.set(ControlMode.PercentOutput, 1);

    


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

    configgy.discoverMotors();
    configgy.discoverPneumatics();

    System.out.println("Teleop initialization complete.");
  }

  /**
   * This function is called periodically during teleoperated mode.
   */
  @Override
  public void teleopPeriodic()
  {
    //TODO
    //drewnote: this is temporary
    return;


    // updateVars();
    // updateShuffleboard();

    // move motors by joystick
    // motorPwm0.set(joyDriver1.getX());
    // motorPwm1.set(joyDriver1.getY());
    // System.out.println("1(X, Y) = (" + joyDriver1.getX() + ", " + joyDriver1.getY() + ")");
    // System.out.println("2(X, Y) = (" + joyDriver2.getX() + ", " + joyDriver2.getY() + ")");
    
    // System.out.println("----------------");

    // // left stick
    // // right = positive
    // // left = negative
    // System.out.println(joyDriver1.getRawAxis(0));

    // // left stick
    // // up = negative
    // // down = positive
    // System.out.println(joyDriver1.getRawAxis(1));

    // // left trigger
    // // neutral = 0
    // // depressed = 1
    // System.out.println(joyDriver1.getRawAxis(2));
    
    // // right trigger
    // // neutral = 0
    // // depressed = 1
    // System.out.println(joyDriver1.getRawAxis(3));

    // // right stick
    // // left = negative
    // // rigth = positive
    // System.out.println(joyDriver1.getRawAxis(4));

    // // right stick
    // // up = negative
    // // down = positive
    // System.out.println(joyDriver1.getRawAxis(5));
    // System.out.println("----------------");


    // double left_joystick = Math.round(joyDriver1.getRawAxis(1));
    // double right_joystick = Math.round(joyDriver1.getRawAxis(5));
    // double left_joystick_lr = Math.round(joyDriver1.getRawAxis(0));
    // double right_joystick_lr = Math.round(joyDriver1.getRawAxis(4));
    // double left_trigger = Math.round(joyDriver1.getRawAxis(2));
    // double right_trigger = Math.round(joyDriver1.getRawAxis(3));

    // double left_wheel_amt = left_joystick * (0.5 + (0.5 * left_trigger));
    // double right_wheel_amt = -1 * (right_joystick * (0.5 + (0.5 * right_trigger)));


    // // drive robot
    // driveTrain.drive(Math.round(joyDriver1.getRawAxis(1)), Math.round(joyDriver1.getRawAxis(5)));

    // if(joyDriver1.getRawButtonPressed(1))
    // {
    //   System.out.println("Drive tank toggled from: " + drive_tank + " to " + !drive_tank);
    //   drive_tank = !drive_tank;
    // }


    // // btn 5 = lb
    // // btn 6 = rb
    
    // if(joyDriver1.getRawButtonPressed(5))
    // {
    //   System.out.println("Turning left!");
    //   turning_left = !turning_left;
    //   turning_init_x = yaw;
    //   System.out.println("Current yaw: " + turning_init_x);
    //   System.out.println("Turning to: " + (turning_init_x - 90));
    // }
    // if(joyDriver1.getRawButtonPressed(6))
    // {
    //   System.out.println("Turning right!");
    //   turning_right = !turning_right;
    //   turning_init_x = yaw;
    //   System.out.println("Current yaw: " + turning_init_x);
    // }

    // if(turning_left)
    // {
    //   System.out.println("Turning left: " + (Math.abs(turning_init_x - yaw) % 360));
    //   if(Math.abs(turning_init_x - yaw) % 360 < 90)
    //   {
    //     set_left_motors(0.25);
    //     set_right_motors(0.25);
    //   }
    //   else
    //   {
    //     turning_left = false;
    //     set_left_motors(0);
    //     set_right_motors(0);
    //   }
    // }

    // if(turning_right)
    // {
    //   System.out.println("Turning right: " + (Math.abs(gyro.getYaw() - turning_init_x) % 360));
    //   if(Math.abs(gyro.getYaw() - turning_init_x) % 360 < 90)
    //   {
    //     set_left_motors(0.25);
    //     set_right_motors(0.25);
    //   }
    //   else
    //   {
    //     turning_right = false;
    //     set_left_motors(0);
    //     set_right_motors(0);
    //   }
    // }


  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic()
  {

  }

  private void drive_tank(double left_speed, double right_speed)
  {
    // motorCan1.set(ControlMode.PercentOutput, left_speed);
    // motorCan0.set(ControlMode.PercentOutput, right_speed);

    // motorCan2.set(ControlMode.PercentOutput, left_speed);
    // motorCan3.set(ControlMode.PercentOutput, right_speed);
  }

  private void drive_normal(double speed, double lr_stick)
  {
    double left_speed = 0;
    double right_speed = 0;

    // set_left_motors(speed + (lr_stick));//left_speed * (0.5 * lr_stick));
    // set_right_motors(speed + (lr_stick));

    // left
    // speed + ((Math.signum(lr_stick) * lr_stick))
    
    // // right
    // speed * (-1 * Math.signum(lr_stick))

    // // up
    // speed

    // down

  }


  // use this method to set all motors to a speed
  // useful for testing and maybe that's it
  private void setMotors(double speed)
  {
    // motorPwm0.set(speed);
    // motorPwm1.set(speed);
    // motorCan0.set(ControlMode.PercentOutput, speed * 10);
    // motorCan1.set(ControlMode.PercentOutput, speed * 10);
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
