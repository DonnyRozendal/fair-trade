package nl.hva.fairtrade.models.assets

import nl.hva.fairtrade.models.Asset
import org.hyperledger.fabric.contract.annotation.DataType
import org.hyperledger.fabric.contract.annotation.Property

@DataType
data class ChocShipment(
    @Property override val id: String,
    @Property val bagIds: List<String>,
    @Property val pricePaidPerBag: Double,
    @Property val state: State
) : Asset() {

    enum class State {
        READY_TO_SHIP,
        DELIVERED
    }

}