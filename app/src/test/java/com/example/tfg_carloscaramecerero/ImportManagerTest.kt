package com.example.tfg_carloscaramecerero

import com.example.tfg_carloscaramecerero.data.util.ImportManager
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios de ImportManager.
 * Verifica detección de tipo CSV y parseo de cada formato sin dependencias de Android.
 */
class ImportManagerTest {

    // ─── detectCsvType ────────────────────────────────────────────────────────

    @Test
    fun `detectCsvType identifica CSV de pesos por columna peso`() {
        val csv = "Fecha;Peso (kg)\n01/05/2025;80.0"
        assertEquals(ImportManager.CsvType.WEIGHTS, ImportManager.detectCsvType(csv))
    }

    @Test
    fun `detectCsvType identifica CSV de nutricion por tipo comida`() {
        val csv = "Día;Tipo comida;Descripción;Gramos;Calorías;Proteínas;Hidratos;Grasas\n" +
                  "Lunes;desayuno;Leche;200;90;3.5;12.0;3.0"
        assertEquals(ImportManager.CsvType.NUTRITION, ImportManager.detectCsvType(csv))
    }

    @Test
    fun `detectCsvType identifica CSV de ejercicios por grupo muscular`() {
        val csv = "Nombre;Descripción;Grupo muscular;Tipo\nPress banca;Ejercicio pecho;Pecho;STRENGTH"
        assertEquals(ImportManager.CsvType.EXERCISES, ImportManager.detectCsvType(csv))
    }

    @Test
    fun `detectCsvType identifica CSV de rutinas por fecha creacion`() {
        val csv = "Nombre;Descripción;Fecha creación\nFuerza A;Rutina de torso;01/01/2025"
        assertEquals(ImportManager.CsvType.ROUTINES, ImportManager.detectCsvType(csv))
    }

    @Test
    fun `detectCsvType devuelve UNKNOWN para cabecera desconocida`() {
        val csv = "Campo1;Campo2\nValor1;Valor2"
        assertEquals(ImportManager.CsvType.UNKNOWN, ImportManager.detectCsvType(csv))
    }

    @Test
    fun `detectCsvType devuelve UNKNOWN para cadena vacia`() {
        assertEquals(ImportManager.CsvType.UNKNOWN, ImportManager.detectCsvType(""))
    }

    // ─── parseWeightsCsv ──────────────────────────────────────────────────────

    @Test
    fun `parseWeightsCsv parsea dos lineas correctamente`() {
        val csv = "Fecha;Peso (kg)\n01/05/2025;78.5\n15/05/2025;77.0"
        val weights = ImportManager.parseWeightsCsv(csv)
        assertEquals(2, weights.size)
        assertEquals(78.5, weights[0].weight, 0.001)
        assertEquals(77.0, weights[1].weight, 0.001)
    }

    @Test
    fun `parseWeightsCsv omite la cabecera y solo cuenta filas de datos`() {
        val csv = "Fecha;Peso (kg)\n01/01/2025;70.0"
        val weights = ImportManager.parseWeightsCsv(csv)
        assertEquals(1, weights.size)
    }

    @Test
    fun `parseWeightsCsv ignora lineas con fecha invalida`() {
        val csv = "Fecha;Peso (kg)\nno-es-fecha;80.0\n01/06/2025;75.0"
        val weights = ImportManager.parseWeightsCsv(csv)
        assertEquals(1, weights.size)
        assertEquals(75.0, weights[0].weight, 0.001)
    }

    @Test
    fun `parseWeightsCsv ignora lineas con peso no numerico`() {
        val csv = "Fecha;Peso (kg)\n01/01/2025;no-es-numero\n01/06/2025;70.0"
        val weights = ImportManager.parseWeightsCsv(csv)
        assertEquals(1, weights.size)
    }

    @Test
    fun `parseWeightsCsv devuelve lista vacia si solo hay cabecera`() {
        val csv = "Fecha;Peso (kg)"
        val weights = ImportManager.parseWeightsCsv(csv)
        assertTrue(weights.isEmpty())
    }

    @Test
    fun `parseWeightsCsv funciona correctamente tras eliminar BOM`() {
        val csvConBom = "\uFEFFFecha;Peso (kg)\n01/01/2025;80.0"
        val csvSinBom = csvConBom.removePrefix("\uFEFF")
        val weights = ImportManager.parseWeightsCsv(csvSinBom)
        assertEquals(1, weights.size)
        assertEquals(80.0, weights[0].weight, 0.001)
    }

    // ─── parseNutritionCsv ────────────────────────────────────────────────────

    @Test
    fun `parseNutritionCsv parsea entrada nutricional completa`() {
        val csv = "Día;Tipo comida;Descripción;Gramos;Calorías;Proteínas;Hidratos;Grasas\n" +
                  "Lunes;almuerzo;Pollo con arroz;300;400.0;35.0;45.0;8.0"
        val entries = ImportManager.parseNutritionCsv(csv)
        assertEquals(1, entries.size)
        with(entries[0]) {
            assertEquals("Pollo con arroz", description)
            assertEquals("almuerzo", mealType)
            assertEquals(1, dayOfWeek)
            assertEquals(300, grams)
            assertEquals(400.0, calories!!, 0.001)
            assertEquals(35.0, protein!!, 0.001)
            assertTrue("aiAnalyzed debería ser true cuando hay calorías", aiAnalyzed)
        }
    }

    @Test
    fun `parseNutritionCsv ignora dia desconocido`() {
        val csv = "Día;Tipo comida;Descripción\nMañana;desayuno;Leche"
        val entries = ImportManager.parseNutritionCsv(csv)
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `parseNutritionCsv mapea los siete dias de la semana correctamente`() {
        val csv = "Día;Tipo comida;Descripción\n" +
                  "Lunes;desayuno;A\nMartes;desayuno;B\nMiércoles;desayuno;C\n" +
                  "Jueves;desayuno;D\nViernes;desayuno;E\nSábado;desayuno;F\nDomingo;desayuno;G"
        val entries = ImportManager.parseNutritionCsv(csv)
        assertEquals(7, entries.size)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), entries.map { it.dayOfWeek })
    }

    @Test
    fun `parseNutritionCsv aiAnalyzed es false cuando no hay calorias`() {
        val csv = "Día;Tipo comida;Descripción\nLunes;cena;Ensalada"
        val entries = ImportManager.parseNutritionCsv(csv)
        assertEquals(1, entries.size)
        assertFalse(entries[0].aiAnalyzed)
        assertNull(entries[0].calories)
    }

    // ─── parseRoutinesCsv ─────────────────────────────────────────────────────

    @Test
    fun `parseRoutinesCsv parsea nombre y descripcion correctamente`() {
        val csv = "Nombre;Descripción;Fecha creación\nFuerza upper;Rutina de torso;01/01/2025"
        val routines = ImportManager.parseRoutinesCsv(csv)
        assertEquals(1, routines.size)
        assertEquals("Fuerza upper", routines[0].name)
        assertEquals("Rutina de torso", routines[0].description)
    }

    @Test
    fun `parseRoutinesCsv ignora filas con nombre en blanco`() {
        val csv = "Nombre;Descripción;Fecha creación\n;Sin nombre;01/01/2025\nPierna;Piernas;01/01/2025"
        val routines = ImportManager.parseRoutinesCsv(csv)
        assertEquals(1, routines.size)
        assertEquals("Pierna", routines[0].name)
    }

    @Test
    fun `parseRoutinesCsv admite descripcion vacia`() {
        val csv = "Nombre;Descripción;Fecha creación\nFullbody;;01/01/2025"
        val routines = ImportManager.parseRoutinesCsv(csv)
        assertEquals(1, routines.size)
        assertEquals("", routines[0].description)
    }

    @Test
    fun `parseRoutinesCsv devuelve vacio si solo hay cabecera`() {
        val csv = "Nombre;Descripción;Fecha creación"
        val routines = ImportManager.parseRoutinesCsv(csv)
        assertTrue(routines.isEmpty())
    }

    // ─── parseExercisesCsv ────────────────────────────────────────────────────

    @Test
    fun `parseExercisesCsv parsea ejercicio con tipo CARDIO`() {
        val csv = "Nombre;Descripción;Grupo muscular;Tipo\nCarrera;Correr en cinta;Cardiovascular;CARDIO"
        val exercises = ImportManager.parseExercisesCsv(csv)
        assertEquals(1, exercises.size)
        assertEquals("Carrera", exercises[0].name)
        assertEquals("Cardiovascular", exercises[0].muscleGroup)
        assertEquals("CARDIO", exercises[0].exerciseType)
    }

    @Test
    fun `parseExercisesCsv asigna STRENGTH por defecto si el tipo no es CARDIO`() {
        val csv = "Nombre;Descripción;Grupo muscular;Tipo\nPress banca;Ejercicio de pecho;Pecho;FUERZA"
        val exercises = ImportManager.parseExercisesCsv(csv)
        assertEquals(1, exercises.size)
        assertEquals("STRENGTH", exercises[0].exerciseType)
    }

    @Test
    fun `parseExercisesCsv asigna STRENGTH cuando falta columna de tipo`() {
        val csv = "Nombre;Descripción;Grupo muscular\nSentadilla;Ejercicio pierna;Piernas"
        val exercises = ImportManager.parseExercisesCsv(csv)
        assertEquals(1, exercises.size)
        assertEquals("STRENGTH", exercises[0].exerciseType)
    }

    @Test
    fun `parseExercisesCsv ignora filas con nombre en blanco`() {
        val csv = "Nombre;Descripción;Grupo muscular;Tipo\n;Desc;Pierna;STRENGTH"
        val exercises = ImportManager.parseExercisesCsv(csv)
        assertTrue(exercises.isEmpty())
    }

    @Test
    fun `parseExercisesCsv ignora filas con grupo muscular en blanco`() {
        val csv = "Nombre;Descripción;Grupo muscular;Tipo\nSentadilla;Desc;;STRENGTH"
        val exercises = ImportManager.parseExercisesCsv(csv)
        assertTrue(exercises.isEmpty())
    }

    @Test
    fun `parseExercisesCsv parsea multiples ejercicios`() {
        val csv = "Nombre;Descripción;Grupo muscular;Tipo\n" +
                  "Press banca;...;Pecho;STRENGTH\n" +
                  "Dominadas;...;Espalda;STRENGTH\n" +
                  "Bici estática;...;Cardiovascular;CARDIO"
        val exercises = ImportManager.parseExercisesCsv(csv)
        assertEquals(3, exercises.size)
        assertEquals(2, exercises.count { it.exerciseType == "STRENGTH" })
        assertEquals(1, exercises.count { it.exerciseType == "CARDIO" })
    }
}

