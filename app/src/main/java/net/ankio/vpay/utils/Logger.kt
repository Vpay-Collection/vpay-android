package net.ankio.vpay.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    private const val TAG = "net.ankio.vpay.utils.Logger"
    private const val LOG_EXPIRATION_DAYS = 2


    fun d(tag: String, message: String,context: Context) {
        Log.d(tag, message)
        writeLog(context, " $message")
    }

    private fun writeLog(context: Context, message: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val currentTime = timeFormat.format(Date())
        val logFileName = "log_$currentDate.txt"
        val logMessage = "[$currentTime] $message\n"
        val logFile = File(context.externalCacheDir, logFileName)
        try {
            val outputStream = FileOutputStream(logFile, true)
            outputStream.write(logMessage.toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        deleteExpiredLogs(context)
    }

    fun readLog(context: Context): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val logFileName = "log_$currentDate.txt"
        val logFile = File(context.externalCacheDir, logFileName)
        if (!logFile.exists()) {
            return ""
        }
        val stringBuilder = StringBuilder()
        try {
            val bufferedReader = BufferedReader(FileReader(logFile))
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                stringBuilder.append(line).append("\n")
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }

    fun empty(context: Context){
        val logDir = context.externalCacheDir
        logDir?.listFiles { file -> true }?.forEach { expiredLog ->
            expiredLog.delete()
        }
    }

    private fun deleteExpiredLogs(context: Context) {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val expirationDate = Calendar.getInstance().apply {
            time = currentDate
            add(Calendar.DATE, -LOG_EXPIRATION_DAYS)
        }.time
        val logDir = context.externalCacheDir
        logDir?.listFiles { file ->
            file.name.startsWith("log_") && file.name.endsWith(".txt") && run {
                val logDateStr = file.name.substring(4, 14)
                try {
                    val logDate = dateFormat.parse(logDateStr)
                    logDate?.before(expirationDate) ?: false
                } catch (e: Exception) {
                    false
                }
            }
        }?.forEach { expiredLog ->
            expiredLog.delete()
        }
    }
}
