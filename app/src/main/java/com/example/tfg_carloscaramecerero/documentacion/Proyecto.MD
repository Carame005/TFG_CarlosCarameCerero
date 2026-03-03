# 📱 TFG DAM – App Fitness Inteligente con IA

## 🧩 Descripción general

Este proyecto consiste en el desarrollo de una aplicación móvil Android utilizando Jetpack Compose orientada al seguimiento de entrenamiento físico, nutrición y evolución corporal del usuario.

El objetivo principal es proporcionar una herramienta integral que no solo permita registrar datos, sino también analizarlos y generar recomendaciones personalizadas mediante un sistema inteligente.

---

## 🎯 Objetivos del proyecto

* Desarrollar una app moderna con arquitectura MVVM
* Permitir el registro de rutinas de entrenamiento
* Gestionar información fisiológica del usuario
* Realizar seguimiento de la dieta y macros
* Analizar datos semanales de rendimiento y nutrición
* Generar recomendaciones inteligentes personalizadas

---

## 🏋️ Módulo 1 – Entrenamiento

Funcionalidades:

* Creación y gestión de ejercicios
* Creación de rutinas personalizadas
* Registro de sesiones de entrenamiento
* Control de series, repeticiones, peso y tiempo
* Histórico de entrenamientos
* Visualización del progreso

---

## ❤️ Módulo 2 – Datos fisiológicos

Funcionalidades:

* Registro de peso corporal
* Índice de masa corporal (IMC)
* Medidas corporales
* Evolución temporal mediante gráficos

---

## 🥗 Módulo 3 – Nutrición

Funcionalidades:

* Registro diario de alimentos
* Cálculo de calorías y macronutrientes
* Seguimiento de objetivos nutricionales
* Historial de ingesta

---

## 🤖 Módulo 4 – Sistema inteligente de recomendaciones

Este módulo constituye la parte diferencial del proyecto.

### 🔍 Análisis de datos

El sistema analizará datos semanales del usuario:

* Consumo calórico medio
* Distribución de macronutrientes
* Evolución del peso
* Rendimiento en entrenamientos
* Progreso en ejercicios clave

### 🧠 Motor de recomendaciones

Se implementará un sistema basado en reglas que permita detectar patrones como:

* Déficit o superávit calórico
* Estancamiento en rendimiento
* Falta de progreso
* Desbalance nutricional

Ejemplo de reglas:

* Si el objetivo es ganar masa muscular y el peso baja → recomendar aumento calórico
* Si el rendimiento disminuye → sugerir descanso o ajuste nutricional
* Si la proteína es insuficiente → recomendar incremento

### 💬 Generación de recomendaciones

El sistema generará mensajes claros para el usuario, como:

* Ajustes en la dieta
* Cambios en la rutina
* Consejos de recuperación

Opcionalmente, se podrá integrar una API externa de IA para generar explicaciones en lenguaje natural.

---

## 🏗️ Arquitectura técnica

* Lenguaje: Kotlin
* UI: Jetpack Compose
* Arquitectura: MVVM + Clean Architecture
* Base de datos: Room
* Inyección de dependencias: Hilt
* Concurrencia: Coroutines y Flow

Capas:

* UI (Compose)
* ViewModel
* Casos de uso (UseCases)
* Repositorios
* Base de datos

---

## 📊 Funcionalidades adicionales (opcionales)

* Autenticación de usuario
* Sincronización en la nube
* Integración con APIs externas
* Sistema de logros o estadísticas

---

## 🚀 Valor añadido del proyecto

Este proyecto destaca por:

* Integración de múltiples áreas (fitness + nutrición + análisis)
* Uso de tecnologías modernas Android
* Implementación de lógica inteligente
* Aplicación práctica en un contexto real

---

## 📌 Conclusión

La aplicación propuesta no solo permite registrar información, sino que aporta valor mediante el análisis y la generación de recomendaciones personalizadas, convirtiéndose en una herramienta útil para la mejora del rendimiento y la salud del usuario.
