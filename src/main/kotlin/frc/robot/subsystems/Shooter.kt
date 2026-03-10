package frc.robot.subsystems

import beaverlib.controls.PIDConstants
import beaverlib.controls.toPID
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.AngleUnit
import beaverlib.utils.Units.Angular.AngularVelocity
import beaverlib.utils.Units.Angular.RPM
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.asRPM
import beaverlib.utils.Units.Angular.asRotations
import beaverlib.utils.Units.Angular.asRotationsPerSecond
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Angular.rotationsPerSecond
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.engine.utils.initMotorControllers
import frc.robot.TeleOp

object ShooterConstants {
    val hoodID = 11
    val feederID = 10
    val shooterID = 9
    val hoodLimitSwitchID = 0
    val HOOD_MIN = 0.0.degrees
    val HOOD_MAX = 55.0.degrees
    val MIN_RPM = 0.0.RPM // todo
    val RPM_LIMIT = 5500.0.RPM
    val MAX_VOLTS = 12.0.volts
}

/**
 * NOTE: The usage of the JVM name stuff here is unknown, but I had to because it got angry.
 * So therefore, "BiteMe!"
 */
object Shooter : SubsystemBase() {
    val feedMotor = SparkMax(ShooterConstants.feederID, SparkLowLevel.MotorType.kBrushed) // 775
    val hoodMotor = SparkMax(ShooterConstants.hoodID, SparkLowLevel.MotorType.kBrushed) // 775
    val shooterMotor = SparkMax(ShooterConstants.shooterID, SparkLowLevel.MotorType.kBrushless) // NEO
    val lowerLimitSwitch = DigitalInput(ShooterConstants.hoodLimitSwitchID)

    val hoodPIDConstants = PIDConstants(1.0, 0.0, 0.0) // todo tune
    val shooterPIDConstants = PIDConstants(0.00065, 0.0, 0.000) // todo tune
    val hoodPID = hoodPIDConstants.toPID()
    val shooterPID = shooterPIDConstants.toPID()


    /**
     * This is the zero value for the encoder for the shooter hood, in degrees
     */
    var zeroValue: AngleUnit = 0.0.degrees

    /**
     * this is the current angle for the shooter hood, in degrees
     */
    var currentAngle: AngleUnit = 0.0.degrees

    /**
     * THis is the target angle for the shooter hood, in degrees
     */
    @get:JvmName("BiteMe!")
    var targetAngle: AngleUnit = 0.0.degrees

    /**
     * This is the current RPM for the shooter flywheel, in RPM
     */
    var currentRPM: AngularVelocity = 0.0.RPM

    /**
     * This is the target RPm for the shooter flywheel, in RPM
     */
    @get:JvmName("BiteMe!!")
    var targetRPM: AngularVelocity = 0.0.RPM

    init {
        // configure motors
        initMotorControllers(30, SparkBaseConfig.IdleMode.kBrake, hoodMotor)
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, feedMotor)
        initMotorControllers(40, SparkBaseConfig.IdleMode.kCoast, shooterMotor)

        // configure PID for the hood
        hoodPID.setTolerance(0.05.degrees.asDegrees) // tolerant to 0.05 degrees (encoder uses rotations)
        shooterPID.setTolerance(5.0.RPM.asRPM)
    }

    /**
     * A command to run the shooter at the inputted voltage.
     * @param voltage the voltage to run the shooter motor at.
     */
    fun ShootVoltageCommand(voltage: VoltageUnit = 1.0.volts) : Command {
        return run { runShooter(voltage) }
            .finallyDo({ interrupted ->
                runShooter(0.0.volts)
            })
    }

    /**
     * A command to zero the angle of the hood.
     */
    fun ZeroHoodCommand() : Command {
        return run { runHood(-(0.5).volts) }
            .until { lowerLimitSwitch.get() }
            .finallyDo({ interrupted ->
                runHood(0.0.volts)
                setZero()
            })
    }

    /**
     * A command to run the shooter feed at the inputted voltage.
     * @param voltage the voltage to run the feeder motor at.
     */
    fun RunShooterFeedCommand(voltage: VoltageUnit = 1.0.volts) : Command {
        return run { runFeed(voltage) }
            .finallyDo({ interrupted ->
                runFeed(0.0.volts)
            })
    }

    /**
     * A command to run the hood motor at the inputted voltage.
     * @param voltage to voltage to run the hood motor at.
     */
    fun MoveHoodVoltageCommand(voltage: VoltageUnit = 1.0.volts) : Command {
        return run { runHood(voltage) }
            .until { lowerLimitSwitch.get() && TeleOp.OI.hoodUp.asBoolean == false }
            .finallyDo({ interrupted ->
                runHood(0.0.volts)
            })
    }

    override fun periodic() {
        currentAngle = getCurrentAngle()
        currentRPM = shooterMotor.encoder.velocity.RPM
        // put data on dashboard
        SmartDashboard.putNumber("Subsystems/Shooter/Shooter RPM", currentRPM.asRPM)
        SmartDashboard.putNumber("Subsystems/Shooter/Target RPM", targetRPM.asRPM)
        SmartDashboard.putNumber("Subsystems/Shooter/Hood Angle", currentAngle.asDegrees) // adjusted by 90 degrees - zero
        SmartDashboard.putNumber("Subsystems/Shooter/Target Hood Angle", targetAngle.asDegrees)
        SmartDashboard.putBoolean("Subsystems/Shooter/Hood limit switch", lowerLimitSwitch.get())
    }

    /**
     * Sets the zero for the hood, in degrees.
     * This will automatically get the current encoder value in degrees, unless specified to
     * override.
     * @param override whether to use a custom value instead.
     * @param angle the custom angle to use.
     */
    fun setZero(override: Boolean = false, angle: AngleUnit = 0.0.degrees): Boolean {
        if (override) { zeroValue = angle }
        else { zeroValue = getCurrentAngle(true) }
        return true
    }

    /**
     * Sets the target angle for the hood, in degrees.
     */
    @JvmName("BiteMe!!!")
    fun setTargetAngle(target: AngleUnit) { targetAngle = target }

    /**
     * Sets the target RPM for the shooter flywheel, in RPM.
     */
    @JvmName("BiteMe!!!!")
    fun setTargetRPM(target: AngularVelocity) { targetRPM = target }

    /**
     * returns the current angle of the shooter hood with the ratio (4/87).
     * @param raw whether to return the value without the zero.
     */
    @JvmName("BiteMe!!!!!")
    fun getCurrentAngle(raw: Boolean = false) : AngleUnit {
        val pos = (90.0.degrees.asDegrees -
                (hoodMotor.encoder.position * 360.0 * (4.0/87.0)) * -1.0).degrees // ratio for hood
        if (raw) return pos
        else return pos - zeroValue
    }

    /**
     * Runs the shooter flywheel with a voltage.
     * @param voltage the voltage to run the motor at.
     * Positive is to shoot, negative is to reverse.
     */
    fun runShooter(voltage: VoltageUnit = 1.0.volts) { shooterMotor.setVoltage(
        -voltage.asVolts.clamp(
            -ShooterConstants.MAX_VOLTS.asVolts,
            ShooterConstants.MAX_VOLTS.asVolts
        )) }

    /**
     * Runs the hood motor at a given voltage.
     * @param voltage the voltage to run the motor at.
     * Positive to extend the hood, negative to retract the hood.
     *
     * NOTE: Running hood has a 87/4 gear ratio (but should not affect encoder).
      */
    fun runHood(voltage: VoltageUnit = 1.0.volts) { hoodMotor.set(
        -voltage.asVolts.clamp(
            -ShooterConstants.MAX_VOLTS.asVolts,
            ShooterConstants.MAX_VOLTS.asVolts
        )
    )}

    /**
     * Runs the feed motor for the shooter.
     * @param voltage the voltage to run the motor at.
     * Positive is to intake, negative is to outtake.
     *
     * NOTE: Running feeder has a 10:1 gear ratio.
     */
    fun runFeed(voltage: VoltageUnit = 1.0.volts) { feedMotor.set(
        -voltage.asVolts.clamp(
            -ShooterConstants.MAX_VOLTS.asVolts,
            ShooterConstants.MAX_VOLTS.asVolts
        )
    )}
}