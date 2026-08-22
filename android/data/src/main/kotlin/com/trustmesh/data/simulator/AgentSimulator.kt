package com.trustmesh.data.simulator

import com.trustmesh.data.remote.TransactionRequest
import com.trustmesh.data.remote.TrustMeshApi
import com.trustmesh.domain.model.Agent
import com.trustmesh.domain.model.Category
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AgentSimulator @Inject constructor(
    private val api: TrustMeshApi,
    private val okHttpClient: OkHttpClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating = _isSimulating.asStateFlow()

    @Serializable
    private data class DummyProductResponse(val products: List<DummyProduct>)
    
    @Serializable
    private data class DummyProduct(val title: String, val category: String, val price: Double)

    private val indianMerchants = mapOf(
        Category.GROCERY to listOf("Blinkit", "Zepto", "Swiggy Instamart", "BigBasket", "JioMart"),
        Category.ELECTRONICS to listOf("Flipkart", "Reliance Digital", "Croma", "Tata Neu"),
        Category.TRAVEL to listOf("MakeMyTrip", "EaseMyTrip", "Uber India", "Ola Cabs"),
        Category.OTHER to listOf("Myntra", "Amazon India", "Nykaa", "Zomato")
    )

    fun start(agent: Agent) {
        if (_isSimulating.value) return
        _isSimulating.value = true

        job = scope.launch {
            while (isActive) {
                try {
                    // Fetch real product details from free public API
                    val request = Request.Builder()
                        .url("https://dummyjson.com/products")
                        .build()
                    val response = okHttpClient.newCall(request).execute()
                    if (response.isSuccessful && response.body != null) {
                        val bodyStr = response.body!!.string()
                        val data = Json { ignoreUnknownKeys = true }.decodeFromString<DummyProductResponse>(bodyStr)
                        val products = data.products
                        if (products.isNotEmpty()) {
                            // Find products matching the agent's authorized categories
                            val matchedProducts = products.filter { p ->
                                val cat = mapToDomainCategory(p.category)
                                agent.categoryScope.contains(cat)
                            }
                            
                            val product = if (matchedProducts.isNotEmpty()) {
                                matchedProducts[Random.nextInt(matchedProducts.size)]
                            } else {
                                products[Random.nextInt(products.size)]
                            }

                            val category = mapToDomainCategory(product.category)
                            val merchantList = indianMerchants[category] ?: indianMerchants[Category.OTHER]!!
                            val merchantName = merchantList[Random.nextInt(merchantList.size)]

                            // Convert USD price to realistic Indian Rupees (1 USD ≈ 85 INR)
                            val originalPriceINR = product.price * 85.0
                            val negotiatedPriceINR = originalPriceINR * (0.80 + Random.nextDouble(0.12)) // 8%-20% discount negotiation
                            val savedINR = originalPriceINR - negotiatedPriceINR

                            val negotiationLog = """
                                Procurement target: ${product.title} (Category: $category)
                                Initial merchant quote: ₹${String.format("%.2f", originalPriceINR)}
                                Bid 1 (Agent): ₹${String.format("%.2f", originalPriceINR * 0.75)}
                                Counter 1 (Merchant): ₹${String.format("%.2f", originalPriceINR * 0.92)}
                                Bid 2 (Agent): ₹${String.format("%.2f", originalPriceINR * 0.82)}
                                Accepted Counter (Final): ₹${String.format("%.2f", negotiatedPriceINR)}
                                Aggregate negotiation outcome: Saved ₹${String.format("%.2f", savedINR)} (discount of ${((savedINR/originalPriceINR)*100).toInt()}%).
                            """.trimIndent()

                            // Fire transaction request to backend enforcer
                            val txReq = TransactionRequest(
                                agentId = agent.id,
                                merchantName = merchantName,
                                merchantCategory = category.name,
                                amount = negotiatedPriceINR,
                                negotiationDetail = negotiationLog
                            )
                            
                            api.requestTransaction(txReq)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(20000) // Simulate periodic actions every 20 seconds
            }
        }
    }

    fun stop() {
        job?.cancel()
        _isSimulating.value = false
    }

    private fun mapToDomainCategory(category: String): Category {
        return when (category.lowercase()) {
            "groceries" -> Category.GROCERY
            "laptops", "smartphones", "tablets" -> Category.ELECTRONICS
            "automotive", "motorcycle" -> Category.TRAVEL
            "home-decoration", "furniture" -> Category.OTHER
            else -> Category.OTHER
        }
    }
}
