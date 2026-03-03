package com.example.tfg_carloscaramecerero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    val routinesWithExercises: StateFlow<List<RoutineWithExercises>> =
        routineRepository.getAllRoutinesWithExercises()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<ExerciseEntity>> =
        exerciseRepository.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val muscleGroups: StateFlow<List<String>> =
        exerciseRepository.getAllMuscleGroups()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentRoutine = MutableStateFlow<RoutineWithExercises?>(null)
    val currentRoutine: StateFlow<RoutineWithExercises?> = _currentRoutine.asStateFlow()

    private val _currentSession = MutableStateFlow<SessionWithSets?>(null)
    val currentSession: StateFlow<SessionWithSets?> = _currentSession.asStateFlow()

    private val _sessionRoutineName = MutableStateFlow<String?>(null)
    val sessionRoutineName: StateFlow<String?> = _sessionRoutineName.asStateFlow()

    private val _sessionRoutineExercises = MutableStateFlow<List<ExerciseEntity>>(emptyList())
    val sessionRoutineExercises: StateFlow<List<ExerciseEntity>> = _sessionRoutineExercises.asStateFlow()

    // Historial de sesiones por rutina
    private val _routineSessions = MutableStateFlow<List<SessionWithSets>>(emptyList())
    val routineSessions: StateFlow<List<SessionWithSets>> = _routineSessions.asStateFlow()

    fun loadRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.getRoutineWithExercises(routineId).collect {
                _currentRoutine.value = it
            }
        }
        // También cargar historial de sesiones
        loadRoutineSessions(routineId)
    }

    fun loadRoutineSessions(routineId: Long) {
        viewModelScope.launch {
            trainingRepository.getSessionsWithSetsByRoutine(routineId).collect {
                _routineSessions.value = it
            }
        }
    }

    fun loadSession(sessionId: Long) {
        // Cargar nombre de la rutina y sus ejercicios una sola vez
        viewModelScope.launch {
            val session = trainingRepository.getSessionWithSets(sessionId).firstOrNull()
            val routineId = session?.session?.routineId
            if (routineId != null) {
                val routineWithExercises = routineRepository.getRoutineWithExercises(routineId).firstOrNull()
                _sessionRoutineName.value = routineWithExercises?.routine?.name
                _sessionRoutineExercises.value = routineWithExercises?.exercises ?: emptyList()
            } else {
                _sessionRoutineName.value = null
                _sessionRoutineExercises.value = emptyList()
            }
        }
        // Observar sesión con sets (reactivo a inserciones/borrados)
        viewModelScope.launch {
            trainingRepository.getSessionWithSets(sessionId).collect {
                _currentSession.value = it
            }
        }
    }

    // ── Ejercicios ──
    fun createExercise(
        name: String,
        muscleGroup: String,
        description: String = "",
        exerciseType: ExerciseType = ExerciseType.STRENGTH
    ) {
        viewModelScope.launch {
            exerciseRepository.insert(
                ExerciseEntity(
                    name = name,
                    muscleGroup = muscleGroup,
                    description = description,
                    exerciseType = exerciseType.name
                )
            )
        }
    }

    fun deleteExercise(exercise: ExerciseEntity) {
        viewModelScope.launch { exerciseRepository.delete(exercise) }
    }

    // ── Rutinas ──
    fun createRoutine(name: String, description: String = "") {
        viewModelScope.launch {
            routineRepository.insert(RoutineEntity(name = name, description = description))
        }
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch { routineRepository.delete(routine) }
    }

    fun addExerciseToRoutine(routineId: Long, exerciseId: Long, order: Int = 0) {
        viewModelScope.launch {
            routineRepository.addExerciseToRoutine(
                RoutineExerciseCrossRef(
                    routineId = routineId,
                    exerciseId = exerciseId,
                    orderIndex = order
                )
            )
        }
    }

    fun removeExerciseFromRoutine(routineId: Long, exerciseId: Long) {
        viewModelScope.launch {
            routineRepository.removeExerciseFromRoutine(
                RoutineExerciseCrossRef(routineId = routineId, exerciseId = exerciseId)
            )
        }
    }

    // ── Sesiones ──
    fun startSession(routineId: Long, restSeconds: Int = 60, onSessionCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val sessionId = trainingRepository.insertSession(
                TrainingSessionEntity(routineId = routineId, restSeconds = restSeconds)
            )
            onSessionCreated(sessionId)
        }
    }

    fun addSet(sessionId: Long, exerciseId: Long, setNumber: Int, reps: Int, weight: Double) {
        viewModelScope.launch {
            trainingRepository.insertSet(
                TrainingSetEntity(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    setNumber = setNumber,
                    reps = reps,
                    weight = weight,
                    isCardio = false
                )
            )
        }
    }

    fun addCardioSet(
        sessionId: Long,
        exerciseId: Long,
        setNumber: Int,
        durationSeconds: Int,
        distanceKm: Double = 0.0
    ) {
        viewModelScope.launch {
            trainingRepository.insertSet(
                TrainingSetEntity(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    setNumber = setNumber,
                    durationSeconds = durationSeconds,
                    distanceKm = distanceKm,
                    isCardio = true
                )
            )
        }
    }

    fun deleteSet(set: TrainingSetEntity) {
        viewModelScope.launch { trainingRepository.deleteSet(set) }
    }

    fun deleteSession(session: TrainingSessionEntity) {
        viewModelScope.launch { trainingRepository.deleteSession(session) }
    }
}

