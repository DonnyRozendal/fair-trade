package routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import services.ChainCodeService

fun Route.createCocoBeanBag(chainCodeService: ChainCodeService) {

    route("/create-cocobeanbag") {

        post("/{participant-id}") {
            val participantId = call.parameters["participant-id"] ?: ""
            val json = call.receiveText()

            call.respond(chainCodeService.createCocoBeanBag(participantId, json))
        }

    }

}