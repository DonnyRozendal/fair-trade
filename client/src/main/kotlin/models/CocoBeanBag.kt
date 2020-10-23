package models

data class CocoBeanBag(
    val id: String,
    val state: State
) {

    enum class State {
        AVAILABLE,
        TAKEN
    }

}