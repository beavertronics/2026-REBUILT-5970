package Engine

import Engine.BeaverDutyCycleEncoder.Companion.angleDistanceTo
import beaverlib.utils.Units.Angular.radians
import beaverlib.utils.Units.Angular.radiansPerSecond
import beaverlib.utils.Units.seconds
import com.revrobotics.AbsoluteEncoder
import com.revrobotics.RelativeEncoder
import edu.wpi.first.wpilibj.Timer

class BeaverAbsoluteToRelativeEncoder(private val encoder : RelativeEncoder, private val ratio: Double = 1.0) { // todo relative or absolute?
    private val rateTimer = Timer()
    private var lastPos = 0.0.radians
    var rate = 0.0.radiansPerSecond
    var position = 0.0.radians

    init { rateTimer.restart() }

    /**
     * Run every frame to update the encoders rate
     */
    fun updateRate() {
        val minDist = lastPos.angleDistanceTo(encoder.position.radians)
        rate = (minDist * ratio) / rateTimer.get().seconds
        position += (minDist * ratio)
        lastPos = encoder.position.radians
        rateTimer.restart()
    }
}