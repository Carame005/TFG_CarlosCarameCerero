package com.example.tfg_carloscaramecerero

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tfg_carloscaramecerero.data.local.AppDatabase
import com.example.tfg_carloscaramecerero.data.local.dao.BodyWeightDao
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests instrumentados del DAO de peso corporal.
 * Usa una base de datos Room en memoria para no afectar datos de usuario.
 */
@RunWith(AndroidJUnit4::class)
class BodyWeightDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: BodyWeightDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.bodyWeightDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ─── Insert ───────────────────────────────────────────────────────────────

    @Test
    fun insert_devuelve_id_positivo() = runBlocking {
        val id = dao.insert(BodyWeightEntity(weight = 75.0))
        assertTrue("El id generado debe ser > 0", id > 0)
    }

    @Test
    fun insert_multiple_pesos_todos_accesibles() = runBlocking {
        dao.insert(BodyWeightEntity(weight = 70.0))
        dao.insert(BodyWeightEntity(weight = 72.5))
        dao.insert(BodyWeightEntity(weight = 74.0))
        val all = dao.getAll().first()
        assertEquals(3, all.size)
    }

    // ─── GetAll ───────────────────────────────────────────────────────────────

    @Test
    fun getAll_base_datos_vacia_devuelve_lista_vacia() = runBlocking {
        val result = dao.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getAll_devuelve_entradas_ordenadas_por_fecha_descendente() = runBlocking {
        dao.insert(BodyWeightEntity(weight = 68.0, date = 1000L))
        dao.insert(BodyWeightEntity(weight = 72.0, date = 3000L))
        dao.insert(BodyWeightEntity(weight = 70.0, date = 2000L))
        val result = dao.getAll().first()
        assertEquals(72.0, result[0].weight, 0.001) // fecha 3000 primero
        assertEquals(70.0, result[1].weight, 0.001) // fecha 2000 segundo
        assertEquals(68.0, result[2].weight, 0.001) // fecha 1000 tercero
    }

    // ─── GetLatest ────────────────────────────────────────────────────────────

    @Test
    fun getLatest_sin_datos_devuelve_null() = runBlocking {
        val result = dao.getLatest().first()
        assertNull(result)
    }

    @Test
    fun getLatest_devuelve_el_registro_mas_reciente() = runBlocking {
        dao.insert(BodyWeightEntity(weight = 65.0, date = 1000L))
        dao.insert(BodyWeightEntity(weight = 70.0, date = 5000L))  // el más reciente
        dao.insert(BodyWeightEntity(weight = 67.0, date = 3000L))
        val latest = dao.getLatest().first()
        assertNotNull(latest)
        assertEquals(70.0, latest!!.weight, 0.001)
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Test
    fun delete_elimina_el_registro_correcto() = runBlocking {
        val id = dao.insert(BodyWeightEntity(weight = 80.0))
        val id2 = dao.insert(BodyWeightEntity(weight = 82.0))
        dao.delete(BodyWeightEntity(id = id, weight = 80.0))
        val remaining = dao.getAll().first()
        assertEquals(1, remaining.size)
        assertEquals(id2, remaining.first().id)
    }

    @Test
    fun delete_todo_deja_tabla_vacia() = runBlocking {
        val id1 = dao.insert(BodyWeightEntity(weight = 70.0))
        val id2 = dao.insert(BodyWeightEntity(weight = 75.0))
        dao.delete(BodyWeightEntity(id = id1, weight = 70.0))
        dao.delete(BodyWeightEntity(id = id2, weight = 75.0))
        val all = dao.getAll().first()
        assertTrue(all.isEmpty())
    }

    // ─── GetBetweenDates ──────────────────────────────────────────────────────

    @Test
    fun getBetweenDates_filtra_correctamente_por_rango() = runBlocking {
        dao.insert(BodyWeightEntity(weight = 65.0, date = 1000L))  // fuera del rango
        dao.insert(BodyWeightEntity(weight = 70.0, date = 5000L))  // dentro
        dao.insert(BodyWeightEntity(weight = 72.0, date = 8000L))  // dentro
        dao.insert(BodyWeightEntity(weight = 75.0, date = 12000L)) // fuera del rango
        val result = dao.getBetweenDates(from = 3000L, to = 10000L).first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.weight in 70.0..72.0 })
    }

    @Test
    fun getBetweenDates_sin_registros_en_rango_devuelve_lista_vacia() = runBlocking {
        dao.insert(BodyWeightEntity(weight = 70.0, date = 1000L))
        val result = dao.getBetweenDates(from = 5000L, to = 10000L).first()
        assertTrue(result.isEmpty())
    }
}

