package com.lenatopoleva.bluetoothclient.util.logger

import android.content.Context
import com.orhanobut.logger.*
import java.io.File


fun createLoggerWithDiskLogAdapter(context: Context, tag: String) {
    Logger.clearLogAdapters()
    val file = File(context.getExternalFilesDir(null)?.absolutePath + File.separatorChar + "logger")
    if (!file.exists()) {
        file.mkdir()
    }

    Logger.addLogAdapter(object : DiskLogAdapter(
        CsvFormatStrategy.newBuilder().tag(tag)
            .logStrategy(
                DiskLogStrategy(
                    DiskLogHandler(
                        file.absolutePath,
                        BuildConfig.APPLICATION_ID,
                        100 * 1024
                    )
                )
            )
            .build()
    ) {
        override fun isLoggable(priority: Int, tag: String?): Boolean {
            return true;
        }
    })
}

fun createLoggerWithAndroidLogAdapter(tag: String) {
    Logger.clearLogAdapters()
    val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
        .showThreadInfo(false) // (Optional) Whether to show thread info or not. Default true
        .methodCount(2) // (Optional) How many method line to show. Default 2
        .methodOffset(7) // (Optional) Hides internal method calls up to offset. Default 5
        .tag(tag) // (Optional) Global tag for every log. Default PRETTY_LOGGER
        .build()
    Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
}
