package com.example.tfg_carloscaramecerero

import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.NutritionalGoalEntity
import com.example.tfg_carloscaramecerero.domain.repository.NutritionRepository
import com.example.tfg_carloscaramecerero.viewmodel.MealItemInput
import com.example.tfg_carloscaramecerero.viewmodel.NutritionViewModel
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
 * Tests unitarios de NutritionViewModel.
 * Se usan fakes en memoria para el repositorio, sin necesidad de base de datos real.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NutritionViewModelTest {

    private lateinit var fakeRepo: FakeNutritionRepository
    private lateinit var viewModel: NutritionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRepo = FakeNutritionRepository()
        viewModel = NutritionViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── Estado inicial ───────────────────────────────────────────────────────

    @Test
    fun `selectedDay inicial esta en rango valido de 1 a 7`() {
        val day = viewModel.selectedDay.value
        assertTrue("El día $day debe estar entre 1 y 7", day in 1..7)
    }

    // ─── selectDay ────────────────────────────────────────────────────────────

    @Test
    fun `selectDay actualiza selectedDay al valor dado`() {
        viewModel.selectDay(4)
        assertEquals(4, viewModel.selectedDay.value)
    }

    @Test
    fun `selectDay llunes es 1`() {
        viewModel.selectDay(1)
        assertEquals(1, viewModel.selectedDay.value)
    }

    @Test
    fun `selectDay domingo es 7`() {
        viewModel.selectDay(7)
        assertEquals(7, viewModel.selectedDay.value)
    }

    // ─── addMealEntry ─────────────────────────────────────────────────────────

    @Test
    fun `addMealEntry inserta entrada con datos correctos`() {
        viewModel.addMealEntry(
            description = "Pollo con arroz",
            mealType = "almuerzo",
            dayOfWeek = 1,
            time = "14:00",
            foodType = "comida",
            grams = 300
        )
        assertEquals(1, fakeRepo.insertedEntries.size)
        with(fakeRepo.insertedEntries.first()) {
            assertEquals("Pollo con arroz", description)
            assertEquals("almuerzo", mealType)
            assertEquals(1, dayOfWeek)
            assertEquals("14:00", time)
            assertEquals("comida", foodType)
            assertEquals(300, grams)
        }
    }

    @Test
    fun `addMealEntry multiples veces acumula entradas`() {
        viewModel.addMealEntry("Huevos", "desayuno", 1)
        viewModel.addMealEntry("Fruta", "desayuno", 1)
        assertEquals(2, fakeRepo.insertedEntries.size)
    }

    // ─── deleteEntry ──────────────────────────────────────────────────────────

    @Test
    fun `deleteEntry llama al repositorio con la entrada correcta`() {
        val entry = FoodEntryEntity(id = 1L, description = "Test", mealType = "cena", dayOfWeek = 5)
        viewModel.deleteEntry(entry)
        assertEquals(1, fakeRepo.deletedEntries.size)
        assertEquals(entry, fakeRepo.deletedEntries.first())
    }

    // ─── updateEntry ──────────────────────────────────────────────────────────

    @Test
    fun `updateEntry llama al repositorio con la entrada actualizada`() {
        val entry = FoodEntryEntity(id = 2L, description = "Antiguo", mealType = "snack", dayOfWeek = 3)
        val updated = entry.copy(description = "Nuevo")
        viewModel.updateEntry(updated)
        assertEquals(1, fakeRepo.updatedEntries.size)
        assertEquals("Nuevo", fakeRepo.updatedEntries.first().description)
    }

    // ─── addMultipleMealEntries ───────────────────────────────────────────────

    @Test
    fun `addMultipleMealEntries ignora descripciones en blanco`() {
        val items = listOf(
            MealItemInput("Tostada integral", "comida", 80),
            MealItemInput("", "comida", null),     // vacío → ignorar
            MealItemInput("   ", "comida", null),  // espacio → ignorar
            MealItemInput("Zumo naranja", "bebida", 200)
        )
        viewModel.addMultipleMealEntries(items, "desayuno", 1)
        assertEquals(2, fakeRepo.insertedEntries.size)
    }

    @Test
    fun `addMultipleMealEntries inserta con mealType y dayOfWeek correctos`() {
        val items = listOf(
            MealItemInput("Ensalada", "comida", 150)
        )
        viewModel.addMultipleMealEntries(items, "cena", 5)
        val inserted = fakeRepo.insertedEntries.first()
        assertEquals("cena", inserted.mealType)
        assertEquals(5, inserted.dayOfWeek)
    }

    // ─── Fake Repository ──────────────────────────────────────────────────────

    class FakeNutritionRepository : NutritionRepository {
        private val _entries = MutableStateFlow<List<FoodEntryEntity>>(emptyList())

        val insertedEntries = mutableListOf<FoodEntryEntity>()
        val deletedEntries = mutableListOf<FoodEntryEntity>()
        val updatedEntries = mutableListOf<FoodEntryEntity>()

        override fun getAllEntries(): Flow<List<FoodEntryEntity>> = _entries
        override fun getEntriesByDayOfWeek(dayOfWeek: Int): Flow<List<FoodEntryEntity>> =
            _entries.map { it.filter { e -> e.dayOfWeek == dayOfWeek } }
        override fun getEntriesForWeek(weekStart: Long, weekEnd: Long): Flow<List<FoodEntryEntity>> =
            _entries.map { it.filter { e -> e.date in weekStart..weekEnd } }
        override fun getUnanalyzedEntries(): Flow<List<FoodEntryEntity>> =
            _entries.map { it.filter { e -> !e.aiAnalyzed } }
        override fun getDaysWithEntries(): Flow<List<Int>> =
            _entries.map { it.map { e -> e.dayOfWeek }.distinct() }

        override suspend fun insertEntry(entry: FoodEntryEntity): Long {
            insertedEntries.add(entry)
            _entries.value = _entries.value + entry
            return entry.id
        }
        override suspend fun updateEntry(entry: FoodEntryEntity) {
            updatedEntries.add(entry)
            _entries.value = _entries.value.map { if (it.id == entry.id) entry else it }
        }
        override suspend fun deleteEntry(entry: FoodEntryEntity) {
            deletedEntries.add(entry)
            _entries.value = _entries.value - entry
        }
        override fun getCurrentGoal(): Flow<NutritionalGoalEntity?> = flowOf(null)
        override suspend fun insertGoal(goal: NutritionalGoalEntity): Long = 0L
        override suspend fun updateGoal(goal: NutritionalGoalEntity) {}
    }
}

