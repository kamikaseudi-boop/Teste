package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.DailyMacroSummary
import com.example.data.model.UserProfileEntity

object NotificationHelper {

    private const val CHANNEL_ID_MEALS = "nutri_meals_channel"
    private const val CHANNEL_ID_WATER = "nutri_water_channel"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val mealsChannel = NotificationChannel(
                CHANNEL_ID_MEALS,
                "Lembretes de Refeições",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações para lembrar de registrar refeições"
            }

            val waterChannel = NotificationChannel(
                CHANNEL_ID_WATER,
                "Lembretes de Hidratação",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Lembretes regulares para beber água"
            }

            notificationManager.createNotificationChannel(mealsChannel)
            notificationManager.createNotificationChannel(waterChannel)
        }
    }

    fun sendMealReminderNotification(context: Context, mealName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_MEALS)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle("Hora do $mealName! 🥗")
            .setContentText("Fotografe seu prato com a IA do NutriIA para calcular seus macros.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(101, builder.build())
    }

    fun sendWaterReminderNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_WATER)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Lembrete de Hidratação 💧")
            .setContentText("Mantenha seu metabolismo acelerado! Que tal beber 250ml de água agora?")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(102, builder.build())
    }

    fun shareDailyProgressViaWhatsApp(
        context: Context,
        user: UserProfileEntity,
        summary: DailyMacroSummary,
        nutritionistPhone: String = ""
    ) {
        val message = buildString {
            append("🥗 *NutriIA - Meu Relatório Diário*\n")
            append("👤 Aluno(a): *${user.name}*\n\n")
            append("🔥 *Calorias Consumidas:* ${summary.totalCalories} / ${user.calorieGoal} kcal\n")
            append("🥩 *Proteínas:* ${summary.totalProtein.toInt()}g / ${user.proteinGoalGrams}g\n")
            append("🌾 *Carboidratos:* ${summary.totalCarbs.toInt()}g / ${user.carbGoalGrams}g\n")
            append("🥑 *Gorduras:* ${summary.totalFat.toInt()}g / ${user.fatGoalGrams}g\n")
            append("💧 *Água Ingerida:* ${summary.totalWaterMl}ml / ${user.waterGoalMl}ml\n")
            append("🍽️ *Refeições Registradas:* ${summary.mealsCount}\n\n")
            append("✨ _Registrado e calculado pelo NutriIA com Reconhecimento Visual de Alimentos._")
        }

        val cleanPhone = nutritionistPhone.replace(Regex("[^0-9]"), "")
        val uri = if (cleanPhone.isNotEmpty()) {
            Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
        } else {
            Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")
        }

        val whatsappIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(whatsappIntent)
        } catch (e: Exception) {
            // If WhatsApp package is not installed, open standard chooser or browser
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(genericIntent, "Compartilhar no WhatsApp"))
        }
    }

    fun sendWhatsAppMessageToStudent(
        context: Context,
        student: UserProfileEntity,
        customMessage: String
    ) {
        val cleanPhone = student.phone.replace(Regex("[^0-9]"), "")
        val uri = if (cleanPhone.isNotEmpty()) {
            Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(customMessage)}")
        } else {
            Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(customMessage)}")
        }

        val whatsappIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.whatsapp")
        }

        try {
            context.startActivity(whatsappIntent)
        } catch (e: Exception) {
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, customMessage)
            }
            context.startActivity(Intent.createChooser(genericIntent, "Enviar mensagem pelo WhatsApp"))
        }
    }

    fun shareAchievementOnSocial(
        context: Context,
        user: UserProfileEntity,
        streakDays: Int,
        caloriesLogged: Int
    ) {
        val shareText = "🔥 Bati minha meta de $caloriesLogged kcal hoje com o @NutriIA! Estou há $streakDays dias seguidos no foco da minha alimentação saudável. 🥗💪 #NutriIA #FocoNosMacros #VidaSaudável"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Meu Progresso no NutriIA")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar conquista"))
    }
}
