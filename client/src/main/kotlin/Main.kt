import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import routes.*
import services.ChainCodeService

fun main() {
    startServer()
}

private fun startServer() {
    embeddedServer(Netty, 8080, module = Application::module).start(true)
}

private fun Application.module() {
    install(ContentNegotiation) { gson { setPrettyPrinting() } }
    install(DefaultHeaders)
    install(StatusPages)

    install(Routing) {
        test(ChainCodeService())
        createCocoBeanBag(ChainCodeService())
        createChocShipment(ChainCodeService())
        registerStorage(ChainCodeService())
        loadStorage(ChainCodeService())
        validatePrice()
        readAsset(ChainCodeService())
        deleteAsset(ChainCodeService())
        registerAdmin(ChainCodeService())
        registerParticipant(ChainCodeService())
    }
}