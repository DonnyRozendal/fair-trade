package services

import com.google.gson.Gson
import models.ChocShipment
import models.CocoBeanBag
import models.Participant
import network.createContract
import network.enrollAdmin
import network.notify
import network.registerUser
import org.hyperledger.fabric.gateway.Contract

class ChainCodeService {

    private val gson = Gson()

    private val adminContract by lazy {
        createContract("admin")
    }

    private var farmerContract: Contract? = null
    private var producerContract: Contract? = null
    private var storeContract: Contract? = null

    fun test(): String {
        return try {
            adminContract.notify("test") { it }
            String(adminContract.submitTransaction("test"))
        } catch (exception: Exception) {
            exception.toString()
        }
    }

    fun createCocoBeanBag(participantId: String, json: String): String {
        try {
            if (farmerContract == null) {
                farmerContract = createContract(participantId).apply {
                    notify("bagMinimumReached") { payload ->
                        val availableBags = gson.fromJson(payload, Array<CocoBeanBag>::class.java)
                        availableBags.map {
                            it.id
                        }.toString()
                    }
                }
            }

            farmerContract?.submitTransaction("createCocoBeanBag", json)
        } catch (exception: Exception) {
            return exception.toString()
        }

        return "Transaction executed"
    }

    fun createChocShipment(participantId: String, json: String): String {
        try {
            if (producerContract == null) {
                producerContract = createContract(participantId).apply {
                    notify("shipmentReady") { payload ->
                        gson.fromJson(payload, ChocShipment::class.java).id
                    }
                }
            }

            producerContract?.submitTransaction("createChocShipment", json)
        } catch (exception: Exception) {
            return exception.toString()
        }

        return "Transaction executed"
    }

    fun registerStorage(participantId: String, json: String): String {
        try {
            if (storeContract == null) {
                storeContract = createContract(participantId)
            }
            storeContract?.submitTransaction("registerStorage", json)
        } catch (exception: Exception) {
            return exception.toString()
        }

        return "Transaction executed"
    }

    fun loadStorage(participantId: String, storageId: String, shipmentId: String): String {
        try {
            if (storeContract == null) {
                storeContract = createContract(participantId)
            }
            storeContract?.submitTransaction("loadStorage", storageId, shipmentId)
        } catch (exception: Exception) {
            return exception.toString()
        }

        return "Transaction executed"
    }

    fun read(type: String, id: String): Any {
        return try {
            gson.fromJson(String(adminContract.evaluateTransaction("readAsset", type, id)), Any::class.java)
        } catch (exception: Exception) {
            exception.toString()
        }
    }

    fun delete(type: String, id: String): String {
        try {
            adminContract.submitTransaction("deleteAsset", type, id)
        } catch (exception: Exception) {
            return exception.toString()
        }

        return "Asset deleted"
    }

    fun registerAdmin(): String {
        try {
            enrollAdmin()
        } catch (exception: Exception) {
            return exception.toString()
        }

        return "Admin registered"
    }

    fun registerParticipant(participant: Participant): String {
        try {
            adminContract.submitTransaction(
                "createParticipant",
                participant.id,
                participant.name,
                participant.role.name
            )
            registerUser(participant)
        } catch (exception: Exception) {
            return exception.toString()
        }

        return "Transaction executed and user registered"
    }

}