package routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import models.ChocShipment

const val FAIR_PRICE = 15.20

fun Route.validatePrice() {

    route("/validate-price") {

        post {
            val chocShipment = call.receive<ChocShipment>()

            if (chocShipment.pricePaidPerBag >= FAIR_PRICE) {
                call.respond("Price accepted")
            } else {
                call.respond(HttpStatusCode.SwitchProxy, "Price denied")
            }
        }

    }

}