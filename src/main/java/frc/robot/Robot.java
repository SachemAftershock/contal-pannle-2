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

    @Override
    public void robotInit() {
        joe = new SparkMax(2, MotorType.kBrushless);

        SmartDashboard.putNumber("User Input", 0.5);
        SmartDashboard.putNumber("Current Value", Speed);

        // Mode Toggle Button
        modButton = Shuffleboard.getTab("Controls")
            .add("switch modes", false)
            .withWidget(BuiltInWidgets.kToggleButton)
            .getEntry();

        // Mode Selector
        modeChooser.setDefaultOption("Stop", "stop");
        modeChooser.addOption("Turbo", "turbo");
        modeChooser.addOption("Fast", "Fast");
        modeChooser.addOption("Slow", "Slow");
        modeChooser.addOption("Reverse Mode", "Reverse");
        Shuffleboard.getTab("Modes").add("Motor Mode", modeChooser);

        // Reverse Toggle Button
        reverseButton = Shuffleboard.getTab("Controls")
            .add("Reverse Direction", false)
            .withWidget(BuiltInWidgets.kToggleButton)
            .getEntry();
    }

    @Override
    public void teleopPeriodic() {
        SmartDashboard.putNumber("RP.M", joe.getEncoder().getVelocity());

           // Ensure Shuffleboard updates modButton
    modButton.setBoolean(modButton.getBoolean(false));

    // Read the actual value of mod
    boolean newModValue = modButton.getBoolean(false);
    System.out.println("Raw modButton Value: " + newModValue);

    // Update mod variable
    mod = newModValue;
    System.out.println("Updated mod Value: " + mod);

    // Read mode selection
    String selectedMode = modeChooser.getSelected();
    if (selectedMode == null) {
        selectedMode = "stop";
    }

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
        default:
            targetSpeed = 0.0;
            break;
    }

    // Apply reverse direction
    isReversed = reverseButton.getBoolean(false);
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
    System.out.println("Current Speed (With Ramping): " + currentSpeed); }}