package routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import models.Participant
import services.ChainCodeService

fun Route.registerParticipant(chainCodeService: ChainCodeService) {

    route("/register-participant") {

        post {
            val participant = call.receive<Participant>()

            call.respond(chainCodeService.registerParticipant(participant))
        }

    }

}