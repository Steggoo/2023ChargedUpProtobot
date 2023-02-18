// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.commands.nosupersystem.ArmAngToSetpoint;
import frc.robot.commands.nosupersystem.ArmExtToSetpoint;
import frc.robot.commands.test.TestArmFullCommandGroup;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.commands.test.WristToSetpointCommand;
import frc.robot.subsystems.arm.ArmExtSubsystem;
import frc.robot.supersystems.ArmPose;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import frc.robot.commands.SwerveTeleopDrive;
import frc.robot.commands.autonomous.AutoUtils;
import frc.robot.subsystems.arm.ArmAngleSubsystem;
import frc.robot.subsystems.swerve.SwerveDrivetrain;
import frc.robot.subsystems.wrist.WristSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  //Subsystems
  private WristSubsystem m_wrist;
  private SwerveDrivetrain m_drive;
  private ArmAngleSubsystem m_arm;
  private ArmExtSubsystem m_ext;

  //Controllers
  private final CommandXboxController m_driveController = new CommandXboxController(Constants.DRIVER_PORT);

  //Logged chooser for auto
  private final LoggedDashboardChooser<Command> m_autoChooser = new LoggedDashboardChooser<>("Auto Modes");

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
  switch (Constants.CURRENT_MODE) {
    // Beta robot hardware implementation
    case THANOS:
    case HELIOS:
      m_drive = new SwerveDrivetrain();
      m_wrist = new WristSubsystem();
      m_arm = new ArmAngleSubsystem();
      m_ext = new ArmExtSubsystem();
      break;

    case SIM:
      break;

    // Default case, should be set to a replay mode
    default:
  }
    // Configure the button bindings
    configureButtonBindings();
    configAutoChooser();
    configDashboard();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    m_drive.setDefaultCommand(new SwerveTeleopDrive(m_drive, m_driveController));

    m_driveController.button(7).onTrue(m_drive.resetGyroBase());
    m_driveController.start().onTrue(m_drive.toggleFieldRelative());

    m_driveController.leftTrigger().whileTrue(m_wrist.setIntakeSpeedFactory(-1.0))
                    .whileFalse(m_wrist.setIntakeSpeedFactory(0.0));
    m_driveController.rightTrigger().whileTrue(m_wrist.setIntakeSpeedFactory(1.0))
            .whileFalse(m_wrist.setIntakeSpeedFactory(0.0));
//
    m_driveController.a().whileTrue(m_wrist.setWristPowerFactory(0.25))
      .whileFalse(m_wrist.setWristPowerFactory(0.0));
    m_driveController.b().whileTrue(m_wrist.setWristPowerFactory(-0.25))
      .whileFalse(m_wrist.setWristPowerFactory(0.0));


    m_driveController.x().whileTrue(m_arm.setAngleSpeedFactory(0.5))
      .whileFalse(m_arm.setAngleSpeedFactory(0.0));
    m_driveController.y().whileTrue(m_arm.setAngleSpeedFactory(-0.5))
            .whileFalse(m_arm.setAngleSpeedFactory(0.0));

    m_driveController.povDown().whileTrue(new TestArmFullCommandGroup
            (4.4, 62.7, -36.1 + 80.0, m_arm, m_wrist, m_ext));
    m_driveController.povUp().whileTrue(new TestArmFullCommandGroup
            (8, 142.0, 80, m_arm, m_wrist, m_ext));
    m_driveController.povRight().whileTrue(new TestArmFullCommandGroup(
            4.00, 70, 80, m_arm, m_wrist, m_ext));
    m_driveController.povLeft().whileTrue(new TestArmFullCommandGroup(
            25, 133.5, -3.3 + 80, m_arm, m_wrist, m_ext));

      m_driveController.rightBumper().whileTrue(m_ext.setArmSpeedFactory(0.5))
              .whileFalse(m_ext.setArmSpeedFactory(0.0));
    m_driveController.leftBumper().whileTrue(m_ext.setArmSpeedFactory(-0.5))
            .whileFalse(m_ext.setArmSpeedFactory(0.0));
  }

  /**
   * Use this method to add autonomous routines to a sendable chooser
   */
  public void configAutoChooser(){
    m_autoChooser.addDefaultOption("Default Trajectory", AutoUtils.getDefaultTrajectory(m_drive));
    m_autoChooser.addOption("Event Map Trajectory", AutoUtils.getPathWithEvents(m_drive));
  }

  /**
   * This method sets up Shuffleboard tabs for test commands
   */
  public void configDashboard(){
    ShuffleboardTab testCommands = Shuffleboard.getTab("Commands");

    testCommands.add("Test Stow Zone", new ArmPose(1, 10, 90));
    testCommands.add("Go To Stow", new ArmPose(0.0, 0.0, 30.0));
    testCommands.add("Go To Scoring Zone", new ArmPose(5, 90, 200));

    testCommands.add("Reset Pose", new InstantCommand(() -> m_drive.resetPoseBase()));

//    testCommands.add("Arm to 90", new ArmAngToSetpoint(m_arm, 90));
//    testCommands.add("Arm to 40", new ArmAngToSetpoint(m_arm, 40));
//    testCommands.add("Arm to 140", new ArmAngToSetpoint(m_arm, 140));
//
//    testCommands.add("Wrist to 0", new WristToSetpointCommand(m_wrist, 0));
//    testCommands.add("Wrist to 20", new WristToSetpointCommand(m_wrist, 20));
//    testCommands.add("Wrist to -20", new WristToSetpointCommand(m_wrist, -20));
//
//    testCommands.add("Arm extend to 3", new ArmExtToSetpoint(m_ext, 3));
//    testCommands.add("Arm extend to 5", new ArmExtToSetpoint(m_ext, 5));
//    testCommands.add("Arm extend to 7", new ArmExtToSetpoint(m_ext, 7));
//
//    testCommands.add("Test for full arm",
//            new TestArmFullCommandGroup(3.0, 180.0, -20.0, m_arm, m_wrist, m_ext));
//    testCommands.add("Test for full arm 2.0",
//            new TestArmFullCommandGroup(7.0, 90.0, 20.0, m_arm, m_wrist, m_ext));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An ExampleCommand will run in autonomous
    return m_autoChooser.get();
  }
}
