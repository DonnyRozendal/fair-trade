package nl.hva.fairtrade.contract

import com.google.gson.Gson
import nl.hva.fairtrade.models.Asset
import nl.hva.fairtrade.models.Role
import nl.hva.fairtrade.models.assets.ChocShipment
import nl.hva.fairtrade.models.assets.CocoBeanBag
import nl.hva.fairtrade.models.assets.Participant
import nl.hva.fairtrade.models.assets.Storage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.hyperledger.fabric.contract.Context
import org.hyperledger.fabric.contract.ContractInterface
import org.hyperledger.fabric.contract.annotation.Contract
import org.hyperledger.fabric.contract.annotation.Default
import org.hyperledger.fabric.contract.annotation.Transaction
import org.hyperledger.fabric.shim.ChaincodeException

const val FAIR_PRICE = 15.20

@Contract
@Default
class FairTradeContract : ContractInterface {

    private val gson = Gson()

    // Arbitrary test function
    @Transaction
    fun test(ctx: Context): String {
        val json = """{"id":"1","bagIds":["1","2"],"pricePaidPerBag":15.20}"""
        return validatePrice(json)
    }

    @Transaction
    fun createCocoBeanBag(ctx: Context, json: String) {
        if (!isFarmer(ctx)) {
            throw ChaincodeException("Only a farmer can create a CocoBeanBag")
        }

        // Create the asset
        val cocoBeanBag = try {
            gson.fromJson(json, CocoBeanBag::class.java)
        } catch (exception: Exception) {
            throw ChaincodeException(exception)
        }
        create(ctx, cocoBeanBag)

        // Query all CocoBeanBags, and send an event when there are enough CocoBeanBags
        val bagAmountRequirement = 2

        val query = """{"selector":{"_id":{"${"$"}regex":"CocoBeanBag"}}}"""
        val queryResult = ctx.stub.getQueryResult(query)
        val availableBags = queryResult.map {
            gson.fromJson(it.stringValue, CocoBeanBag::class.java)
        }.run {
            toMutableList().apply {
                add(cocoBeanBag)
            }
        }.filter {
            it.state == CocoBeanBag.State.AVAILABLE
        }

        if (availableBags.size >= bagAmountRequirement) {
            val payload = gson.toJson(availableBags).toByteArray()
            ctx.stub.setEvent("bagMinimumReached", payload)
        }
    }

    @Transaction
    fun createChocShipment(ctx: Context, json: String) {
        if (!isProducer(ctx)) {
            throw ChaincodeException("Only a producer can create a ChocShipment")
        }

        val chocShipment = try {
            gson.fromJson(json, ChocShipment::class.java)
        } catch (exception: Exception) {
            throw ChaincodeException(exception)
        }

        // Check if the shipment contains unavailable bags
        val unavailableBags = chocShipment.bagIds.map {
            gson.fromJson(read(ctx, "CocoBeanBag:$it"), CocoBeanBag::class.java)
        }.filter {
            it.state == CocoBeanBag.State.TAKEN
        }

        if (unavailableBags.isNotEmpty()) {
            val unavailableBagIds = unavailableBags.joinToString { it.id }
            throw ChaincodeException("The given bags: $unavailableBagIds are not available")
        }

        // Validate the price paid per bag
        if (chocShipment.pricePaidPerBag >= FAIR_PRICE) {
            // Update the used bean bags with the new state
            for (bagId in chocShipment.bagIds) {
                val updatedBag = CocoBeanBag(bagId, CocoBeanBag.State.TAKEN)
                update(ctx, updatedBag)
            }

            // Create the shipment
            create(ctx, chocShipment)

            // Notify the store that the shipment is ready
            val payload = gson.toJson(chocShipment).toByteArray()
            ctx.stub.setEvent("shipmentReady", payload)
        } else {
            throw ChaincodeException("Price denied")
        }
    }

    @Transaction
    fun registerStorage(ctx: Context, json: String) {
        if (!isStore(ctx)) {
            throw ChaincodeException("Only a store can register storage")
        }

        val storage = try {
            gson.fromJson(json, Storage::class.java)
        } catch (exception: Exception) {
            throw ChaincodeException(exception)
        }

        create(ctx, storage)
    }

    @Transaction
    fun loadStorage(ctx: Context, storageId: String, shipmentId: String) {
        if (!isStore(ctx)) {
            throw ChaincodeException("Only a store can load storage")
        }

        val storage = try {
            gson.fromJson(read(ctx, "Storage:$storageId"), Storage::class.java)
        } catch (exception: Exception) {
            throw ChaincodeException(exception)
        }

        // Check if shipment is available
        val shipment = gson.fromJson(read(ctx, "ChocShipment:$shipmentId"), ChocShipment::class.java)
        if (shipment.state == ChocShipment.State.DELIVERED) {
            throw ChaincodeException("The shipment: ${shipment.id} is not available")
        }

        // Update shipment state
        val updatedShipment = shipment.copy(state = ChocShipment.State.DELIVERED)
        update(ctx, updatedShipment)

        // Update storage
        val updatedShipmentIds = storage.shipmentIds.run { toMutableList().apply { add(shipmentId) } }
        val updatedStorage = storage.copy(shipmentIds = updatedShipmentIds)

        update(ctx, updatedStorage)
    }

    @Transaction
    fun createParticipant(ctx: Context, id: String, name: String, role: String) {
        if (!isAdmin(ctx)) {
            throw ChaincodeException("Only an admin may create participants")
        }

        val roleEnum = try {
            Role.valueOf(role)
        } catch (illegalArgumentException: IllegalArgumentException) {
            throw ChaincodeException("Role $role is unknown")
        }
        val participant = Participant(id, name, roleEnum)

        create(ctx, participant)
    }

    @Transaction
    fun readAsset(ctx: Context, type: String, id: String): String {
        val key = "$type:$id"
        return read(ctx, key)
    }

    @Transaction
    fun deleteAsset(ctx: Context, type: String, id: String) {
        val key = "$type:$id"
        delete(ctx, key)
    }

    private fun create(ctx: Context, asset: Asset) {
        if (assetExists(ctx, asset.key)) {
            throw ChaincodeException("The asset ${asset.key} already exists")
        } else {
            ctx.stub.putStringState(asset.key, asset.value)
        }
    }

    private fun read(ctx: Context, key: String): String {
        return if (!assetExists(ctx, key)) {
            throw ChaincodeException("The asset $key does not exist")
        } else {
            ctx.stub.getStringState(key)
        }
    }

    private fun update(ctx: Context, asset: Asset) {
        if (!assetExists(ctx, asset.key)) {
            throw ChaincodeException("The asset ${asset.key} does not exist")
        } else {
            ctx.stub.putStringState(asset.key, asset.value)
        }
    }

    private fun delete(ctx: Context, key: String) {
        if (!assetExists(ctx, key)) {
            throw ChaincodeException("The asset $key does not exist")
        } else {
            ctx.stub.delState(key)
        }
    }

    private fun assetExists(ctx: Context, assetId: String): Boolean {
        val buffer = ctx.stub.getState(assetId)
        return buffer != null && buffer.isNotEmpty()
    }

    private fun isAdmin(ctx: Context): Boolean {
        val regex = """x509::CN=(?<user>admin)""".toRegex()
        val result = regex.find(ctx.clientIdentity.id)?.groups?.get("user")?.value

        return result == "admin"
    }

    private fun isFarmer(ctx: Context): Boolean {
        return ctx.clientIdentity.assertAttributeValue("role", Role.FARMER.name)
    }

    private fun isProducer(ctx: Context): Boolean {
        return ctx.clientIdentity.assertAttributeValue("role", Role.PRODUCER.name)
    }

    private fun isStore(ctx: Context): Boolean {
        return ctx.clientIdentity.assertAttributeValue("role", Role.STORE.name)
    }

    // This function was supposed to execute an external call to check if the price paid per bag
    // is sufficient. Unfortunately it doesn't work for some reason.
    private fun validatePrice(json: String): String {
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://localhost:8080/validate-price")
            .post(body)
            .build()
        return OkHttpClient().newCall(request).execute().body?.string() ?: ""
    }

}