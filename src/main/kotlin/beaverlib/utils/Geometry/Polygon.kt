package beaverlib.utils.geometry
// File adapted from 2898's bpsrobotics engine 
import edu.wpi.first.math.geometry.Pose2d
import beaverlib.utils.Units.Angular.radians
import beaverlib.utils.Units.Linear.DistanceUnit
import kotlin.math.PI

/**
 * A two-dimensional shape with at least 3 sides. (Sides cannot intersect)
 * @property coordinates List of polygon vertices of the polygon
 * @author Ozy King
 */
class Polygon(vararg val coordinates : Vector2){
    val lines = mutableListOf<Line>()
    init {
        for ((p1, p2) in coordinates.toList().plus(coordinates.first()).zipWithNext()){
            lines.add(Line(p1,p2))
        }
    }

    /**
     * Returns true if the coordinate is within the bounds of the polygon
     * @param coordinate Coordinate to check
     * @return If point is in polygon
     * @author Ozy King
     */
    fun contains(coordinate: Vector2) : Boolean {
        var intersections = 0
        for (i in lines){
            if (i.intersects(Raycast2D(coordinate, PI.radians))) intersections++
        }
        return intersections % 2 == 1
    }
    /**
     * Returns true if the point is within the bounds of the polygon
     * @param x X value of the point to check
     * @param y Y value of the point to check
     * @return If point is in polygon
     * @author Ozy King
     */
    fun contains(x: Double, y: Double) : Boolean{
        return contains(Vector2(x,y))
    }
    /**
     * Returns true if the pose is within the bounds of the polygon
     * @param pose The Pose2d value of the point to check
     * @return If point is in polygon
     * @author Ozy King
     */
    fun contains(pose: Pose2d): Boolean{
        return contains(Vector2(pose))
    }
    /**
     * Returns a polygon reflected over the x value given
     * @param x X value of the vertical line to reflect over
     * @return Reflected polygon
     * @author Ozy King
     */
    fun reflectHorizontally(x: Double) : Polygon{
        val newCoordinates = coordinates.map { it.reflectHorizontally(x) }
        return Polygon(*newCoordinates.toTypedArray())
    }
}