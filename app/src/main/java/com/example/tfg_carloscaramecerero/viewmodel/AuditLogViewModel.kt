package com.example.tfg_carloscaramecerero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.local.entity.AuditLogEntity
import com.example.tfg_carloscaramecerero.domain.repository.AuditLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuditLogViewModel @Inject constructor(
    private val auditLogRepository: AuditLogRepository
) : ViewModel() {

    companion object {
        val CATEGORIES = listOf("Entrenamiento", "Nutrición", "Cuerpo", "Sistema")
    }

    private val _allEntries: StateFlow<List<AuditLogEntity>> =
        auditLogRepository.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val filteredEntries: StateFlow<List<AuditLogEntity>> =
        combine(_allEntries, _selectedCategory) { entries, category ->
            if (category == null) entries
            else entries.filter { it.category == category }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun clearLog() {
        viewModelScope.launch { auditLogRepository.deleteAll() }
    }
}

