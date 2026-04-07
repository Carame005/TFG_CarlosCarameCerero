# 📋 Análisis y Requisitos del Proyecto

## TFG DAM – App Fitness Inteligente con IA (FitnessApp)

**Autor:** Carlos Carame Cerero  
**Titulación:** Desarrollo de Aplicaciones Multiplataforma (DAM)  
**Fecha:** Marzo 2026

---

## Índice

1. [Objetivos del proyecto](#1-objetivos-del-proyecto)
2. [Requisitos funcionales y no funcionales](#2-requisitos-funcionales-y-no-funcionales)
3. [Identificación de actores](#3-identificación-de-actores)
4. [Casos de uso / Historias de usuario](#4-casos-de-uso--historias-de-usuario)
5. [Alcance del proyecto](#5-alcance-del-proyecto)

---

## 1. Objetivos del proyecto

### 1.1 Objetivo general

Diseñar y desarrollar una aplicación móvil nativa para Android que permita al usuario realizar un seguimiento integral de su entrenamiento físico, nutrición y evolución corporal, integrando un sistema inteligente basado en **Google Gemini AI** que analice los datos registrados y genere recomendaciones personalizadas, proporcionando una experiencia de asistente fitness conversacional.

### 1.2 Objetivos específicos

| ID | Objetivo |
|----|----------|
| OE-01 | Implementar un módulo de **entrenamiento** que permita crear ejercicios (fuerza y cardio), diseñar rutinas personalizadas y registrar sesiones de entrenamiento con series, repeticiones, peso, duración y distancia. |
| OE-02 | Desarrollar un módulo de **seguimiento corporal** que registre el historial de peso, medidas corporales (pecho, cintura, cadera, bíceps, muslos), altura y cálculo del IMC. |
| OE-03 | Crear un módulo de **nutrición** que permita registrar la alimentación diaria del usuario organizada por días de la semana y tipos de comida (desayuno, almuerzo, cena, snack), diferenciando entre comida y bebida con registro opcional de gramos. |
| OE-04 | Integrar un **asistente de IA conversacional** basado en Google Gemini 2.5 Flash que analice todos los datos del usuario (entrenamiento, nutrición, cuerpo, condiciones de salud y analíticas) para ofrecer respuestas personalizadas en tiempo real mediante streaming. |
| OE-05 | Implementar un sistema de **generación automática de consejos personalizados** que utilice IA para analizar los datos globales del usuario y producir recomendaciones categorizadas (entrenamiento, nutrición, cuerpo). |
| OE-06 | Permitir la gestión del **perfil de salud** del usuario, incluyendo condiciones médicas, objetivo fitness y subida de documentos de salud (analíticas en PDF) cuyo contenido se extrae y se envía a la IA para análisis. |
| OE-07 | Aplicar una arquitectura **MVVM + Clean Architecture** con inyección de dependencias (Hilt), base de datos local (Room) y programación reactiva (Kotlin Coroutines + Flow). |
| OE-08 | Desarrollar una interfaz de usuario moderna y responsiva utilizando **Jetpack Compose** con Material Design 3 y navegación con barra inferior. |
| OE-09 | Implementar un sistema de **control de peticiones (Rate Limiter)** que limite las llamadas a la API de Gemini (5 por minuto, 50 por día) con persistencia en SharedPreferences. |
| OE-10 | Gestionar un **historial de conversaciones** con el asistente IA que se persiste en base de datos, permitiendo crear nuevas conversaciones, cargar anteriores y eliminar antiguas (auto-limpieza a 30 días). |

---

## 2. Requisitos funcionales y no funcionales

### 2.1 Requisitos funcionales

#### Módulo de Entrenamiento

| ID | Requisito | Descripción |
|----|-----------|-------------|
| RF-01 | Gestión de ejercicios | El sistema debe permitir crear, listar y eliminar ejercicios, especificando nombre, grupo muscular, descripción y tipo (fuerza o cardio). |
| RF-02 | Gestión de rutinas | El usuario debe poder crear y eliminar rutinas con nombre y descripción, y asociar/desasociar ejercicios a cada rutina con orden personalizable. |
| RF-03 | Inicio de sesión de entrenamiento | El usuario debe poder iniciar una sesión de entrenamiento vinculada a una rutina, configurando el tiempo de descanso entre series. |
| RF-04 | Registro de series de fuerza | Durante una sesión, el usuario debe poder registrar series indicando ejercicio, número de serie, repeticiones y peso (kg). |
| RF-05 | Registro de series de cardio | Durante una sesión, el usuario debe poder registrar series de ejercicio cardiovascular indicando duración (segundos) y distancia (km). |
| RF-06 | Eliminación de series | El usuario debe poder eliminar series individuales ya registradas dentro de una sesión. |
| RF-07 | Historial de sesiones | El sistema debe mostrar el historial de sesiones de entrenamiento realizadas por rutina, incluyendo fecha, duración y detalle de series. |
| RF-08 | Eliminación de sesiones | El usuario debe poder eliminar sesiones completas de entrenamiento de su historial. |
| RF-09 | Catálogo de ejercicios | El sistema debe mostrar un listado de ejercicios con filtro por grupo muscular. |

#### Módulo de Cuerpo / Seguimiento Corporal

| ID | Requisito | Descripción |
|----|-----------|-------------|
| RF-10 | Registro de peso | El usuario debe poder registrar su peso corporal en kg, almacenándose con la fecha actual. |
| RF-11 | Historial de peso | El sistema debe mostrar el historial completo de registros de peso ordenado cronológicamente, mostrando el peso actual más reciente. |
| RF-12 | Eliminación de registros de peso | El usuario debe poder eliminar registros de peso individuales con confirmación previa. |
| RF-13 | Registro de medidas corporales | El usuario debe poder registrar medidas corporales (pecho, cintura, cadera, bíceps, muslos) en cm, pudiendo rellenar solo las que desee. |
| RF-14 | Historial de medidas | El sistema debe mostrar el historial de medidas corporales y las últimas medidas registradas en tarjetas de resumen. |
| RF-15 | Eliminación de registros de medidas | El usuario debe poder eliminar registros de medidas corporales con confirmación previa. |
| RF-16 | Registro y edición de altura | El usuario debe poder establecer y modificar su altura en cm. |
| RF-17 | Cálculo de IMC | El sistema debe calcular automáticamente el Índice de Masa Corporal a partir de la altura y el último peso registrado, mostrándolo en la pestaña de medidas. |

#### Módulo de Salud (dentro de Cuerpo)

| ID | Requisito | Descripción |
|----|-----------|-------------|
| RF-18 | Selección de objetivo fitness | El usuario debe poder seleccionar un objetivo fitness entre opciones predefinidas (Musculación, Perder peso, Mantenimiento, Resistencia, Flexibilidad, Salud general) o introducir uno personalizado. |
| RF-19 | Registro de condiciones de salud | El usuario debe poder describir en texto libre sus condiciones de salud (enfermedades crónicas, alergias alimentarias, intolerancias, trastornos, etc.). |
| RF-20 | Guardado del perfil de salud | El sistema debe persistir el objetivo fitness y las condiciones de salud en la base de datos y utilizarlos como contexto para la IA. |
| RF-21 | Subida de documentos PDF | El usuario debe poder subir documentos de salud en formato PDF (analíticas, informes médicos) desde el almacenamiento del dispositivo. |
| RF-22 | Almacenamiento local de PDFs | El sistema debe copiar los PDFs al almacenamiento interno de la app y registrar la referencia en la base de datos. |
| RF-23 | Extracción de texto de PDFs | El sistema debe extraer el texto embebido de los PDFs subidos para proporcionarlo como contexto a la IA. |
| RF-24 | Eliminación de documentos | El usuario debe poder eliminar documentos de salud con confirmación previa, borrando tanto el registro en BD como el archivo físico. |

#### Módulo de Nutrición

| ID | Requisito | Descripción |
|----|-----------|-------------|
| RF-25 | Registro de alimentos | El usuario debe poder registrar alimentos indicando descripción, tipo de comida (desayuno, almuerzo, cena, snack), día de la semana, hora aproximada, tipo (comida o bebida) y gramos opcionales. |
| RF-26 | Registro múltiple de alimentos | El sistema debe permitir añadir varios alimentos de una misma comida en un único diálogo, cada uno con su descripción, tipo (comida/bebida) y gramos. |
| RF-27 | Vista semanal de nutrición | El sistema debe mostrar la planificación nutricional organizada por días de la semana (lunes a domingo), indicando visualmente qué días tienen registros. |
| RF-28 | Selección de día | El usuario debe poder navegar entre días de la semana para ver y gestionar los alimentos de cada día. |
| RF-29 | Edición de entradas | El usuario debe poder editar los datos de una entrada de alimentación existente. |
| RF-30 | Eliminación de entradas | El usuario debe poder eliminar entradas de alimentación con confirmación previa. |

#### Módulo de Asistente IA

| ID | Requisito | Descripción |
|----|-----------|-------------|
| RF-31 | Chat conversacional con IA | El usuario debe poder conversar con un asistente IA que responde en español, con respuestas en streaming (token a token) para una experiencia fluida. |
| RF-32 | Contexto personalizado | El asistente IA debe recibir automáticamente como contexto todos los datos del usuario: perfil, peso, medidas, rutinas, sesiones recientes, nutrición, condiciones de salud y contenido extraído de analíticas PDF. |
| RF-33 | Historial de conversaciones | El sistema debe guardar las conversaciones del asistente en la base de datos, permitiendo al usuario acceder al historial completo. |
| RF-34 | Carga de conversaciones previas | El usuario debe poder abrir una conversación anterior para continuarla, reconstruyendo el historial de Gemini con los últimos 20 mensajes. |
| RF-35 | Nuevo chat | El usuario debe poder iniciar una nueva conversación limpia en cualquier momento. |
| RF-36 | Eliminación de conversaciones | El usuario debe poder eliminar conversaciones individuales del historial. |
| RF-37 | Auto-limpieza de conversaciones | El sistema debe eliminar automáticamente las conversaciones con más de 30 días de antigüedad al iniciar la app. |
| RF-38 | Indicador de escritura | El sistema debe mostrar un indicador visual (typing) mientras el asistente está generando la respuesta, ocultándolo al recibir el primer token de streaming. |

#### Módulo de Recomendaciones / Consejos

| ID | Requisito | Descripción |
|----|-----------|-------------|
| RF-39 | Generación de consejos con IA | El sistema debe generar entre 3 y 5 consejos personalizados usando Gemini AI, analizando todos los datos del usuario (perfil, entrenamientos, nutrición, medidas). |
| RF-40 | Categorización de consejos | Cada consejo generado debe estar categorizado en uno de tres tipos: entrenamiento (training), nutrición (nutrition) o cuerpo (body). |
| RF-41 | Filtro de consejos | El usuario debe poder filtrar los consejos mostrados por categoría (todos, entrenamiento, nutrición, cuerpo). |
| RF-42 | Marcar consejo como leído | El usuario debe poder marcar consejos como leídos, con un contador de no leídos visible. |
| RF-43 | Eliminación de consejos | El usuario debe poder eliminar consejos individuales o todos a la vez. |
| RF-44 | Cooldown de generación | El sistema debe imponer un período de espera de 5 minutos entre generaciones de consejos para evitar abuso. |

---

### 2.2 Requisitos no funcionales

#### Rendimiento

| ID | Requisito | Descripción |
|----|-----------|-------------|
| RNF-01 | Respuesta fluida de la interfaz | La interfaz de usuario debe mantener una tasa de refresco de 60 fps, evitando bloqueos en el hilo principal mediante el uso de corrutinas y `StateFlow`. |
| RNF-02 | Streaming de IA | Las respuestas del asistente IA deben mostrarse de forma progresiva (token a token) mediante Server-Sent Events (SSE), evitando tiempos de espera largos sin feedback visual. |
| RNF-03 | Carga reactiva de datos | Todas las consultas a la base de datos deben ser reactivas (Kotlin Flow), actualizando la UI automáticamente cuando los datos cambian sin recargas manuales. |
| RNF-04 | Timeouts de red | Las conexiones a la API de Gemini deben configurar timeouts adecuados (conexión: 30s, lectura: 120s, escritura: 30s). |

#### Seguridad

| ID | Requisito | Descripción |
|----|-----------|-------------|
| RNF-05 | API key protegida | La clave de la API de Gemini debe almacenarse en `local.properties` (excluido del control de versiones) e inyectarse mediante `BuildConfig` en tiempo de compilación. |
| RNF-06 | Rate limiting local | El sistema debe implementar un limitador de peticiones persistente (SharedPreferences) que restrinja a 5 peticiones/minuto y 50 peticiones/día, lanzando excepciones descriptivas cuando se excedan. |
| RNF-07 | Almacenamiento local seguro | Los documentos de salud (PDFs) deben almacenarse en el directorio interno de la aplicación (`filesDir`), no accesible por otras apps. |
| RNF-08 | Validación de entradas | Todos los campos numéricos (peso, medidas, series, repeticiones, etc.) deben validarse antes de su procesamiento, con conversión segura de comas a puntos para locales europeos. |

#### Usabilidad

| ID | Requisito | Descripción |
|----|-----------|-------------|
| RNF-09 | Material Design 3 | La interfaz debe seguir las directrices de Material Design 3 con tema coherente, utilizando componentes estándar de `Material3` (tarjetas, chips, diálogos, FAB, barra de navegación inferior, etc.). |
| RNF-10 | Navegación intuitiva | La app debe contar con una barra de navegación inferior con 5 secciones principales (Entreno, Cuerpo, Asistente, Nutrición, Consejos) y navegación anidada para sub-pantallas. |
| RNF-11 | Confirmación de eliminación | Toda acción de eliminación (ejercicios, rutinas, sesiones, pesos, medidas, documentos, conversaciones, consejos) debe requerir confirmación explícita mediante diálogo. |
| RNF-12 | Mensajes de estado vacío | Cuando una sección no tiene datos, debe mostrarse un mensaje descriptivo con icono indicando cómo empezar (ej: "No hay registros de peso. Pulsa + para añadir uno."). |
| RNF-13 | Idioma español | Toda la interfaz de usuario y las respuestas del asistente IA deben estar en español. |
| RNF-14 | Feedback de errores | Los errores de la API (límite de peticiones, fallos de red, errores de autenticación) deben mostrarse al usuario con mensajes claros y descriptivos en español. |

#### Mantenibilidad y Arquitectura

| ID | Requisito | Descripción |
|----|-----------|-------------|
| RNF-15 | Arquitectura MVVM | El proyecto debe seguir el patrón MVVM separando la lógica de presentación (Compose), lógica de negocio (ViewModels) y acceso a datos (Repositorios + DAOs). |
| RNF-16 | Clean Architecture | El proyecto debe mantener capas bien definidas: presentación (`screens`, `components`), dominio (`domain/repository` interfaces), datos (`data/local`, `data/remote`, `data/repository` implementaciones). |
| RNF-17 | Inyección de dependencias | Todas las dependencias deben inyectarse mediante Hilt (`@HiltViewModel`, `@Inject`, `@Module`), evitando instanciaciones directas. |
| RNF-18 | Persistencia con Room | Todos los datos del usuario deben persistirse en una base de datos SQLite local gestionada con Room, con migración de esquema (versión 9 actual) y exportación de esquemas. |

#### Compatibilidad

| ID | Requisito | Descripción |
|----|-----------|-------------|
| RNF-19 | SDK mínimo | La aplicación debe ser compatible con dispositivos Android desde API 24 (Android 7.0 Nougat). |
| RNF-20 | SDK objetivo | La aplicación debe compilarse contra el SDK 36 (último estable). |
| RNF-21 | Conexión a Internet | La aplicación requiere conexión a Internet únicamente para las funcionalidades de IA (asistente y generación de consejos). El resto de funcionalidades deben operar offline. |

---

## 3. Identificación de actores

### 3.1 Diagrama de actores

```
┌─────────────────────────────────────────────────────┐
│                  FitnessApp                         │
│                                                     │
│  ┌──────────┐     ┌──────────────────────────────┐  │
│  │          │     │  Módulos internos             │  │
│  │ Usuario  │────▶│  · Entrenamiento             │  │
│  │          │     │  · Cuerpo / Salud             │  │
│  └──────────┘     │  · Nutrición                 │  │
│                   │  · Asistente IA              │  │
│                   │  · Consejos                  │  │
│                   └──────────────────────────────┘  │
│                              │                      │
│                              ▼                      │
│                   ┌──────────────────────────────┐  │
│                   │  API Google Gemini 2.5 Flash │  │
│                   │  (Sistema externo)           │  │
│                   └──────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

### 3.2 Descripción de actores

| Actor | Tipo | Descripción |
|-------|------|-------------|
| **Usuario** | Principal (Persona) | Persona que utiliza la aplicación para gestionar su entrenamiento, nutrición y salud. Es el único actor humano del sistema. No existe registro ni autenticación; la app es de uso personal en un único dispositivo. |
| **API Google Gemini** | Secundario (Sistema externo) | Servicio de inteligencia artificial generativa de Google al que la aplicación envía peticiones REST con el contexto del usuario para obtener respuestas conversacionales y generación de consejos. Se comunica mediante HTTP con formato JSON y soporte de Server-Sent Events (SSE) para streaming. |
| **Base de datos local (Room)** | Secundario (Sistema interno) | Sistema de persistencia SQLite local que almacena todos los datos del usuario: ejercicios, rutinas, sesiones, peso, medidas, perfil, nutrición, conversaciones, consejos y documentos de salud. Actúa como fuente de verdad para toda la aplicación. |
| **Sistema de archivos local** | Secundario (Sistema interno) | Almacenamiento interno del dispositivo donde se guardan los documentos PDF de salud subidos por el usuario. |

---

## 4. Casos de uso / Historias de usuario

### 4.1 Diagrama general de casos de uso

```
                          ┌─────────────┐
                          │   Usuario   │
                          └──────┬──────┘
                                 │
          ┌──────────────────────┼──────────────────────────┐
          │                      │                          │
    ┌─────▼──────┐       ┌──────▼───────┐         ┌───────▼────────┐
    │ Entreno    │       │   Cuerpo     │         │   Nutrición    │
    │            │       │              │         │                │
    │ CU-01..09  │       │ CU-10..24    │         │ CU-25..30      │
    └────────────┘       └──────────────┘         └────────────────┘
          │                      │                          │
    ┌─────▼──────┐       ┌──────▼───────┐                   │
    │ Asistente  │       │  Consejos    │                   │
    │ IA         │       │              │                   │
    │ CU-31..38  │       │ CU-39..44    │                   │
    └────────────┘       └──────────────┘                   │
          │                      │                          │
          └──────────────────────┼──────────────────────────┘
                                 │
                          ┌──────▼──────┐
                          │ API Gemini  │
                          └─────────────┘
```

### 4.2 Historias de usuario detalladas

---

#### 🏋️ Módulo de Entrenamiento

**HU-01: Crear un ejercicio**
- **Como** usuario, **quiero** crear un ejercicio indicando nombre, grupo muscular, descripción y tipo (fuerza/cardio), **para** poder usarlo en mis rutinas.
- **Criterios de aceptación:**
  - Se muestra un diálogo con campos para nombre, grupo muscular, descripción y tipo.
  - El ejercicio se persiste en la base de datos.
  - El ejercicio aparece inmediatamente en el listado de ejercicios.

**HU-02: Ver catálogo de ejercicios**
- **Como** usuario, **quiero** ver un listado de todos mis ejercicios con filtro por grupo muscular, **para** encontrar rápidamente los ejercicios que necesito.
- **Criterios de aceptación:**
  - Se muestra la lista completa de ejercicios.
  - Se puede filtrar por grupo muscular.
  - Se muestra tipo (fuerza/cardio) y grupo muscular de cada ejercicio.

**HU-03: Eliminar un ejercicio**
- **Como** usuario, **quiero** eliminar un ejercicio que ya no necesito, **para** mantener organizado mi catálogo.
- **Criterios de aceptación:**
  - Se muestra diálogo de confirmación antes de eliminar.
  - Al eliminar, se eliminan en cascada las series asociadas a ese ejercicio.

**HU-04: Crear una rutina**
- **Como** usuario, **quiero** crear una rutina con nombre y descripción, **para** organizar mis entrenamientos.
- **Criterios de aceptación:**
  - La rutina se crea y aparece en el listado principal.
  - Se puede acceder al detalle para añadir ejercicios.

**HU-05: Gestionar ejercicios de una rutina**
- **Como** usuario, **quiero** añadir y quitar ejercicios a una rutina, **para** personalizar mi plan de entrenamiento.
- **Criterios de aceptación:**
  - Se pueden añadir ejercicios del catálogo a la rutina.
  - Se pueden eliminar ejercicios de la rutina.
  - Se mantiene un orden personalizable.

**HU-06: Iniciar sesión de entrenamiento**
- **Como** usuario, **quiero** iniciar una sesión de entrenamiento basada en una rutina, **para** registrar mi progreso.
- **Criterios de aceptación:**
  - Se crea una sesión vinculada a la rutina con fecha actual.
  - Se puede configurar el tiempo de descanso entre series.
  - Se navega automáticamente al detalle de la sesión.

**HU-07: Registrar series de fuerza**
- **Como** usuario, **quiero** registrar series indicando repeticiones y peso, **para** llevar un control detallado de mi entrenamiento de fuerza.
- **Criterios de aceptación:**
  - Se selecciona el ejercicio, se indica nº de serie, repeticiones y peso en kg.
  - La serie se guarda y se muestra inmediatamente.

**HU-08: Registrar series de cardio**
- **Como** usuario, **quiero** registrar series de cardio indicando duración y distancia, **para** llevar un control de mi entrenamiento cardiovascular.
- **Criterios de aceptación:**
  - Se indica duración en segundos y distancia en km.
  - La serie se marca como cardio automáticamente.

**HU-09: Consultar historial de sesiones**
- **Como** usuario, **quiero** ver el historial de sesiones realizadas para una rutina, **para** analizar mi progreso a lo largo del tiempo.
- **Criterios de aceptación:**
  - Se muestran las sesiones ordenadas por fecha.
  - Se puede acceder al detalle de cada sesión con sus series.
  - Se puede eliminar sesiones con confirmación.

---

#### ❤️ Módulo de Cuerpo

**HU-10: Registrar peso corporal**
- **Como** usuario, **quiero** registrar mi peso actual, **para** hacer seguimiento de mi evolución ponderal.
- **Criterios de aceptación:**
  - Se muestra un diálogo para introducir peso en kg (soporta decimales con coma y punto).
  - El registro se guarda con la fecha actual.
  - Se actualiza el "peso actual" mostrado en pantalla.

**HU-11: Consultar historial de peso**
- **Como** usuario, **quiero** ver mi historial completo de pesos, **para** analizar mi tendencia.
- **Criterios de aceptación:**
  - Se muestra el peso actual destacado en una tarjeta de resumen.
  - Se muestra el historial completo con fecha y peso de cada registro.

**HU-12: Eliminar registro de peso**
- **Como** usuario, **quiero** eliminar un registro de peso erróneo, **para** mantener datos precisos.
- **Criterios de aceptación:**
  - Se muestra diálogo de confirmación indicando el peso a eliminar.
  - Al confirmar, el registro desaparece del historial.

**HU-13: Registrar medidas corporales**
- **Como** usuario, **quiero** registrar mis medidas corporales (pecho, cintura, cadera, bíceps, muslos), **para** hacer seguimiento de mi composición corporal.
- **Criterios de aceptación:**
  - Se puede rellenar cualquier combinación de medidas (no todas son obligatorias).
  - Las medidas se registran en cm.

**HU-14: Ver resumen de medidas y IMC**
- **Como** usuario, **quiero** ver un resumen con mis últimas medidas, altura e IMC, **para** tener una visión rápida de mi estado corporal.
- **Criterios de aceptación:**
  - Se muestra la altura actual y el IMC calculado (si hay altura y peso).
  - Se muestran las últimas medidas en tarjetas de resumen.

**HU-15: Establecer/modificar altura**
- **Como** usuario, **quiero** registrar mi altura, **para** que el sistema calcule correctamente el IMC.
- **Criterios de aceptación:**
  - Se puede establecer y cambiar la altura desde un botón en la pestaña de medidas.
  - Al cambiar la altura, el IMC se recalcula automáticamente.

**HU-16: Seleccionar objetivo fitness**
- **Como** usuario, **quiero** definir mi objetivo fitness, **para** que la IA adapte sus recomendaciones a mi meta.
- **Criterios de aceptación:**
  - Se muestran chips predefinidos (Musculación, Perder peso, Mantenimiento, Resistencia, Flexibilidad, Salud general).
  - Se puede crear un objetivo personalizado con texto libre.
  - Se puede eliminar un objetivo personalizado previamente creado.
  - Solo un objetivo puede estar seleccionado a la vez.

**HU-17: Registrar condiciones de salud**
- **Como** usuario, **quiero** registrar mis condiciones de salud y alergias, **para** que la IA las tenga en cuenta en sus recomendaciones.
- **Criterios de aceptación:**
  - Campo de texto libre multilínea.
  - Se guarda con botón explícito "Guardar".
  - La información se envía al asistente IA como parte del contexto.

**HU-18: Subir analítica en PDF**
- **Como** usuario, **quiero** subir mis analíticas médicas en PDF, **para** que la IA pueda analizarlas y darme recomendaciones basadas en mis valores.
- **Criterios de aceptación:**
  - Se abre el selector de archivos del sistema filtrando por PDF.
  - El PDF se copia al almacenamiento interno de la app.
  - Se muestra en el listado de documentos con nombre y fecha de subida.
  - El texto del PDF se extrae y se incluye en el contexto de la IA.

**HU-19: Eliminar documento de salud**
- **Como** usuario, **quiero** eliminar un documento de salud, **para** gestionar mis archivos.
- **Criterios de aceptación:**
  - Confirmación antes de eliminar.
  - Se elimina tanto el registro en BD como el archivo físico.

---

#### 🥗 Módulo de Nutrición

**HU-20: Registrar alimentos de una comida**
- **Como** usuario, **quiero** registrar lo que como en cada comida del día, **para** llevar un seguimiento de mi alimentación.
- **Criterios de aceptación:**
  - Se puede añadir uno o varios alimentos en un mismo diálogo.
  - Para cada alimento se indica: descripción, tipo (comida/bebida) y gramos (opcional).
  - Se selecciona el tipo de comida (desayuno, almuerzo, cena, snack) y la hora.

**HU-21: Vista semanal de nutrición**
- **Como** usuario, **quiero** ver mi planificación nutricional organizada por días de la semana, **para** tener una visión global.
- **Criterios de aceptación:**
  - Se muestran los 7 días de la semana con indicador visual de cuáles tienen registros.
  - El día actual se selecciona por defecto al abrir la pantalla.
  - Se muestran las comidas del día seleccionado agrupadas por tipo (desayuno, almuerzo, cena, snack).

**HU-22: Editar entrada de alimentación**
- **Como** usuario, **quiero** editar una entrada de alimentación existente, **para** corregir errores.
- **Criterios de aceptación:**
  - Se puede modificar la descripción, tipo de comida, tipo de alimento, gramos y hora.
  - Los cambios se reflejan inmediatamente en la vista.

**HU-23: Eliminar entrada de alimentación**
- **Como** usuario, **quiero** eliminar una entrada de alimentación, **para** mantener registros precisos.
- **Criterios de aceptación:**
  - Confirmación antes de eliminar.
  - La entrada desaparece de la vista del día.

---

#### 🤖 Módulo de Asistente IA

**HU-24: Conversar con el asistente IA**
- **Como** usuario, **quiero** chatear con un asistente fitness inteligente, **para** recibir asesoramiento personalizado basado en todos mis datos.
- **Criterios de aceptación:**
  - Se escribe un mensaje y se envía.
  - La respuesta aparece progresivamente (streaming token a token).
  - Se muestra indicador de "escribiendo" mientras se espera la respuesta.
  - El asistente tiene acceso al perfil, peso, medidas, rutinas, sesiones, nutrición, condiciones de salud y analíticas del usuario.

**HU-25: Gestionar historial de conversaciones**
- **Como** usuario, **quiero** acceder a mis conversaciones anteriores con el asistente, **para** consultarlas o continuarlas.
- **Criterios de aceptación:**
  - Se muestra un listado de conversaciones pasadas con título y fecha.
  - Se puede abrir una conversación para continuarla.
  - Se reconstruye el historial de la conversación con Gemini al abrirla.
  - Se pueden eliminar conversaciones individuales.

**HU-26: Iniciar nuevo chat**
- **Como** usuario, **quiero** empezar una conversación nueva, **para** consultar un tema diferente.
- **Criterios de aceptación:**
  - Se limpia el chat actual.
  - Se muestra el mensaje de bienvenida.
  - Se crea una nueva conversación en BD al enviar el primer mensaje.

**HU-27: Gestión de errores de la IA**
- **Como** usuario, **quiero** recibir mensajes de error claros cuando la IA falle, **para** entender qué ocurre.
- **Criterios de aceptación:**
  - Si se excede el rate limit: mensaje con tiempo de espera.
  - Si hay error de red: mensaje indicando comprobar conexión.
  - Si la API key es inválida: mensaje descriptivo.
  - Los errores de la API (429, 500, etc.) se mapean a mensajes en español.

---

#### 💡 Módulo de Consejos / Recomendaciones

**HU-28: Generar consejos personalizados**
- **Como** usuario, **quiero** generar consejos personalizados basados en mis datos, **para** mejorar mi entrenamiento y nutrición.
- **Criterios de aceptación:**
  - Se analizan todos los datos del usuario (perfil, entrenos, nutrición, medidas).
  - Se generan entre 3 y 5 consejos categorizados.
  - Se muestra indicador de carga durante la generación.
  - No se puede regenerar hasta pasados 5 minutos (cooldown).

**HU-29: Filtrar consejos por categoría**
- **Como** usuario, **quiero** filtrar los consejos por categoría (entrenamiento, nutrición, cuerpo), **para** centrarme en el área que me interesa.
- **Criterios de aceptación:**
  - Filtros: Todos, Entrenamiento, Nutrición, Cuerpo.
  - Se actualiza la lista mostrada según el filtro seleccionado.

**HU-30: Gestionar consejos**
- **Como** usuario, **quiero** marcar consejos como leídos y eliminarlos, **para** gestionar las recomendaciones recibidas.
- **Criterios de aceptación:**
  - Se puede marcar como leído.
  - Se puede eliminar individualmente.
  - Se puede eliminar todos a la vez.
  - Se muestra contador de consejos no leídos.

---

### 4.3 Tabla resumen de casos de uso

| ID | Nombre | Actor principal | Actor secundario | Módulo |
|----|--------|----------------|-----------------|--------|
| CU-01 | Crear ejercicio | Usuario | BD local | Entrenamiento |
| CU-02 | Ver catálogo de ejercicios | Usuario | BD local | Entrenamiento |
| CU-03 | Eliminar ejercicio | Usuario | BD local | Entrenamiento |
| CU-04 | Crear rutina | Usuario | BD local | Entrenamiento |
| CU-05 | Gestionar ejercicios de rutina | Usuario | BD local | Entrenamiento |
| CU-06 | Iniciar sesión de entrenamiento | Usuario | BD local | Entrenamiento |
| CU-07 | Registrar series de fuerza | Usuario | BD local | Entrenamiento |
| CU-08 | Registrar series de cardio | Usuario | BD local | Entrenamiento |
| CU-09 | Consultar historial de sesiones | Usuario | BD local | Entrenamiento |
| CU-10 | Registrar peso | Usuario | BD local | Cuerpo |
| CU-11 | Consultar historial de peso | Usuario | BD local | Cuerpo |
| CU-12 | Eliminar registro de peso | Usuario | BD local | Cuerpo |
| CU-13 | Registrar medidas corporales | Usuario | BD local | Cuerpo |
| CU-14 | Ver resumen corporal e IMC | Usuario | BD local | Cuerpo |
| CU-15 | Establecer/modificar altura | Usuario | BD local | Cuerpo |
| CU-16 | Seleccionar objetivo fitness | Usuario | BD local | Salud |
| CU-17 | Registrar condiciones de salud | Usuario | BD local | Salud |
| CU-18 | Subir analítica PDF | Usuario | BD local, Sistema archivos | Salud |
| CU-19 | Eliminar documento de salud | Usuario | BD local, Sistema archivos | Salud |
| CU-20 | Registrar alimentos | Usuario | BD local | Nutrición |
| CU-21 | Vista semanal de nutrición | Usuario | BD local | Nutrición |
| CU-22 | Editar entrada de alimentación | Usuario | BD local | Nutrición |
| CU-23 | Eliminar entrada de alimentación | Usuario | BD local | Nutrición |
| CU-24 | Conversar con asistente IA | Usuario | API Gemini, BD local | Asistente |
| CU-25 | Gestionar historial de chats | Usuario | BD local | Asistente |
| CU-26 | Iniciar nuevo chat | Usuario | — | Asistente |
| CU-27 | Gestión de errores IA | Usuario | API Gemini | Asistente |
| CU-28 | Generar consejos personalizados | Usuario | API Gemini, BD local | Consejos |
| CU-29 | Filtrar consejos | Usuario | BD local | Consejos |
| CU-30 | Gestionar consejos | Usuario | BD local | Consejos |

---

## 5. Alcance del proyecto

### 5.1 Incluido en el desarrollo ✅

| Área | Funcionalidad |
|------|---------------|
| **Entrenamiento** | Gestión completa de ejercicios (fuerza y cardio), rutinas personalizadas, sesiones de entrenamiento con series detalladas e historial. |
| **Cuerpo** | Seguimiento de peso corporal, medidas corporales (5 medidas), altura, cálculo de IMC y visualización de historial. |
| **Salud** | Perfil de salud con condiciones médicas, objetivo fitness (predefinido o personalizado), subida de analíticas PDF con extracción de texto. |
| **Nutrición** | Registro de alimentación semanal con múltiples comidas por día, diferenciando comida/bebida, con gramos opcionales. Edición y eliminación de entradas. |
| **Asistente IA** | Chat conversacional con Google Gemini 2.5 Flash con streaming, contexto completo del usuario, historial de conversaciones persistente y auto-limpieza a 30 días. |
| **Consejos** | Generación de consejos personalizados por IA, categorizados y filtrables, con sistema de leídos/no leídos y cooldown de regeneración. |
| **Arquitectura** | MVVM + Clean Architecture, Hilt, Room (14 entidades, 12 DAOs), Kotlin Coroutines + Flow, Jetpack Compose con Material Design 3. |
| **Control de API** | Rate limiter local persistente (5 req/min, 50 req/día) con manejo de errores exhaustivo y mensajes en español. |
| **Navegación** | 5 tabs principales + 4 sub-pantallas (detalle rutina, detalle sesión, catálogo ejercicios, historial chats). |
| **Persistencia** | Base de datos SQLite con Room (versión 9), migraciones, exportación de esquemas, claves foráneas con cascada. |
| **Extracción de PDFs** | Extracción de texto embebido en PDFs mediante lectura raw (bloques BT/ET y streams), con límite de 4.000 caracteres. |

### 5.2 Excluido del desarrollo ❌

| Área | Justificación |
|------|---------------|
| **Autenticación de usuario** | La app es de uso personal en un único dispositivo, sin sistema de login/registro. Queda fuera del alcance temporal del TFG. |
| **Sincronización en la nube** | Todos los datos se almacenan localmente. No se implementa backup ni sincronización entre dispositivos por complejidad adicional. |
| **Cálculo automático de macronutrientes** | Los campos de calorías, proteínas, carbohidratos y grasas existen en la entidad `FoodEntryEntity` pero no se calculan automáticamente en esta versión. Queda preparado para futuras iteraciones con IA. |
| **Gráficos y visualizaciones avanzadas** | No se implementan gráficos de evolución (líneas de tendencia, barras comparativas). Se usan tarjetas de resumen y listados históricos. |
| **Notificaciones push** | No se implementan recordatorios de entrenamiento, comidas o hidratación. |
| **Integración con wearables** | No se implementa conexión con dispositivos como Google Fit, smartwatches o pulseras de actividad. |
| **Sistema de logros/gamificación** | No se implementa sistema de medallas, rachas o desafíos. |
| **Soporte multi-idioma** | La app solo está disponible en español. No se implementa i18n. |
| **Backend propio** | La carpeta `backend/` existe en el proyecto pero no se utiliza. Toda la lógica de IA se ejecuta directamente contra la API de Google Gemini desde el cliente. |
| **Tests automatizados extensivos** | Se incluye la estructura de tests (unitarios y de instrumentación) pero no se desarrolla una suite completa de tests como parte del alcance del TFG. |
| **Modo offline para IA** | Las funcionalidades de IA requieren conexión a Internet. El resto de la app funciona completamente offline. |
| **Exportación de datos** | No se implementa exportación de datos del usuario a CSV, PDF u otros formatos. |

### 5.3 Limitaciones conocidas

| Limitación | Descripción |
|------------|-------------|
| Extracción de PDF | El extractor de texto solo funciona con PDFs que contengan texto embebido. Los PDFs escaneados (imágenes) no pueden ser procesados sin OCR. |
| Cuota de Gemini | La API de Gemini tiene límites en el plan gratuito. Si se excede la cuota de Google, la IA dejará de funcionar hasta que se renueve. El rate limiter local mitiga esto parcialmente. |
| Almacenamiento local | Al no haber sincronización en la nube, los datos se pierden si se desinstala la app o se cambia de dispositivo. |
| Precisión de la IA | Las recomendaciones y análisis de la IA son orientativos y no sustituyen asesoramiento médico o de un profesional del fitness. |

### 5.4 Tecnologías utilizadas

| Tecnología | Versión / Detalle | Propósito |
|------------|-------------------|-----------|
| Kotlin | — | Lenguaje de programación principal |
| Jetpack Compose | BOM (último estable) | Framework de UI declarativa |
| Material Design 3 | Material3 + Icons Extended | Sistema de diseño |
| Room | — | Base de datos local (SQLite) |
| Hilt | — | Inyección de dependencias |
| Kotlin Coroutines + Flow | — | Programación asíncrona y reactiva |
| Navigation Compose | — | Navegación entre pantallas |
| OkHttp | — | Cliente HTTP para API REST |
| Gson | — | Serialización/deserialización JSON |
| Google Gemini 2.5 Flash | API REST directa | Inteligencia artificial generativa |
| KSP | — | Procesador de anotaciones (Room, Hilt) |
| Android SDK | minSdk 24, targetSdk 36 | Plataforma objetivo |

---

*Documento generado para el TFG de Desarrollo de Aplicaciones Multiplataforma — Curso 2025/2026*

