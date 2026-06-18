package com.sailguard.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sailguard.app.data.database.AppDatabase
import com.sailguard.app.data.model.TripHistoryEntity
import com.sailguard.app.data.network.ConnectaApiClient
import com.sailguard.app.data.network.ConnectaApiClient.SyncedTrip
import com.sailguard.app.data.repository.SettingsRepository
import com.sailguard.app.data.repository.TripHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TripHistoryRepository(
        AppDatabase.getDatabase(app).tripHistoryDao()
    )
    private val settings = SettingsRepository(app)

    val trips: StateFlow<List<TripHistoryEntity>> = repository.allTrips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _syncedTrips = MutableStateFlow<List<SyncedTrip>>(emptyList())
    val syncedTrips: StateFlow<List<SyncedTrip>> = _syncedTrips.asStateFlow()

    init {
        val sessionId = settings.connectaLinkCode
        if (!sessionId.isNullOrBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                _syncedTrips.value = ConnectaApiClient.fetchTripsBySession(sessionId)
            }
        }
    }
}
