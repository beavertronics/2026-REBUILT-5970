package frc.beaverlib.misc

class AggregateException(message: String?, primary: Exception?, vararg others: Throwable) : Exception(message, primary) {
    init {
        others.forEach {err -> addSuppressed(err)}
    }
}