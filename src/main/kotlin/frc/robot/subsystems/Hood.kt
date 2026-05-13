package frc.robot.subsystems

import beaverlib.controls.PIDConstants
import beaverlib.controls.toPID
import beaverlib.fieldmap.FieldMapREBUILTWelded
import beaverlib.utils.Sugar.clamp
import beaverlib.utils.Units.Angular.AngleUnit
import beaverlib.utils.Units.Angular.asDegrees
import beaverlib.utils.Units.Angular.asRPM
import beaverlib.utils.Units.Angular.degrees
import beaverlib.utils.Units.Angular.radians
import beaverlib.utils.Units.Electrical.VoltageUnit
import beaverlib.utils.Units.Electrical.volts
import beaverlib.utils.Units.Linear.earthGravity
import beaverlib.utils.Units.Linear.feet
import beaverlib.utils.Units.Linear.inches
import beaverlib.utils.Units.Linear.meters
import beaverlib.utils.Units.Linear.metersPerSecond
import beaverlib.utils.Units.Linear.metersPerSecondSquared
import beaverlib.utils.geometry.Vector2
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.engine.utils.initMotorControllers
import frc.robot.Constants
import frc.robot.TeleOp
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.sqrt

object HoodConstants {
    val hoodID = 11
    val HOOD_MIN = 0.0.degrees
    val HOOD_MAX = 55.0.degrees
    val hoodLimitSwitchID = 0
}

object AAC {
    val shooterHeight = 6.0.feet.asMeters.meters
    val hubHeight = 72.0.inches.asMeters.meters
    val heightDiff = hubHeight - shooterHeight
    val yOffset = 1.7.meters // todo how is this used?
}

object Hood : SubsystemBase() {
    val hoodMotor = SparkMax(HoodConstants.hoodID, SparkLowLevel.MotorType.kBrushed) // 775
    val lowerLimitSwitch = DigitalInput(HoodConstants.hoodLimitSwitchID)
    val hoodPIDConstants = PIDConstants(0.05, 0.0, 0.0)
    val hoodPID = hoodPIDConstants.toPID()

    /**
     * This is the zero value for the encoder for the shooter hood, in degrees
     */
    var zeroValue: AngleUnit = 0.0.degrees

    /**
     * this is the current angle for the shooter hood, in degrees
     */
    var currentAngle: AngleUnit = 0.0.degrees
        get() = getCurrentAngle(false)

    /**
     * Thi is the target angle for the shooter hood, in degrees
     */
    @get:JvmName("BiteMe!")
    var targetAngle: AngleUnit
        get() = SmartDashboard.getNumber("Subsystems/Shooter/Target Hood Angle", 0.0).degrees
        set(angle) {
            angle.asDegrees.clamp(
                HoodConstants.HOOD_MIN.asDegrees,
                HoodConstants.HOOD_MAX.asDegrees
            ).degrees
        }

    init {
        initMotorControllers(30, SparkBaseConfig.IdleMode.kBrake, hoodMotor)
        hoodPID.setTolerance(3.0) // tolerant to x degrees (encoder uses rotations)
    }

    /**
     * A command to zero the angle of the hood.
     */
    fun ZeroHoodCommand() : Command {
        return run { runHood(-(0.15).volts) }
            .until {
                lowerLimitSwitch.get()
            }
            .andThen(
                run { runHood(0.0725.volts) }
                    .until {
                        !lowerLimitSwitch.get()
                    }
                    .finallyDo({ interrupted ->
                        runHood(0.0.volts)
                        setZero(false)
                    })
            )
    }

    /**
     * A command to run the hood motor at the inputted voltage.
     * @param voltage to voltage to run the hood motor at.
     */
    fun MoveHoodVoltageCommand(voltage: VoltageUnit = 1.0.volts) : Command {
        return run { runHood(voltage) }
            .until {
                lowerLimitSwitch.get() && TeleOp.OI.hoodUp.asBoolean == false
            }
            .finallyDo({ interrupted ->
                runHood(0.0.volts)
            })
    }

    override fun periodic() {
        autoCalculateHood() // TODO TESTING ONLY

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
            -Constants.MAX_VOLTS.asVolts,
            Constants.MAX_VOLTS.asVolts
        )
    )}

    // todo test
    fun autoCalculateHood(dynamic: Boolean = false): AngleUnit {
        var velocity: Double
        // get flywheel velocity (inches / min)
        if (dynamic) { velocity = Shooter.currentRPM.asRPM * (PI * 4) } // live RPM
        else { velocity = Shooter.targetRPM.asRPM * (PI * 4) } // fixed RPM

        // inches per min to meters per sec^2
        val vsq = (velocity / 2362.0)  // todo is this right?
            .metersPerSecond
            .asMetersPerSecond
            .metersPerSecondSquared

        // get distance (hypotenuse) from hub
        val distance = Vector2(
            `according to all known laws of aviation, our robot should not be able to fly`.pose)
            .distance(FieldMapREBUILTWelded.teamHub.center)
            .meters

        // calculated the sqrt
        val calculatedSqrt = sqrt(
            vsq.asMetersPerSecondSquared * vsq.asMetersPerSecondSquared
                    - earthGravity.asMetersPerSecondSquared
                    * (
                    earthGravity.asMetersPerSecondSquared
                            * distance.asMeters
                            * distance.asMeters
                            + 2
                            * AAC.heightDiff.asMeters
                            * vsq.asMetersPerSecondSquared
                    )
        )

        // get the positive and negative parts of sqrt
        val calculatedPos = atan((vsq.asMetersPerSecondSquared + calculatedSqrt) /
                (earthGravity.asMetersPerSecondSquared * distance.asMeters))
        val calculatedNeg = atan((vsq.asMetersPerSecondSquared - calculatedSqrt) /
                (earthGravity.asMetersPerSecondSquared * distance.asMeters))

        // clamp to constrained degrees
        val posAngle =
            (90.0.degrees.asDegrees - calculatedPos.radians.asDegrees)
                .clamp(
                    0.0.degrees.asDegrees, HoodConstants.HOOD_MAX.asDegrees
                ).degrees

        val negAngle =
            (90.0.degrees.asDegrees - calculatedNeg.radians.asDegrees)
                .clamp(
                    0.0.degrees.asDegrees, HoodConstants.HOOD_MAX.asDegrees
                ).degrees

        // get final (bigger) angle
        val hoodAngle = when {
            (posAngle.asDegrees > negAngle.asDegrees) -> posAngle
            (negAngle.asDegrees > posAngle.asDegrees) -> negAngle
            (negAngle.asDegrees == posAngle.asDegrees) -> posAngle
            else -> 0.0.degrees
        }

        return hoodAngle
    }
}