//package frc.robot.subsystems
//
//import edu.wpi.first.wpilibj.Compressor
//import edu.wpi.first.wpilibj.DoubleSolenoid
//import edu.wpi.first.wpilibj.PneumaticsModuleType
//import edu.wpi.first.wpilibj.Solenoid
//import edu.wpi.first.wpilibj2.command.SubsystemBase
//
//object Pneumatics : SubsystemBase() {
//    val compressor = Compressor(PneumaticsModuleType.CTREPCM)
//    val leftSolenoid = DoubleSolenoid(PneumaticsModuleType.CTREPCM, 4, 6)
//    val rightSolenoid = DoubleSolenoid(PneumaticsModuleType.CTREPCM, 5, 7)
//    init {
//        compressor.enableDigital()
//    }
//
//    fun set(state: DoubleSolenoid.Value) {
//        leftSolenoid.set(state)
//        rightSolenoid.set(state)
//    }
//}