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
import beaverlib.utils.Units.Angular.rotations
import beaverlib.utils.Units.Angular.rotationsPerSecond
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import com.revrobotics.PersistMode
import com.revrobotics.ResetMode
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import com.revrobotics.spark.config.SparkMaxConfig
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.engine.utils.initMotorControllers

object ShooterConstants {
    val hoodID = 11
    val shooterID = 9
    val hoodLimitSwitchID = 0
    val HOOD_MIN = 0.0.degrees
    val HOOD_MAX = 55.0.degrees
    val RPM_LIMIT = 5500.0.RPM
    val MAX_VOLTS = 12.0.volts
    val MIN_RPM = 0.0.RPM // todo
}

/**
 * NOTE: The usage of the JVM name stuff here is unknown, but I had to because it got angry.
 * So therefore, "BiteMe!"
 */
object Shooter : SubsystemBase() {
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
    var currentRPM: AngularVelocity = 0.0.rotationsPerSecond.asRPM.RPM

    /**
     * This is the target RPm for the shooter flywheel, in RPM
     */
    @get:JvmName("BiteMe!!")
    var targetRPM: AngularVelocity = 0.0.rotationsPerSecond.asRPM.RPM

    init {
        // configure motors
        initMotorControllers(30, SparkBaseConfig.IdleMode.kBrake, hoodMotor)
        // configure the shooter flywheel to ramp up its speed instead of going straight to max speed
        shooterMotor.configure(
            SparkMaxConfig()
                .smartCurrentLimit(40)
                .idleMode(SparkBaseConfig.IdleMode.kCoast)
                .closedLoopRampRate(0.0) // time to go from 0 to max speed, in seconds (for safety reasons)
            , ResetMode.kNoResetSafeParameters,
            PersistMode.kNoPersistParameters
        )

        // configure PID for the hood
        hoodPID.setTolerance(0.05.degrees.asRotations) // tolerant to 0.05 degrees (encoder uses rotations)
        shooterPID.setTolerance(5.0.rotationsPerSecond.asRotationsPerSecond)
    }

    override fun periodic() {
        currentAngle = hoodMotor.encoder.position.rotations.asDegrees.degrees
        currentRPM = shooterMotor.encoder.velocity.RPM
        // put data on dashboard
        SmartDashboard.putNumber("Subsystems/Shooter/Shooter RPM", currentRPM.asRPM)
        SmartDashboard.putNumber("Subsystems/Shooter/Target RPM", targetRPM.asRPM)
        SmartDashboard.putNumber("Subsystems/Shooter/Hood Angle", currentAngle.asDegrees)
        SmartDashboard.putNumber("Subsystems/Shooter/Target Hood Angle", targetAngle.asDegrees)
    }

    /**
     * Sets the zero for the hood, in degrees.
     * This will automatically get the current encoder value in degrees, unless specified to
     * override.
     * @param override whether to use a custom value instead.
     * @param angle the custom angle to use.
     */
    fun setZero(override: Boolean = false, angle: AngleUnit = 0.0.degrees) {
        if (override) { zeroValue = angle }
        else { zeroValue = hoodMotor.encoder.position.rotations.asDegrees.degrees }
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
     * Positive to extend the hood, negative to retract the hood. // todo figure out if true
     *
     * NOTE: Running hood has a 87/4 gear ratio (but should not affect encoder).
      */
    fun runHood(voltage: VoltageUnit = 1.0.volts) { hoodMotor.set(
        voltage.asVolts.clamp(
            -ShooterConstants.MAX_VOLTS.asVolts,
            ShooterConstants.MAX_VOLTS.asVolts
        )
    )} // todo figure out sign
}