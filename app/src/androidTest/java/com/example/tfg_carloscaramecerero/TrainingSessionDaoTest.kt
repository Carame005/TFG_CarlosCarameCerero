package com.example.tfg_carloscaramecerero

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tfg_carloscaramecerero.data.local.AppDatabase
import com.example.tfg_carloscaramecerero.data.local.dao.RoutineDao
import com.example.tfg_carloscaramecerero.data.local.dao.TrainingSessionDao
import com.example.tfg_carloscaramecerero.data.local.dao.TrainingSetDao
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSessionEntity
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSetEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests instrumentados de TrainingSessionDao.
 * Verifica operaciones CRUD sobre sesiones y su relación con series (sets).
 */
@RunWith(AndroidJUnit4::class)
class TrainingSessionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var sessionDao: TrainingSessionDao
    private lateinit var routineDao: RoutineDao
    private lateinit var setDao: TrainingSetDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionDao = database.trainingSessionDao()
        routineDao = database.routineDao()
        setDao = database.trainingSetDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ─── Insert ───────────────────────────────────────────────────────────────

    @Test
    fun insert_devuelve_id_positivo() = runBlocking {
        val id = sessionDao.insert(TrainingSessionEntity())
        assertTrue(id > 0)
    }

    @Test
    fun insert_session_sin_rutina_routineId_null() = runBlocking {
        val id = sessionDao.insert(TrainingSessionEntity(routineId = null))
        val session = sessionDao.getById(id).first()
        assertNotNull(session)
        assertNull(session!!.routineId)
    }

    // ─── GetAll ───────────────────────────────────────────────────────────────

    @Test
    fun getAll_tabla_vacia_devuelve_lista_vacia() = runBlocking {
        val result = sessionDao.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getAll_devuelve_todas_las_sesiones_insertadas() = runBlocking {
        sessionDao.insert(TrainingSessionEntity(date = 1000L))
        sessionDao.insert(TrainingSessionEntity(date = 2000L))
        sessionDao.insert(TrainingSessionEntity(date = 3000L))
        val result = sessionDao.getAll().first()
        assertEquals(3, result.size)
    }

    @Test
    fun getAll_ordenado_por_fecha_descendente() = runBlocking {
        sessionDao.insert(TrainingSessionEntity(date = 1000L, durationMinutes = 30))
        sessionDao.insert(TrainingSessionEntity(date = 3000L, durationMinutes = 60))
        sessionDao.insert(TrainingSessionEntity(date = 2000L, durationMinutes = 45))
        val result = sessionDao.getAll().first()
        assertEquals(60, result[0].durationMinutes) // fecha 3000 primero
        assertEquals(45, result[1].durationMinutes)
        assertEquals(30, result[2].durationMinutes)
    }

    // ─── GetById ──────────────────────────────────────────────────────────────

    @Test
    fun getById_devuelve_sesion_correcta() = runBlocking {
        val id = sessionDao.insert(TrainingSessionEntity(durationMinutes = 45))
        val result = sessionDao.getById(id).first()
        assertNotNull(result)
        assertEquals(45, result!!.durationMinutes)
    }

    @Test
    fun getById_id_inexistente_devuelve_null() = runBlocking {
        val result = sessionDao.getById(999L).first()
        assertNull(result)
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Test
    fun delete_elimina_la_sesion_correcta() = runBlocking {
        val id1 = sessionDao.insert(TrainingSessionEntity(date = 1000L))
        val id2 = sessionDao.insert(TrainingSessionEntity(date = 2000L))
        sessionDao.delete(TrainingSessionEntity(id = id1, date = 1000L))
        val remaining = sessionDao.getAll().first()
        assertEquals(1, remaining.size)
        assertEquals(id2, remaining.first().id)
    }

    // ─── GetSessionsByRoutine ─────────────────────────────────────────────────

    @Test
    fun getSessionsByRoutine_filtra_por_rutina_correctamente() = runBlocking {
        val routineId = routineDao.insert(RoutineEntity(name = "Push Day"))
        sessionDao.insert(TrainingSessionEntity(routineId = routineId, date = 1000L))
        sessionDao.insert(TrainingSessionEntity(routineId = routineId, date = 2000L))
        sessionDao.insert(TrainingSessionEntity(routineId = null, date = 3000L)) // sin rutina
        val result = sessionDao.getSessionsByRoutine(routineId).first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.routineId == routineId })
    }

    @Test
    fun getSessionsByRoutine_sin_sesiones_para_esa_rutina_devuelve_vacio() = runBlocking {
        val routineId = routineDao.insert(RoutineEntity(name = "Pull Day"))
        val otherRoutineId = routineDao.insert(RoutineEntity(name = "Leg Day"))
        sessionDao.insert(TrainingSessionEntity(routineId = otherRoutineId))
        val result = sessionDao.getSessionsByRoutine(routineId).first()
        assertTrue(result.isEmpty())
    }

    // ─── GetSessionsBetweenDates ──────────────────────────────────────────────

    @Test
    fun getSessionsBetweenDates_filtra_correctamente_por_rango() = runBlocking {
        sessionDao.insert(TrainingSessionEntity(date = 500L))   // fuera
        sessionDao.insert(TrainingSessionEntity(date = 1000L))  // dentro
        sessionDao.insert(TrainingSessionEntity(date = 5000L))  // dentro
        sessionDao.insert(TrainingSessionEntity(date = 9000L))  // fuera
        val result = sessionDao.getSessionsBetweenDates(from = 800L, to = 6000L).first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.date in 800L..6000L })
    }

    // ─── GetSessionWithSets (@Transaction) ────────────────────────────────────

    @Test
    fun getSessionWithSets_sesion_vacia_devuelve_lista_sets_vacia() = runBlocking {
        val sessionId = sessionDao.insert(TrainingSessionEntity())
        val result = sessionDao.getSessionWithSets(sessionId).first()
        assertNotNull(result)
        assertTrue(result!!.sets.isEmpty())
    }

    @Test
    fun getSessionWithSets_devuelve_sets_asociados_a_la_sesion() = runBlocking {
        // Necesitamos un ejercicio para la FK de TrainingSetEntity
        val exerciseId = database.exerciseDao().insert(
            ExerciseEntity(name = "Sentadilla", muscleGroup = "Piernas")
        )
        val sessionId = sessionDao.insert(TrainingSessionEntity())
        setDao.insert(TrainingSetEntity(sessionId = sessionId, exerciseId = exerciseId, setNumber = 1, reps = 10, weight = 80.0))
        setDao.insert(TrainingSetEntity(sessionId = sessionId, exerciseId = exerciseId, setNumber = 2, reps = 8, weight = 85.0))
        val result = sessionDao.getSessionWithSets(sessionId).first()
        assertNotNull(result)
        assertEquals(2, result!!.sets.size)
    }

    @Test
    fun getAllSessionsWithSets_devuelve_todas_con_sus_sets() = runBlocking {
        val exerciseId = database.exerciseDao().insert(
            ExerciseEntity(name = "Press Banca", muscleGroup = "Pecho")
        )
        val sessionId1 = sessionDao.insert(TrainingSessionEntity(date = 1000L))
        val sessionId2 = sessionDao.insert(TrainingSessionEntity(date = 2000L))
        setDao.insert(TrainingSetEntity(sessionId = sessionId1, exerciseId = exerciseId, setNumber = 1))
        setDao.insert(TrainingSetEntity(sessionId = sessionId1, exerciseId = exerciseId, setNumber = 2))
        // sessionId2 sin sets
        val all = sessionDao.getAllSessionsWithSets().first()
        assertEquals(2, all.size)
        val session1WithSets = all.find { it.session.id == sessionId1 }
        assertEquals(2, session1WithSets!!.sets.size)
        val session2WithSets = all.find { it.session.id == sessionId2 }
        assertTrue(session2WithSets!!.sets.isEmpty())
    }
}

