package com.example.tfg_carloscaramecerero

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tfg_carloscaramecerero.data.local.AppDatabase
import com.example.tfg_carloscaramecerero.data.local.dao.FoodEntryDao
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests instrumentados del DAO de entradas nutricionales.
 */
@RunWith(AndroidJUnit4::class)
class FoodEntryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FoodEntryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.foodEntryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun entry(
        description: String = "Pollo asado",
        mealType: String = "almuerzo",
        dayOfWeek: Int = 1,
        aiAnalyzed: Boolean = false,
        calories: Double? = null
    ) = FoodEntryEntity(
        description = description,
        mealType = mealType,
        dayOfWeek = dayOfWeek,
        aiAnalyzed = aiAnalyzed,
        calories = calories
    )

    // ─── Insert ───────────────────────────────────────────────────────────────

    @Test
    fun insert_devuelve_id_positivo() = runBlocking {
        val id = dao.insert(entry())
        assertTrue(id > 0)
    }

    @Test
    fun insert_multiple_entradas_accesibles_con_getAll() = runBlocking {
        dao.insert(entry("Huevos revueltos", "desayuno", 1))
        dao.insert(entry("Pasta carbonara", "almuerzo", 1))
        dao.insert(entry("Ensalada mixta", "cena", 2))
        val all = dao.getAll().first()
        assertEquals(3, all.size)
    }

    // ─── GetAll ───────────────────────────────────────────────────────────────

    @Test
    fun getAll_tabla_vacia_devuelve_lista_vacia() = runBlocking {
        val result = dao.getAll().first()
        assertTrue(result.isEmpty())
    }

    // ─── GetByDayOfWeek ───────────────────────────────────────────────────────

    @Test
    fun getByDayOfWeek_filtra_por_dia_correctamente() = runBlocking {
        dao.insert(entry("Desayuno Lunes", "desayuno", dayOfWeek = 1))
        dao.insert(entry("Almuerzo Lunes", "almuerzo", dayOfWeek = 1))
        dao.insert(entry("Cena Martes", "cena", dayOfWeek = 2))
        val lunes = dao.getByDayOfWeek(1).first()
        assertEquals(2, lunes.size)
        assertTrue(lunes.all { it.dayOfWeek == 1 })
    }

    @Test
    fun getByDayOfWeek_dia_sin_entradas_devuelve_lista_vacia() = runBlocking {
        dao.insert(entry(dayOfWeek = 1))
        val miercoles = dao.getByDayOfWeek(3).first()
        assertTrue(miercoles.isEmpty())
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Test
    fun delete_elimina_la_entrada_correcta() = runBlocking {
        val id1 = dao.insert(entry("Ensalada", "almuerzo", 1))
        dao.insert(entry("Tortilla", "cena", 1))
        dao.delete(FoodEntryEntity(id = id1, description = "Ensalada", mealType = "almuerzo", dayOfWeek = 1))
        val remaining = dao.getAll().first()
        assertEquals(1, remaining.size)
        assertEquals("Tortilla", remaining.first().description)
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Test
    fun update_modifica_la_entrada_correctamente() = runBlocking {
        val id = dao.insert(entry("Arroz blanco", "almuerzo", 1))
        val updated = FoodEntryEntity(
            id = id,
            description = "Arroz integral",
            mealType = "almuerzo",
            dayOfWeek = 1,
            grams = 200,
            calories = 260.0
        )
        dao.update(updated)
        val all = dao.getAll().first()
        val result = all.find { it.id == id }
        assertNotNull(result)
        assertEquals("Arroz integral", result!!.description)
        assertEquals(200, result.grams)
        assertEquals(260.0, result.calories!!, 0.001)
    }

    // ─── GetDaysWithEntries ───────────────────────────────────────────────────

    @Test
    fun getDaysWithEntries_devuelve_dias_con_al_menos_una_entrada() = runBlocking {
        dao.insert(entry(dayOfWeek = 1))
        dao.insert(entry(dayOfWeek = 1))
        dao.insert(entry(dayOfWeek = 3))
        dao.insert(entry(dayOfWeek = 5))
        val days = dao.getDaysWithEntries().first()
        assertEquals(3, days.size)
        assertTrue(days.containsAll(listOf(1, 3, 5)))
    }

    // ─── GetUnanalyzed ────────────────────────────────────────────────────────

    @Test
    fun getUnanalyzed_devuelve_solo_entradas_no_procesadas_por_ia() = runBlocking {
        dao.insert(entry("Pollo", aiAnalyzed = false))
        dao.insert(entry("Arroz", aiAnalyzed = true, calories = 200.0))
        dao.insert(entry("Brócoli", aiAnalyzed = false))
        val unanalyzed = dao.getUnanalyzed().first()
        assertEquals(2, unanalyzed.size)
        assertTrue(unanalyzed.all { !it.aiAnalyzed })
    }

    @Test
    fun getUnanalyzed_todos_analizados_devuelve_lista_vacia() = runBlocking {
        dao.insert(entry("Salmón", aiAnalyzed = true, calories = 300.0))
        dao.insert(entry("Espinacas", aiAnalyzed = true, calories = 50.0))
        val unanalyzed = dao.getUnanalyzed().first()
        assertTrue(unanalyzed.isEmpty())
    }
}

