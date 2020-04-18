package net.dixq.unlimiteddiary.content

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import net.dixq.unlimiteddiary.R

class Notification {

    companion object {

        fun post(context:Context){
            val channelId = "NOTIFICATION_CHANNEL_ID_UNLIMITEDDIARY"
            val builder = NotificationCompat.Builder(context, channelId).apply {
                setSmallIcon(R.drawable.ic_launcher_foreground)
                setContentTitle("無制限日記")
                setContentText("投稿内容をクラウドにアップロード中です...")
                priority = NotificationCompat.PRIORITY_DEFAULT
            }

    // API 26 以上の場合は NotificationChannel に登録する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "チャンネルの名前"
                val description = "チャンネルの説明文"
                val importance = NotificationManager.IMPORTANCE_MAX
                val channel = NotificationChannel(channelId, name, importance).apply {
                    this.description = description
                }

                // システムにチャンネルを登録する
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }

            with(NotificationManagerCompat.from(context)) {
                notify(ID, builder.build())
            }
        }

        fun cancel(context: Context){
            with(NotificationManagerCompat.from(context)) {
                cancel(ID)
            }
        }

        private const val ID = 1234
    }
}