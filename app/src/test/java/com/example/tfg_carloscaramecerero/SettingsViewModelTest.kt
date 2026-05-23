package com.example.tfg_carloscaramecerero

import android.content.Context
import com.example.tfg_carloscaramecerero.data.local.entity.AuditLogEntity
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.data.preferences.UserPreferencesRepository
import com.example.tfg_carloscaramecerero.domain.repository.AuditLogRepository
import com.example.tfg_carloscaramecerero.domain.repository.BodyRepository
import com.example.tfg_carloscaramecerero.domain.repository.ExerciseRepository
import com.example.tfg_carloscaramecerero.domain.repository.NutritionRepository
import com.example.tfg_carloscaramecerero.domain.repository.RoutineRepository
import com.example.tfg_carloscaramecerero.viewmodel.SettingsViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios de SettingsViewModel.
 * Se usa MockK para UserPreferencesRepository (clase concreta) y fakes para repositorios-interfaz.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var mockPrefs: UserPreferencesRepository
    private lateinit var fakeAuditLog: FakeAuditLogForSettings
    private lateinit var fakeBodyRepo: FakeBodyForSettings
    private lateinit var fakeNutritionRepo: FakeNutritionForSettings
    private lateinit var fakeRoutineRepo: FakeRoutineForSettings
    private lateinit var fakeExerciseRepo: FakeExerciseForSettings
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Mock de UserPreferencesRepository (clase concreta, no interfaz)
        mockPrefs = mockk(relaxed = true) {
            every { isDarkMode }            returns flowOf(null)
            every { notificationsEnabled } returns flowOf(false)
            every { aiCanCreateRoutines }  returns flowOf(false)
            every { aiCanCreateExercises } returns flowOf(false)
            every { aiCanCreateFoodSchedule } returns flowOf(false)
            every { biometricLock }        returns flowOf(false)
            every { termsAccepted }        returns flowOf(false)
        }

        fakeAuditLog     = FakeAuditLogForSettings()
        fakeBodyRepo     = FakeBodyForSettings()
        fakeNutritionRepo= FakeNutritionForSettings()
        fakeRoutineRepo  = FakeRoutineForSettings()
        fakeExerciseRepo = FakeExerciseForSettings()

        val ctx = mockk<Context>(relaxed = true)

        viewModel = SettingsViewModel(
            prefsRepository     = mockPrefs,
            auditLogRepository  = fakeAuditLog,
            bodyRepository      = fakeBodyRepo,
            nutritionRepository = fakeNutritionRepo,
            routineRepository   = fakeRoutineRepo,
            exerciseRepository  = fakeExerciseRepo,
            context             = ctx
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── acceptTerms ──────────────────────────────────────────────────────────

    @Test
    fun `acceptTerms llama a setTermsAccepted con true`() {
        viewModel.acceptTerms()
        coVerify { mockPrefs.setTermsAccepted(true) }
    }

    @Test
    fun `acceptTerms registra entrada en audit log`() {
        viewModel.acceptTerms()
        assertTrue(
            "Debería registrar la aceptación en el audit log",
            fakeAuditLog.loggedActions.isNotEmpty()
        )
        assertTrue(fakeAuditLog.loggedActions.any { it.contains("Términos") })
    }

    // ─── importWeights ────────────────────────────────────────────────────────

    @Test
    fun `importWeights inserta todos los registros de la lista`() {
        val weights = listOf(
            BodyWeightEntity(id = 10L, weight = 80.0),
            BodyWeightEntity(id = 11L, weight = 78.5)
        )
        viewModel.importWeights(weights)
        assertEquals(2, fakeBodyRepo.insertedWeights.size)
    }

    @Test
    fun `importWeights inserta pesos con id 0 para evitar conflictos de clave primaria`() {
        val weights = listOf(BodyWeightEntity(id = 99L, weight = 75.0))
        viewModel.importWeights(weights)
        assertEquals(0L, fakeBodyRepo.insertedWeights.first().id)
        assertEquals(75.0, fakeBodyRepo.insertedWeights.first().weight, 0.001)
    }

    // ─── importNutrition ──────────────────────────────────────────────────────

    @Test
    fun `importNutrition inserta entradas nutricion con id 0`() {
        val entries = listOf(
            FoodEntryEntity(id = 5L, description = "Leche", mealType = "desayuno", dayOfWeek = 1)
        )
        viewModel.importNutrition(entries)
        assertEquals(1, fakeNutritionRepo.insertedEntries.size)
        assertEquals(0L, fakeNutritionRepo.insertedEntries.first().id)
        assertEquals("Leche", fakeNutritionRepo.insertedEntries.first().description)
    }

    // ─── importRoutines ───────────────────────────────────────────────────────

    @Test
    fun `importRoutines inserta rutinas con id 0`() {
        val routines = listOf(
            RoutineEntity(id = 7L, name = "Fuerza A", description = ""),
            RoutineEntity(id = 8L, name = "Fuerza B", description = "")
        )
        viewModel.importRoutines(routines)
        assertEquals(2, fakeRoutineRepo.insertedRoutines.size)
        assertTrue(fakeRoutineRepo.insertedRoutines.all { it.id == 0L })
    }

    // ─── importExercises ──────────────────────────────────────────────────────

    @Test
    fun `importExercises inserta ejercicios con id 0`() {
        val exercises = listOf(
            ExerciseEntity(id = 3L, name = "Press banca", muscleGroup = "Pecho", exerciseType = "STRENGTH")
        )
        viewModel.importExercises(exercises)
        assertEquals(1, fakeExerciseRepo.insertedExercises.size)
        assertEquals(0L, fakeExerciseRepo.insertedExercises.first().id)
        assertEquals("Press banca", fakeExerciseRepo.insertedExercises.first().name)
    }

    // ─── setDarkMode ──────────────────────────────────────────────────────────

    @Test
    fun `setDarkMode llama al repositorio de preferencias con el valor correcto`() {
        viewModel.setDarkMode(true)
        coVerify { mockPrefs.setDarkMode(true) }
    }

    @Test
    fun `setDarkMode registra cambio en audit log`() {
        viewModel.setDarkMode(false)
        assertTrue(fakeAuditLog.loggedActions.any { it.contains("Tema") || it.contains("tema") || it.contains("Claro") })
    }

    // ─── setBiometricLock ─────────────────────────────────────────────────────

    @Test
    fun `setBiometricLock activa el bloqueo y registra en audit log`() {
        viewModel.setBiometricLock(true)
        coVerify { mockPrefs.setBiometricLock(true) }
        assertTrue(fakeAuditLog.loggedActions.any { it.contains("biométrico") || it.contains("Biométrico") })
    }

    // ─── Fakes ────────────────────────────────────────────────────────────────

    class FakeAuditLogForSettings : AuditLogRepository {
        val loggedActions = mutableListOf<String>()
        override fun getAll(): Flow<List<AuditLogEntity>> = flowOf(emptyList())
        override fun getByCategory(category: String): Flow<List<AuditLogEntity>> = flowOf(emptyList())
        override suspend fun logAction(category: String, action: String, detail: String) {
            loggedActions.add("$category|$action|$detail")
        }
        override suspend fun deleteAll() { loggedActions.clear() }
    }

    class FakeBodyForSettings : BodyRepository by mockk(relaxed = true) {
        val insertedWeights = mutableListOf<BodyWeightEntity>()
        override suspend fun insertWeight(bodyWeight: BodyWeightEntity): Long {
            insertedWeights.add(bodyWeight)
            return bodyWeight.id
        }
    }

    class FakeNutritionForSettings : NutritionRepository by mockk(relaxed = true) {
        val insertedEntries = mutableListOf<FoodEntryEntity>()
        override suspend fun insertEntry(entry: FoodEntryEntity): Long {
            insertedEntries.add(entry)
            return entry.id
        }
    }

    class FakeRoutineForSettings : RoutineRepository by mockk(relaxed = true) {
        val insertedRoutines = mutableListOf<RoutineEntity>()
        override suspend fun insert(routine: RoutineEntity): Long {
            insertedRoutines.add(routine)
            return routine.id
        }
    }

    class FakeExerciseForSettings : ExerciseRepository by mockk(relaxed = true) {
        val insertedExercises = mutableListOf<ExerciseEntity>()
        override suspend fun insert(exercise: ExerciseEntity): Long {
            insertedExercises.add(exercise)
            return exercise.id
        }
    }
}


