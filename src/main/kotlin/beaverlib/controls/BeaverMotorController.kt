package beaverlib.controls

import beaverlib.utils.Units.Electrical.VoltageUnit
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.TalonSRXControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.revrobotics.spark.SparkBase
import com.revrobotics.spark.SparkBase.PersistMode
import com.revrobotics.spark.SparkLowLevel
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode
import com.revrobotics.spark.config.SparkMaxConfig


interface BeaverMotorController {
    /** The percent output of the motor (-1.0.. 1.0 */
    fun get() : Double

    /** Runs the motor connected to the controller at the voltage "volts"
     *  (WARNING: May be unpredictable with non SparkMax controllers.)
     *  @param volts Voltage to run the motor at */
    fun setVoltage(volts : VoltageUnit)
    fun configure(idleMode: BeaverIdleMode, currentLimit : Int)
}
enum class BeaverIdleMode(val SparkValue : SparkBaseConfig.IdleMode, val SRXValue : NeutralMode) {
    UNSET(SparkBaseConfig.IdleMode.kCoast, NeutralMode.Coast),
    BREAK(SparkBaseConfig.IdleMode.kBrake, NeutralMode.Brake),
    COAST(SparkBaseConfig.IdleMode.kCoast, NeutralMode.Coast)}

class BeaverSparkMax(ID : Int, MotorType: SparkLowLevel.MotorType) : BeaverMotorController {
    var motorController : SparkMax = SparkMax(ID, MotorType)
    override fun get() = motorController.get()
    override fun setVoltage(volts: VoltageUnit) = motorController.setVoltage(volts.asVolts)
    override fun configure(idleMode: BeaverIdleMode, currentLimit : Int) {
        val sparkConfig : SparkMaxConfig = SparkMaxConfig();
        sparkConfig.idleMode(IdleMode.kCoast);
        sparkConfig.smartCurrentLimit(currentLimit)
        // Don't persist parameters since it takes time and this change is temporary
        motorController.configure(sparkConfig, SparkBase.ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);
    }
}

class BeaverTalonSRX(ID: Int) : BeaverMotorController {
    var motorController : TalonSRX = TalonSRX(ID)

    override fun get() = motorController.motorOutputPercent
    override fun setVoltage(volts: VoltageUnit) = motorController.set(TalonSRXControlMode.PercentOutput, volts.asVolts/12)
    override fun configure(idleMode: BeaverIdleMode, currentLimit: Int) {
        motorController.configFactoryDefault()
        motorController.configContinuousCurrentLimit(currentLimit)
        motorController.setNeutralMode(idleMode.SRXValue)
    }
}