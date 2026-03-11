package frc.robot.subsystems

import beaverlib.controls.PIDConstants
import beaverlib.controls.toPID
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.AngleUnit
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.degrees
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

object HoodConstants {
    val hoodID = 11
    val HOOD_MIN = 0.0.degrees
    val HOOD_MAX = 55.0.degrees
    val hoodLimitSwitchID = 0
    val MAX_VOLTS = 12.0.volts
}

object Hood : SubsystemBase() {
    val hoodMotor = SparkMax(HoodConstants.hoodID, SparkLowLevel.MotorType.kBrushed) // 775
    val lowerLimitSwitch = DigitalInput(HoodConstants.hoodLimitSwitchID)
    val hoodPIDConstants = PIDConstants(0.050, 0.0, 0.0) // todo tune
    val hoodPID = hoodPIDConstants.toPID()

    /**
     * This is the zero value for the encoder for the shooter hood, in degrees
     */
    var zeroValue: AngleUnit = 0.0.degrees

    /**
     * this is the current angle for the shooter hood, in degrees
     */
    var currentAngle: AngleUnit = 0.0.degrees

    /**
     * Thi is the target angle for the shooter hood, in degrees
     */
    @get:JvmName("BiteMe!")
    var targetAngle: AngleUnit = 0.0.degrees

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kBrake, hoodMotor)
        hoodPID.setTolerance(3.0) // tolerant to 0.05 degrees (encoder uses rotations)
    }

    /**
     * A command to zero the angle of the hood.
     */
    fun ZeroHoodCommand() : Command {
        return run { runHood(-(0.225).volts) }
            .until { lowerLimitSwitch.get() }
            .andThen(
                run { runHood(0.05.volts) }
                    .until { !lowerLimitSwitch.get() }
                    .finallyDo({ interrupted ->
                        runHood(0.0.volts)
                        zeroValue = getCurrentAngle(raw = true)
                    })
            )
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
    fun setZero(override: Boolean = false, angle: AngleUnit = 0.0.degrees) {
        if (override) { zeroValue = angle }
        else { zeroValue = getCurrentAngle(true) }
    }

    /**
     * Sets the target angle for the hood, in degrees.
     */
    @JvmName("BiteMe!!!")
    fun setTargetAngle(target: AngleUnit) { targetAngle = target }

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
}