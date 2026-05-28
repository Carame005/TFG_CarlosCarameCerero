# 🎤 Guión de Exposición – TFG DAM
## FitAI: Aplicación Android de Fitness Inteligente con IA
**Carlos Carame Cerero · DAM · Mayo 2026**  
**Duración objetivo: 18–20 minutos**

---

## ⏱ Distribución del tiempo

| Bloque | Contenido | Minutos |
|--------|-----------|---------|
| 1 | Introducción | 2 min |
| 2 | Contextualización | 3 min |
| 3 | Tecnologías y arquitectura | 4 min |
| 4 | Demo de la app + fragmento de código | 7 min |
| 5 | Pruebas y calidad | 2 min |
| Cierre | Conclusiones + turno de preguntas | 2 min |

---

## 🟢 BLOQUE 1 – Introducción (2 min)

### Qué decir

> *"Buenos días, me llamo Carlos Carame Cerero y voy a presentar mi Trabajo de Fin de Grado del ciclo de Desarrollo de Aplicaciones Multiplataforma."*

> *"La aplicación se llama **FitAI** — Fitness con Inteligencia Artificial."*

> *"En resumen: es una aplicación Android nativa que permite llevar un seguimiento completo de tu entrenamiento, nutrición y evolución corporal, y que incorpora un asistente de inteligencia artificial que no solo responde preguntas, sino que puede **actuar sobre la app** y crear rutinas o planes de comidas por ti."*

### Puntos clave a mencionar
- Nombre: **FitAI**
- Plataforma: Android (mínimo Android 7.0)
- Lenguaje: Kotlin con Jetpack Compose
- IA integrada: Google Gemini 2.5 Flash
- Versión actual: v2.4 (Mayo 2026)

---

## 🟡 BLOQUE 2 – Contextualización (3 min)

### Por qué lo hice

> *"La idea surge de observar el mercado actual: hay muchas apps de fitness, pero ninguna combina en un solo producto el seguimiento de entrenamiento, nutrición, datos corporales Y un asistente IA que conozca tu historial completo."*

| App del mercado | ¿Registra entreno? | ¿Registra nutrición? | ¿IA conversacional? | ¿IA crea contenido? |
|---|---|---|---|---|
| MyFitnessPal | ❌ | ✅ | ❌ | ❌ |
| Hevy | ✅ | ❌ | ❌ | ❌ |
| Strong | ✅ | ❌ | ❌ | ❌ |
| **FitAI** | ✅ | ✅ | ✅ | ✅ |

### Para quién sirve
- **Deportista amateur** que entrena en el gimnasio sin entrenador personal
- **Persona que quiere perder peso o ganar músculo** con un plan personalizado
- **Usuario con condiciones de salud** que necesita que la IA tenga en cuenta sus analíticas o patologías
- **Cualquier persona** que quiera centralizar todo su seguimiento fitness en una sola app gratuita

### El diferenciador

> *"El punto clave que distingue FitAI es que el asistente IA no solo responde preguntas genéricas — conoce TU perfil, TU historial de entrenamientos, TU alimentación semanal y hasta tus documentos médicos en PDF. Y lo más interesante: puede **escribir en la base de datos** de la app para crear rutinas o planes de comidas completos con una sola instrucción."*

### Tendencias que lo justifican
1. IA generativa en apps móviles (mercado IA fitness: ~40.000M$ en 2030)
2. Demanda de personalización extrema en fitness
3. Privacidad: todos los datos quedan en el dispositivo, sin servidores externos

---

## 🔵 BLOQUE 3 – Tecnologías y Arquitectura (4 min)

### Stack tecnológico

| Capa | Tecnología | Para qué |
|---|---|---|
| Lenguaje | **Kotlin 2.0.21** | Código principal |
| UI | **Jetpack Compose** + Material Design 3 | Interfaz declarativa |
| Arquitectura | **MVVM + Clean Architecture** | Separación de responsabilidades |
| Inyección de dependencias | **Hilt** (Dagger) | Gestión de dependencias |
| Base de datos local | **Room** (SQLite) v11 | Persistencia de todos los datos |
| Navegación | **Navigation Compose** | Navegación entre pantallas |
| IA | **Google Gemini 2.5 Flash** (REST) | Asistente conversacional |
| HTTP | **OkHttp 4.12** | Cliente HTTP para la API de Gemini |
| Tareas background | **WorkManager** | Recordatorios y notificaciones |
| Preferencias | **DataStore** | Tema, tokens, configuración |
| Tests | **JUnit + MockK + Turbine** | Tests unitarios |
| Tests BD | **Room Testing (in-memory)** | Tests instrumentados |

### Arquitectura por capas

```
┌─────────────────────────────────────────────┐
│         PRESENTACIÓN (Jetpack Compose)       │
│   15 pantallas · Navigation Compose · M3    │
├─────────────────────────────────────────────┤
│              VIEWMODELS (Hilt)               │
│     StateFlow + Coroutines + flatMapLatest   │
├─────────────────────────────────────────────┤
│          DOMINIO (interfaces puras)          │
│       Repository contracts · Use cases       │
├─────────────────────────────────────────────┤
│           DATOS (implementaciones)           │
│   Room DAOs · GeminiService · DataStore     │
├─────────────────────────────────────────────┤
│              FUENTES DE DATOS                │
│  SQLite (16 entidades) · Gemini API · Prefs  │
└─────────────────────────────────────────────┘
```

### Base de datos: 16 entidades Room

> *"La base de datos tiene 16 entidades y ha pasado por 11 versiones con migraciones explícitas en SQL — nunca se pierden los datos del usuario al actualizar."*

Entidades principales:
- `ExerciseEntity`, `RoutineEntity`, `RoutineExerciseCrossRef`
- `TrainingSessionEntity`, `TrainingSetEntity`
- `BodyWeightEntity`, `BodyMeasurementEntity`, `UserProfileEntity`
- `FoodEntryEntity`, `NutritionalGoalEntity`, **`MealScheduleEntity`** ← (v11, nueva)
- `ChatConversationEntity`, `ChatMessageEntity`
- `HealthDocumentEntity`, `AuditLogEntity`, `RecommendationEntity`

### El componente más técnico: GeminiService con streaming SSE

> *"La integración con la IA utiliza Server-Sent Events (SSE) a través de OkHttp para conseguir el efecto de escritura en tiempo real — cada fragmento de texto llega en cuanto Gemini lo genera, sin esperar la respuesta completa."*

```kotlin
// Fragmento de GeminiService.kt — streaming SSE
fun sendMessageStream(messages: List<GeminiContent>): Flow<String> = flow {
    val request = Request.Builder()
        .url("$BASE_URL?key=$apiKey&alt=sse")
        .post(body)
        .build()

    client.newCall(request).execute().use { response ->
        val reader = BufferedReader(response.body!!.charStream())
        reader.forEachLine { line ->
            if (line.startsWith("data:")) {
                val json = line.removePrefix("data:").trim()
                val text = Gson().fromJson(json, GeminiResponse::class.java)
                    ?.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrEmpty()) emit(text)
            }
        }
    }
}
```

---

## 🟣 BLOQUE 4 – Demo de la App + Código (7 min)

### 📱 Recorrido de pantallas sugerido (en el móvil)

**4.1 Dashboard (30 seg)**
- Mostrar el resumen: peso actual, sesiones de la semana
- Abrir un acceso rápido ("Añadir peso" inline)
- Señalar el FAB de consejos IA

**4.2 Entrenamiento (1.5 min)**
- Abrir una rutina → mostrar los ejercicios con sus chips de grupo muscular
- Pulsar "Iniciar sesión"
- Añadir un set de musculación → señalar las nuevas tarjetas de set:
  - Círculo con número + celdas stat-card (reps/kg)
- Mostrar el temporizador de descanso con barra de progreso animada
- Mostrar que es editable pulsando la tarjeta de descanso

**4.3 Nutrición (1 min)**
- Mostrar la cabecera con el horario activo + botón "Gestionar"
- Navegar a MealSchedulesScreen: varios horarios (Mi dieta, Volumen...)
- Activar uno diferente y volver → la pantalla de Nutrición muestra el nuevo activo

**4.4 Asistente IA — LA PARTE WOW (2 min)**
- Escribe: *"Crea una rutina de 4 días para ganar masa muscular con los ejercicios que tengo"*
- Mostrar el streaming en tiempo real (efecto de escritura)
- Mostrar que la rutina aparece inmediatamente en la pantalla de Entrenamiento
- Opcionalmente: subir un PDF de analítica y preguntar *"¿Tengo déficit de hierro?"*

**4.5 Cuerpo + Ajustes (1 min)**
- Mostrar la gráfica de peso
- Abrir Ajustes → mostrar exportación CSV, copia de seguridad .db, biometría
- Abrir Registro de acciones (AuditLog)

### 💻 Fragmento de código a enseñar

#### Opción A: La magia de la IA creando una rutina (AssistantViewModel)

```kotlin
// AssistantViewModel.kt — La IA analiza el JSON de la respuesta y crea la rutina
private suspend fun executeCreateRoutine(json: String): String {
    val dto = Gson().fromJson(json, CreateRoutineDto::class.java)

    // Validación anti-duplicados
    val existing = trainingRepository.getAllRoutines().first()
    if (existing.any { it.name.trim().equals(dto.name.trim(), ignoreCase = true) }) {
        return "⚠️ Ya existe una rutina llamada \"${dto.name}\". No se ha creado duplicado."
    }

    // Creación en Room mediante repositorio
    val routineId = trainingRepository.createRoutine(
        RoutineEntity(name = dto.name, description = dto.description ?: "")
    )
    dto.exercises.forEach { exerciseDto ->
        val exerciseId = trainingRepository.createExercise(ExerciseEntity(...))
        trainingRepository.addExerciseToRoutine(routineId, exerciseId)
    }
    return "✅ Rutina \"${dto.name}\" creada con ${dto.exercises.size} ejercicios."
}
```

#### Opción B: Múltiples horarios con reactividad (NutritionViewModel)

```kotlin
// NutritionViewModel.kt — flatMapLatest: cuando cambia el horario activo,
// automáticamente se recarga la lista de comidas sin reiniciar el ViewModel
private val _currentScheduleId = MutableStateFlow<Long?>(null)

val allEntries: StateFlow<List<FoodEntryEntity>> =
    _currentScheduleId.flatMapLatest { scheduleId ->
        if (scheduleId != null)
            nutritionRepository.getEntriesBySchedule(scheduleId)
        else
            flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

#### Opción C: Migración Room preservando datos (si preguntan por la BD)

```kotlin
// AppDatabaseMigrations.kt — Migración v10→v11: nueva tabla + columna FK
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS meal_schedules (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """)
        // Insertar horario por defecto para que los datos existentes no queden huérfanos
        database.execSQL("INSERT INTO meal_schedules (id, name, createdAt) VALUES (1, 'Mi dieta', ${System.currentTimeMillis()})")
        // Añadir FK con DEFAULT 1 → todos los datos existentes van al horario por defecto
        database.execSQL("ALTER TABLE food_entries ADD COLUMN scheduleId INTEGER NOT NULL DEFAULT 1")
    }
}
```

---

## 🟠 BLOQUE 5 – Pruebas y Calidad (2 min)

### Resumen de testing

| Tipo | Cantidad | Herramienta |
|---|---|---|
| Tests unitarios (ViewModels + parsers) | **78 tests** | JUnit 4 + MockK + Turbine |
| Tests instrumentados (DAOs Room) | **31 tests** | Room in-memory (BD real) |
| Tests UI Compose | **24 tests** | Compose Testing |
| Tests manuales documentados | **37 pruebas** | — |
| **Total** | **~170 pruebas** | |

### Categorías de tests unitarios

- `NutritionViewModelTest`: 10 tests — añadir entradas, horarios múltiples
- `TrainingViewModelTest`: 16 tests — crear rutinas, ejercicios, sesiones, sets
- `BodyViewModelTest`: 15 tests — peso, medidas, perfil de salud
- `SettingsViewModelTest`: 10 tests — tema, límites, configuración
- `ImportManagerTest`: 26 tests — detección de CSV, parseo de 4 formatos
- `ExampleUnitTest`: 1 test

### Rendimiento medido

| Métrica | Resultado |
|---|---|
| Cold start de la app | **~420 ms** (Samsung Galaxy A54) |
| Inserción de set (UI → Room) | **< 120 ms** |
| Consulta Room @Transaction (500+ registros) | **~18 ms** |

---

## ✅ CIERRE – Conclusiones (2 min)

### Lo que se ha logrado

> *"En 3 meses de desarrollo individual he implementado una aplicación funcional, completa y diferenciadora con:"*

- ✅ 4 módulos funcionales principales (Entrenamiento, Nutrición, Cuerpo, IA)
- ✅ 16 entidades Room con migraciones v1→v11 sin pérdida de datos
- ✅ 15 pantallas con Jetpack Compose y Material Design 3
- ✅ Asistente IA con contexto personalizado y capacidad de escritura en BD
- ✅ ~170 pruebas automatizadas + manuales
- ✅ Funcionalidades avanzadas: biometría, backup .db, streaming SSE, WorkManager

### El aprendizaje más valioso

> *"El reto técnico más interesante fue diseñar el sistema de permisos del asistente IA — la IA puede crear rutinas o comidas, pero solo si el usuario lo ha autorizado explícitamente desde Ajustes, y siempre con validación anti-duplicados. Esto requirió pensar en la seguridad de los datos desde el diseño, no solo desde la implementación."*

### Mejoras futuras priorizadas

1. Análisis nutricional automático por IA (campo `aiAnalyzed` ya preparado en BD)
2. Gráficas detalladas de progreso de series a lo largo del tiempo
3. Sincronización opcional en la nube (Firebase)

---

## ❓ Preguntas frecuentes que pueden hacer

**"¿Por qué Gemini y no ChatGPT?"**
> Gemini 2.5 Flash tiene capa gratuita generosa, integración nativa con el ecosistema Google y soporte para `inline_data` que permite enviar PDFs directamente sin procesado previo.

**"¿Los datos son seguros? ¿Se envían al servidor?"**
> Los únicos datos que salen del dispositivo son los que el usuario envía explícitamente al asistente IA. El resto (peso, entrenamientos, nutrición) solo se almacena en Room (SQLite) en el propio teléfono.

**"¿Qué pasa si Gemini deja de ser gratuito?"**
> La arquitectura está desacoplada: `GeminiService.kt` y `AIModule.kt` son los únicos dos archivos que hay que cambiar para sustituir el proveedor de IA. El resto de la app no se toca.

**"¿Has probado la app con usuarios reales?"**
> Sí, se realizaron pruebas de usabilidad con 3 usuarios con distintos perfiles. La nota media fue **4.5/5**. Los puntos de mejora identificados ya están aplicados en las versiones 2.x.

**"¿Cuántas líneas de código tiene?"**
> Aproximadamente 10.000 líneas de Kotlin repartidas en unos 80 ficheros fuente.

---

## 📋 Checklist antes de la exposición

- [ ] Móvil cargado al 100% y en modo no molestar
- [ ] App instalada con datos de ejemplo (rutinas, sesiones, varios horarios de nutrición, PDF médico subido)
- [ ] API key de Gemini activa y con saldo disponible
- [ ] Modo presentación activado (sin notificaciones)
- [ ] Tener el repositorio GitHub abierto en el portátil por si piden ver el código
- [ ] Cronometrar el ensayo: objetivo < 18 min para tener margen

---

*Esquema generado: Mayo 2026 · FitAI v2.4*

