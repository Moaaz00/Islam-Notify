package com.islamnotify.sounds.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.database.ContentObserver
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media.VolumeProviderCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.islamnotify.R
import com.islamnotify.common.AppUtils
import com.islamnotify.common.AppUtils.getLocalizedContext
import com.islamnotify.prayer_times.domain.model.PrayerTypes
import com.islamnotify.sounds.domain.SoundsConfig
import com.islamnotify.sounds.utils.SoundsUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class SoundsMediaService() : Service() {

    @Inject
    lateinit var soundsDataStore: SoundsDataStore

    @Inject
    lateinit var crashReporter: com.islamnotify.common.domain.CrashReporter
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    var player: ExoPlayer? = null
    var wakeLock: PowerManager.WakeLock? = null
    var soundsConfig: SoundsConfig? = null
    var audioFocusRequest: AudioFocusRequest? = null
    private val audioManager: AudioManager by lazy {
        getSystemService(AUDIO_SERVICE) as AudioManager
    }
    var mediaSession: MediaSessionCompat? = null
    private var afRequestTime: Long? = null
    private var action: String? = null
    private var detachNotification = false

    private val cancelAzanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals(CANCEL_AZAN_ACTION)) {
                Log.d(TAG, "Cancel Azan Receiver: service stopped")
                cleanUpAndStopService()
            }
        }
    }

    private var volumeObserver: ContentObserver? = null
    private val lastVolumes = mutableMapOf<Int, Int>()
    private val monitoredStreams = listOf(
        AudioManager.STREAM_ALARM,
        AudioManager.STREAM_MUSIC,
        AudioManager.STREAM_RING,
        AudioManager.STREAM_NOTIFICATION
    )


    companion object {
        const val TAG = "SoundsFlow"
        const val WAKE_LOCK_TAG = "IslamNotify:SoundsWakeLock"
        const val MEDIA_SESSION_TAG = "IslamNotify:MediaSession"
        val AUDIO_FOCUS_DELAYED_TIMEOUT = TimeUnit.MINUTES.toMillis(5)
        private const val CANCEL_AZAN_ACTION = "com.islamnotify.action.CANCEL_AZAN_INTENT"
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(CANCEL_AZAN_ACTION)
        ContextCompat.registerReceiver(this, cancelAzanReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        action = intent?.action
        val localizedContext = this.getLocalizedContext()

        // to specify the prayer name
        val prayerName: String =
            intent?.getStringExtra(SoundsUtils.EXTRA_PRAYER_TYPE) ?: PrayerTypes.EMPTY.name
        val notificationData: NotificationData? = formatPrayerType(prayerName)

        val title = notificationData?.title
            ?: localizedContext.getString(R.string.sounds_default_notification_title)
        val subtitle = notificationData?.subtitle ?: String()

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle(title)
            .bigText(subtitle)

        val cancelAzanIntent = Intent(CANCEL_AZAN_ACTION).apply {
            setPackage(packageName)
        }

        val cancelAzanPendingIntent = PendingIntent.getBroadcast(
            this,
            10999,
            cancelAzanIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, AppUtils.SOUNDS_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setColor(ContextCompat.getColor(this, R.color.light_green))
                .setContentText(subtitle)
                .setStyle(bigTextStyle)
                .addAction(
                    R.drawable.ic_stop_sounds,
                    localizedContext.getString(R.string.sounds_cancel_azan),
                    cancelAzanPendingIntent
                )
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    SoundsUtils.SOUNDS_NOTIFICATION_ID,
                    builder.build(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(SoundsUtils.SOUNDS_NOTIFICATION_ID, builder.build())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Service didn't start", e)
            crashReporter.recordNonFatal(e)
            cleanUpAndStopService()
        }

        if (intent == null || action == null) {
            Log.e(TAG, "onStartCommand: intent or action is null")
            crashReporter.log("SoundsMediaService.onStartCommand: null intent/action, stopping service")
            cleanUpAndStopService()
            return START_NOT_STICKY
        }

        scope.launch {
            soundsConfig = soundsDataStore.getConfig().first()
            val playInMute = soundsConfig?.isPlayWhileMute ?: SoundsConfig().isPlayWhileMute
            val ringerMode = audioManager.ringerMode

            if (playInMute) {
                playSound(action ?: String())
            } else {
                when (ringerMode) {
                    AudioManager.RINGER_MODE_NORMAL -> {
                        playSound(action ?: String())
                    }

                    AudioManager.RINGER_MODE_VIBRATE -> {
                        vibrate()
                        stopForeground(STOP_FOREGROUND_DETACH)
                        cleanUpAndStopService()
                    }

                    AudioManager.RINGER_MODE_SILENT -> {
                        stopForeground(STOP_FOREGROUND_DETACH)
                        cleanUpAndStopService()
                    }
                }
            }
        }

        return START_REDELIVER_INTENT
    }


    private fun playSound(action: String) {
        cleanUp()
        var soundUri: Uri? = null
        when (action) {
            SoundsUtils.ACTION_AZAN_SOUND -> {
                soundUri = soundsConfig?.azanSoundUriString?.toUri()
                    ?: "android.resource://${packageName}/${R.raw.azhan_nasser_alqatamy}".toUri()
            }

            SoundsUtils.ACTION_IQAMA_SOUND -> {
                soundUri = soundsConfig?.iqamaSoundUriString?.toUri()
                    ?: "android.resource://${packageName}/${R.raw.iqama_nasser_alqatamy}".toUri()
            }

            SoundsUtils.ACTION_NOTIFY_SOUND -> {
                soundUri = soundsConfig?.notifySoundUriString?.toUri()
                    ?: "android.resource://${packageName}/${R.raw.notify_sound}".toUri()
            }

        }

        if (soundUri == null) {
            Log.e(TAG, "playSound: sound uri is null")
            crashReporter.log("SoundsMediaService.playSound: sound uri null, using default notify sound")
            soundUri = "android.resource://${packageName}/${R.raw.notify_sound}".toUri()
        }

        // acquiring wake lock
        val powerManager: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
        wakeLock?.setReferenceCounted(false)
        wakeLock?.acquire(TimeUnit.MINUTES.toMillis(10))


        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG).apply {
            // initializing media session
            isActive = true

            val volumeProvider = object : VolumeProviderCompat(
                VOLUME_CONTROL_RELATIVE,
                100,
                audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            ) {
                override fun onAdjustVolume(direction: Int) {
                    Log.d(TAG, "Volume Key Pressed -> Stopping Service")
                    scope.launch { cleanUpAndStopService() }
                }
            }

            setPlaybackToRemote(volumeProvider)
            setupAdditionalVolumeInterceptor()

            setCallback(object : MediaSessionCompat.Callback() {
                override fun onStop() {
                    super.onStop()
                    Log.d(TAG, "Media Session onStop: media session stopped")
                    cleanUpAndStopService()
                }

                override fun onPause() {
                    super.onPause()
                    Log.d(TAG, "Media Session onPause: stopping service")
                    cleanUpAndStopService()
                }
            }
            )
        }


        // initializing exo player
        player = ExoPlayer.Builder(this).build().apply {
            // 1. Set Alarm Attributes & Focus Handling
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_ALARM)
                .setContentType(C.AUDIO_CONTENT_TYPE_SONIFICATION)
                .build()
            setAudioAttributes(audioAttributes, false)

            // 2. Add the Listener to monitor state
            addListener(playerListener)

            // 3. Prepare source
            val mediaItem = MediaItem.fromUri(soundUri)
            setMediaItem(mediaItem)
            prepare()

            // 4. request audio focus and play
            val afResult = requestAudioFocus()
            when (afResult) {
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                    Log.d(TAG, "Focus granted immediately")
                    mediaSession?.setActive(true)
                    updateMediaSessionState(PlaybackStateCompat.STATE_PLAYING)
                    play()
                }

                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                    Log.d(TAG, "Focus delayed. Waiting for GAIN signal...")
                    afRequestTime = System.currentTimeMillis()
                    updateMediaSessionState(PlaybackStateCompat.STATE_PAUSED)
                }

                else -> {
                    Log.e(TAG, "Focus denied")
                    crashReporter.log("SoundsMediaService: audio focus request denied, stopping service")
                    cleanUpAndStopService()
                    return
                }
            }
        }
    }


    private fun setupAdditionalVolumeInterceptor(){
        for (stream in monitoredStreams) {
            lastVolumes[stream] = audioManager.getStreamVolume(stream)
        }

        volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                for(stream in monitoredStreams){
                    val currentVolume: Int = audioManager.getStreamVolume(stream)
                    val lastVolume: Int = lastVolumes[stream] ?: currentVolume

                    if (currentVolume != lastVolume) {
                        lastVolumes[stream] = currentVolume
                        Log.d(TAG, "Content Observer (additional volume interceptor) onChange: volume changed, stopping the service.")
                        cleanUpAndStopService()
                        break
                    }
                }
            }
        }

        contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            volumeObserver!!
        )
    }


    private fun updateMediaSessionState(state: Int) {
        val mediaSessionState = PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_PLAY)
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
            .build()

        mediaSession?.setPlaybackState(mediaSessionState)
    }


    private val onAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    Log.w(TAG, "onAudioFocusChangeListener: audio focus lost. stopping the service")
                    detachNotification = true
                    cleanUpAndStopService()
                }

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    Log.w(TAG, "onAudioFocusChangeListener: audio focus lost transient. muting")
//                    player?.volume = 0f
                    player?.pause()
                }

                AudioManager.AUDIOFOCUS_GAIN, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> {
                    afRequestTime?.let { requestTime ->
                        if (System.currentTimeMillis() - requestTime > AUDIO_FOCUS_DELAYED_TIMEOUT) {
                            cleanUpAndStopService()
                            return@OnAudioFocusChangeListener
                        }
                    }

                    player?.apply {
                        Log.d(
                            TAG,
                            "onAudioFocusChangeListener: audio focus gained. playing the sound"
                        )
//                        volume = 1f
                        play()
                    }
                }
            }
        }


    private fun requestAudioFocus(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(onAudioFocusChangeListener)
                .build()

            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                onAudioFocusChangeListener,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }


    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                Log.d(TAG, "onPlaybackStateChanged: sound finished")
                cleanUpAndStopService()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "onPlayerError: ", error)
            crashReporter.recordNonFatal(error)
            stopForeground(STOP_FOREGROUND_DETACH)
            cleanUpAndStopService()
        }

    }


    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (vibrator.hasVibrator()) {
            val pattern = longArrayOf(0, 700, 500, 700)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    private fun formatPrayerType(prayerName: String): NotificationData? {
        val localizedContext = this.getLocalizedContext()
        val isFriday = AppUtils.isTodayFriday()

        val (titleRes, subtitleRes) = when (prayerName) {
            PrayerTypes.FAJR.name ->
                R.string.sounds_fajr_notification_title to R.string.sounds_fajr_notification_subtitle

            PrayerTypes.IQAMA_FAJR.name ->
                R.string.sounds_iqama_fajr_notification_title to R.string.sounds_fajr_notification_subtitle

            PrayerTypes.SUNRISE.name ->
                R.string.sounds_sunrise_notification_title to R.string.sounds_sunrise_notification_subtitle

            PrayerTypes.DUHA.name ->
                R.string.sounds_duha_notification_title to R.string.sounds_duha_notification_subtitle

            PrayerTypes.ZUHR.name -> if (isFriday) {
                R.string.sounds_jummah_notification_title to R.string.sounds_jummah_notification_subtitle
            } else {
                R.string.sounds_zuhr_notification_title to R.string.sounds_zuhr_notification_subtitle
            }

            PrayerTypes.IQAMA_ZUHR.name ->
                R.string.sounds_iqama_zuhr_notification_title to R.string.sounds_zuhr_notification_subtitle

            PrayerTypes.ASR.name ->
                R.string.sounds_asr_notification_title to R.string.sounds_asr_notification_subtitle

            PrayerTypes.IQAMA_ASR.name ->
                R.string.sounds_iqama_asr_notification_title to R.string.sounds_asr_notification_subtitle

            PrayerTypes.SUNSET.name ->
                R.string.sounds_sunset_notification_title to R.string.sounds_sunset_notification_subtitle

            PrayerTypes.IQAMA_SUNSET.name ->
                R.string.sounds_iqama_sunset_notification_title to R.string.sounds_sunset_notification_subtitle

            PrayerTypes.ISHA.name ->
                R.string.sounds_isha_notification_title to R.string.sounds_isha_notification_subtitle

            PrayerTypes.IQAMA_ISHA.name ->
                R.string.sounds_iqama_isha_notification_title to R.string.sounds_isha_notification_subtitle

            PrayerTypes.MIDNIGHT.name ->
                R.string.sounds_midnight_notification_title to R.string.sounds_midnight_notification_subtitle

            PrayerTypes.LAST_THIRD.name ->
                R.string.sounds_last_third_notification_title to R.string.sounds_last_third_notification_subtitle

            else -> return null
        }

        return NotificationData(
            title = "${localizedContext.getString(R.string.sounds_notification_starting_sentence)} ${
                localizedContext.getString(
                    titleRes
                )
            }",
            subtitle = localizedContext.getString(subtitleRes)
        )
    }

    private fun cleanUp() {
        wakeLock?.let { if (it.isHeld) it.release() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(onAudioFocusChangeListener)
        }

        player?.let {
            it.removeListener(playerListener)
            it.stop()
            it.release()
            player = null
        }


        mediaSession?.let {
            it.setActive(false)
            it.release()
            updateMediaSessionState(PlaybackStateCompat.STATE_STOPPED)
            mediaSession = null
        }

        volumeObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
        Log.d(TAG, "cleanUp: finished")
    }

    private fun cleanUpAndStopService() {
        cleanUp()
        if (action == SoundsUtils.ACTION_NOTIFY_SOUND || detachNotification) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        cleanUp()
        try {
            this.unregisterReceiver(cancelAzanReceiver)
        }catch (e: Exception){
            Log.w(TAG, "onDestroy: receiver already registered")
            crashReporter.recordNonFatal(e)
        }
        super.onDestroy()
    }

    override fun onTimeout(startId: Int) {
        scope.cancel()
        cleanUp()
        super.onTimeout(startId)
    }
}

data class NotificationData(
    var title: String,
    var subtitle: String
)