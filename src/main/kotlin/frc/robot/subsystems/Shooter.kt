package frc.robot.subsystems

import beaverlib.controls.PIDConstants
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.asRotations
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Angular.rotations
import com.revrobotics.PersistMode
import com.revrobotics.ResetMode
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import com.revrobotics.spark.config.SparkMaxConfig
import edu.wpi.first.math.controller.PIDController
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.engine.utils.initMotorControllers

object ShooterConstants {
    val hoodID = 11
    val shooterID = 9
    val feederID = 10
    val hoodLimitSwitchID = 0
}

object Shooter : SubsystemBase() {
    private val hoodMotor = SparkMax(ShooterConstants.hoodID, SparkLowLevel.MotorType.kBrushed) // 775
    private val shooterMotor = SparkMax(ShooterConstants.shooterID, SparkLowLevel.MotorType.kBrushless) // NEO
    private val feedMotor = SparkMax(ShooterConstants.feederID, SparkLowLevel.MotorType.kBrushed) // 775
    private val lowerLimitSwitch = DigitalInput(ShooterConstants.hoodLimitSwitchID)

    // PID for moving the hood // todo needs to be tuned
    val hoodPIDConstants = PIDConstants(1.0, 0.0, 0.0)
    val hoodPID = PIDController(hoodPIDConstants.P, hoodPIDConstants.I, hoodPIDConstants.D)

    // the value set by fun zeroHood()
    var zeroValue = 0.0

    init {
        // configure motors
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, hoodMotor, shooterMotor, feedMotor)
        // configure the shooter flywheel to ramp up its speed instead of going straight to max speed
        shooterMotor.configure(
            SparkMaxConfig()
                .closedLoopRampRate(2.0), // time to go from 0 to max speed, in seconds
        ResetMode.kNoResetSafeParameters,
        PersistMode.kNoPersistParameters
        )

        // configure PID for the hood
        hoodPID.setTolerance(0.05.degrees.asRotations) // tolerant to 0.05 degrees (encoder uses rotations)
    }

    /**
     * Runs the shooter flywheel.
     * @param voltage the voltage to run the motor at.
     * Positive is to shoot, negative is to outtake.
     */
    fun runShooter(voltage: Double = 1.0) { shooterMotor.setVoltage(-voltage); return } // todo figure out sign

    /**
     * Runs the feed motor for the shooter.
     * @param voltage the voltage to run the motor at.
     * Positive is to intake, negative is to outtake.
     */
    fun runFeed(voltage: Double = 1.0) { feedMotor.set(voltage); return } // todo figure out sign

    /**
     * Runs the hood motor at a given voltage.
     * @param voltage the voltage to run the motor at.
     * Positive to extend the hood, negative to retract the hood.
      */
    fun runHood(voltage: Double = 1.0) { hoodMotor.set(voltage); return } // todo figure out sign

    /**
     * Moves the hood to the inputted angle.
     * @param angle the angle, in degrees, of which to move the hood to.
     * @param voltage the voltage at which to run the motor at.
     * 0 degrees will have the hood be fully retracted.
     * ~55 degrees will have the hood be fully extended.
     */
    fun moveHoodToAngle(angle: Double = 0.0, voltage: Double = 1.0) {
        val clamped = (angle - zeroValue)
            .clamp(0.0.degrees.asDegrees, 55.0.degrees.asDegrees) // keep within limits
        val pos = hoodMotor.absoluteEncoder.position.rotations.asRotations
        val calculated = hoodPID.calculate(pos, clamped.rotations.asRotations)
       while (!hoodPID.atSetpoint()) {
           hoodMotor.setVoltage(calculated * voltage) // todo figure out PID, sign
       }
        hoodMotor.stopMotor()
        return
    }

    /**
     * Zeros the hood, then moves to the inputted angle.
     * @param angle the angle, in degrees, of to move the hood to after zeroing it.
     * 0 degrees will have the hood be fully retracted.
     * ~55 degrees will have the hood be fully extended.
     */
    fun zeroHood(angle: Double = 0.0) {
        while (!lowerLimitSwitch.get()) { runShooter(-1.0) } // todo figure out sign
        runShooter(0.0)
        zeroValue = hoodMotor.alternateEncoder.position // reset encoder
        moveHoodToAngle(angle)
    }
}