/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.wayzatarobotics;


import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot2014 extends IterativeRobot {
   
    Joystick joy1;
    Joystick joy2;
    CANJaguar frontLeft;
    CANJaguar rearLeft;
    CANJaguar frontRight;
    CANJaguar rearRight;
    RobotDrive drive;
    CANJaguar shoot1;
    CANJaguar shoot2;
    Compressor compressor;
    Solenoid extend;
    Solenoid retract;
    Victor launch;
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        
        try {
            
            // Set each motor
            frontLeft = new CANJaguar(3);
            rearLeft = new CANJaguar(2);
            frontRight = new CANJaguar(4);
            rearRight = new CANJaguar(1);
            
            // Set drive
            drive = new RobotDrive(frontLeft, rearLeft, frontRight, rearRight);
            
            // Invert corrctly
            drive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
            drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
            
            // Create shooting motors
            shoot1 = new CANJaguar(5);
            shoot2 = new CANJaguar(6);
            
            // Make them both use voltage
            //shoot1.changeControlMode(CANJaguar.ControlMode.kVoltage);
            //shoot2.changeControlMode(CANJaguar.ControlMode.kVoltage);
            
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
        
        // Set compressor
        compressor = new Compressor(1, 1);

        // Set the pistons
        extend = new Solenoid(1);
        retract = new Solenoid(2);
        
        // Set joysticks
        joy1 = new Joystick(1);
        joy2 = new Joystick(2);
        
        // Set the launch
        launch = new Victor(2);
        
    }

    /**
     * This function is called once when operator control starts
     */
    public void teleopInit() {
        
        // Catch annoying CAN errors
        try {
            
            // Start the compressor when the match starts
            compressor.start();
            
            // Make sure pistons are off
            extend.set(false);
            retract.set(true);
            
            // Use percentage in this mode
            frontLeft.changeControlMode(CANJaguar.ControlMode.kPercentVbus);
            rearLeft.changeControlMode(CANJaguar.ControlMode.kPercentVbus);
            frontRight.changeControlMode(CANJaguar.ControlMode.kPercentVbus);
            rearRight.changeControlMode(CANJaguar.ControlMode.kPercentVbus);
            
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
        
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        
        // Special adjustment for messed up joystick
        // Joystick throttle axis reads from -1 at full and -.33 and least
        double default_max = -1;
        double default_min = .33;
        // Scaled to proper values
        double scaled_max = 1;
        double scaled_min = .3;
        
        // Start with actual value to correct range
        double magnitude = (((joy1.getRawAxis(3) - default_min) * (scaled_max - scaled_min)) / (default_max - default_min)) + scaled_min;
        
        // Drive with polar because scaled magnitude
        drive.mecanumDrive_Polar(
                magnitude * joy1.getMagnitude(),
                joy1.getDirectionDegrees(),
                joy1.getRawAxis(4) * .4
        );
        
        // Catch annoying CAN errors
        try {
            
            // Run shoot wheel at full power if button pressed
            shoot1.setX(joy2.getRawAxis(3) * -1);
            shoot2.setX(joy2.getRawAxis(3) * -1);
            
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
        
        // Amount axis has to be over to qualify as value
        double cutoff = .6;
        // If value past cutoff change appropriatly
        if (joy2.getRawAxis(2) >= cutoff) {
            extend.set(false);
            retract.set(true);
        } else if (joy2.getRawAxis(2) <= -cutoff) {
            extend.set(true);
            retract.set(false);
        }
        
        // If trigger buttons press, fire
        launch.set(joy2.getRawAxis(4));
        
    }
    
    /**
     * This function is called once when the autonomous starts
     */
    public void autonomousInit() {
        
        // Start the compressor
        compressor.start();
        
        // Catch annoying CAN errors
        /*try {
            
            // Start the motors first so they ramp up
            shoot1.setX(10);
            shoot2.setX(10);
            
            // Make drive motors in voltage mode so they are reliable
            frontLeft.changeControlMode(CANJaguar.ControlMode.kVoltage);
            rearLeft.changeControlMode(CANJaguar.ControlMode.kVoltage);
            frontRight.changeControlMode(CANJaguar.ControlMode.kVoltage);
            rearRight.changeControlMode(CANJaguar.ControlMode.kVoltage);
            
            // Drive using voltage
            frontLeft.setX(10);
            rearLeft.setX(10);
            frontRight.setX(10);
            rearRight.setX(10);
            
            // Wait 4 seconds
            Timer.delay(4);
            
            // Stop driving to shoot
            frontLeft.setX(0);
            rearLeft.setX(0);
            frontRight.setX(0);
            rearRight.setX(0);
            
            // Pull wheels back to shoot
            //retract.set(true);
            //extend.set(false);
            
            // Wait to fully shoot
            Timer.delay(3);
            
            // Drive using voltage again
            frontLeft.setX(10);
            rearLeft.setX(10);
            frontRight.setX(10);
            rearRight.setX(10);
            
            // Wait 2 seconds
            Timer.delay(2);
            
            // Stop driving to finish
            frontLeft.setX(0);
            rearLeft.setX(0);
            frontRight.setX(0);
            rearRight.setX(0);
            
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }*/
        
    }
    
    /**
     * This function is called once when the robot disables
     */
    public void disabledInit() {
        
        try {
        
            // Stop the compressor when the match end
            compressor.stop();

            // Make sure pistons are off and wide open
            extend.set(false);
            retract.set(true);
        
        } catch (NullPointerException e) {
            // Catch annoying bug
        }
        
    }
    
}
