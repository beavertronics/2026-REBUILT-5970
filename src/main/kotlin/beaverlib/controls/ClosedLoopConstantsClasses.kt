package beaverlib.controls

import edu.wpi.first.math.controller.SimpleMotorFeedforward

/** @property kS (roughly) how much voltage to overcome static friction
 *  @property kV How much voltage to maintain a velocity
 *  @property kA How much voltage to accelerate- Can go unused (0) */
data class SimpleMotorFeedForwardConstants(val kS: Double, val kV: Double, val kA: Double)


/** @property P Proportional to the error (if it's bad, fix it. If it's really bad, fix it harder based on how bad it is)
 *
 * @property I Proportional to the slope (derivative) of the error
 * (If it's OK but it's starting to go bad, fix it ahead of time, and if it's bad but it's
 * getting close to being good, fix it less hard so it doesn't overshoot)
 *
 * @property D Propotional to the integral of the error
 * (If it's been bad for a long time, fix it harder.)
 * Usually very unstable; much safer to leave at 0.0 if possible
 */

data class PIDConstants(val P: Double, val I: Double, val D: Double)

/**
 * @return A WPILib SimpleMotorFeedForward using the feedforward constants in the class
 */
fun SimpleMotorFeedForwardConstants.toFeedForward() : SimpleMotorFeedforward {
    return SimpleMotorFeedforward(this.kS, this.kV, this.kA)
}

/**
 * @return A 5970 Engine Controller.PID controller using the pid constants in the class
 */
fun PIDConstants.toPID() : Controller.PID {
    return Controller.PID(this.P, this.I, this.D)
}