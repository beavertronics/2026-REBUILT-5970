package frc.robot.commands.tests

import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj2.command.Command

class Wait(val time: Double) : Command() {
    val timer = Timer()
    var start = 0.0

    override fun initialize() {
        timer.restart()
        start = timer.get()
    }

    override fun isFinished(): Boolean {
        return timer.get() - start >= time
    }
}