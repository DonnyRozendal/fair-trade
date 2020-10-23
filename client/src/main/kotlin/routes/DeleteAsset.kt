package routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import services.ChainCodeService

fun Route.deleteAsset(chainCodeService: ChainCodeService) {

    route("/delete-asset") {

        delete("/{type}/{id}") {
            val type = call.parameters["type"] ?: ""
            val id = call.parameters["id"] ?: ""

            call.respond(chainCodeService.delete(type, id))
        }

    }

}