//package frc.robot.subsystems
//
//import beaverlib.utils.Sugar.clamp
//import beaverlib.utils.Units.Electrical.VoltageUnit
//import beaverlib.utils.Units.Electrical.volts
//import com.revrobotics.spark.SparkLowLevel
//import com.revrobotics.spark.SparkMax
//import com.revrobotics.spark.config.SparkBaseConfig
//import edu.wpi.first.wpilibj2.command.Command
//import edu.wpi.first.wpilibj2.command.SubsystemBase
//import frc.engine.utils.initMotorControllers
//
//object ShooterFeedConstants {
//    val feederID = 10
//    val MAX_VOLTS = 12.0.volts
//}
//
//object ShooterFeed: SubsystemBase() {
//    val feedMotor = SparkMax(ShooterFeedConstants.feederID, SparkLowLevel.MotorType.kBrushed) // 775
//
//    init {
//        initMotorControllers(30, SparkBaseConfig.IdleMode.kCoast, feedMotor)
//    }
//
//    /**
//     * A command to run the shooter feed at the inputted voltage.
//     * @param voltage the voltage to run the feeder motor at.
//     */
//    fun RunShooterFeedCommand(voltage: VoltageUnit = 1.0.volts) : Command {
//        return run { runFeed(voltage) }
//            .finallyDo({ interrupted ->
//                runFeed(0.0.volts)
//            })
//    }
//
//    /**
//     * Runs the feed motor for the shooter.
//     * @param voltage the voltage to run the motor at.
//     * Positive is to intake, negative is to outtake.
//     *
//     * NOTE: Running feeder has a 10:1 gear ratio.
//     */
//    fun runFeed(voltage: VoltageUnit = 1.0.volts) { feedMotor.set(
//        -voltage.asVolts.clamp(
//            -ShooterConstants.MAX_VOLTS.asVolts,
//            ShooterConstants.MAX_VOLTS.asVolts
//        )
//    )}
//}