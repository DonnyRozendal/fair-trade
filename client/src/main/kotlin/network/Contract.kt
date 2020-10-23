package network

import org.hyperledger.fabric.gateway.Contract
import org.hyperledger.fabric.gateway.Gateway
import org.hyperledger.fabric.gateway.Wallets
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.file.Paths

fun createContract(userId: String): Contract {
    System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true")

    val walletPath = Paths.get("wallet")
    val wallet = Wallets.newFileSystemWallet(walletPath)

    val networkConfigPath = Paths.get(
        "..",
        "test-network",
        "organizations",
        "peerOrganizations",
        "org1.example.com",
        "connection-org1.yaml"
    )
    val gatewayBuilder = Gateway.createBuilder()
        .identity(wallet, userId)
        .networkConfig(networkConfigPath)
        .discovery(true)

    return gatewayBuilder.connect()
        .getNetwork("mychannel")
        .getContract("chaincode")
}

fun Contract.notify(eventName: String, function: (payload: String) -> String) {
    addContractListener({ event ->
        object : WebSocketClient(URI("http://localhost:${WEB_SOCKET_PORT}/")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                send(eventName)
                send(function(String(event.payload.get())))
                close()
            }

            override fun onMessage(message: String?) {}
            override fun onClose(code: Int, reason: String?, remote: Boolean) {}
            override fun onError(ex: java.lang.Exception?) {}
        }.connect()

    }, eventName)
}