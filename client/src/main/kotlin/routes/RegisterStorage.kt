package routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import services.ChainCodeService

fun Route.registerStorage(chainCodeService: ChainCodeService) {

    route("/register-storage") {

        post("/{participant-id}") {
            val participantId = call.parameters["participant-id"] ?: ""
            val json = call.receiveText()

            call.respond(chainCodeService.registerStorage(participantId, json))
        }

    }

}