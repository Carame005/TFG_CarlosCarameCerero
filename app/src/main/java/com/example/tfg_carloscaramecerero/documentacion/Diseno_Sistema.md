# 🏗️ Diseño del Sistema – FitnessApp

**Autor:** Carlos Carame Cerero  
**Fecha:** 18 de marzo de 2026

---

## 1. Arquitectura del sistema

### 1.1 Organización interna y capas

La aplicación sigue una arquitectura **MVVM + Clean Architecture** estructurada en varias capas:

- **Capa de Presentación (UI):**
  - Implementada con Jetpack Compose (Material Design 3).
  - Pantallas principales: Entreno, Cuerpo, Asistente, Nutrición, Consejos.
  - Navegación gestionada por Navigation Compose.

- **ViewModel:**
  - Gestiona el estado de la UI y la lógica de presentación.
  - Utiliza Kotlin Coroutines y StateFlow/Flow para reactividad.
  - Inyección de dependencias con Hilt.

- **Dominio (UseCases y Repositorios):**
  - Define interfaces de repositorio y casos de uso.
  - Separa la lógica de negocio de la infraestructura.

- **Datos:**
  - Implementaciones de repositorios.
  - Acceso a base de datos local (Room) y servicios externos (Gemini API).
  - DAOs para cada entidad.

- **Base de datos:**
  - Room Database con 14 entidades y 12 DAOs.
  - Persistencia local de todos los datos del usuario.

### 1.2 Tecnologías y frameworks

- **Kotlin** (lenguaje principal)
- **Jetpack Compose** (UI)
- **Material Design 3** (estilo visual)
- **Room** (base de datos local)
- **Hilt** (inyección de dependencias)
- **Kotlin Coroutines + Flow** (reactividad)
- **Navigation Compose** (navegación)
- **OkHttp** (cliente HTTP)
- **Gson** (JSON)
- **Google Gemini 2.5 Flash** (IA)

---

## 2. Diagramas UML

### 2.1 Diagrama de casos de uso

```
┌─────────────┐
│   Usuario   │
└──────┬──────┘
       │
┌──────▼───────┐
│  FitnessApp  │
└──────┬───────┘
       │
┌──────▼───────┐
│  Módulos     │
│ Entreno      │
│ Cuerpo       │
│ Nutrición    │
│ Asistente IA │
│ Consejos     │
└──────┬───────┘
       │
┌──────▼───────┐
│ API Gemini   │
└──────────────┘
```

### 2.2 Diagrama de clases (simplificado)

- **AppDatabase**
  - DAOs: ExerciseDao, RoutineDao, TrainingSessionDao, TrainingSetDao, BodyWeightDao, BodyMeasurementDao, FoodEntryDao, NutritionalGoalDao, RecommendationDao, UserProfileDao, HealthDocumentDao, ChatDao
- **Entidades:**
  - ExerciseEntity, RoutineEntity, TrainingSessionEntity, TrainingSetEntity, BodyWeightEntity, BodyMeasurementEntity, FoodEntryEntity, NutritionalGoalEntity, RecommendationEntity, UserProfileEntity, HealthDocumentEntity, ChatConversationEntity, ChatMessageEntity
- **ViewModels:**
  - TrainingViewModel, BodyViewModel, NutritionViewModel, AssistantViewModel, RecommendationsViewModel
- **Repositorios:**
  - TrainingRepository, RoutineRepository, RecommendationRepository, etc.

### 2.3 Diagrama de secuencia (ejemplo: generación de consejo IA)

```
Usuario → UI → RecommendationsViewModel → RecommendationRepository → GeminiAPI → RecommendationsViewModel → UI → Usuario
```

---

## 3. Diseño de la base de datos

- **Room Database**
- **Entidades principales:**
  - **ExerciseEntity:** Ejercicios (fuerza/cardio)
  - **RoutineEntity:** Rutinas personalizadas
  - **TrainingSessionEntity:** Sesiones de entrenamiento
  - **TrainingSetEntity:** Series de entrenamiento
  - **BodyWeightEntity:** Registros de peso
  - **BodyMeasurementEntity:** Medidas corporales
  - **FoodEntryEntity:** Alimentación diaria
  - **NutritionalGoalEntity:** Objetivos nutricionales
  - **RecommendationEntity:** Consejos IA
  - **UserProfileEntity:** Perfil de usuario
  - **HealthDocumentEntity:** Documentos de salud (PDF)
  - **ChatConversationEntity / ChatMessageEntity:** Historial de conversaciones IA

- **Relaciones:**
  - Rutinas ↔ Ejercicios (cross-ref)
  - Sesiones ↔ Rutinas
  - Series ↔ Sesiones

- **Migración de esquemas:**
  - Versión actual: 9
  - Exportación de esquemas y claves foráneas

---

## 4. Diseño de la interfaz de usuario

*Este apartado se completará con capturas y mockups realizados por el autor.*
- Pantallas principales: Entreno, Cuerpo, Nutrición, Asistente, Consejos
- Estilo visual: Material Design 3, colores personalizados
- Navegación inferior

---

## 5. Diseño de la API y servicios externos

- **Google Gemini 2.5 Flash**
  - Comunicación vía REST (HTTP)
  - Respuestas en streaming (SSE)
  - Contexto personalizado: datos de usuario, entrenamientos, nutrición, salud, analíticas PDF
  - Rate limiting local: 5 peticiones/minuto, 50/día
  - API key protegida en local.properties

---

*Documento generado para la entrega de Diseño del Sistema del TFG DAM – App Fitness Inteligente con IA*

