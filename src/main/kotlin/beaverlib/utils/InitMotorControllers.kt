package frc.engine.utils

import beaverlib.controls.BeaverIdleMode
import beaverlib.controls.BeaverMotorController
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.revrobotics.spark.SparkBase
import com.revrobotics.spark.SparkBase.PersistMode
import com.revrobotics.spark.SparkMax
import com.revrobotics.spark.config.SparkBaseConfig
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode
import com.revrobotics.spark.config.SparkMaxConfig

/**
 * Restores the factory defaults for the given spark motor controllers,
 * and sets the current limits & idle mode
 * @param currentLimit Current limit to set the motors to
 * @param idle The idle mode to set the controller to
 */
fun initMotorControllers(currentLimit : Int, idle: SparkBaseConfig.IdleMode, vararg  motors : SparkMax){
    motors.forEach {
        val config = SparkMaxConfig()
        config.idleMode(idle)
        config.smartCurrentLimit(currentLimit)

        // Don't persist parameters since it takes time and this change is temporary

        // Don't persist parameters since it takes time and this change is temporary
        it.configure(config, SparkBase.ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters)

    }
}
/**
 * Restores the factory defaults for the given TalonSRX controllers, and sets the current limits
 * @param currentLimit Current limit to set the motors to
 */
fun initMotorControllers(currentLimit : Int, vararg  motors : TalonSRX){
    motors.forEach {
        it.configFactoryDefault()
        it.configContinuousCurrentLimit(currentLimit)
    }
}
/**
 * Restores the factory defaults for the given TalonSRX controllers, and sets the current limits
 * @param currentLimit Current limit to set the motors to
 * @param idle The idle mode to set the controller to
 */
fun initMotorControllers(currentLimit : Int, idle: NeutralMode, vararg  motors : TalonSRX){
    motors.forEach {
        it.configFactoryDefault()
        it.configContinuousCurrentLimit(currentLimit)
        it.setNeutralMode(idle)
    }
}
/**
 * Restores the factory defaults for the given TalonSRX controllers, and sets the current limits
 * @param currentLimit Current limit to set the motors to
 * @param idle The idle mode to set the controller to
 */
fun initMotorControllers(currentLimit : Int, idle: BeaverIdleMode, vararg  motors : BeaverMotorController){
    motors.forEach {
        it.configure(idle, currentLimit)
    }
}
