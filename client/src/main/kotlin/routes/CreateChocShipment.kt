package routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import services.ChainCodeService

fun Route.createChocShipment(chainCodeService: ChainCodeService) {

    route("/create-chocshipment") {

        post("/{participant-id}") {
            val participantId = call.parameters["participant-id"] ?: ""
            val json = call.receiveText()

            call.respond(chainCodeService.createChocShipment(participantId, json))
        }

    }

}