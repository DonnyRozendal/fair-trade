package routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import services.ChainCodeService

fun Route.readAsset(chainCodeService: ChainCodeService) {

    route("/read-asset") {

        get("/{type}/{id}") {
            val type = call.parameters["type"] ?: ""
            val id = call.parameters["id"] ?: ""

            call.respond(chainCodeService.read(type, id))
        }

    }

}