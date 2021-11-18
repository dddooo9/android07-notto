package com.gojol.notto.ui.option

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.gojol.notto.R
import com.gojol.notto.ui.MainActivity
import java.util.*

object DayNotificationManager {

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                DayNotificationReceiver.CHANNEL_ID,
                DayNotificationReceiver.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.option_notification_description)
            }

            with(NotificationManagerCompat.from(context)) {
                createNotificationChannel(notificationChannel)
            }
        }
    }

    fun showNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            DayNotificationReceiver.NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(
            context,
            DayNotificationReceiver.CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_notto_launcher_foreground)
            .setContentTitle(context.getString(R.string.option_notification_title))
            .setContentText(context.getString(R.string.option_notification_content))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        with(NotificationManagerCompat.from(context)) {
            notify(DayNotificationReceiver.NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    fun setAlarm(context: Context) {
        val alarmManager: AlarmManager = context.getSystemService() ?: return
        val intent = Intent(context, DayNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DayNotificationReceiver.NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            getSelectedTime(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun getSelectedTime(): Long {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 50)
        }

        var selectedTime = calendar.timeInMillis
        if (currentTime > selectedTime) selectedTime += 24 * 60 * 60 * 1000 //하루
        return selectedTime
    }

    fun cancelAlarm(context: Context) {
        val alarmManager: AlarmManager = context.getSystemService() ?: return
        val intent = Intent(context, DayNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, DayNotificationReceiver.NOTIFICATION_ID, intent, PendingIntent.FLAG_NO_CREATE
        )
        pendingIntent?.let {
            alarmManager.cancel(pendingIntent)
        }
    }
}