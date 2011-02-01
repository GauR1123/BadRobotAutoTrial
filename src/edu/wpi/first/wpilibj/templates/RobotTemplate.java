/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.first.wpilibj.DriverStation;
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RobotTemplate extends IterativeRobot 
{
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    
    Joystick j1 = new Joystick(1);
    Joystick j2 = new Joystick(2);
    CANJaguar fLeft, fRight, bLeft, bRight,unused1, unused2; //motors
    DigitalOutput output; // for ultrasonic
    DigitalInput input;
    Ultrasonic ultraSonic;
    AxisCamera cam; // camera
    Timer timer = new Timer(); // timer
    DigitalInput left; // for LineTracker
    DigitalInput middle;
    DigitalInput right;
    DriverStation ds;
    boolean[] digitalIns; //Array of digital inputs on the Driver Station Dashboard
    
    public void robotInit()
    {
            try
            {
                fLeft = new CANJaguar(10); // motors for wheels with CAN ports as arguements
                fRight = new CANJaguar(4);
                bLeft = new CANJaguar(9);
                bRight = new CANJaguar(7);
               // setCoast(fLeft); // set them to drive in coast mode (no sudden brakes)
               // setCoast(fRight);
               // setCoast(bLeft);
               // setCoast(bRight);

                left = new DigitalInput(3); // for LineTracker
                middle = new DigitalInput(2);
                right = new DigitalInput(14);

                output = new DigitalOutput(10); // initialize ultrasonic
                input = new DigitalInput(8);
                ultraSonic  = new Ultrasonic(output, input, Ultrasonic.Unit.kMillimeter);
                ultraSonic.setEnabled(true);
                ultraSonic.setAutomaticMode(true);

                ds = DriverStation.getInstance();
                digitalIns = new boolean[8];
                for (int x = 1; x<=8; x++)
                {
                    digitalIns[x-1] = ds.getDigitalIn(x); // Initializes every array node to represent a boolean on the Driver Station Dashboard
                }

            } catch (Exception e) { e.printStackTrace(); }
        timer.delay(1);
    }

    /**
     * This function is called periodically during autonomous
     */

    boolean atFork = false; // if robot has arrived at fork
    int lastSense = 0; // last LineTracker which saw line (1 for left, 2 for right)
    public void autonomousPeriodic()
    {
        //System.out.print("Autonomous Start");
        double speed = 0;
        
        if (!atFork && !closerThan(500)) // while not at fork, stop if closer than 500mm
        {
            //System.out.println(atFork + "Hey y'all" + ultraSonic.getRangeMM());
            // read the sensors
            boolean leftValue = left.get();
            boolean middleValue = middle.get();
            boolean rightValue = right.get();

            speed = .3; // can change

            if(middleValue && (!leftValue && !rightValue)) // if it's centered
            {
                setLefts(speed);
                setRights(speed);
            }

            else if(!leftValue && rightValue) // if it's too far right go right
            {
                lastSense = 2;
                setRights(0);//speed * 0.7);
                setLefts(speed); //(-turn/45 + 1)); // run right motors slower to turn 
            }

            else if (!rightValue && leftValue) // if it's too far left go left
            {
                lastSense = 1;
                setLefts(0);//speed*0.7);
                setRights(speed); //(-turn/45 + 1)); 
            }

            else if (!middleValue && !leftValue && !rightValue)
            {
                if (lastSense == 1) // left is last seen, go left
                {
                    setLefts(-speed);//speed * 0.7);
                    setRights(speed);
                }
                else if (lastSense == 2) // go right
                {
                    setLefts(speed);
                    setRights(-speed);//speed * 0.7);
                }  
                else
                {
                    setLefts(0.2); // CAUTION!  Go Slow!
                    setRights(0.2);
                }
                
            }

            else if (leftValue && rightValue) // at fork
            {
                atFork = true;
            }
        }
        
        else if (atFork && !closerThan(500)) // after fork
        {
            if (digitalIns[0]) // if Driverstation input 1 is clicked, go left
            {
                System.out.print("we get di");
                setRights(speed);
                setLefts(-speed);//speed * 0.7);
                timer.delay(1.5);
                setRights(speed);
                setLefts(speed);
                atFork = false;
            }

            else // if Driverstation input 2 is clicked, go right (DEFAULT)
            {
                setLefts(speed);
                setRights(-speed);//speed * 0.7);
                timer.delay(1.5);
                setRights(speed);
                setLefts(speed);
                atFork = false;
            }
        }
        
        if (closerThan(500))
        {
            setLefts(0);
            setRights(0);
        }
        

        
        /*System.out.print("autonomous starts");
        timer.start();// obvious

        try {
                //fLeft = new CANJaguar(9);
                fLeft.setX(0.5); // DEPRECATED METHOD NO JUTSU
                bLeft.setX(0.5);
                while (true)
                    ultraSonicAct();
            } catch(Exception e) { e.printStackTrace(); }

        timer.delay(5); // drive for 5s

        try {
            fLeft.setX(0);
            bLeft.setX(0);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }

        timer.stop();  // stop*/
    }
    
    public void teleopPeriodic() 
    {
        try{
        setCoast(fLeft); // set them to drive in coast mode (no sudden brakes)
        setCoast(fRight);
        setCoast(bLeft);
        setCoast(bRight);
        }catch (Exception e) {}

        setLefts(deadzone(-j1.getY()));
        setRights(deadzone(-j2.getY()));
    }

    private void setLefts(double d)
    {
        try{
        fLeft.setX(d);
        bLeft.setX(d);
        } catch (Exception e){e.printStackTrace();}
    }

    private void setRights(double d)
    {
        try{
        fRight.setX(-d);
        bRight.setX(-d);
        } catch (Exception e){e.printStackTrace();}
    }

    public void setCoast(CANJaguar jag) throws CANTimeoutException
    {//Sets the drive motors to coast mode
        try{jag.configNeutralMode(CANJaguar.NeutralMode.kCoast);} catch (Exception e) {e.printStackTrace();}
    }

    public double deadzone(double d)
    {//deadzone for input devices
        if (Math.abs(d) < .05) {
            return 0;
        }
        return d / Math.abs(d) * ((Math.abs(d) - .05) / .95);
    }

    int lastRange = 0; // ascertains that you are less than 1500mm
    public boolean closerThan(int millimeters)
    {
        if (ultraSonic.isRangeValid() && ultraSonic.getRangeMM() < millimeters)
        {
            if (lastRange > 4) // 4 checks to stop
            {
                return true;
            } 
            else lastRange++;
        }
        return false;
    }
    /*public void ultraSonicAct()
    {
        System.out.println(ultraSonic.getRangeMM() + "\t" + ultraSonic.isRangeValid());
        if (ultraSonic.isRangeValid() && ultraSonic.getRangeMM() <= 100)
        {
            System.out.print("Object is within 100 mm of sensor");
        }
    }*/
    
}
