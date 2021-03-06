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

import edu.wpi.first.wpilibj.Preferences;

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

  //  drewnote: this is set to full speed - bad idea!
  public double speedModifier = 1; //0.6;

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

  Boolean initted = false;
  
  Preferences prefs;
  
  double testGoalAngle = 0;

  // enable this to allow movement in Test mode
  // this can be really dangerous if you don't know what you're doing!
  Boolean movementInTest = false;

  /**
   * This function is run once each time the robot enters autonomous mode.
   */
  @Override
  public void autonomousInit()
  {
    System.out.println("Initializing autonomous mode...");

    // zero arm encoder only in autonomous
    //cargoArm.zeroEncoder();

    commonInit();

    // move hatch finger down
    
    hatchArm.fingerSentDown = false;
    hatchArm.toggleFinger();

    System.out.println("Autonomous initialization complete.");
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic()
  {
    commonPeriodic();
  }

  public void commonInit()
  {
    movementInTest = false;


    if(!initted)
    {

      driveTrain.stop();
      System.out.println("Motors stopped.");
      //driveTrain.init();
      //System.out.println("Drive Train encoders zeroed.");

      updateShuffleboard();
  
      //cargoArm.init();
  
      // check controllers - do they exist?
      if(driver1.joystick == null || driver2.joystick == null)
      {
        // init joysticks
        Joystick[] joysticks = getControllers();
  
        driver1 = new Buttons(joysticks[0]);
        driver2 = new Buttons(joysticks[1]);
      }
      

      hatchArm.finger_target = 0;
      hatchArm.finger_moving = false;
      hatchArm.dirMoving = 0;
      hatchArm.zero();
      

      initted = true;
    }
    else
    {
      System.out.println("Not re-initializing!");
    }
    
  }

  public void commonPeriodic()
  {

    // debug for now
    //cargoArm.periodic();
    //driveTrain.periodic();
 
    // drive robot
    double d1LeftJoystick = driver1.getAxis(driver1.LAxisUD);
    double d1RightJoystick = driver1.getAxis(driver1.RAxisUD);

    double d2LeftJoystick = driver2.getAxis(driver2.LAxisUD);
    double d2RightJoystick = driver2.getAxis(driver2.RAxisUD);

    // -------------------- DRIVER 1

    driveTrain.drive(d1LeftJoystick * speedModifier, d1RightJoystick * speedModifier);

    // driver1 controls ramp
    // press start and select together to deploy
    if(driver1.down(driver1.Start) && driver1.down(driver1.Select))
    {
      ramp.deploy();
      hatchArm.fingerGrab();
    }
    else
    {
      ramp.undeploy();
    }

    // driver 1 can double speed by holding L2
    //driveTrain.fastSpeed = driver1.down(driver1.L2);
    if(driver1.pressed(driver1.A))
    {
      driveTrain.slowSpeed = !driveTrain.slowSpeed;
      System.out.println("Fast speed toggled to: " + driveTrain.slowSpeed);
    }

    //drive straight
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
    cargoArm.spinBallMotor(-1 * d2LeftJoystick * 0.9);

    // control hatch placement pistons
    if(driver2.pressed(driver2.A))
    {
      hatchArm.pushPistons();
    }
    else if (driver2.released(driver2.A))
    {
      hatchArm.retractPistons();
    }

    // open/close hatch grabber arms
    if(driver2.pressed(driver2.B))
    {
      hatchArm.toggleGrabber();
    }

    if(driver2.pressed(driver2.Select))
    {
      //cargoArm.toggleArmLock();
      hatchArm.toggleFinger();
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
    if(driver2.down(driver2.L1))
    {
      hatchArm.rotateFinger(1);
    }
    else
    {
      if(driver2.down(driver2.L2))
      {
        hatchArm.rotateFinger(-1);
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
    if(driver2.down(driver2.R1))
    {
      hatchArm.rotateArm(-1 * speedModifier);// * 0.75);
    }
    else
    {
      if(driver2.down(driver2.R2))
      {
        hatchArm.rotateArm(1 * speedModifier);// * 0.75);
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
      //cargoArm.requestMove(-1 * speedModifier);
      cargoArm.rotateArm(-1);
      //cargoArm.solArmBrake.set(false);
    }
    else
    {
      if(driver2.down(driver2.Y))
      {
        //cargoArm.requestMove(2 * speedModifier);
        cargoArm.rotateArm(1);
        //cargoArm.solArmBrake.set(false);
      }
      else
      {
        //cargoArm.rotateArm(cargoArm.getArmCalculation());
        //cargoArm.solArmBrake.set(true);
        cargoArm.rotateArm(0);
      }
    }
    if(driver2.released(driver2.X) || driver2.released(driver2.Y))
    {
      //cargoArm.setArmTarget(cargoArm.currentPosition());
      //cargoArm.solArmBrake.set(true);
      cargoArm.rotateArm(0);
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

    if(driver2.pressed(driver2.LThumb))
    {
      cargoArm.armLockEnabled = !cargoArm.armLockEnabled;
      
    }
    if(driver2.pressed(driver2.RThumb))
    {
      cargoArm.toggleBrake();
    }


    // allow move-to calculations to occur
    hatchArm.periodic();
    cargoArm.periodic();
  }

  @Override
  public void disabledInit()
  {
    // disabled for now
    //initted = false;
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

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */
  @Override
  public void robotInit()
  {
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
   * This function is called once each time the robot enters teleoperated mode.
   */
  @Override
  public void teleopInit()
  {
    System.out.println("Initializing teleop mode...");

    commonInit();


    System.out.println("Teleop initialization complete.");
  }

  /**
   * This function is called periodically during teleoperated mode.
   */
  @Override
  public void teleopPeriodic()
  {
    commonPeriodic();
  }

  @Override
  public void testInit()
  {
      commonInit();

      // reset grav constant
      cargoArm.GRAV_CONSTANT = 0.4;
  }
  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic()
  {
    // test is currently built to debug cargo arm placements
    // press 'A' on the second controller to output the encoder's position
    // and whatever angle it thinks we're at
    // press 'Select' on p2 to re-zero the cargo arm
    
    // cargoArm.armUp();
    
    if(driver2.pressed(driver2.A))
    {
        System.out.println("Cargo arm is at encoder value: " + cargoArm.encCargoArm.position());
        System.out.println("Which is " + (cargoArm.encCargoArm.angle() + cargoArm.ZERO_ANGLE) + " degrees.");
 
        System.out.println("Hatch finger is at encoder value: " + hatchArm.fingerEncValue());
    }
    if(driver2.pressed(driver2.Select))
    {
        cargoArm.zeroEncoder();
        System.out.println("Cargo arm zeroed.");

        hatchArm.zero();
        System.out.println("Hatch finger zeroed.");

        hatchArm.finger_target = 0;
        hatchArm.finger_moving = false;
        hatchArm.dirMoving = 0;
    }
    
    // if(driver2.pressed(driver2.B))
    // {
    //     cargoArm.armPositionTarget = testGoalAngle * 56.9;
    //     //double forceToApply = cargoArm.getArmCalculation();
    //     System.out.println("To reach the goal of " + testGoalAngle + ", the following force would be applied: " + forceToApply);
    // }
    
    // adjust test goal angle to combine with the above to see, without moving, a cargo arm motor speed to apply
    if(driver2.dpad(driver2.Up))
    {
        testGoalAngle += 5;
        cargoArm.armPositionTarget = testGoalAngle * 56.9;
        System.out.println("Test goal angle incremented to " + testGoalAngle);
    }
    if(driver2.dpad(driver2.Down))
    {
        testGoalAngle -= 5;
        
        cargoArm.armPositionTarget = testGoalAngle * 56.9;
        System.out.println("Test goal angle decremented to " + testGoalAngle);
    }
    
    // // adjust gravitational constant for on-the-go tweak testing
    // // if you don't know what you're doing, do not try to do this!
    // if(driver2.pressed(driver2.R1))
    // {
    //     cargoArm.GRAV_CONSTANT += 0.02;
    //     System.out.println("Gravitational constant incremented to " + cargoArm.GRAV_CONSTANT);
    // }
    // if(driver2.pressed(driver2.L1))
    // {
    //     cargoArm.GRAV_CONSTANT -= 0.02;
    //     System.out.println("Gravitational constant decremented to " + cargoArm.GRAV_CONSTANT);
    // }
        // button 7: L2
    // button 8: R2
    // L2 will reverse the finger
    // R2 will rotate it forward
    if(driver2.down(driver2.L1))
    {
      hatchArm.rotateFinger(1);
    }
    else
    {
      if(driver2.down(driver2.L2))
      {
        hatchArm.rotateFinger(-1);
      }
      else
      {
        hatchArm.rotateFinger(0);
      }
    }
    hatchArm.periodic();

    if(driver2.pressed(driver2.B))
    {
      hatchArm.toggleFinger();
    }

    // test brake power
    if(driver2.pressed(driver2.Start))
    {
      cargoArm.toggleBrake();
      System.out.println("Bike brake toggled to: " + cargoArm.solArmBrake.get());
    }

    // press 'x' to toggle whether or not arm should attempt to ONLY counterract the force of gravity
    // use this to check if the grav constant is correct. if it is, the arm should not fall
    if(driver2.pressed(driver2.X))
    {
      cargoArm.toggleArmLock();
      System.out.println("Arm lock toggled.");
      if(cargoArm.armLockEnabled)
      {
        System.out.println("Arm should stay constant at given location.");
      }
    }

    // if(driver2.down(driver2.L2) && driver2.pressed(driver2.R2))
    // {
    //   movementInTest = !movementInTest;
    //   if(movementInTest)
    //   {
    //     System.out.println("Movement in test has been enabled!");
    //   }
    //   else
    //   {
    //     System.out.println("Movement in test has been disabled.");
    //   }
    // }
    if(driver2.pressed(driver2.R1))
    {
      System.out.println("Moving hatch finger to UP");
      hatchArm.fingerUp();
    }
    if(driver2.pressed(driver2.R2))
    {
      System.out.println("Moving hatch finger to GRAB");
      hatchArm.fingerGrab();
    }

    // if(movementInTest)
    // {
    //   cargoArm.rotateArm(cargoArm.getArmCalculation());
    // }
    
    // // this is an attempt to read values from the DriverStation so we can edit constants without redeploying
    // // this may need tweaking b/c the resource that said this was possible was from 2013
    // if(driver2.pressed(driver2.Start))
    // {
    //     System.out.println("Grabbing constants from Smart Dashboard...");
    //     cargoArm.GRAV_CONSTANT = prefs.getDouble("Grav_Constant", 0.5);
    //     // cargoArm.ENCODER_RATIO = prefs.getDouble("Cargo_Gear_Ratio", 1);
    //     cargoArm.armPositions[1] = prefs.getDouble("Cargo_Full_Down_Pos", -6000);
    // }

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

}
