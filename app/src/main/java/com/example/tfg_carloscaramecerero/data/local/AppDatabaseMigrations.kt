package com.example.tfg_carloscaramecerero.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migraciones de la base de datos Room.
 * Cada objeto Migration describe exactamente los cambios SQL entre dos versiones consecutivas,
 * preservando los datos del usuario durante las actualizaciones de la app.
 *
 * Historial de versiones:
 *  v1 → v2  : food_entries reestructurada (name→description, macros opcionales, +dayOfWeek, +time, +aiAnalyzed)
 *  v2 → v3  : training_sessions +restSeconds
 *  v3 → v4  : exercises +exerciseType | training_sets +durationSeconds, +distanceKm, +isCardio
 *  v4 → v5  : food_entries +foodType, +grams
 *  v5 → v6  : nueva tabla user_profile
 *  v6 → v7  : nueva tabla health_documents
 *  v7 → v8  : nuevas tablas chat_conversations y chat_messages
 *  v8 → v9  : user_profile +fitnessGoal
 */
object AppDatabaseMigrations {

    // ─── v1 → v2 ──────────────────────────────────────────────────────────────
    // food_entries se reestructura: "name" pasa a "description", los macros pasan
    // a ser opcionales (nullable), y se añaden dayOfWeek, time y aiAnalyzed.
    // Dado que SQLite no permite DROP COLUMN ni cambio de tipo en ALTER TABLE,
    // se recrea la tabla completa preservando los datos compatibles.
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 1. Crear tabla nueva con el esquema v2
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS food_entries_new (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    description TEXT    NOT NULL DEFAULT '',
                    mealType    TEXT    NOT NULL DEFAULT '',
                    dayOfWeek   INTEGER NOT NULL DEFAULT 1,
                    time        TEXT    NOT NULL DEFAULT '',
                    date        INTEGER NOT NULL,
                    calories    REAL,
                    protein     REAL,
                    carbs       REAL,
                    fat         REAL,
                    aiAnalyzed  INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            // 2. Migrar datos: "name" → "description", dayOfWeek=1 por defecto, aiAnalyzed=0
            database.execSQL("""
                INSERT INTO food_entries_new (id, description, mealType, dayOfWeek, time, date, calories, protein, carbs, fat, aiAnalyzed)
                SELECT id, name, mealType, 1, '', date, calories, protein, carbs, fat, 0
                FROM food_entries
            """.trimIndent())
            // 3. Reemplazar la tabla antigua
            database.execSQL("DROP TABLE food_entries")
            database.execSQL("ALTER TABLE food_entries_new RENAME TO food_entries")
        }
    }

    // ─── v2 → v3 ──────────────────────────────────────────────────────────────
    // training_sessions: añadir columna restSeconds (segundos de descanso entre series).
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE training_sessions ADD COLUMN restSeconds INTEGER NOT NULL DEFAULT 60"
            )
        }
    }

    // ─── v3 → v4 ──────────────────────────────────────────────────────────────
    // exercises: añadir exerciseType (STRENGTH / CARDIO).
    // training_sets: añadir soporte para cardio (durationSeconds, distanceKm, isCardio).
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE exercises ADD COLUMN exerciseType TEXT NOT NULL DEFAULT 'STRENGTH'"
            )
            database.execSQL(
                "ALTER TABLE training_sets ADD COLUMN durationSeconds INTEGER NOT NULL DEFAULT 0"
            )
            database.execSQL(
                "ALTER TABLE training_sets ADD COLUMN distanceKm REAL NOT NULL DEFAULT 0.0"
            )
            database.execSQL(
                "ALTER TABLE training_sets ADD COLUMN isCardio INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    // ─── v4 → v5 ──────────────────────────────────────────────────────────────
    // food_entries: añadir foodType (comida/bebida) y grams (nullable).
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE food_entries ADD COLUMN foodType TEXT NOT NULL DEFAULT 'comida'"
            )
            database.execSQL(
                "ALTER TABLE food_entries ADD COLUMN grams INTEGER"
            )
        }
    }

    // ─── v5 → v6 ──────────────────────────────────────────────────────────────
    // Nueva tabla user_profile: almacena altura y condiciones de salud del usuario.
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS user_profile (
                    id               INTEGER NOT NULL,
                    height           REAL,
                    healthConditions TEXT    NOT NULL DEFAULT '',
                    PRIMARY KEY(id)
                )
            """.trimIndent())
        }
    }

    // ─── v6 → v7 ──────────────────────────────────────────────────────────────
    // Nueva tabla health_documents: gestión de analíticas médicas en PDF.
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS health_documents (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    fileName   TEXT    NOT NULL,
                    filePath   TEXT    NOT NULL,
                    uploadDate INTEGER NOT NULL
                )
            """.trimIndent())
        }
    }

    // ─── v7 → v8 ──────────────────────────────────────────────────────────────
    // Nuevas tablas para el historial de conversaciones con el asistente IA.
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS chat_conversations (
                    id        INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title     TEXT    NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """.trimIndent())
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS chat_messages (
                    id             INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    conversationId INTEGER NOT NULL,
                    content        TEXT    NOT NULL,
                    isUser         INTEGER NOT NULL,
                    timestamp      INTEGER NOT NULL,
                    FOREIGN KEY(conversationId) REFERENCES chat_conversations(id)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            // Índice sobre la FK conversationId para mejorar el rendimiento de las consultas
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_chat_messages_conversationId ON chat_messages (conversationId)"
            )
        }
    }

    // ─── v8 → v9 ──────────────────────────────────────────────────────────────
    // user_profile: añadir fitnessGoal (objetivo de fitness del usuario).
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE user_profile ADD COLUMN fitnessGoal TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    /** Lista ordenada de todas las migraciones para registrar en Room. */
    val ALL = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9
    )
}

