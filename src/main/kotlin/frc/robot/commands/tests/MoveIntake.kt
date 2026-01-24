//package frc.robot.commands.tests
//
//import edu.wpi.first.wpilibj.DoubleSolenoid
//import edu.wpi.first.wpilibj2.command.Command
//import frc.robot.subsystems.Pneumatics
//
//class MoveIntake(val state: DoubleSolenoid.Value) : Command() {
//
//    init {
//        addRequirements(Pneumatics)
//    }
//
//    override fun execute() {
//        Pneumatics.set(state)
//    }
//
//    override fun isFinished(): Boolean {
//        return false
//    }
//}