package models

data class ChocShipment(
    val id: String,
    val bagIds: List<String>,
    val pricePaidPerBag: Double,
    val state: State
) {

    enum class State {
        READY_TO_SHIP,
        DELIVERED
    }

}