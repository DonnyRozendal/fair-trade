package nl.hva.fairtrade.models.assets

import nl.hva.fairtrade.models.Asset
import org.hyperledger.fabric.contract.annotation.DataType
import org.hyperledger.fabric.contract.annotation.Property

@DataType
data class Storage(
    @Property override val id: String,
    @Property val shipmentIds: List<String>
) : Asset()