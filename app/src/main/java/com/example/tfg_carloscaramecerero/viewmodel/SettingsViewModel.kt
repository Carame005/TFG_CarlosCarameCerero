package com.example.tfg_carloscaramecerero.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.local.AppDatabase
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodCatalogEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.preferences.UserPreferencesRepository
import com.example.tfg_carloscaramecerero.data.util.ImportManager
import com.example.tfg_carloscaramecerero.domain.repository.AuditLogRepository
import com.example.tfg_carloscaramecerero.domain.repository.BodyRepository
import com.example.tfg_carloscaramecerero.domain.repository.ExerciseRepository
import com.example.tfg_carloscaramecerero.domain.repository.NutritionRepository
import com.example.tfg_carloscaramecerero.domain.repository.RoutineRepository
import com.example.tfg_carloscaramecerero.domain.repository.TrainingRepository
import com.example.tfg_carloscaramecerero.notifications.TrainingReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val auditLogRepository: AuditLogRepository,
    private val bodyRepository: BodyRepository,
    private val nutritionRepository: NutritionRepository,
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    private val trainingRepository: TrainingRepository,
    private val appDatabase: AppDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** null = seguir sistema, true = oscuro, false = claro */
    val darkMode: StateFlow<Boolean?> = prefsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notificationsEnabled: StateFlow<Boolean> = prefsRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val aiCanCreateRoutines: StateFlow<Boolean> = prefsRepository.aiCanCreateRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val aiCanCreateExercises: StateFlow<Boolean> = prefsRepository.aiCanCreateExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val aiCanCreateFoodSchedule: StateFlow<Boolean> = prefsRepository.aiCanCreateFoodSchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** true = bloqueo biométrico activo, false = sin bloqueo (valor por defecto) */
    val biometricLock: StateFlow<Boolean> = prefsRepository.biometricLock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** true = el usuario ya aceptó los T&C */
    val termsAccepted: StateFlow<Boolean> = prefsRepository.termsAccepted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDarkMode(enabled: Boolean?) {
        viewModelScope.launch {
            prefsRepository.setDarkMode(enabled)
            val label = when (enabled) { true -> "Oscuro"; false -> "Claro"; else -> "Auto" }
            auditLogRepository.logAction("Sistema", "Tema cambiado", label)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setNotificationsEnabled(enabled)
            if (enabled) TrainingReminderWorker.scheduleDaily(context)
            else TrainingReminderWorker.cancel(context)
            auditLogRepository.logAction("Sistema", "Notificaciones ${if (enabled) "activadas" else "desactivadas"}")
        }
    }

    fun setAiCanCreateRoutines(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setAiCanCreateRoutines(enabled)
            auditLogRepository.logAction("Sistema", "Permiso IA – rutinas ${if (enabled) "activado" else "desactivado"}")
        }
    }

    fun setAiCanCreateExercises(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setAiCanCreateExercises(enabled)
            auditLogRepository.logAction("Sistema", "Permiso IA – ejercicios ${if (enabled) "activado" else "desactivado"}")
        }
    }

    fun setAiCanCreateFoodSchedule(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setAiCanCreateFoodSchedule(enabled)
            auditLogRepository.logAction("Sistema", "Permiso IA – nutrición ${if (enabled) "activado" else "desactivado"}")
        }
    }

    fun logDataExport(type: String) {
        viewModelScope.launch {
            auditLogRepository.logAction("Sistema", "Datos exportados", type)
        }
    }

    // ─── Importación ──────────────────────────────────────────────────────────

    fun logDataImport(type: String) {
        viewModelScope.launch {
            auditLogRepository.logAction("Sistema", "Datos importados", type)
        }
    }

    fun importWeights(weights: List<BodyWeightEntity>) {
        viewModelScope.launch {
            weights.forEach { bodyRepository.insertWeight(it.copy(id = 0)) }
            auditLogRepository.logAction("Sistema", "Datos importados", "Historial de peso (${weights.size} registros)")
            _importResult.value = "✅ ${weights.size} registro(s) de peso importado(s)"
        }
    }

    fun importNutrition(entries: List<FoodEntryEntity>) {
        viewModelScope.launch {
            entries.forEach { nutritionRepository.insertEntry(it.copy(id = 0)) }
            auditLogRepository.logAction("Sistema", "Datos importados", "Registro nutricional (${entries.size} entradas)")
            _importResult.value = "✅ ${entries.size} entrada(s) nutricional(es) importada(s)"
        }
    }

    fun importRoutines(routines: List<RoutineEntity>) {
        viewModelScope.launch {
            routines.forEach { routineRepository.insert(it.copy(id = 0)) }
            auditLogRepository.logAction("Sistema", "Datos importados", "Rutinas (${routines.size} rutinas)")
            _importResult.value = "✅ ${routines.size} rutina(s) importada(s)"
        }
    }

    fun importExercises(exercises: List<ExerciseEntity>) {
        viewModelScope.launch {
            exercises.forEach { exerciseRepository.insert(it.copy(id = 0)) }
            auditLogRepository.logAction("Sistema", "Datos importados", "Ejercicios (${exercises.size} ejercicios)")
            _importResult.value = "✅ ${exercises.size} ejercicio(s) importado(s)"
        }
    }

    fun importFoodCatalog(items: List<FoodCatalogEntity>) {
        viewModelScope.launch {
            items.forEach { nutritionRepository.insertCatalogItem(it.copy(id = 0)) }
            auditLogRepository.logAction("Sistema", "Datos importados", "Catálogo alimentos (${items.size} ítems)")
            _importResult.value = "✅ ${items.size} ítem(s) del catálogo importado(s)"
        }
    }

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setBiometricLock(enabled)
            auditLogRepository.logAction("Sistema", "Bloqueo biométrico ${if (enabled) "activado" else "desactivado"}")
        }
    }

    fun acceptTerms() {
        viewModelScope.launch {
            prefsRepository.setTermsAccepted(true)
            auditLogRepository.logAction("Sistema", "Términos y condiciones aceptados")
        }
    }

    // ─── Copia de seguridad completa (.db) ────────────────────────────────────

    /** null = sin intento, true = éxito, false = error */
    private val _dbRestoreSuccess = MutableStateFlow<Boolean?>(null)
    val dbRestoreSuccess: StateFlow<Boolean?> = _dbRestoreSuccess.asStateFlow()

    private val _dbExportError = MutableStateFlow<String?>(null)
    val dbExportError: StateFlow<String?> = _dbExportError.asStateFlow()

    /** Mensaje de éxito/error para importaciones CSV o BD. null = sin mensaje pendiente. */
    private val _importResult = MutableStateFlow<String?>(null)
    val importResult: StateFlow<String?> = _importResult.asStateFlow()

    fun clearRestoreState() { _dbRestoreSuccess.value = null }
    fun clearExportError() { _dbExportError.value = null }
    fun clearImportResult() { _importResult.value = null }

    /**
     * Exporta la base de datos completa como archivo .db y abre el selector de compartir.
     * Realiza checkpoint WAL en background y lanza el intent en el hilo principal.
     */
    fun exportDatabase() {
        viewModelScope.launch {
            try {
                val backupFile = withContext(Dispatchers.IO) {
                    // execSQL está bloqueado por Room; usamos query() para el checkpoint WAL
                    appDatabase.openHelper.writableDatabase
                        .query("PRAGMA wal_checkpoint(FULL)", emptyArray<Any?>()).close()
                    val dbFile = context.getDatabasePath("fitness_database")
                    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                    val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val out = File(exportDir, "fitapp_backup_$ts.db")
                    dbFile.copyTo(out, overwrite = true)
                    out
                }
                val uri = FileProvider.getUriForFile(
                    context, "${context.packageName}.fileprovider", backupFile
                )
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "FitApp – Copia de seguridad")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                // FLAG_ACTIVITY_NEW_TASK debe ir en el chooser (es el intent que se lanza)
                val chooser = Intent.createChooser(sendIntent, "Guardar copia de seguridad").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                auditLogRepository.logAction("Sistema", "Base de datos exportada")
            } catch (e: Exception) {
                _dbExportError.value = e.message ?: "Error desconocido al exportar"
            }
        }
    }

    /**
     * Cierra Room, sobreescribe el archivo de BD con el contenido de [uri] y señaliza el resultado.
     * La pantalla debe reiniciar la app cuando [dbRestoreSuccess] == true.
     */
    fun restoreDatabase(uri: Uri) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                ImportManager.restoreDatabase(context, uri, appDatabase)
            }
            _dbRestoreSuccess.value = success
        }
    }

    // ─── Importación de sesiones detalladas ───────────────────────────────────

    fun importDetailedSessions(parsedSessions: List<ImportManager.ParsedSession>) {
        viewModelScope.launch {
            parsedSessions.forEach { ps ->
                val newSessionId = trainingRepository.insertSession(ps.session)
                if (ps.sets.isNotEmpty()) {
                    trainingRepository.insertSets(ps.sets.map { it.copy(sessionId = newSessionId) })
                }
            }
            auditLogRepository.logAction(
                "Sistema", "Datos importados",
                "Sesiones detalladas (${parsedSessions.size} sesiones)"
            )
            _importResult.value = "✅ ${parsedSessions.size} sesión(es) importada(s)"
        }
    }
}

