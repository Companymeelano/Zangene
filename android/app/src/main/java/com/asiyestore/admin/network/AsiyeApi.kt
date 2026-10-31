package com.asiyestore.admin.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * ASIYE STORE — Unified Retrofit API Interface (v2.0)
 * Note: Authorization Bearer header is injected centrally via OkHttp AuthInterceptor
 * instead of passing raw tokens manually on every endpoint.
 */
interface AsiyeApi {

    // Authentication
    @POST("v1/auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): OtpSentResponse

    @POST("v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): AuthTokenResponse

    @POST("v1/auth/admin/login")
    suspend fun adminLogin(@Body request: AdminLoginRequest): AuthTokenResponse

    // Storefront Catalog
    @GET("v1/products")
    suspend fun getProducts(
        @Query("category") categoryId: Long? = null,
        @Query("search") search: String? = null,
        @Query("sort_by") sortBy: String = "created_at",
        @Query("direction") direction: String = "desc"
    ): PaginatedProductResponse

    // Admin Executive Suite (RBAC Protected)
    @GET("v1/admin/dashboard/stats")
    suspend fun getAdminStats(): DashboardStatsDto

    @GET("v1/admin/products")
    suspend fun getAdminProducts(): PaginatedProductResponse

    @POST("v1/admin/products")
    suspend fun createProduct(@Body request: CreateProductRequest): ProductDto

    @PUT("v1/admin/products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: Long,
        @Body request: UpdateProductRequest
    ): ProductDto

    @DELETE("v1/admin/products/{id}")
    suspend fun deleteProduct(@Path("id") productId: Long): MessageResponse

    @POST("v1/admin/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Long,
        @Body request: UpdateOrderStatusRequest
    ): OrderDto

    @POST("v1/admin/inventory/adjust")
    suspend fun adjustInventory(@Body request: AdjustInventoryRequest): MessageResponse

    // Customer Atomic Checkout & BNPL Installments
    @POST("v1/checkout")
    suspend fun processCheckout(@Body request: CheckoutRequest): CheckoutResponse

    @POST("v1/installment/request")
    suspend fun requestInstallment(@Body request: InstallmentRequest): InstallmentResponse
}

// DTOs accurately mapped to Laravel Eloquent JSON payloads
data class SendOtpRequest(val mobile: String)
data class OtpSentResponse(val message: String, val expires_in: Int)
data class VerifyOtpRequest(val mobile: String, val otp: String)
data class AdminLoginRequest(val mobile_or_email: String, val password: String)
data class AuthTokenResponse(val access_token: String, val token_type: String)
data class MessageResponse(val message: String)

data class PaginatedProductResponse(
    val data: List<ProductDto>,
    val current_page: Int,
    val total: Int
)

data class ProductImageDto(
    val id: Long,
    val path: String,
    val is_primary: Boolean = false
)

data class InventoryDto(
    val id: Long,
    val quantity: Int,
    val low_stock_threshold: Int = 5
)

data class ProductVariantDto(
    val id: Long,
    val color: String? = null,
    val size: String? = null,
    val price_offset: String = "0.00",
    val inventory: InventoryDto? = null
)

data class ProductDto(
    val id: Long,
    val name_fa: String,
    val name_en: String,
    val sku: String,
    val sale_price: String,
    val original_price: String,
    val discount_percent: Int = 0,
    val published: Boolean = true,
    val total_inventory: Int = 0,
    val primary_image: String? = null,
    val images: List<ProductImageDto> = emptyList(),
    val variants: List<ProductVariantDto> = emptyList()
)

data class DashboardStatsDto(
    val total_sales_today: Long = 0,
    val total_sales_month: Long = 0,
    val new_orders_count: Int = 0,
    val low_inventory_count: Int = 0,
    val new_customers_count: Int = 0,
    val installment_orders_count: Int = 0
)

data class CreateProductRequest(
    val name_fa: String,
    val name_en: String,
    val brand_id: Long,
    val category_id: Long,
    val description: String,
    val country: String,
    val original_price: Double,
    val sale_price: Double,
    val sku: String,
    val initial_stock: Int = 0
)

data class UpdateProductRequest(
    val name_fa: String? = null,
    val original_price: Double? = null,
    val sale_price: Double? = null,
    val published: Boolean? = null
)

data class UpdateOrderStatusRequest(
    val status: String,
    val tracking_code: String? = null,
    val notes: String? = null
)

data class AdjustInventoryRequest(
    val product_variant_id: Long,
    val type: String, // "in", "out", "adjustment"
    val quantity: Int,
    val reason: String
)

data class OrderDto(
    val id: Long,
    val order_number: String,
    val status: String,
    val total_amount: String
)

data class OrderItemRequest(val variant_id: Long, val quantity: Int)

data class CheckoutRequest(
    val items: List<OrderItemRequest>,
    val address_id: Long,
    val shipping_method_id: Long,
    val payment_gateway_id: Long?,
    val payment_type: String = "online",
    val coupon_code: String? = null
)

data class CheckoutResponse(
    val order_id: Long,
    val order_number: String,
    val total_amount: String,
    val message: String,
    val payment_authority: String?
)

data class InstallmentRequest(
    val order_id: Long,
    val provider: String, // "snapp_pay" | "tara"
    val installments: Int // 2, 3, 4, 6
)

data class InstallmentResponse(
    val installment_order_id: Long,
    val request_id: String,
    val per_installment_amount: Double,
    val status: String,
    val message: String
)
