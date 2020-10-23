package nl.hva.fairtrade.models.assets

import nl.hva.fairtrade.models.Asset
import nl.hva.fairtrade.models.Role
import org.hyperledger.fabric.contract.annotation.DataType
import org.hyperledger.fabric.contract.annotation.Property

@DataType
data class Participant(
    @Property override val id: String,
    @Property val name: String,
    @Property val role: Role
) : Asset()