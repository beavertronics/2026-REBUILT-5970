package frc.robot.subsystems

import com.ctre.phoenix6.Orchestra
import com.ctre.phoenix6.StatusCode
import com.ctre.phoenix6.hardware.TalonFX
import edu.wpi.first.wpilibj2.command.SubsystemBase

object Orchestrator : SubsystemBase() {
    /**
     * Refer to the following link for information about Orchestra:
     * - https://v6.docs.ctr-electronics.com/en/latest/docs/api-reference/api-usage/orchestra.html#playback-while-disabled-frc
     * Refer to the following link for information about converting MIDI files into CHRP:
     * - https://v6.docs.ctr-electronics.com/en/latest/docs/tuner/tools/chrp-converter.html
     */

    val orchestra = Orchestra()

    init {
        // add all krakens as instruments
        orchestra.clearInstruments()
        Drivetrain.swerveDrive.modules.forEach {
            val driveMotor = it.driveMotor.motor as TalonFX
            println("ORCHESTRA: Motor added (" +
                    driveMotor.deviceID +
                    "): " +
                    orchestra.addInstrument(it.driveMotor.motor as TalonFX)
            )
        }
    }

    /**
     * Loads the music file to play (.chrp file).
     * Any files put into the deploy directory will be in the default folder.
     * Any path must include "orchestra/(filename).(file extension)"
     */
    fun load(filepath: String) {
        val status = orchestra.loadMusic(filepath)
        if (!status.isOK()) {
            println(status.description) // todo
            return
        }
    }
    /**
     * Plays the loaded music file.
     */
    fun play() : StatusCode { return orchestra.play() }

    /**
     * Stops the loaded music file.
     */
    fun stop() { orchestra.stop() }
}