package frc.robot;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.networktables.GenericEntry;

public class Robot extends TimedRobot {
    private SparkMax joe;
    private SendableChooser<String> modeChooser = new SendableChooser<>();
    private boolean isReversed = false;

    private double targetSpeed = 0.0;
    private double currentSpeed = 0.0;
    private final double rampRate = 0.02;
    private double Speed = 0.0;

    private GenericEntry modButton;
    private boolean mod = false;
    private GenericEntry reverseButton;
    
    private int robot_id = 0; // Default value

    private double C_1 = 0.0;
    private double C_2 = 0.0;
    private double C_3 = 0.0;

    @Override
    public void robotInit() {
        // Read the robot ID from SmartDashboard
        robot_id = (int) SmartDashboard.getNumber("robot id", 0.0);
        System.out.println("Initializing Robot with ID: " + robot_id);

        // Initialize the motor controller
        joe = new SparkMax(robot_id, MotorType.kBrushless);

        // SmartDashboard Inputs
        SmartDashboard.putNumber("User Input", 0.0);
        SmartDashboard.putNumber("Current Value", Speed);
        SmartDashboard.putNumber("robot id", robot_id);
        SmartDashboard.putNumber("custom 1", C_1);
        SmartDashboard.putNumber("custom 2", C_2);
        SmartDashboard.putNumber("custom 3", C_3);

        // Mode Toggle Button
        modButton = Shuffleboard.getTab("Controls")
            .add("Switch Modes", false)
            .withWidget(BuiltInWidgets.kToggleButton)
            .getEntry();

        // Reverse Toggle Button
        reverseButton = Shuffleboard.getTab("Controls")
            .add("Reverse Direction", false)
            .withWidget(BuiltInWidgets.kToggleButton)
            .getEntry();

        // Mode Selector
        modeChooser.setDefaultOption("Stop", "stop");
        modeChooser.addOption("Turbo", "turbo");
        modeChooser.addOption("Fast", "Fast");
        modeChooser.addOption("Slow", "Slow");
        modeChooser.addOption("Reverse Mode", "Reverse");
        modeChooser.addOption("Custom 1", "custom_1");
        modeChooser.addOption("Custom 2", "custom_2");
        modeChooser.addOption("Custom 3", "custom_3");

        Shuffleboard.getTab("Modes").add("Motor Mode", modeChooser);
    }

    @Override
    public void robotPeriodic() {
        // Check if robot ID has changed
        int newRobotID = (int) SmartDashboard.getNumber("robot id", 0.0);
        if (newRobotID != robot_id) {
            robot_id = newRobotID;
            joe = new SparkMax(robot_id, MotorType.kBrushless);
            System.out.println("Updated Robot ID to: " + robot_id);
        }
    }

    @Override
    public void teleopPeriodic() {
       
        SmartDashboard.putNumber("RP.M", joe.getEncoder().getVelocity());
       
        // Update custom variables
        C_1 = SmartDashboard.getNumber("custom 1", 0.0);
        C_2 = SmartDashboard.getNumber("custom 2", 0.0);
        C_3 = SmartDashboard.getNumber("custom 3", 0.0);

        // Read the mode selection
        String selectedMode = modeChooser.getSelected();
        if (selectedMode == null) {
            selectedMode = "stop";
        }

        // Read button states
        mod = modButton.getBoolean(false);
        isReversed = reverseButton.getBoolean(false);

        // Set target speed based on mode
        switch (selectedMode) {
            case "turbo":
                targetSpeed = 2.0;
                break;
            case "Fast":
                targetSpeed = 0.7;
                break;
            case "Slow":
                targetSpeed = 0.1;
                break;
            case "Reverse":
                targetSpeed = -0.1;
                break;
            case "custom_1":
                targetSpeed = C_1;
                break;
            case "custom_2":
                targetSpeed = C_2;
                break;
            case "custom_3":
                targetSpeed = C_3;
                break;
            default:
                targetSpeed = 0.0;
                break;
        }

        // Apply reverse direction
        targetSpeed *= (isReversed ? -1 : 1);

        // Read user input speed
        double userInputSpeed = SmartDashboard.getNumber("User Input", Speed);

        // Determine the desired speed based on mod state
        double desiredSpeed = mod ? targetSpeed : userInputSpeed;

        // Apply ramping logic
        if (currentSpeed < desiredSpeed) {
            currentSpeed += rampRate;
            if (currentSpeed > desiredSpeed) {
                currentSpeed = desiredSpeed;
            }
        } else if (currentSpeed > desiredSpeed) {
            currentSpeed -= rampRate;
            if (currentSpeed < desiredSpeed) {
                currentSpeed = desiredSpeed;
            }
        }

        // Set motor speed with ramping applied
        joe.set(currentSpeed);

        // Debugging Outputs
        System.out.println("Mode: " + selectedMode);
        System.out.println("Mod: " + mod + " | Reverse: " + isReversed);
        System.out.println("Custom 1: " + C_1 + " | Custom 2: " + C_2 + " | Custom 3: " + C_3);
        System.out.println("Target Speed: " + targetSpeed);
        System.out.println("Current Speed: " + currentSpeed);
        System.out.println("Motor RPM: " + joe.getEncoder().getVelocity());
    }
}
