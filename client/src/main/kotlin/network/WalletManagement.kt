package network

import models.Participant
import org.hyperledger.fabric.gateway.Identities
import org.hyperledger.fabric.gateway.Wallet
import org.hyperledger.fabric.gateway.Wallets
import org.hyperledger.fabric.gateway.X509Identity
import org.hyperledger.fabric.sdk.Enrollment
import org.hyperledger.fabric.sdk.User
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory
import org.hyperledger.fabric_ca.sdk.Attribute
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest
import org.hyperledger.fabric_ca.sdk.HFCAClient
import org.hyperledger.fabric_ca.sdk.RegistrationRequest
import java.nio.file.Paths
import java.security.PrivateKey
import java.util.*

fun main() {
    enrollAdmin()
}

private val caClient: HFCAClient
    get() {
        val props = Properties().apply {
            put(
                "pemFile",
                "../test-network/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem"
            )
            put("allowAllHostNames", "true")
        }
        return HFCAClient.createNewInstance("https://localhost:7054", props).apply {
            cryptoSuite = CryptoSuiteFactory.getDefault().cryptoSuite
        }
    }

private val wallet: Wallet
    get() {
        return Wallets.newFileSystemWallet(Paths.get("wallet"))
    }

fun enrollAdmin(): String {
    if (wallet["admin"] != null) {
        return "An identity for the admin user \"admin\" already exists in the wallet"
    }

    val enrollmentRequestTLS = EnrollmentRequest().apply {
        addHost("localhost")
        profile = "tls"
    }

    val enrollment = caClient.enroll("admin", "adminpw", enrollmentRequestTLS)
    val user = Identities.newX509Identity("Org1MSP", enrollment)
    wallet.put("admin", user)

    return "Successfully enrolled user \"admin\" and imported it into the wallet"
}

fun registerUser(participant: Participant): String {
    if (wallet[participant.id] != null) {
        return "An identity for the user \"${participant.id}\" already exists in the wallet"
    }

    val adminIdentity = wallet["admin"] as X509Identity?
        ?: return "\"admin\" needs to be enrolled and added to the wallet first"

    val admin = object : User {
        override fun getName(): String {
            return "admin"
        }

        override fun getRoles(): Set<String>? {
            return null
        }

        override fun getAccount(): String? {
            return null
        }

        override fun getAffiliation(): String {
            return "org1.department1"
        }

        override fun getEnrollment(): Enrollment {
            return object : Enrollment {
                override fun getKey(): PrivateKey {
                    return adminIdentity.privateKey
                }

                override fun getCert(): String {
                    return Identities.toPemString(adminIdentity.certificate)
                }
            }
        }

        override fun getMspId(): String {
            return "Org1MSP"
        }
    }

    val registrationRequest = RegistrationRequest(participant.id).apply {
        affiliation = "org1.department1"
        enrollmentID = participant.id
        addAttribute(Attribute("id", participant.id))
        addAttribute(Attribute("name", participant.name))
        addAttribute(Attribute("role", participant.role.name))
    }
    val enrollmentSecret = caClient.register(registrationRequest, admin)

    val enrollmentRequest = EnrollmentRequest().apply {
        addAttrReq("id")
        addAttrReq("name")
        addAttrReq("role")
    }
    val enrollment = caClient.enroll(participant.id, enrollmentSecret, enrollmentRequest)

    val user = Identities.newX509Identity("Org1MSP", enrollment)
    wallet.put(participant.id, user)

    wallet[participant.id].toString()

    return "Successfully enrolled user \"${participant.id}\" and imported it into the wallet"
}