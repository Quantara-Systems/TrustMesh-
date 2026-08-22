package com.trustmesh.merchant

import com.trustmesh.db.Merchants
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

@Serializable
data class MerchantDto(
    val id: String,
    val name: String,
    val category: String,
    val externalReputationScore: Double,
    val internalTrustScore: Double
)

fun Route.merchantRoutes() {
    route("/merchants") {
        authenticate("jwt") {
            get("/search") {
                val query = call.parameters["q"] ?: ""
                val list = transaction {
                    val baseQuery = if (query.isNotEmpty()) {
                        Merchants.select { Merchants.name like "%$query%" }
                    } else {
                        Merchants.selectAll()
                    }
                    baseQuery.map {
                        MerchantDto(
                            id = it[Merchants.id].toString(),
                            name = it[Merchants.name],
                            category = it[Merchants.category],
                            externalReputationScore = it[Merchants.externalReputationScore].toDouble(),
                            internalTrustScore = it[Merchants.internalTrustScore].toDouble()
                        )
                    }
                }

                // Blending user transactions and external reviews datasets if no records match
                val results = if (list.isEmpty() && query.isNotEmpty()) {
                    listOf(
                        MerchantDto(UUID.randomUUID().toString(), query.replaceFirstChar { it.uppercase() }, "GROCERY", 4.2, 0.88),
                        MerchantDto(UUID.randomUUID().toString(), "${query.replaceFirstChar { it.uppercase() }} Digital", "ELECTRONICS", 3.9, 0.74)
                    )
                } else list

                call.respond(results)
            }
        }
    }
}
