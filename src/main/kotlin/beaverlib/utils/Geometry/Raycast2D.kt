package beaverlib.utils.geometry
// File adapted from 2898 2023 bpsrobotics engine
import beaverlib.utils.Sugar.eqEpsilon
import edu.wpi.first.math.geometry.Pose2d
import beaverlib.utils.Units.Angular.AngleUnit
import beaverlib.utils.Units.Angular.beaverRadians
import beaverlib.utils.Units.Angular.radians
import beaverlib.utils.Units.Linear.DistanceUnit
import beaverlib.utils.Units.Linear.meters
import kotlin.math.*

class Raycast2D(val origin : Vector2, val angle : AngleUnit){
    constructor(pose: Pose2d) : this(Vector2(pose),pose.rotation.beaverRadians)

}