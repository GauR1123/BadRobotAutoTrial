/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStationLCD;
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
    Ultrasonic hFromGround; // for measuring height of arm from ground
    AxisCamera cam; // camera
    Timer timer = new Timer(); // timer
    DigitalInput left; // for LineTracker
    DigitalInput middle;
    DigitalInput right;
    DriverStation ds;
    boolean forkLeft;
    Compressor airComp;

    final int LOWS = 100; //height from ground in mm on the low side bar
    final int MEDS = 400; // medium side
    final int HIGHS = 700; // you get the picture
    final int LOWM = 200; // btw fix these numbers
    final int MEDM = 500;
    final int HIGHH = 800;
   
    public void robotInit()
    {
            try
            {
                fLeft = new CANJaguar(10); // motors for wheels with CAN ports as arguements
                fRight = new CANJaguar(4);
                bLeft = new CANJaguar(9);
                bRight = new CANJaguar(7);

                left = new DigitalInput(3); // for LineTracker
                middle = new DigitalInput(2);
                right = new DigitalInput(14);

                output = new DigitalOutput(10); // ultrasonic output
                input = new DigitalInput(8); //ultrasonic input
                ultraSonic  = new Ultrasonic(output, input, Ultrasonic.Unit.kMillimeter); //initialize ultrasonic
                ultraSonic.setEnabled(true);
                ultraSonic.setAutomaticMode(true);

                ds = DriverStation.getInstance();
                airComp = new Compressor(); // <-- fix that
            } catch (Exception e) { e.printStackTrace(); }
        timer.delay(1);
    }

    /**
     * This function is called periodically during autonomous
     */

    int lastSense = 0; // last LineTracker which saw line (1 for left, 2 for right)
    public void autonomousPeriodic()
    {
         forkLeft =  ds.getDigitalIn(1);//left from DS
         updateAirComp(airComp); // update the air compressor
         boolean leftValue = left.get(); // from sensors
         boolean middleValue = middle.get();
         boolean rightValue = right.get();

        double speed = 0.3; // robot speed

        int lineState = (int)(rightValue?1:0)+
                        (int)(middleValue?2:0)+
                        (int)(leftValue?4:0);

         if(closerThan(500))
         {
            
            straight(0);
            return;
         }

        switch (lineState) // follow the line
        {
            case 0: //No sensors see the line
                System.out.println("Lost the line: " + lastSense);
                speed = .25;
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
                    setLefts(0.2); // CAUTION!  Go Slowly!
                    setRights(0.2);
                }
                break;
            case 1: //Right sees the line
                softRight(speed);
                lastSense = 2;
                break;
            case 2: //Middle sees the line
                straight(speed);
                break;
            case 3: //Middle and right sees the line
                softRight(speed);
                lastSense = 2;
                break;
            case 4: //Left sees the line
               // System.out.println("Hard left");
                softLeft(speed);
                lastSense = 1;
                break;
            case 5: //Left and right see the line
                System.out.println("At Cross");
                if(forkLeft)
                {
                    hardLeft(speed);
                }
                else
                {
                    hardRight(speed);
                }
                break;
            case 6: //Left and middle see the line
                softLeft(speed);
                lastSense = 1;
                break;
            case 7: //All three see the line
                System.out.println("At Cross 7");
                if(forkLeft)
                {
                    hardLeft(speed);
                }
                else
                {
                    hardRight(speed);
                }
                break;
            default:
                System.out.println("You're doomed. Run.");
        }
    }

    //BELOW ARE BLACK BOX METHODS.  DO NOT MEDDLE.  OR ELSE.  MUHAHAHAHAHAHAAAA!!!

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
        } catch (CANTimeoutException e){
            DriverStationLCD lcd = DriverStationLCD.getInstance();
            lcd.println(DriverStationLCD.Line.kMain6, 1, "CAN on the Left!!!");
            lcd.updateLCD();
        }
    }

    private void setRights(double d)
    {
        try{
        fRight.setX(-d);
        bRight.setX(-d);
        } catch (CANTimeoutException e){
            e.printStackTrace();
            DriverStationLCD lcd = DriverStationLCD.getInstance();
            lcd.println(DriverStationLCD.Line.kMain6, 1, "CAN on the Right!!!");
            lcd.updateLCD();
        }
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

    public void straight(double speed)
    {
        setLefts(speed);
        setRights(speed);
    }

    public void hardLeft(double speed)
    {
        setLefts(-speed);
        setRights(speed);
    }

    public void hardRight(double speed)
    {
        setLefts(speed);
        setRights(-speed);
    }

    public void softLeft(double speed)
    {
        setLefts(0);
        setRights(speed);
    }

    public void softRight(double speed)
    {
        setLefts(speed);
        setRights(0);
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
        else
        {
            lastRange = 0;
        }
        return false;
    }
    
    public void updateAirComp(Compressor comp)
    {//updates Compressor comp to the either run or stop
            if (comp.getPressureSwitchValue())
            {
                comp.stop();
            }
            else
            {
                comp.start();
            }
        
    }
    
}

