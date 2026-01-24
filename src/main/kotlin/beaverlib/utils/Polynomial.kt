package frc.engine.utils
// File adapted from 2898's bpsrobotics engine
import kotlin.math.pow

class Polynomial(vararg coefficients: Double) {
    val coef = coefficients

    fun calculate(value: Double) : Double {
        var returnValue = 0.0
        for (i: Int in 0..coef.size-1){
            returnValue += coef[i]*(value.pow(coef.size-i))
        }
        return returnValue
    }
    fun calculate(value: Int) : Double = calculate(value.toDouble())
}