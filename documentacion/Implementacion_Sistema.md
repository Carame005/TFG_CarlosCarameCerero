# ⚙️ Implementación del Sistema – FitnessApp

**Autor:** Carlos Carame Cerero  
**Fecha:** 15 de abril de 2026  
**Entrega:** Entrega 4 – Implementación del Sistema

---

## 1. Estructura inicial del proyecto

### 1.1 Estructura de carpetas y paquetes

El proyecto sigue la arquitectura **MVVM + Clean Architecture** definida en la Entrega 3. A continuación se muestra la organización real de paquetes bajo `com.example.tfg_carloscaramecerero`:

```
com.example.tfg_carloscaramecerero/
│
├── FitnessApp.kt                    ← Application class (@HiltAndroidApp)
├── MainActivity.kt                  ← Actividad principal, NavController, BottomBar
│
├── data/                            ← CAPA DE DATOS
│   ├── local/
│   │   ├── AppDatabase.kt           ← Room Database (v9, 14 entidades)
│   │   ├── dao/                     ← 12 interfaces DAO
│   │   │   ├── ExerciseDao.kt
│   │   │   ├── RoutineDao.kt
│   │   │   ├── TrainingSessionDao.kt
│   │   │   ├── TrainingSetDao.kt
│   │   │   ├── BodyWeightDao.kt
│   │   │   ├── BodyMeasurementDao.kt
│   │   │   ├── FoodEntryDao.kt
│   │   │   ├── NutritionalGoalDao.kt
│   │   │   ├── RecommendationDao.kt
│   │   │   ├── UserProfileDao.kt
│   │   │   ├── HealthDocumentDao.kt
│   │   │   └── ChatDao.kt
│   │   ├── entity/                  ← 14 entidades Room
│   │   │   ├── ExerciseEntity.kt
│   │   │   ├── RoutineEntity.kt
│   │   │   ├── RoutineExerciseCrossRef.kt
│   │   │   ├── TrainingSessionEntity.kt
│   │   │   ├── TrainingSetEntity.kt
│   │   │   ├── BodyWeightEntity.kt
│   │   │   ├── BodyMeasurementEntity.kt
│   │   │   ├── FoodEntryEntity.kt
│   │   │   ├── NutritionalGoalEntity.kt
│   │   │   ├── RecommendationEntity.kt
│   │   │   ├── UserProfileEntity.kt
│   │   │   ├── HealthDocumentEntity.kt
│   │   │   ├── ChatConversationEntity.kt
│   │   │   └── ChatMessageEntity.kt
│   │   └── relation/                ← Relaciones Room (@Embedded / @Relation)
│   │       ├── RoutineWithExercises.kt
│   │       └── SessionWithSets.kt
│   ├── preferences/
│   │   └── UserPreferencesRepository.kt  ← DataStore (modo oscuro, notificaciones)
│   ├── remote/
│   │   └── GeminiService.kt         ← Cliente REST Gemini (OkHttp + streaming SSE)
│   ├── repository/                  ← Implementaciones de repositorio
│   │   ├── BodyRepositoryImpl.kt
│   │   ├── ChatRepositoryImpl.kt
│   │   ├── ExerciseRepositoryImpl.kt
│   │   ├── NutritionRepositoryImpl.kt
│   │   ├── RecommendationRepositoryImpl.kt
│   │   ├── RoutineRepositoryImpl.kt
│   │   └── TrainingRepositoryImpl.kt
│   └── util/
│       ├── ExportManager.kt         ← Exportación CSV (sesiones, peso, nutrición)
│       └── PdfTextExtractor.kt      ← Extracción de texto de PDFs médicos
│
├── di/                              ← INYECCIÓN DE DEPENDENCIAS (Hilt)
│   ├── AIModule.kt                  ← Provee GeminiService
│   ├── DatabaseModule.kt            ← Provee AppDatabase y todos los DAOs
│   └── RepositoryModule.kt          ← Bind interfaces → implementaciones
│
├── domain/                          ← CAPA DE DOMINIO
│   └── repository/                  ← Interfaces de repositorio (contratos)
│       ├── BodyRepository.kt
│       ├── ChatRepository.kt
│       ├── ExerciseRepository.kt
│       ├── NutritionRepository.kt
│       ├── RecommendationRepository.kt
│       ├── RoutineRepository.kt
│       └── TrainingRepository.kt
│
├── navigation/                      ← NAVEGACIÓN
│   ├── Screen.kt                    ← Sealed class con todas las rutas
│   └── FitnessNavGraph.kt           ← NavHost con todos los destinos
│
├── notifications/
│   └── TrainingReminderWorker.kt    ← WorkManager periódico (recordatorios diarios)
│
├── screens/                         ← CAPA DE PRESENTACIÓN (UI)
│   ├── assistant/
│   │   ├── AssistantScreen.kt       ← Chat IA con streaming
│   │   └── ChatHistoryScreen.kt     ← Historial de conversaciones
│   ├── body/
│   │   └── BodyScreen.kt            ← Peso, medidas, perfil, documentos PDF
│   ├── home/
│   │   └── DashboardScreen.kt       ← Pantalla principal / resumen
│   ├── nutrition/
│   │   └── NutritionScreen.kt       ← Registro alimentación semanal
│   ├── recommendations/
│   │   └── RecommendationsScreen.kt ← Consejos generados por IA
│   ├── settings/
│   │   └── SettingsScreen.kt        ← Ajustes (tema, notificaciones)
│   └── training/
│       ├── TrainingScreen.kt        ← Lista de rutinas y ejercicios
│       ├── ExerciseListScreen.kt    ← Catálogo de ejercicios
│       ├── RoutineDetailScreen.kt   ← Detalle de rutina + historial sesiones
│       └── SessionDetailScreen.kt  ← Detalle de sesión activa/registrada
│
├── components/                      ← Componentes Compose reutilizables
├── viewmodel/                       ← ViewModels
│   ├── AssistantViewModel.kt
│   ├── BodyViewModel.kt
│   ├── DashboardViewModel.kt
│   ├── NutritionViewModel.kt
│   ├── RecommendationsViewModel.kt
│   ├── RoutineTemplates.kt          ← Plantillas de rutinas predefinidas
│   ├── SettingsViewModel.kt
│   └── TrainingViewModel.kt
└── ui/theme/                        ← Tema Material Design 3
```

---

### 1.2 Configuración del entorno – Dependencias

El archivo `gradle/libs.versions.toml` centraliza todas las versiones:

| Tecnología | Versión |
|---|---|
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.0.21 |
| KSP | 2.0.21-1.0.27 |
| Jetpack Compose BOM | 2024.09.00 |
| Room | 2.7.1 |
| Hilt | 2.51.1 |
| Navigation Compose | 2.8.5 |
| OkHttp | 4.12.0 |
| Gson | 2.11.0 |
| WorkManager | 2.10.1 |
| DataStore Preferences | 1.1.4 |
| `compileSdk` / `targetSdk` | 36 |
| `minSdk` | 24 (Android 7.0) |

```kotlin
// app/build.gradle.kts (extracto clave)
android {
    compileSdk = 36
    defaultConfig {
        minSdk = 24
        targetSdk = 36
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }
    buildFeatures { compose = true; buildConfig = true }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")  // exporta esquemas Room
}
```

La API key de Gemini se lee de `local.properties` en tiempo de compilación y se expone como `BuildConfig.GEMINI_API_KEY`, manteniéndola fuera del control de versiones.

---

### 1.3 Implementación de la arquitectura por capas

La arquitectura MVVM + Clean Architecture se ha implementado de la siguiente forma:

```
┌─────────────────────────────────────────────────┐
│          CAPA DE PRESENTACIÓN (UI)               │
│   Jetpack Compose + Material3 + Navigation       │
│   screens/ + components/                         │
├─────────────────────────────────────────────────┤
│              VIEWMODELS                          │
│   @HiltViewModel + StateFlow + Coroutines        │
│   viewmodel/                                     │
├─────────────────────────────────────────────────┤
│            CAPA DE DOMINIO                       │
│   Interfaces de repositorio (contratos)          │
│   domain/repository/                             │
├─────────────────────────────────────────────────┤
│            CAPA DE DATOS                         │
│   Implementaciones + DAOs + Remote               │
│   data/repository/ + data/local/ + data/remote/  │
├─────────────────────────────────────────────────┤
│          FUENTES DE DATOS                        │
│   Room (SQLite local) │ Gemini API REST          │
│   DataStore (prefs)   │ WorkManager              │
└─────────────────────────────────────────────────┘
```

**Flujo de datos (ejemplo: cargar rutinas):**

```
TrainingScreen
    │ observa StateFlow<List<RoutineWithExercises>>
    ▼
TrainingViewModel
    │ routineRepository.getAllRoutinesWithExercises()
    ▼
RoutineRepository (interfaz dominio)
    │ implementado por
    ▼
RoutineRepositoryImpl
    │ routineDao.getAllRoutinesWithExercises()
    ▼
RoutineDao (@Dao Room)
    │ SQL: SELECT * FROM routines + JOIN exercises
    ▼
AppDatabase (Room / SQLite)
```

El resultado se emite como `Flow<List<RoutineWithExercises>>`, que el ViewModel convierte a `StateFlow` con `stateIn(SharingStarted.WhileSubscribed(5000))`. La UI observa ese `StateFlow` con `collectAsState()`, actualizándose automáticamente ante cualquier cambio en base de datos.

---

### 1.4 Inyección de dependencias – Módulos Hilt

Se han creado tres módulos Hilt instalados en `SingletonComponent`:

**`DatabaseModule.kt`** — Provee la base de datos y todos los DAOs como Singleton:
```kotlin
@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "fitness_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()
    // ... un @Provides por cada DAO (12 en total)
}
```

**`RepositoryModule.kt`** — Vincula interfaces de dominio con sus implementaciones:
```kotlin
@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository
    @Binds @Singleton
    abstract fun bindRoutineRepository(impl: RoutineRepositoryImpl): RoutineRepository
    // ... 7 repositorios en total
}
```

**`AIModule.kt`** — Provee el servicio de IA:
```kotlin
@Module @InstallIn(SingletonComponent::class)
object AIModule {
    @Provides @Singleton
    fun provideGeminiService(@ApplicationContext context: Context): GeminiService =
        GeminiService(apiKey = BuildConfig.GEMINI_API_KEY, context = context)
}
```

**`FitnessApp.kt`** — Entry point de Hilt con integración de WorkManager:
```kotlin
@HiltAndroidApp
class FitnessApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        TrainingReminderWorker.createNotificationChannel(this)
    }
}
```

---

## 2. Primeras pantallas e interfaz

### 2.1 Navegación implementada

La navegación se gestiona mediante `Navigation Compose`. Las rutas se definen en una `sealed class`:

```kotlin
sealed class Screen(val route: String, val label: String = "", val icon: ImageVector? = null) {
    // ── Tabs de la barra inferior ──
    data object Dashboard   : Screen("dashboard",     "Inicio",    Icons.Default.Home)
    data object Training    : Screen("training",      "Entreno",   Icons.Default.FitnessCenter)
    data object Assistant   : Screen("assistant",     "Asistente", Icons.Default.SmartToy)
    data object Body        : Screen("body",          "Cuerpo",    Icons.Default.MonitorWeight)
    data object Nutrition   : Screen("nutrition",     "Nutrición", Icons.Default.Restaurant)
    data object Recommendations : Screen("recommendations", "Consejos", Icons.Default.Lightbulb)

    // ── Sub-pantallas con argumentos ──
    data object RoutineDetail : Screen("routine_detail/{routineId}") {
        fun createRoute(routineId: Long) = "routine_detail/$routineId"
    }
    data object SessionDetail : Screen("session_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "session_detail/$sessionId"
    }
    data object AssistantChat : Screen("assistant_chat?conversationId={conversationId}") {
        fun createRoute(conversationId: Long? = null) = ...
    }
    data object ExerciseList : Screen("exercise_list")
    data object ChatHistory  : Screen("chat_history")
    data object Settings     : Screen("settings")

    companion object {
        val bottomNavItems = listOf(Dashboard, Training, Assistant, Body, Nutrition)
    }
}
```

**Barra de navegación inferior** con 5 tabs: Inicio, Entreno, **Asistente** (botón central destacado), Cuerpo y Nutrición. La barra se oculta automáticamente en sub-pantallas (detalle de rutina, sesión, chat, etc.):

```kotlin
// MainActivity.kt
val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }
```

### 2.2 Pantallas implementadas

| Pantalla | Ruta | ViewModel | Descripción |
|---|---|---|---|
| `DashboardScreen` | `dashboard` | `DashboardViewModel` | Resumen: peso actual, sesiones recientes, calorías del día, rutinas |
| `TrainingScreen` | `training` | `TrainingViewModel` | Lista de rutinas, acceso a ejercicios |
| `ExerciseListScreen` | `exercise_list` | `TrainingViewModel` | Catálogo completo de ejercicios con filtros |
| `RoutineDetailScreen` | `routine_detail/{id}` | `TrainingViewModel` | Detalle de rutina, ejercicios asignados, historial de sesiones |
| `SessionDetailScreen` | `session_detail/{id}` | `TrainingViewModel` | Registro activo/detalle de sesión con series |
| `BodyScreen` | `body` | `BodyViewModel` | Peso, medidas corporales, perfil, documentos PDF |
| `NutritionScreen` | `nutrition` | `NutritionViewModel` | Registro alimentación por día de semana |
| `AssistantScreen` | `assistant_chat` | `AssistantViewModel` | Chat IA con streaming de respuestas |
| `ChatHistoryScreen` | `chat_history` | `AssistantViewModel` | Historial de conversaciones guardadas |
| `RecommendationsScreen` | `recommendations` | `RecommendationsViewModel` | Consejos personalizados generados por IA |
| `SettingsScreen` | `settings` | `SettingsViewModel` | Modo oscuro/claro, notificaciones |

### 2.3 Tema visual implementado

- **Material Design 3** con soporte de tema dinámico oscuro/claro.
- El modo oscuro/claro se persiste en **DataStore** y se aplica en `MainActivity`:

```kotlin
val darkModePreference by settingsViewModel.darkMode.collectAsState()
val systemDark = isSystemInDarkTheme()
val isDark = darkModePreference ?: systemDark   // respeta preferencia o sigue el sistema
TFG_CarlosCarameCereroTheme(darkTheme = isDark) { ... }
```

---

## 3. Conexión con la base de datos

### 3.1 Configuración de Room

La base de datos se define en `AppDatabase.kt` como una clase abstracta que extiende `RoomDatabase`:

```kotlin
@Database(
    entities = [
        ExerciseEntity::class, RoutineEntity::class, RoutineExerciseCrossRef::class,
        TrainingSessionEntity::class, TrainingSetEntity::class,
        BodyWeightEntity::class, BodyMeasurementEntity::class,
        FoodEntryEntity::class, NutritionalGoalEntity::class,
        RecommendationEntity::class, UserProfileEntity::class,
        HealthDocumentEntity::class, ChatConversationEntity::class, ChatMessageEntity::class
    ],
    version = 9,
    exportSchema = true          // genera JSON de esquema en app/schemas/
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    // ... 12 DAOs en total
}
```

### 3.2 Entidades y tablas creadas

| Entidad | Tabla | Campos principales |
|---|---|---|
| `ExerciseEntity` | `exercises` | `id`, `name`, `description`, `muscleGroup`, `exerciseType` (`STRENGTH`/`CARDIO`) |
| `RoutineEntity` | `routines` | `id`, `name`, `description`, `createdAt` |
| `RoutineExerciseCrossRef` | `routine_exercise_cross_ref` | `routineId`, `exerciseId`, `orderIndex`, `defaultSets`, `defaultReps` |
| `TrainingSessionEntity` | `training_sessions` | `id`, `routineId` (FK→routines), `date`, `durationMinutes`, `notes`, `restSeconds` |
| `TrainingSetEntity` | `training_sets` | `id`, `sessionId` (FK→sessions), `exerciseId` (FK→exercises), `setNumber`, `reps`, `weight`, `durationSeconds`, `distanceKm`, `isCardio` |
| `BodyWeightEntity` | `body_weight` | `id`, `weight`, `date` |
| `BodyMeasurementEntity` | `body_measurements` | `id`, `date`, `chest`, `waist`, `hips`, `biceps`, `thighs` |
| `FoodEntryEntity` | `food_entries` | `id`, `description`, `mealType`, `dayOfWeek`, `time`, `foodType`, `grams`, `calories`, `protein`, `carbs`, `fat`, `aiAnalyzed` |
| `NutritionalGoalEntity` | `nutritional_goals` | `id`, `calories`, `protein`, `carbs`, `fat`, `createdAt` |
| `RecommendationEntity` | `recommendations` | `id`, `type` (`training`/`nutrition`/`body`), `message`, `createdAt`, `isRead` |
| `UserProfileEntity` | `user_profile` | `id=1` (único), `height`, `healthConditions`, `fitnessGoal` |
| `HealthDocumentEntity` | `health_documents` | `id`, `fileName`, `filePath`, `uploadDate` |
| `ChatConversationEntity` | `chat_conversations` | `id`, `title`, `createdAt`, `updatedAt` |
| `ChatMessageEntity` | `chat_messages` | `id`, `conversationId` (FK→conversations), `content`, `isUser`, `timestamp` |

### 3.3 Relaciones implementadas

**Relación muchos-a-muchos: Rutinas ↔ Ejercicios**

Implementada mediante tabla intermedia `RoutineExerciseCrossRef` y la anotación `@Junction` de Room:

```kotlin
data class RoutineWithExercises(
    @Embedded val routine: RoutineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RoutineExerciseCrossRef::class,
            parentColumn = "routineId",
            entityColumn = "exerciseId"
        )
    )
    val exercises: List<ExerciseEntity>
)
```

**Relación uno-a-muchos: Sesiones → Series**

```kotlin
data class SessionWithSets(
    @Embedded val session: TrainingSessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val sets: List<TrainingSetEntity>
)
```

**Claves foráneas con integridad referencial:**
- `TrainingSessionEntity.routineId` → `routines.id` (ON DELETE SET NULL)
- `TrainingSetEntity.sessionId` → `training_sessions.id` (ON DELETE CASCADE)
- `TrainingSetEntity.exerciseId` → `exercises.id` (ON DELETE CASCADE)
- `ChatMessageEntity.conversationId` → `chat_conversations.id` (ON DELETE CASCADE)

### 3.4 DAOs con operaciones CRUD

Ejemplo del `ExerciseDao` implementado y funcional:

```kotlin
@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Update  suspend fun update(exercise: ExerciseEntity)
    @Delete  suspend fun delete(exercise: ExerciseEntity)

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup ORDER BY name ASC")
    fun getByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>

    @Query("SELECT DISTINCT muscleGroup FROM exercises ORDER BY muscleGroup ASC")
    fun getAllMuscleGroups(): Flow<List<String>>
}
```

`RoutineDao` incluye además operaciones `@Transaction` para cargar relaciones completas:

```kotlin
@Transaction
@Query("SELECT * FROM routines ORDER BY createdAt DESC")
fun getAllRoutinesWithExercises(): Flow<List<RoutineWithExercises>>

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertRoutineExerciseCrossRef(crossRef: RoutineExerciseCrossRef)

@Query("DELETE FROM routine_exercise_cross_ref WHERE routineId = :routineId")
suspend fun deleteAllExercisesFromRoutine(routineId: Long)
```

### 3.5 Esquemas exportados

Room exporta automáticamente el esquema JSON a `app/schemas/com.example.tfg_carloscaramecerero.data.local.AppDatabase/` en cada versión. La versión actual es la **9**, fruto de múltiples migraciones durante el desarrollo de nuevas funcionalidades.

---

## 4. Funcionalidades transversales implementadas

### 4.1 Integración con Google Gemini 2.5 Flash

El servicio `GeminiService` implementa la comunicación REST con la API de Gemini usando `OkHttp`:

- **Respuesta en streaming (SSE):** el texto del asistente aparece palabra a palabra en tiempo real mediante `sendMessageStream()`, que emite un `Flow<String>` con cada fragmento recibido.
- **Respuesta completa:** `sendMessage()` devuelve el texto íntegro para casos como la generación de consejos.
- **Control de tasa local (ApiRateLimiter):** persiste timestamps en `SharedPreferences` y aplica límites de **10 peticiones/minuto** y **500 peticiones/día**. Si se excede el límite, lanza `RateLimitExceededException` con el tiempo de espera.
- **Contexto enriquecido:** el `AssistantViewModel` construye un *system prompt* que incluye datos del perfil del usuario, historial de entrenamientos recientes, registro nutricional, medidas corporales y texto extraído de documentos PDF médicos.

### 4.2 WorkManager – Recordatorios diarios

`TrainingReminderWorker` es un `@HiltWorker` que se ejecuta periódicamente cada 24 horas:

```kotlin
fun scheduleDaily(context: Context) {
    val request = PeriodicWorkRequestBuilder<TrainingReminderWorker>(1, TimeUnit.DAYS)
        .addTag("training_reminder").build()
    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork("training_reminder", REPLACE, request)
}
```

El canal de notificaciones se crea en `FitnessApp.onCreate()` antes de que WorkManager ejecute cualquier trabajo.

### 4.3 DataStore – Preferencias de usuario

`UserPreferencesRepository` gestiona las preferencias persistentes mediante `DataStore`:

```kotlin
val isDarkMode: Flow<Boolean?>              // null = seguir sistema operativo
val notificationsEnabled: Flow<Boolean>

// Permisos de creación del asistente IA
val aiCanCreateRoutines: Flow<Boolean>      // IA puede crear rutinas
val aiCanCreateExercises: Flow<Boolean>     // IA puede crear ejercicios
val aiCanCreateFoodSchedule: Flow<Boolean>  // IA puede crear horario de comidas
```

Los permisos de creación de la IA se gestionan desde **Ajustes → Asistente IA**. Cuando están activados, el asistente puede crear contenido directamente en la app a petición del usuario, incluyendo bloques de acción estructurados en su respuesta que el `AssistantViewModel` parsea y ejecuta automáticamente.

### 4.4 Exportación de datos (ExportManager)

La clase `ExportManager` permite exportar los datos del usuario en formato **CSV** (sesiones de entrenamiento, registros de peso y entradas de nutrición) compartiendo el archivo mediante un `Intent.ACTION_SEND`, usando `FileProvider` para acceso seguro al archivo.

### 4.5 Extracción de texto PDF (PdfTextExtractor)

`PdfTextExtractor` extrae el contenido en texto plano de documentos PDF subidos por el usuario (analíticas médicas, informes de laboratorio). El texto extraído se incluye en el contexto del asistente IA para que pueda interpretar y responder preguntas sobre los documentos de salud.

### 4.6 ViewModels implementados

| ViewModel | Repositorios usados | Estados principales |
|---|---|---|
| `DashboardViewModel` | Training, Body, Nutrition, Routine | `latestWeight`, `allSessions`, `todayFoodEntries`, `routines` |
| `TrainingViewModel` | Exercise, Routine, Training | `routinesWithExercises`, `allExercises`, `currentSession`, `routineSessions` |
| `BodyViewModel` | Body | `weightHistory`, `measurements`, `userProfile`, `healthDocuments` |
| `NutritionViewModel` | Nutrition | `weeklyEntries`, `nutritionalGoal` |
| `AssistantViewModel` | Chat + GeminiService | `messages`, `isLoading`, `currentConversationId`, `streamingText` |
| `RecommendationsViewModel` | Recommendation + GeminiService | `recommendations`, `isGenerating`, `error` |
| `SettingsViewModel` | UserPreferencesRepository | `darkMode`, `notificationsEnabled`, `aiCanCreateRoutines`, `aiCanCreateExercises`, `aiCanCreateFoodSchedule` |

---

## 5. Capturas de pantalla

> **Nota:** Las capturas de pantalla reales deben adjuntarse por el autor.  
> A continuación se describe el contenido de cada pantalla implementada.

### Pantalla 1 – Dashboard (Inicio)
*Muestra un resumen del día: peso actual del usuario, número de sesiones completadas esta semana, calorías del día y acceso rápido a las rutinas recientes.*

### Pantalla 2 – Entreno
*Lista de rutinas creadas con sus ejercicios asociados. Botón para crear nueva rutina. Acceso al catálogo de ejercicios. Al pulsar una rutina, navega a su pantalla de detalle.*

### Pantalla 3 – Detalle de Rutina
*Muestra los ejercicios de la rutina, permite añadir/eliminar ejercicios (selección múltiple) y ver el historial de sesiones anteriores de esa rutina.*

### Pantalla 4 – Sesión de Entrenamiento
*Pantalla de registro de series en tiempo real con temporizador de descanso. Permite registrar repeticiones, peso (fuerza) o tiempo/distancia (cardio) por ejercicio.*

### Pantalla 5 – Catálogo de Ejercicios
*Lista de todos los ejercicios con filtrado por grupo muscular y tipo. Permite crear ejercicios personalizados y editarlos pulsando sobre su tarjeta.*

### Pantalla 6 – Cuerpo
*Gráfica de evolución del peso, registro de medidas corporales (pecho, cintura, cadera, bíceps, muslos), perfil del usuario y gestión de documentos PDF médicos.*

### Pantalla 7 – Nutrición
*Registro de comidas por día de la semana y tipo (desayuno, almuerzo, cena, snack). Objetivo nutricional diario configurado.*

### Pantalla 8 – Asistente IA
*Chat conversacional con Google Gemini 2.5 Flash. Las respuestas aparecen en streaming (efecto de escritura). Soporta texto en negrita con `**texto**`. El asistente conoce el perfil, historial y documentos del usuario.*

### Pantalla 9 – Historial de Conversaciones
*Lista de conversaciones anteriores con el asistente. Permite retomar o eliminar conversaciones pasadas.*

### Pantalla 10 – Ajustes
*Toggle para modo oscuro/claro (o seguir sistema). Toggle para activar/desactivar notificaciones diarias de entrenamiento. Sección **"Asistente IA – Permisos de creación"** con tres toggles para permitir al asistente crear rutinas, ejercicios y entradas en el horario de comidas.*

---

## 6. Conclusión

### 6.1 Qué se ha conseguido hasta ahora

- ✅ **Arquitectura completa** MVVM + Clean Architecture implementada y funcional.
- ✅ **Base de datos Room** con 14 entidades, 12 DAOs, relaciones complejas y exportación de esquemas (versión 9).
- ✅ **Inyección de dependencias** con Hilt en todos los componentes (ViewModels, Repositorios, Workers, Service).
- ✅ **11 pantallas** implementadas con Jetpack Compose y navegación entre todas ellas.
- ✅ **Integración con Gemini AI** funcional: chat con streaming SSE, generación de consejos, contexto personalizado con datos del usuario.
- ✅ **Control de tasa local** persistente para la API de Gemini.
- ✅ **WorkManager** para recordatorios diarios de entrenamiento con notificaciones.
- ✅ **Asistente IA con creación de contenido:** puede crear rutinas, ejercicios y horarios de comida a petición del usuario (controlado por permisos en Ajustes).
- ✅ **DataStore** para persistencia de preferencias de usuario (tema, notificaciones, permisos IA).
- ✅ **Exportación CSV** de datos de entrenamiento, peso y nutrición.
- ✅ **Extracción de texto PDF** para analíticas médicas en el asistente.
- ✅ **Tema dinámico** oscuro/claro con Material Design 3.
- ✅ **Barra de navegación inferior** con 5 tabs y botón central destacado para el asistente IA.

### 6.2 Qué queda pendiente

- 🔲 Análisis nutricional automático con IA (campo `aiAnalyzed` en `FoodEntryEntity` preparado).
- 🔲 Gráficas detalladas de progreso en la pantalla de Cuerpo.
- 🔲 Capturas de pantalla y mockups para la documentación de entrega.
- 🔲 Tests unitarios e instrumentados.
- 🔲 Posibles mejoras de rendimiento y optimización de consultas Room.

### 6.3 Viabilidad del proyecto

El proyecto es **completamente viable** dentro del tiempo establecido. La estructura técnica está íntegramente implementada y funcional: la base de datos persiste datos reales, la integración con Gemini responde con contexto personalizado, y todas las pantallas principales están operativas. Las funcionalidades pendientes son mejoras incrementales sobre una base sólida, no bloqueos estructurales.

---

*Documento generado para la Entrega 4 – Implementación del Sistema del TFG DAM – App Fitness Inteligente con IA*

