package routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import services.ChainCodeService

fun Route.test(chainCodeService: ChainCodeService) {

    route("/test") {

        get {
            call.respond(chainCodeService.test())
        }

    }

}