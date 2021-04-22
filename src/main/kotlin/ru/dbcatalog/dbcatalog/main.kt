package ru.dbcatalog.dbcatalog

import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import java.text.SimpleDateFormat
import java.util.*

fun main(args: Array<String>) {
    val launcher = Launcher()
    launcher.launch(args.toList())
}

class Launcher {
    @Option(name = "-t", usage = "type of content for top")
    private var typeTop = "book"

    @Option(name = "-n", required = true, usage = "quantity")
    private var num = 0

    @Argument(required = true, usage = "type of content")
    private var type = "book"

    fun launch(args: List<String>) {
        val parser = CmdLineParser(this)
        val randomizer = Randomizer()

        try {
            parser.parseArgument(args)
            println("Time start: ${SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())}")
            val timeStart = Calendar.getInstance().timeInMillis
            for (i in 1..num) {
                when (type) {
                    "book" -> randomizer.addBook()
                    "film" -> randomizer.addFilm()
                    "music" -> randomizer.addMusic()
                    "user" -> randomizer.addUser()
                    "top" -> when (typeTop) {
                        "book" -> randomizer.addTop(2)
                        "film" -> randomizer.addTop(0)
                        "music" -> randomizer.addTop(1)
                        else -> randomizer.addTop()
                    }
                    "killtables" -> DB().emptyTables()
                    else -> throw IllegalArgumentException("Invalid type specified")
                }
            }
            println("Time finish: ${SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date())}")
            println("Elapsed time: ${Calendar.getInstance().timeInMillis - timeStart}")
            println("Data generated successfully")
        } catch (e: CmdLineException) {
            System.err.println(e.message)
            System.err.println("java -jar generatorDB.jar book/film/music/user/top [-t type of content for top] quantity")
            parser.printUsage(System.err)
        } catch (e: Exception) {
            System.err.println(e.message)
            e.printStackTrace()
        } finally {
            randomizer.db.closeConnection()
        }
    }
}