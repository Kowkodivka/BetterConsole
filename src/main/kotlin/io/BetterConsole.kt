package io

import arc.Core
import arc.files.Fi
import arc.util.ColorCodes.*
import arc.util.CommandHandler.ResponseType.*
import arc.util.Log
import arc.util.Log.format
import arc.util.Log.formatColors

import mindustry.mod.Plugin
import mindustry.net.Administration
import mindustry.server.ServerControl

import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import kotlin.system.exitProcess

@Suppress("unused")
class BetterConsole : Plugin() {
    override fun init() {
        // js Timer.schedule(() => {Log.info(1)}, 0, 1).run()
        try {
            terminal = TerminalBuilder.builder().system(true).build()
            lineReader = LineReaderBuilder.builder().terminal(terminal).build()

            serverControl = Core.app.listeners.find { listener -> listener is ServerControl } as ServerControl

            serverControl.serverInput = Runnable {
                while (true) {
                    try {
                        val line = lineReader.readLine("")
                        if (line.isNotEmpty()) {
                            Core.app.post { handleCommandString(line) }
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

                val result = "$bold$lightBlack[${dateTime.format(LocalDateTime.now())}] $reset ${format(tags[level.ordinal])} $resultLog"

                if (lineReader.isReading) {
                    lineReader.callWidget(LineReader.CLEAR)
                    lineReader.terminal.writer().println(result)

                    lineReader.callWidget(LineReader.REDRAW_LINE)
                    lineReader.callWidget(LineReader.REDISPLAY)
                } else lineReader.terminal.writer().println(result)

                if (Administration.Config.logging.bool()) {
                    logToFile("[${dateTime.format(LocalDateTime.now())}] ${formatColors(tags[level.ordinal], false)} $resultLog")
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

        private var currentLogFile: Fi? = null
        private const val maxLogLength = 1024 * 1024 * 5
        private val logFolder = Core.settings.dataDirectory.child("logs/")

        private var tags = arrayOf("&lc&fb[D]&fr", "&lb&fb[I]&fr", "&ly&fb[W]&fr", "&lr&fb[E]", "")
        private var dateTime: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")

        fun handleCommandString(line: String) {
            val handler = serverControl.handler
            val response = handler.handleMessage(line)

            when (response.type) {
                unknownCommand -> Log.err("Invalid command. Type 'help' for help.")
                fewArguments -> Log.err("Too few command arguments. Usage: ${response.command.text} ${response.command.paramText}")
                manyArguments -> Log.err("Too many command arguments. Usage: ${response.command.text} ${response.command.paramText}")
                valid -> return
                else -> return
            }
        }

        fun logToFile(log: String) {
            if (currentLogFile != null && currentLogFile!!.length() > maxLogLength) {
                currentLogFile!!.writeString("[End of log file. Date: ${dateTime.format(LocalDateTime.now())}]".trimIndent(), true)
                currentLogFile = null
            }

            for (value in values) {
                log.replace(value, "")
            }

            if (currentLogFile == null) {
                var i = 0
                while (logFolder.child("log-$i.txt").length() >= maxLogLength) {
                    i++
                }

                currentLogFile = logFolder.child("log-$i.txt")
            }

            currentLogFile!!.writeString(log + "\n", true)
        }
    }
}