package routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import services.ChainCodeService

fun Route.loadStorage(chainCodeService: ChainCodeService) {

    route("/load-storage") {

        post("/{participant-id}/{storage-id}/{shipment-id}") {
            val participantId = call.parameters["participant-id"] ?: ""
            val storageId = call.parameters["storage-id"] ?: ""
            val shipmentId = call.parameters["shipment-id"] ?: ""

            call.respond(chainCodeService.loadStorage(participantId, storageId, shipmentId))
        }

    }

}