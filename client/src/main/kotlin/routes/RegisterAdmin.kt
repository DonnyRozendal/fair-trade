package routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import services.ChainCodeService

fun Route.registerAdmin(chainCodeService: ChainCodeService) {

    route("/register-admin") {

        post {
            call.respond(chainCodeService.registerAdmin())
        }

    }

}