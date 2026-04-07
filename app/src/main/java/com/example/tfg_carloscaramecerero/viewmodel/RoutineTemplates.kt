package com.example.tfg_carloscaramecerero.viewmodel

import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseType

data class TemplateExercise(
    val name: String,
    val muscleGroup: String,
    val description: String = "",
    val type: ExerciseType = ExerciseType.STRENGTH
)

data class RoutineTemplate(
    val name: String,
    val description: String,
    val exercises: List<TemplateExercise>
)

object RoutineTemplates {
    val all: List<RoutineTemplate> = listOf(
        RoutineTemplate(
            name = "Full Body",
            description = "Rutina de cuerpo completo para 3 días a la semana",
            exercises = listOf(
                TemplateExercise("Sentadilla", "Piernas", "Ejercicio compuesto para tren inferior"),
                TemplateExercise("Press de banca", "Pecho", "Empuje horizontal con barra"),
                TemplateExercise("Peso muerto", "Espalda", "Ejercicio compuesto de cadena posterior"),
                TemplateExercise("Press militar", "Hombros", "Empuje vertical con barra"),
                TemplateExercise("Dominadas", "Espalda", "Tirón vertical con peso corporal"),
                TemplateExercise("Curl de bíceps", "Bíceps", "Flexión de codo con mancuernas"),
                TemplateExercise("Triceps en polea", "Tríceps", "Extensión de codo en polea alta")
            )
        ),
        RoutineTemplate(
            name = "Push (Empuje)",
            description = "Día de empuje del programa PPL: Pecho, Hombros y Tríceps",
            exercises = listOf(
                TemplateExercise("Press de banca plano", "Pecho", "Empuje horizontal con barra"),
                TemplateExercise("Press de banca inclinado", "Pecho", "Enfoca la parte superior del pecho"),
                TemplateExercise("Aperturas con mancuerna", "Pecho", "Ejercicio de aislamiento"),
                TemplateExercise("Press militar", "Hombros", "Empuje vertical con barra"),
                TemplateExercise("Elevaciones laterales", "Hombros", "Deltoides medial"),
                TemplateExercise("Press francés", "Tríceps", "Extensión de codo con barra EZ"),
                TemplateExercise("Triceps en polea", "Tríceps", "Extensión con cuerda en polea")
            )
        ),
        RoutineTemplate(
            name = "Pull (Tirón)",
            description = "Día de tirón del programa PPL: Espalda y Bíceps",
            exercises = listOf(
                TemplateExercise("Peso muerto", "Espalda", "Movimiento compuesto de cadena posterior"),
                TemplateExercise("Remo con barra", "Espalda", "Tirón horizontal con barra"),
                TemplateExercise("Dominadas", "Espalda", "Tirón vertical con peso corporal"),
                TemplateExercise("Pulldown en polea", "Espalda", "Tirón vertical en máquina"),
                TemplateExercise("Remo en polea baja", "Espalda", "Tirón horizontal en cable"),
                TemplateExercise("Curl de bíceps con barra", "Bíceps", "Flexión de codo con barra"),
                TemplateExercise("Curl martillo", "Bíceps", "Flexión con agarre neutro")
            )
        ),
        RoutineTemplate(
            name = "Legs (Piernas)",
            description = "Día de piernas del programa PPL",
            exercises = listOf(
                TemplateExercise("Sentadilla", "Piernas", "Ejercicio rey del tren inferior"),
                TemplateExercise("Prensa de piernas", "Piernas", "Empuje en máquina"),
                TemplateExercise("Zancadas", "Piernas", "Trabajo unilateral de piernas"),
                TemplateExercise("Extensión de cuádriceps", "Piernas", "Aislamiento de cuádriceps"),
                TemplateExercise("Curl femoral", "Piernas", "Aislamiento de isquiotibiales"),
                TemplateExercise("Elevación de talones", "Piernas", "Trabajo de gemelos")
            )
        ),
        RoutineTemplate(
            name = "Cardio y Core",
            description = "Sesión de cardio combinada con ejercicios de core",
            exercises = listOf(
                TemplateExercise("Carrera", "Cardio", "Carrera continua", ExerciseType.CARDIO),
                TemplateExercise("Bicicleta estática", "Cardio", "Cardio en bicicleta", ExerciseType.CARDIO),
                TemplateExercise("Plancha abdominal", "Core", "Isométrico de core"),
                TemplateExercise("Crunch abdominal", "Core", "Flexión de tronco"),
                TemplateExercise("Elevación de piernas", "Core", "Core inferior tumbado")
            )
        )
    )
}

