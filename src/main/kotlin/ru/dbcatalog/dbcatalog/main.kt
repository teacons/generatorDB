package ru.dbcatalog.dbcatalog

import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option

fun main(args: Array<String>) {
//    val launcher = ru.db_catalog.db_catalog.Launcher()
//    launcher.launch(args.toMutableList())
    val r = Randomizer()
    r.addBook()
}

class Launcher {
    @Option(name = "-t", usage = "type of content for top")
    private var typeTop = "book"

    @Option(name = "-n", required = true, usage = "quantity")
    private var num = 0

    @Argument(required = true, usage = "type of content")
    private var type = "book"

    fun launch(args: MutableList<String>) {
        val parser = CmdLineParser(this)

        try {
            parser.parseArgument(args)
        } catch (e: CmdLineException) {
            System.err.println(e.message)
            System.err.println("java -jar generatorDB.jar book/film/music/user/top [-t type of content for top] quantity")
            parser.printUsage(System.err)
        }

        try {
            val randomizer = Randomizer()
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
                    else -> throw java.lang.Exception("Invalid type specified")
                }
            }
            println("Data generated successfully")
        } catch (e: Exception) {
            System.err.println(e.message)
        }
    }
}