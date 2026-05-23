package com.example.tfg_carloscaramecerero

import com.example.tfg_carloscaramecerero.data.local.entity.AuditLogEntity
import com.example.tfg_carloscaramecerero.data.local.entity.BodyMeasurementEntity
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.HealthDocumentEntity
import com.example.tfg_carloscaramecerero.data.local.entity.UserProfileEntity
import com.example.tfg_carloscaramecerero.domain.repository.AuditLogRepository
import com.example.tfg_carloscaramecerero.domain.repository.BodyRepository
import com.example.tfg_carloscaramecerero.viewmodel.BodyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios de BodyViewModel.
 * Verifica que las operaciones delegan correctamente en el repositorio.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BodyViewModelTest {

    private lateinit var fakeRepo: FakeBodyRepository
    private lateinit var fakeAuditRepo: FakeAuditLogRepository
    private lateinit var viewModel: BodyViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRepo = FakeBodyRepository()
        fakeAuditRepo = FakeAuditLogRepository()
        viewModel = BodyViewModel(fakeRepo, fakeAuditRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── Peso ─────────────────────────────────────────────────────────────────

    @Test
    fun `addWeight inserta peso con valor correcto`() {
        viewModel.addWeight(78.5)
        assertEquals(1, fakeRepo.insertedWeights.size)
        assertEquals(78.5, fakeRepo.insertedWeights.first().weight, 0.001)
    }

    @Test
    fun `addWeight multiples registros se acumulan`() {
        viewModel.addWeight(70.0)
        viewModel.addWeight(70.5)
        viewModel.addWeight(71.0)
        assertEquals(3, fakeRepo.insertedWeights.size)
    }

    @Test
    fun `deleteWeight llama al repositorio con el objeto correcto`() {
        val weight = BodyWeightEntity(id = 1L, weight = 80.0)
        viewModel.deleteWeight(weight)
        assertEquals(1, fakeRepo.deletedWeights.size)
        assertEquals(weight, fakeRepo.deletedWeights.first())
    }

    // ─── Medidas ──────────────────────────────────────────────────────────────

    @Test
    fun `addMeasurement inserta medida con valores correctos`() {
        viewModel.addMeasurement(chest = 100.0, waist = 82.0, hips = 96.0)
        assertEquals(1, fakeRepo.insertedMeasurements.size)
        with(fakeRepo.insertedMeasurements.first()) {
            assertEquals(100.0, chest!!, 0.001)
            assertEquals(82.0, waist!!, 0.001)
            assertEquals(96.0, hips!!, 0.001)
        }
    }

    @Test
    fun `addMeasurement con valores nulos los deja en null`() {
        viewModel.addMeasurement(waist = 80.0)
        val inserted = fakeRepo.insertedMeasurements.first()
        assertNull(inserted.chest)
        assertEquals(80.0, inserted.waist!!, 0.001)
        assertNull(inserted.biceps)
    }

    @Test
    fun `deleteMeasurement llama al repositorio con el objeto correcto`() {
        val m = BodyMeasurementEntity(id = 5L, waist = 80.0)
        viewModel.deleteMeasurement(m)
        assertEquals(1, fakeRepo.deletedMeasurements.size)
        assertEquals(m, fakeRepo.deletedMeasurements.first())
    }

    // ─── Perfil de usuario ────────────────────────────────────────────────────

    @Test
    fun `saveHeight guarda perfil con altura correcta`() {
        viewModel.saveHeight(175.0)
        assertNotNull(fakeRepo.savedProfile)
        assertEquals(175.0, fakeRepo.savedProfile!!.height!!, 0.001)
    }

    @Test
    fun `saveHealthConditions guarda perfil con condiciones correctas`() {
        viewModel.saveHealthConditions("Asma leve, intolerancia al gluten")
        assertEquals("Asma leve, intolerancia al gluten", fakeRepo.savedProfile?.healthConditions)
    }

    @Test
    fun `saveFitnessGoal guarda perfil con objetivo correcto`() {
        viewModel.saveFitnessGoal("Perder 5 kg en 3 meses")
        assertEquals("Perder 5 kg en 3 meses", fakeRepo.savedProfile?.fitnessGoal)
    }

    @Test
    fun `saveHeight sin perfil previo crea perfil con altura y valores por defecto`() {
        // userProfile.value es null por defecto (nadie suscrito al StateFlow)
        // El ViewModel usa UserProfileEntity() como fallback → campos vacíos
        viewModel.saveHeight(175.0)
        assertNotNull(fakeRepo.savedProfile)
        assertEquals(175.0, fakeRepo.savedProfile!!.height!!, 0.001)
        assertEquals("", fakeRepo.savedProfile!!.healthConditions)
        assertEquals("", fakeRepo.savedProfile!!.fitnessGoal)
    }

    // ─── saveHealthProfile (función combinada atómica) ────────────────────────

    @Test
    fun `saveHealthProfile guarda condiciones y objetivo en una sola operacion`() {
        viewModel.saveHealthProfile("Asma", "Perder peso")
        assertNotNull(fakeRepo.savedProfile)
        assertEquals("Asma", fakeRepo.savedProfile!!.healthConditions)
        assertEquals("Perder peso", fakeRepo.savedProfile!!.fitnessGoal)
    }

    @Test
    fun `saveHealthProfile no sobreescribe objetivo al guardar condiciones`() {
        // Simula perfil previo con objetivo ya fijado
        val prevProfile = UserProfileEntity(healthConditions = "Diabetes", fitnessGoal = "Musculación")
        fakeRepo.currentProfile.value = prevProfile

        viewModel.saveHealthProfile("Diabetes + Hipertensión", "Musculación")

        assertEquals("Diabetes + Hipertensión", fakeRepo.savedProfile!!.healthConditions)
        assertEquals("Musculación", fakeRepo.savedProfile!!.fitnessGoal)
    }

    @Test
    fun `saveHealthProfile no sobreescribe condiciones al guardar objetivo`() {
        val prevProfile = UserProfileEntity(healthConditions = "Celiaquía", fitnessGoal = "")
        fakeRepo.currentProfile.value = prevProfile

        viewModel.saveHealthProfile("Celiaquía", "Correr 10 km")

        assertEquals("Celiaquía", fakeRepo.savedProfile!!.healthConditions)
        assertEquals("Correr 10 km", fakeRepo.savedProfile!!.fitnessGoal)
    }

    @Test
    fun `saveHealthProfile registra entrada de audit log con el objetivo`() {
        viewModel.saveHealthProfile("Ninguna", "Ganar masa muscular")
        assertTrue(fakeAuditRepo.loggedActions.isNotEmpty())
        assertTrue(fakeAuditRepo.loggedActions.any { it.contains("Ganar masa muscular") })
    }

    @Test
    fun `saveHealthProfile con objetivo vacio no genera entrada de audit log`() {
        viewModel.saveHealthProfile("Asma", "")
        assertTrue(fakeAuditRepo.loggedActions.isEmpty())
    }

    // ─── Fake Repository ──────────────────────────────────────────────────────

    class FakeBodyRepository : BodyRepository {
        val currentProfile = MutableStateFlow<UserProfileEntity?>(null)
        private val _weights = MutableStateFlow<List<BodyWeightEntity>>(emptyList())
        private val _measurements = MutableStateFlow<List<BodyMeasurementEntity>>(emptyList())

        val insertedWeights = mutableListOf<BodyWeightEntity>()
        val deletedWeights = mutableListOf<BodyWeightEntity>()
        val insertedMeasurements = mutableListOf<BodyMeasurementEntity>()
        val deletedMeasurements = mutableListOf<BodyMeasurementEntity>()
        var savedProfile: UserProfileEntity? = null

        override fun getAllWeights(): Flow<List<BodyWeightEntity>> = _weights
        override fun getLatestWeight(): Flow<BodyWeightEntity?> =
            _weights.map { it.maxByOrNull { w -> w.date } }
        override fun getWeightsBetweenDates(from: Long, to: Long): Flow<List<BodyWeightEntity>> =
            _weights.map { it.filter { w -> w.date in from..to } }

        override suspend fun insertWeight(bodyWeight: BodyWeightEntity): Long {
            insertedWeights.add(bodyWeight)
            _weights.value = _weights.value + bodyWeight
            return bodyWeight.id
        }
        override suspend fun deleteWeight(bodyWeight: BodyWeightEntity) {
            deletedWeights.add(bodyWeight)
            _weights.value = _weights.value - bodyWeight
        }

        override fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>> = _measurements
        override fun getLatestMeasurement(): Flow<BodyMeasurementEntity?> =
            _measurements.map { it.maxByOrNull { m -> m.date } }
        override fun getMeasurementsBetweenDates(from: Long, to: Long): Flow<List<BodyMeasurementEntity>> =
            _measurements.map { it.filter { m -> m.date in from..to } }

        override suspend fun insertMeasurement(measurement: BodyMeasurementEntity): Long {
            insertedMeasurements.add(measurement)
            _measurements.value = _measurements.value + measurement
            return measurement.id
        }
        override suspend fun deleteMeasurement(measurement: BodyMeasurementEntity) {
            deletedMeasurements.add(measurement)
            _measurements.value = _measurements.value - measurement
        }

        override fun getUserProfile(): Flow<UserProfileEntity?> = currentProfile
        override suspend fun saveUserProfile(profile: UserProfileEntity) {
            savedProfile = profile
            currentProfile.value = profile
        }

        override fun getAllHealthDocuments(): Flow<List<HealthDocumentEntity>> = flowOf(emptyList())
        override suspend fun insertHealthDocument(document: HealthDocumentEntity): Long = 0L
        override suspend fun deleteHealthDocument(document: HealthDocumentEntity) {}
    }

    class FakeAuditLogRepository : AuditLogRepository {
        val loggedActions = mutableListOf<String>()

        override fun getAll(): Flow<List<AuditLogEntity>> = flowOf(emptyList())
        override fun getByCategory(category: String): Flow<List<AuditLogEntity>> = flowOf(emptyList())
        override suspend fun logAction(category: String, action: String, detail: String) {
            loggedActions.add("$category|$action|$detail")
        }
        override suspend fun deleteAll() { loggedActions.clear() }
    }
}

