package nl.hva.fairtrade.models.assets

import nl.hva.fairtrade.models.Asset
import org.hyperledger.fabric.contract.annotation.DataType
import org.hyperledger.fabric.contract.annotation.Property

@DataType
class CocoBeanBag(
    @Property override val id: String,
    @Property val state: State
) : Asset() {

    enum class State {
        AVAILABLE,
        TAKEN
    }

}