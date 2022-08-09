package io

import arc.Core
import arc.util.ColorCodes.*
import arc.util.Log
import arc.util.Log.format
import arc.util.Log.formatColors
import arc.util.Reflect

import mindustry.mod.Plugin
import mindustry.net.Administration
import mindustry.server.ServerControl

import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import kotlin.system.exitProcess

@Suppress("unused")
class BetterConsole : Plugin() {
    override fun init() {
        // For testing jline: js Timer.schedule(() => {Log.info(1)}, 0, 1).run()
        try {
            terminal = TerminalBuilder.builder().system(true).build()
            lineReader = LineReaderBuilder.builder().terminal(terminal).build()

            serverControl = Core.app.listeners.find { listener -> listener is ServerControl } as ServerControl

            logToFile = serverControl::class.java.getDeclaredMethod("logToFile", String::class.java)
            logToFile.isAccessible = true

            dateTime = Reflect.get(ServerControl::class.java, "dateTime")
            tags = Reflect.get(ServerControl::class.java, "tags")

            serverControl.serverInput = Runnable {
                while (true) {
                    try {
                        val line = lineReader.readLine("")
                        if (line.isNotEmpty()) {
                            Core.app.post { serverControl.handleCommandString(line) }
                        }
                    } catch (e: EndOfFileException) {
                        exitProcess(0)
                    } catch (e: UserInterruptException) {
                        exitProcess(0)
                    } catch (e: Exception) {
                        Log.err(e)
                    }
                }
            }

            Log.logger = Log.LogHandler { level: Log.LogLevel, log: String ->
                val resultLog: String = if (level == Log.LogLevel.err) {
                    log.replace(reset, lightRed + bold)
                } else log

                val result =
                    "$bold$lightBlack[${dateTime.format(LocalDateTime.now())}] $reset ${format(tags[level.ordinal])} $resultLog"

                if (lineReader.isReading) {
                    lineReader.callWidget(LineReader.CLEAR)
                    lineReader.terminal.writer().println(result)

                    lineReader.callWidget(LineReader.REDRAW_LINE)
                    lineReader.callWidget(LineReader.REDISPLAY)
                } else lineReader.terminal.writer().println(result)

                if (Administration.Config.logging.bool()) {
                    try {
                        logToFile.invoke(
                            serverControl,
                            "[${dateTime.format(LocalDateTime.now())}] ${
                                formatColors(
                                    tags[level.ordinal],
                                    false
                                )
                            } $resultLog"
                        )
                    } catch (e: IllegalAccessException) {
                        Log.err(e)
                    } catch (e: InvocationTargetException) {
                        Log.err(e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.err("Exception: ", e)
            exitProcess(1)
        }
    }

    companion object {
        private lateinit var terminal: Terminal
        private lateinit var lineReader: LineReader
        private lateinit var serverControl: ServerControl

        private lateinit var logToFile: Method
        private lateinit var tags: Array<String>
        private lateinit var dateTime: DateTimeFormatter
    }
}