package com.example.tfg_carloscaramecerero

import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseType
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineExerciseCrossRef
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSessionEntity
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSetEntity
import com.example.tfg_carloscaramecerero.data.local.relation.RoutineWithExercises
import com.example.tfg_carloscaramecerero.data.local.relation.SessionWithSets
import com.example.tfg_carloscaramecerero.domain.repository.ExerciseRepository
import com.example.tfg_carloscaramecerero.domain.repository.RoutineRepository
import com.example.tfg_carloscaramecerero.domain.repository.TrainingRepository
import com.example.tfg_carloscaramecerero.viewmodel.TrainingViewModel
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
 * Tests unitarios de TrainingViewModel.
 * Cubre gestión de rutinas, ejercicios, sesiones y series.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrainingViewModelTest {

    private lateinit var fakeExerciseRepo: FakeExerciseRepository
    private lateinit var fakeRoutineRepo: FakeRoutineRepository
    private lateinit var fakeTrainingRepo: FakeTrainingRepository
    private lateinit var viewModel: TrainingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeExerciseRepo = FakeExerciseRepository()
        fakeRoutineRepo = FakeRoutineRepository()
        fakeTrainingRepo = FakeTrainingRepository()
        viewModel = TrainingViewModel(fakeExerciseRepo, fakeRoutineRepo, fakeTrainingRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── Ejercicios ───────────────────────────────────────────────────────────

    @Test
    fun `createExercise inserta ejercicio con nombre y grupo muscular correctos`() {
        viewModel.createExercise("Press Banca", "Pecho")
        assertEquals(1, fakeExerciseRepo.insertedExercises.size)
        with(fakeExerciseRepo.insertedExercises.first()) {
            assertEquals("Press Banca", name)
            assertEquals("Pecho", muscleGroup)
        }
    }

    @Test
    fun `createExercise por defecto usa tipo STRENGTH`() {
        viewModel.createExercise("Sentadilla", "Piernas")
        val inserted = fakeExerciseRepo.insertedExercises.first()
        assertEquals(ExerciseType.STRENGTH.name, inserted.exerciseType)
    }

    @Test
    fun `createExercise con tipo CARDIO guarda tipo cardio`() {
        viewModel.createExercise("Carrera continua", "Cardio", exerciseType = ExerciseType.CARDIO)
        val inserted = fakeExerciseRepo.insertedExercises.first()
        assertEquals(ExerciseType.CARDIO.name, inserted.exerciseType)
    }

    @Test
    fun `deleteExercise llama al repositorio con el ejercicio correcto`() {
        val exercise = ExerciseEntity(id = 1L, name = "Dominadas", muscleGroup = "Espalda")
        viewModel.deleteExercise(exercise)
        assertEquals(1, fakeExerciseRepo.deletedExercises.size)
        assertEquals(exercise, fakeExerciseRepo.deletedExercises.first())
    }

    @Test
    fun `updateExercise llama al repositorio con el ejercicio actualizado`() {
        val original = ExerciseEntity(id = 1L, name = "Curl biceps", muscleGroup = "Brazos")
        val updated = original.copy(description = "Con barra recta")
        viewModel.updateExercise(updated)
        assertEquals(1, fakeExerciseRepo.updatedExercises.size)
        assertEquals("Con barra recta", fakeExerciseRepo.updatedExercises.first().description)
    }

    // ─── Rutinas ──────────────────────────────────────────────────────────────

    @Test
    fun `createRoutine inserta rutina con nombre correcto`() {
        viewModel.createRoutine("Pierna Lunes")
        assertEquals(1, fakeRoutineRepo.insertedRoutines.size)
        assertEquals("Pierna Lunes", fakeRoutineRepo.insertedRoutines.first().name)
    }

    @Test
    fun `createRoutine con descripcion la guarda correctamente`() {
        viewModel.createRoutine("Push Day", "Pecho, hombros y triceps")
        val inserted = fakeRoutineRepo.insertedRoutines.first()
        assertEquals("Push Day", inserted.name)
        assertEquals("Pecho, hombros y triceps", inserted.description)
    }

    @Test
    fun `deleteRoutine llama al repositorio con la rutina correcta`() {
        val routine = RoutineEntity(id = 1L, name = "Full Body")
        viewModel.deleteRoutine(routine)
        assertEquals(1, fakeRoutineRepo.deletedRoutines.size)
        assertEquals(routine, fakeRoutineRepo.deletedRoutines.first())
    }

    @Test
    fun `updateRoutine llama al repositorio con la rutina actualizada`() {
        val updated = RoutineEntity(id = 1L, name = "Nuevo nombre")
        viewModel.updateRoutine(updated)
        assertEquals(1, fakeRoutineRepo.updatedRoutines.size)
        assertEquals("Nuevo nombre", fakeRoutineRepo.updatedRoutines.first().name)
    }

    @Test
    fun `addExerciseToRoutine inserta cross-ref con ids correctos`() {
        viewModel.addExerciseToRoutine(routineId = 10L, exerciseId = 5L, order = 2)
        assertEquals(1, fakeRoutineRepo.addedCrossRefs.size)
        with(fakeRoutineRepo.addedCrossRefs.first()) {
            assertEquals(10L, routineId)
            assertEquals(5L, exerciseId)
            assertEquals(2, orderIndex)
        }
    }

    @Test
    fun `removeExerciseFromRoutine elimina cross-ref correcta`() {
        viewModel.removeExerciseFromRoutine(routineId = 10L, exerciseId = 5L)
        assertEquals(1, fakeRoutineRepo.removedCrossRefs.size)
        with(fakeRoutineRepo.removedCrossRefs.first()) {
            assertEquals(10L, routineId)
            assertEquals(5L, exerciseId)
        }
    }

    // ─── Sesiones ─────────────────────────────────────────────────────────────

    @Test
    fun `startSession inserta sesion con routineId y restSeconds correctos`() {
        var callbackId = -1L
        viewModel.startSession(routineId = 3L, restSeconds = 90) { id -> callbackId = id }
        assertEquals(1, fakeTrainingRepo.insertedSessions.size)
        with(fakeTrainingRepo.insertedSessions.first()) {
            assertEquals(3L, routineId)
            assertEquals(90, restSeconds)
        }
        assertEquals(fakeTrainingRepo.nextId, callbackId)
    }

    @Test
    fun `deleteSession llama al repositorio con la sesion correcta`() {
        val session = TrainingSessionEntity(id = 7L, routineId = 1L)
        viewModel.deleteSession(session)
        assertEquals(1, fakeTrainingRepo.deletedSessions.size)
        assertEquals(session, fakeTrainingRepo.deletedSessions.first())
    }

    // ─── Series (Sets) ────────────────────────────────────────────────────────

    @Test
    fun `addSet inserta serie de fuerza con parametros correctos`() {
        viewModel.addSet(sessionId = 1L, exerciseId = 2L, setNumber = 1, reps = 10, weight = 60.0)
        assertEquals(1, fakeTrainingRepo.insertedSets.size)
        with(fakeTrainingRepo.insertedSets.first()) {
            assertEquals(1L, sessionId)
            assertEquals(2L, exerciseId)
            assertEquals(1, setNumber)
            assertEquals(10, reps)
            assertEquals(60.0, weight, 0.001)
            assertFalse(isCardio)
        }
    }

    @Test
    fun `addCardioSet inserta serie de cardio con parametros correctos`() {
        viewModel.addCardioSet(
            sessionId = 1L,
            exerciseId = 3L,
            setNumber = 1,
            durationSeconds = 1800,
            distanceKm = 5.0
        )
        assertEquals(1, fakeTrainingRepo.insertedSets.size)
        with(fakeTrainingRepo.insertedSets.first()) {
            assertTrue(isCardio)
            assertEquals(1800, durationSeconds)
            assertEquals(5.0, distanceKm, 0.001)
        }
    }

    @Test
    fun `deleteSet llama al repositorio con la serie correcta`() {
        val set = TrainingSetEntity(id = 9L, sessionId = 1L, exerciseId = 1L, setNumber = 1)
        viewModel.deleteSet(set)
        assertEquals(1, fakeTrainingRepo.deletedSets.size)
        assertEquals(set, fakeTrainingRepo.deletedSets.first())
    }

    // ─── Fake Repositories ────────────────────────────────────────────────────

    class FakeExerciseRepository : ExerciseRepository {
        private val _exercises = MutableStateFlow<List<ExerciseEntity>>(emptyList())
        val insertedExercises = mutableListOf<ExerciseEntity>()
        val deletedExercises = mutableListOf<ExerciseEntity>()
        val updatedExercises = mutableListOf<ExerciseEntity>()

        override fun getAll(): Flow<List<ExerciseEntity>> = _exercises
        override fun getById(id: Long): Flow<ExerciseEntity?> =
            _exercises.map { it.find { e -> e.id == id } }
        override fun getByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>> =
            _exercises.map { it.filter { e -> e.muscleGroup == muscleGroup } }
        override fun getAllMuscleGroups(): Flow<List<String>> =
            _exercises.map { it.map { e -> e.muscleGroup }.distinct() }
        override fun getByType(type: String): Flow<List<ExerciseEntity>> =
            _exercises.map { it.filter { e -> e.exerciseType == type } }

        override suspend fun insert(exercise: ExerciseEntity): Long {
            insertedExercises.add(exercise)
            _exercises.value = _exercises.value + exercise
            return exercise.id
        }
        override suspend fun update(exercise: ExerciseEntity) { updatedExercises.add(exercise) }
        override suspend fun delete(exercise: ExerciseEntity) { deletedExercises.add(exercise) }
    }

    class FakeRoutineRepository : RoutineRepository {
        private val _routines = MutableStateFlow<List<RoutineEntity>>(emptyList())
        val insertedRoutines = mutableListOf<RoutineEntity>()
        val deletedRoutines = mutableListOf<RoutineEntity>()
        val updatedRoutines = mutableListOf<RoutineEntity>()
        val addedCrossRefs = mutableListOf<RoutineExerciseCrossRef>()
        val removedCrossRefs = mutableListOf<RoutineExerciseCrossRef>()

        override fun getAll(): Flow<List<RoutineEntity>> = _routines
        override fun getById(id: Long): Flow<RoutineEntity?> =
            _routines.map { it.find { r -> r.id == id } }
        override fun getRoutineWithExercises(routineId: Long): Flow<RoutineWithExercises?> =
            flowOf(null)
        override fun getAllRoutinesWithExercises(): Flow<List<RoutineWithExercises>> =
            flowOf(emptyList())
        override fun getCrossRefsForRoutine(routineId: Long): Flow<List<RoutineExerciseCrossRef>> =
            flowOf(emptyList())

        override suspend fun insert(routine: RoutineEntity): Long {
            insertedRoutines.add(routine)
            _routines.value = _routines.value + routine
            return routine.id
        }
        override suspend fun update(routine: RoutineEntity) { updatedRoutines.add(routine) }
        override suspend fun delete(routine: RoutineEntity) {
            deletedRoutines.add(routine)
            _routines.value = _routines.value - routine
        }
        override suspend fun addExerciseToRoutine(crossRef: RoutineExerciseCrossRef) {
            addedCrossRefs.add(crossRef)
        }
        override suspend fun removeExerciseFromRoutine(crossRef: RoutineExerciseCrossRef) {
            removedCrossRefs.add(crossRef)
        }
        override suspend fun deleteAllExercisesFromRoutine(routineId: Long) {}
    }

    class FakeTrainingRepository : TrainingRepository {
        var nextId = 1L
        val insertedSessions = mutableListOf<TrainingSessionEntity>()
        val deletedSessions = mutableListOf<TrainingSessionEntity>()
        val insertedSets = mutableListOf<TrainingSetEntity>()
        val deletedSets = mutableListOf<TrainingSetEntity>()

        override fun getAllSessions(): Flow<List<TrainingSessionEntity>> = flowOf(emptyList())
        override fun getSessionById(id: Long): Flow<TrainingSessionEntity?> = flowOf(null)
        override fun getSessionsByRoutine(routineId: Long): Flow<List<TrainingSessionEntity>> =
            flowOf(emptyList())
        override fun getSessionsBetweenDates(from: Long, to: Long): Flow<List<TrainingSessionEntity>> =
            flowOf(emptyList())
        override fun getSessionWithSets(sessionId: Long): Flow<SessionWithSets?> = flowOf(null)
        override fun getAllSessionsWithSets(): Flow<List<SessionWithSets>> = flowOf(emptyList())
        override fun getSessionsWithSetsByRoutine(routineId: Long): Flow<List<SessionWithSets>> =
            flowOf(emptyList())

        override suspend fun insertSession(session: TrainingSessionEntity): Long {
            insertedSessions.add(session)
            return nextId
        }
        override suspend fun deleteSession(session: TrainingSessionEntity) {
            deletedSessions.add(session)
        }

        override fun getSetsBySession(sessionId: Long): Flow<List<TrainingSetEntity>> =
            flowOf(emptyList())
        override fun getSetsByExercise(exerciseId: Long): Flow<List<TrainingSetEntity>> =
            flowOf(emptyList())

        override suspend fun insertSet(set: TrainingSetEntity): Long {
            insertedSets.add(set)
            return set.id
        }
        override suspend fun insertSets(sets: List<TrainingSetEntity>) {
            insertedSets.addAll(sets)
        }
        override suspend fun deleteSet(set: TrainingSetEntity) { deletedSets.add(set) }
        override suspend fun deleteAllSetsBySession(sessionId: Long) {}
    }
}

