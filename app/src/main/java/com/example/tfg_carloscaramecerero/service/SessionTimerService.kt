package com.example.tfg_carloscaramecerero.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.tfg_carloscaramecerero.MainActivity
import com.example.tfg_carloscaramecerero.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ──────────────────────────────────────────────────────────────────────────────
// Modelo compartido
// ──────────────────────────────────────────────────────────────────────────────

enum class TimerMode { COUNTDOWN, STOPWATCH }

data class TimerState(
    val isRunning: Boolean = false,
    val seconds: Int = 0,
    val totalSeconds: Int = 60,
    val mode: TimerMode = TimerMode.COUNTDOWN,
    val isFinished: Boolean = false
)

// ──────────────────────────────────────────────────────────────────────────────
// Servicio en primer plano
// ──────────────────────────────────────────────────────────────────────────────

class SessionTimerService : Service() {

    companion object {
        const val ACTION_START   = "com.example.tfg.SESSION_TIMER_START"
        const val ACTION_RESET   = "com.example.tfg.SESSION_TIMER_RESET"
        const val ACTION_PAUSE   = "com.example.tfg.SESSION_TIMER_PAUSE"
        const val ACTION_DISMISS = "com.example.tfg.SESSION_TIMER_DISMISS"

        const val EXTRA_TOTAL_SECONDS = "extra_total_seconds"
        const val EXTRA_TIMER_MODE    = "extra_timer_mode"

        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID      = "session_timer_channel"

        // Estado observable desde cualquier parte de la app
        private val _timerState = MutableStateFlow(TimerState())
        val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

        /** Reinicia el estado sin interactuar con el servicio (para nueva sesión). */
        fun resetTimerState() {
            _timerState.value = TimerState()
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    // ── Ciclo de vida ──────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val totalSeconds = intent.getIntExtra(EXTRA_TOTAL_SECONDS, 60)
                val modeName = intent.getStringExtra(EXTRA_TIMER_MODE) ?: TimerMode.COUNTDOWN.name
                val mode = runCatching { TimerMode.valueOf(modeName) }.getOrDefault(TimerMode.COUNTDOWN)
                startTimer(totalSeconds, mode)
            }
            ACTION_RESET   -> resetCurrentTimer()
            ACTION_PAUSE   -> pauseTimer()
            ACTION_DISMISS -> {
                // Si llegamos aquí desde startForegroundService hay que llamar
                // startForeground() antes de parar para evitar el crash de Android
                startForeground(NOTIFICATION_ID, buildNotification())
                dismissService()
            }
            else           -> startForeground(NOTIFICATION_ID, buildNotification())
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Lógica del temporizador ────────────────────────────────────────────────

    private fun startTimer(totalSeconds: Int, mode: TimerMode) {
        timerJob?.cancel()
        val initialSeconds = if (mode == TimerMode.COUNTDOWN) totalSeconds else 0
        _timerState.value = TimerState(
            isRunning    = true,
            seconds      = initialSeconds,
            totalSeconds = totalSeconds,
            mode         = mode,
            isFinished   = false
        )
        startForeground(NOTIFICATION_ID, buildNotification())

        timerJob = serviceScope.launch {
            while (true) {
                delay(1000L)
                val current = _timerState.value
                if (!current.isRunning) break

                when (current.mode) {
                    TimerMode.COUNTDOWN -> {
                        val newSecs = current.seconds - 1
                        if (newSecs <= 0) {
                            _timerState.value = current.copy(
                                seconds    = 0,
                                isRunning  = false,
                                isFinished = true
                            )
                            updateNotification()
                            break
                        } else {
                            _timerState.value = current.copy(seconds = newSecs)
                        }
                    }
                    TimerMode.STOPWATCH -> {
                        _timerState.value = current.copy(seconds = current.seconds + 1)
                    }
                }
                updateNotification()
            }
        }
    }

    private fun resetCurrentTimer() {
        val state = _timerState.value
        startTimer(state.totalSeconds, state.mode)
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
        updateNotification()
    }

    private fun dismissService() {
        timerJob?.cancel()
        _timerState.value = TimerState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ── Notificación ──────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Timer de sesión",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Muestra el cronómetro / temporizador de descanso activo"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val state = _timerState.value
        val timeText = formatDuration(state.seconds)

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, SessionTimerService::class.java).apply { action = ACTION_DISMISS },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, content) = when {
            state.isFinished -> "¡Descanso completado!" to "Listo para el siguiente set 💪"
            state.mode == TimerMode.COUNTDOWN && state.isRunning ->
                "Descansando..." to "⏱ Tiempo restante: $timeText"
            state.mode == TimerMode.STOPWATCH && state.isRunning ->
                "Descansando..." to "⏱ Tiempo transcurrido: $timeText"
            state.mode == TimerMode.COUNTDOWN ->
                "Temporizador pausado" to "⏱ Restante: $timeText"
            else ->
                "Cronómetro pausado" to "⏱ Transcurrido: $timeText"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setContentIntent(openAppIntent)
            .setOngoing(state.isRunning)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Cerrar",
                dismissIntent
            )
            .build()
    }

    private fun updateNotification() {
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private fun formatDuration(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val mins  = (totalSeconds % 3600) / 60
        val secs  = totalSeconds % 60
        return when {
            hours > 0                  -> "${hours}h ${mins}min"
            mins > 0 && secs > 0       -> "${mins}min ${secs}s"
            mins > 0                   -> "${mins}min"
            else                       -> "${secs}s"
        }
    }
}

