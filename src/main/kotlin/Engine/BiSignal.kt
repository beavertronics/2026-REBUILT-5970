package Engine

/**
 * A MutableMap used specifically for managing Lambdas
 * Specified Type is used as the input for argument for listener Lambdas
 */
data class DoubleSignal<Type, Type2>(
    val listeners : MutableMap<String, (Type, Type2) -> Unit> = mutableMapOf()
) {

    /**
     * Adds a listener with the given name. The function will be run whenever the parent object calls the update function
     * @param name String to denote the name of the listener, used to remove specific listeners later on
     * @param function The function to run when the listener updates
     */
    fun add(name: String, function: (Type, Type2) -> Unit) {
        listeners.put(name, function)
    }


    /**
     * Removes a listener with the given name
     * @param name The name of the listener to remove
     */
    fun remove(name: String) {
        listeners.remove(name)
    }


    /**
     * Runs all active listeners with the given input
     * @param input The input for each of the listening functions
     */
    fun update(input: Type, input2: Type2) {
        listeners.forEach { (_, listenerFunction) ->
            listenerFunction(input, input2)  // Pass source to the listener
        }
    }
}