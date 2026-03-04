package frc.robot.subsystems

import beaverlib.controls.PIDConstants
import beaverlib.controls.toPID
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.asRotations
import beaverlib.utils.Units.Angular.asRotationsPerSecond
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Angular.rotations
import beaverlib.utils.Units.Angular.rotationsPerSecond
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
    val feederID = 10
    val hoodLimitSwitchID = 0
    val HOOD_MIN = 0.0
    val HOOD_MAX = 55.0
    val RPM_LIMIT = 5500.0
    val MAX_VOLTS = 12.0
}

object Shooter : SubsystemBase() {
    private val hoodMotor = SparkMax(ShooterConstants.hoodID, SparkLowLevel.MotorType.kBrushed) // 775
    private val shooterMotor = SparkMax(ShooterConstants.shooterID, SparkLowLevel.MotorType.kBrushless) // NEO
    private val feedMotor = SparkMax(ShooterConstants.feederID, SparkLowLevel.MotorType.kBrushed) // 775
    private val lowerLimitSwitch = DigitalInput(ShooterConstants.hoodLimitSwitchID)

    val hoodPIDConstants = PIDConstants(1.0, 0.0, 0.0) // todo tune
    val shooterPIDConstants = PIDConstants(1.0, 0.0, 0.0) // todo tune
    val hoodPID = hoodPIDConstants.toPID()
    val shooterPID = shooterPIDConstants.toPID()

    // zero for the encoder in rotations, set by zeroHood()
    // and used by moveHoodToAngle()
    var zeroValue = 0.0
    // current, target RPM for the shooter flywheel
    var currentRPM = 0.0
    var targetRPM = 0.0
    // whether we can score or not (hood angle, distance, etc)
    var scorable = false

    init {
        // configure motors
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, hoodMotor, feedMotor)
        // configure the shooter flywheel to ramp up its speed instead of going straight to max speed
        shooterMotor.configure(
            SparkMaxConfig()
                .smartCurrentLimit(30)
                .idleMode(SparkBaseConfig.IdleMode.kCoast)
                .closedLoopRampRate(1.5) // time to go from 0 to max speed, in seconds (for safety reasons)
        , ResetMode.kNoResetSafeParameters,
        PersistMode.kNoPersistParameters
        )

        // configure PID for the hood
        hoodPID.setTolerance(0.05.degrees.asRotations) // tolerant to 0.05 degrees (encoder uses rotations)
    }

    override fun periodic() {
        // get flywheel to target RPM // todo correct?
        currentRPM = shooterMotor.encoder.velocity // todo is this RPM?
        val calculated = shooterPID.calculate(currentRPM, targetRPM)
        runShooter(
            (calculated * 12.0).clamp(-ShooterConstants.MAX_VOLTS, ShooterConstants.MAX_VOLTS)
        )

        // put data on dashboard
        SmartDashboard.putNumber("Subsystems/Shooter/Shooter RPM", currentRPM)
        SmartDashboard.putNumber("Subsystems/Shooter/Target RPM", targetRPM)
        SmartDashboard.putBoolean("Subsystems/Shooter/Able to score?", scorable)
    }

    /**
     * Runs the shooter flywheel with a voltage.
     * @param voltage the voltage to run the motor at.
     * Positive is to outtake, negative is to shoot. // todo figure out if true
     */
    fun runShooter(voltage: Double = 1.0) { shooterMotor.setVoltage(-voltage); return } // todo figure out sign

    /**
     * Sets the target RPM.
     * @param rpm the RPM to set the flywheel to. There is a limit of 5500 RPM.
     * The flywheel is then adjusted for the RPM through the periodic function.
     */
    fun setRPM(rpm: Double = 0.0) {
        targetRPM = rpm.clamp( -ShooterConstants.RPM_LIMIT, ShooterConstants.RPM_LIMIT )
    }
    /**
     * Runs the feed motor for the shooter.
     * @param voltage the voltage to run the motor at.
     * Positive is to intake, negative is to outtake. // todo figure out if true
     *
     * NOTE: Running feeder has a 10:1 gear ratio.
     */
    fun runFeed(voltage: Double = 1.0) { feedMotor.set(voltage); return } // todo figure out sign

    /**
     * Runs the hood motor at a given voltage.
     * @param voltage the voltage to run the motor at.
     * Positive to extend the hood, negative to retract the hood. // todo figure out if true
     *
     * NOTE: Running hood has a 87/4 gear ratio (but should not affect encoder).
      */
    fun runHood(voltage: Double = 1.0) { hoodMotor.set(voltage); return } // todo figure out sign

    /**
     * Moves the hood to the inputted angle.
     * @param angle the angle, in degrees, of which to move the hood to.
     * - 0 degrees will have the hood be fully retracted. // todo figure out if true
     * - ~55 degrees will have the hood be fully extended. // todo figure out if true
     * @param voltage the voltage at which to run the motor at.
     * - Positive to extend the hood, negative to retract the hood. // todo figure out if true
     */
    fun moveHoodToAngle(angle: Double = 0.0, voltage: Double = 1.0) {
        // clamp value and convert from degrees to rotations
        val clamped = (angle.degrees.asDegrees - zeroValue.rotations.asDegrees) // degrees
            .clamp(ShooterConstants.HOOD_MIN.degrees.asDegrees, ShooterConstants.HOOD_MAX.degrees.asDegrees) // degrees
            .rotations.asRotations // rotations

        // calculate PID value
        val pos = hoodMotor.absoluteEncoder.position.rotations.asRotations
        val calculated = hoodPID.calculate(pos, clamped)
       while (!hoodPID.atSetpoint()) { runHood(calculated * voltage) } // todo figure out PID, sign}
        runHood(0.0)
        return
    }

    /**
     * Zeros the hood, then moves to the inputted angle.
     * @param angle the angle, in degrees, of to move the hood to after zeroing it.
     * - 0 degrees will have the hood be fully retracted. // todo figure out if true
     * - ~55 degrees will have the hood be fully extended. // todo figure out if true
     */
    fun zeroHood(angle: Double = 0.0) {
        while (!lowerLimitSwitch.get()) { runHood(-1.0) } // todo figure out sign, voltage
        runHood(0.0)
        zeroValue = hoodMotor.alternateEncoder.position.rotations.asRotations // reset absolute encoder in rotations
        moveHoodToAngle(angle)
    }
}