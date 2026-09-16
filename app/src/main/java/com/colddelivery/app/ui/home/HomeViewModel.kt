package com.colddelivery.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colddelivery.app.data.local.entity.*
import com.colddelivery.app.data.repository.DeliveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(val customers: List<CustomerEntity> = emptyList(), val todayCustomers: List<CustomerEntity> = emptyList(), val products: List<ProductEntity> = emptyList(), val deliveries: List<DeliveryEntity> = emptyList(), val deliveredCustomerIds: Set<Long> = emptySet(), val totalCartons: Int = 0)

@HiltViewModel class HomeViewModel @Inject constructor(repository: DeliveryRepository) : ViewModel() {
    private val todayDate = LocalDate.now()
    private val todayDay = todayDate.dayOfWeek.name.takeIf { it != "SUNDAY" }?.let { DeliveryDay.valueOf(it) }
    val state: StateFlow<HomeUiState> = combine(repository.observeCustomers(), repository.observeProducts(), repository.observeHistory(), repository.observeDeliveredCustomerIds(todayDate.toEpochDay()), repository.observeTotalStock()) { customers, products, deliveries, deliveredIds, cartons -> HomeUiState(customers, todayDay?.let { day -> customers.filter { it.deliveryDay == day } } ?: emptyList(), products, deliveries, deliveredIds.toSet(), cartons) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
    val today = LocalDate.now()
}
