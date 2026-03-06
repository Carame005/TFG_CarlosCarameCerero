package com.example.tfg_carloscaramecerero.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.local.entity.BodyMeasurementEntity
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.HealthDocumentEntity
import com.example.tfg_carloscaramecerero.data.local.entity.UserProfileEntity
import com.example.tfg_carloscaramecerero.domain.repository.BodyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BodyViewModel @Inject constructor(
    private val bodyRepository: BodyRepository
) : ViewModel() {

    val weights: StateFlow<List<BodyWeightEntity>> =
        bodyRepository.getAllWeights()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestWeight: StateFlow<BodyWeightEntity?> =
        bodyRepository.getLatestWeight()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val measurements: StateFlow<List<BodyMeasurementEntity>> =
        bodyRepository.getAllMeasurements()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestMeasurement: StateFlow<BodyMeasurementEntity?> =
        bodyRepository.getLatestMeasurement()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userProfile: StateFlow<UserProfileEntity?> =
        bodyRepository.getUserProfile()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val healthDocuments: StateFlow<List<HealthDocumentEntity>> =
        bodyRepository.getAllHealthDocuments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWeight(weight: Double) {
        viewModelScope.launch {
            bodyRepository.insertWeight(BodyWeightEntity(weight = weight))
        }
    }

    fun deleteWeight(bodyWeight: BodyWeightEntity) {
        viewModelScope.launch { bodyRepository.deleteWeight(bodyWeight) }
    }

    fun addMeasurement(
        chest: Double? = null,
        waist: Double? = null,
        hips: Double? = null,
        biceps: Double? = null,
        thighs: Double? = null
    ) {
        viewModelScope.launch {
            bodyRepository.insertMeasurement(
                BodyMeasurementEntity(
                    chest = chest,
                    waist = waist,
                    hips = hips,
                    biceps = biceps,
                    thighs = thighs
                )
            )
        }
    }

    fun deleteMeasurement(measurement: BodyMeasurementEntity) {
        viewModelScope.launch { bodyRepository.deleteMeasurement(measurement) }
    }

    fun saveHeight(height: Double) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            bodyRepository.saveUserProfile(current.copy(height = height))
        }
    }

    fun saveHealthConditions(conditions: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            bodyRepository.saveUserProfile(current.copy(healthConditions = conditions))
        }
    }

    fun saveFitnessGoal(goal: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            bodyRepository.saveUserProfile(current.copy(fitnessGoal = goal))
        }
    }

    /**
     * Copia el PDF seleccionado al almacenamiento interno y lo registra en la base de datos.
     */
    fun uploadHealthDocument(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val fileName = getFileNameFromUri(context, uri) ?: "analitica_${UUID.randomUUID()}.pdf"
                val docsDir = File(context.filesDir, "health_documents")
                if (!docsDir.exists()) docsDir.mkdirs()

                val destFile = File(docsDir, "${UUID.randomUUID()}_$fileName")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                bodyRepository.insertHealthDocument(
                    HealthDocumentEntity(
                        fileName = fileName,
                        filePath = destFile.absolutePath
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteHealthDocument(document: HealthDocumentEntity) {
        viewModelScope.launch {
            // Borrar archivo físico
            try {
                File(document.filePath).delete()
            } catch (_: Exception) { }
            bodyRepository.deleteHealthDocument(document)
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}

