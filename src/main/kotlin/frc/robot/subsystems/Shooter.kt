package frc.robot.subsystems

import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.asRotations
import beaverlib.utils.Units.Angular.degrees
import com.revrobotics.spark.SparkBase
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
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

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, hoodMotor, shooterMotor, feedMotor)
    }

    /**
     * Runs the shooter flywheel.
     * @param voltage the voltage to run the motor at.
     */
    fun runShooter(voltage: Double = 1.0) { shooterMotor.setVoltage(voltage); return } // todo figure out sign, PID

    /**
     * Runs the feed motor for the shooter.
     * @param voltage the voltage to run the motor at.
     */
    fun runFeed(voltage: Double = 1.0) { feedMotor.set(voltage); return } // todo figure out sign

    /**
     * Runs the hood motor at a given voltage.
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
        val clamped = angle.clamp(0.0.degrees.asDegrees, 55.0.degrees.asDegrees)
        while (hoodMotor.absoluteEncoder.position != clamped) { // todo absolute encoder?
            hoodMotor.setVoltage(voltage) // todo figure out PID
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
        hoodMotor.alternateEncoder.position = (0.0.degrees.asRotations) // reset encoder when switch is pressed
        moveHoodToAngle(angle)
    }
}