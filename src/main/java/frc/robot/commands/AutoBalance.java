// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants.AutoConstants;
import frc.robot.subsystems.swerve.SwerveDrivetrain;

public class AutoBalance extends CommandBase {
  private SwerveDrivetrain m_drive;

  private Timer m_timer;

  private final PIDController m_balanceController;

  private double m_currentAngle;
  private double m_error;
  private double m_drivePower;
  private int m_counter;
  private boolean m_isLevel;

  /** Creates a new AutoBalance. */
  public AutoBalance(SwerveDrivetrain drive) {
    m_drive = drive;
    m_timer = new Timer();
    m_balanceController = new PIDController(AutoConstants.BALANCE_P, 0.0, AutoConstants.Balance_D);
    m_currentAngle = 0;
    m_counter = 0;
    m_isLevel = false;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(m_drive);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_balanceController.setSetpoint(AutoConstants.DESIRED_BALANCE_ANGLE);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // Gets how far the robot is tilted and calculates proper drive power from it
    m_currentAngle = m_drive.getGyroPitch().getDegrees();
    m_error = AutoConstants.DESIRED_BALANCE_ANGLE - m_currentAngle;

    m_drivePower = Math.min(m_balanceController.calculate(m_currentAngle), 1);
    // Limit max power
    m_drivePower = MathUtil.clamp(m_drivePower, -0.2, 0.2);

    // Counter for checking if robot is truly balanced
   if (Math.abs(m_currentAngle) < AutoConstants.DESIRED_BALANCE_ANGLE) {
     m_timer.start();
   }
   if (Math.abs(m_currentAngle) < AutoConstants.DESIRED_BALANCE_ANGLE && m_timer.get() >= 10) {
     m_timer.stop();
     m_isLevel = true;
   }
   if (m_timer.get() > 0 && Math.abs(m_currentAngle) > AutoConstants.DESIRED_BALANCE_ANGLE) {
     m_timer.stop();
     m_timer.reset();
   }

    m_drive.setModuleStates(m_drivePower, 0.0, 0.0);

    // Logging values for debugging
    SmartDashboard.putNumber("Gyro Angle", m_currentAngle);
    SmartDashboard.putNumber("Drive Power", m_drivePower);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    // Set the module azimuths to 45 degrees so robot doesn't slip off the charge station
    m_drive.setModuleStates(0.0, 0.0, 0.1);
    m_drive.setModuleStates(0.0, 0.0, 0.0);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return m_isLevel; // If current error is within a threshold of one degree
  }
}