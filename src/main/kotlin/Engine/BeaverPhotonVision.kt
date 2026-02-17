package Engine

import edu.wpi.first.math.Matrix
import edu.wpi.first.math.Nat
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N3
import edu.wpi.first.wpilibj2.command.SubsystemBase
import org.photonvision.targeting.PhotonPipelineResult

class BeaverPhotonVision(vararg val cameras: VisionCamera) : SubsystemBase() {
    val listeners = DoubleSignal<PhotonPipelineResult, VisionCamera>()

    override fun periodic() {
        for(camera in cameras){
            for (result in camera.results) {
                listeners.update(result, camera)
            }
        }
    }

    /** Updates all of the cameras pose estimators with the given pose */
    fun setAllCameraReferences(pose: Pose2d) {
        for(camera in cameras) {
            camera.setReference(pose)
        }
    }

    fun getStandardDev(rotationSTD: Double): Matrix<N3, N1>{
        val stdv = Matrix(Nat.N3(), Nat.N1())
        stdv.set(0,0, 3.0)
        stdv.set(1,0, 3.0)
        stdv.set(2,0, rotationSTD)
        return stdv
    }
}