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
import com.revrobotics.spark.SparkBase
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import com.revrobotics.spark.config.SparkMaxConfig
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.engine.utils.initMotorControllers
import frc.robot.commands.subsystems.AutoAngleHood

object ShooterConstants {
    val hoodID = 11
    val shooterID = 9
    val hoodLimitSwitchID = 0
    val HOOD_MIN = 0.0
    val HOOD_MAX = 55.0
    val RPM_LIMIT = 5500.0
    val MAX_VOLTS = 12.0
    val MIN_RPM = 0.0 // todo
}

object Shooter : SubsystemBase() {
     val hoodMotor = SparkMax(ShooterConstants.hoodID, SparkLowLevel.MotorType.kBrushed) // 775
     val shooterMotor = SparkMax(ShooterConstants.shooterID, SparkLowLevel.MotorType.kBrushless) // NEO
     val lowerLimitSwitch = DigitalInput(ShooterConstants.hoodLimitSwitchID)

    val hoodPIDConstants = PIDConstants(1.0, 0.0, 0.0) // todo tune
    val shooterPIDConstants = PIDConstants(0.00065, 0.0, 0.000) // todo tune
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
        currentRPM = shooterMotor.encoder.velocity
        // put data on dashboard
        SmartDashboard.putNumber("Subsystems/Shooter/Shooter RPM", currentRPM)
        SmartDashboard.putNumber("Subsystems/Shooter/Target RPM", targetRPM)
        SmartDashboard.putBoolean("Subsystems/Shooter/Able to score?", scorable)
    }

    /**
     * Runs the shooter flywheel with a voltage.
     * @param voltage the voltage to run the motor at.
     * Positive is to outtake, negative is to shoot.
     */
    fun runShooter(voltage: Double = 1.0) { shooterMotor.setVoltage(-voltage); return }

    /**
     * Sets the target RPM.
     * @param rpm the RPM to set the flywheel to. There is a limit of 5500 RPM.
     * The flywheel is then adjusted for the RPM through the periodic function.
     */
    fun setRPM(rpm: Double = 0.0) {
        targetRPM = rpm.clamp( -ShooterConstants.RPM_LIMIT, ShooterConstants.RPM_LIMIT )
    }

    /**
     * Runs the hood motor at a given voltage.
     * @param voltage the voltage to run the motor at.
     * Positive to extend the hood, negative to retract the hood. // todo figure out if true
     *
     * NOTE: Running hood has a 87/4 gear ratio (but should not affect encoder).
      */
    fun runHood(voltage: Double = 1.0) { hoodMotor.set(voltage); return } // todo figure out sign
}