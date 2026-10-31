package com.asiyestore.admin.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.asiyestore.admin.network.AsiyeApi
import com.asiyestore.admin.network.ProductDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalSalesToday: Long = 0,
    val totalSalesMonth: Long = 0,
    val newOrdersCount: Int = 0,
    val lowInventoryCount: Int = 0,
    val newCustomersCount: Int = 0,
    val installmentOrdersCount: Int = 0
)

data class ProductDomainModel(
    val id: Long,
    val nameFa: String,
    val salePrice: Long,
    val originalPrice: Long,
    val discountPercent: Int,
    val sku: String,
    val imageUrl: String?,
    val inventory: Int,
    val published: Boolean
)

private fun ProductDto.toDomain(): ProductDomainModel = ProductDomainModel(
    id = id,
    nameFa = name_fa,
    salePrice = sale_price.toDoubleOrNull()?.toLong() ?: 0L,
    originalPrice = original_price.toDoubleOrNull()?.toLong() ?: 0L,
    discountPercent = discount_percent,
    sku = sku,
    imageUrl = primary_image ?: images.firstOrNull()?.path,
    inventory = if (total_inventory > 0) total_inventory else variants.sumOf { it.inventory?.quantity ?: 0 },
    published = published
)

class AdminRepository(private val api: AsiyeApi) {

    suspend fun fetchDashboardStats(): Result<DashboardStats> = runCatching {
        val dto = api.getAdminStats()
        DashboardStats(
            totalSalesToday = dto.total_sales_today,
            totalSalesMonth = dto.total_sales_month,
            newOrdersCount = dto.new_orders_count,
            lowInventoryCount = dto.low_inventory_count,
            newCustomersCount = dto.new_customers_count,
            installmentOrdersCount = dto.installment_orders_count
        )
    }

    suspend fun fetchProducts(): Result<List<ProductDomainModel>> = runCatching {
        api.getAdminProducts().data.map { it.toDomain() }
    }
}

class DashboardViewModel(private val repository: AdminRepository) : ViewModel() {

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDomainModel>>(emptyList())
    val products: StateFlow<List<ProductDomainModel>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.fetchDashboardStats()
                .onSuccess { _stats.value = it }
                .onFailure { _errorMessage.value = it.localizedMessage }

            repository.fetchProducts()
                .onSuccess { _products.value = it }
                .onFailure { _errorMessage.value = it.localizedMessage }

            _isLoading.value = false
        }
    }

    class Factory(private val repository: AdminRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return DashboardViewModel(repository) as T
        }
    }
}
