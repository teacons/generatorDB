@file:Suppress("SpellCheckingInspection")

package ru.dbcatalog.dbcatalog

import org.intellij.lang.annotations.Language
import java.sql.*
import java.util.*

class DB {
    private val url = "jdbc:postgresql://10.0.0.100/db_catalog"
    private val props = Properties().apply {
        setProperty("user", "db_catalog")
        setProperty("password", "B8RCcsgy0")
        setProperty("ssl", "false")
    }
    private var connection: Connection = DriverManager.getConnection(url, props)

    private var connectionTransaction: Connection = DriverManager.getConnection(url, props).apply { autoCommit = false }


    fun queryWithResult(statement: PreparedStatement): List<Map<String, Any?>> {
        return try {
            if (connection.isClosed) openConnection()
            val rs = statement.executeQuery()
            val columnsCount = rs.metaData.columnCount
            val answer = mutableListOf<Map<String, Any?>>()
            while (rs.next()) {
                val row = mutableMapOf<String, Any?>()
                for (i in 1..columnsCount) {
                    row[rs.metaData.getColumnName(i)] = rs.getObject(i)
                }
                answer.add(row)
            }
            answer
        } catch (e: SQLException) {
            emptyList()
        }
    }

    fun execute(query: String) {
        if (connectionTransaction.isClosed) openConnectionTransaction()
        connectionTransaction.createStatement().executeUpdate(query)
    }

    fun commit() {
        connectionTransaction.commit()
    }

    fun getConnect(): Connection {
        if (connection.isClosed) openConnection()
        return connection
    }

    fun getConnectTransaction(): Connection {
        if (connection.isClosed) openConnectionTransaction()
        return connectionTransaction
    }

    private fun openConnection() {
        connection = DriverManager.getConnection(url, props)
    }

    private fun openConnectionTransaction() {
        connection = DriverManager.getConnection(url, props)
    }

    fun closeConnection() {
        connection.close()
        connectionTransaction.close()
    }

    fun emptyTables() {
        @Language("PostgreSQL")
        val query =
            "TRUNCATE db.artist, db.artist_has_artist, db.artist_is_people, db.book, db.book_genre," +
                    " db.book_has_book_genre, db.book_has_people, db.book_series, db.film, db.film_genre, " +
                    "db.film_has_film_genre, db.film_has_music,  db.film_has_people, db.film_series, db.music," +
                    " db.music_album, db.music_genre, db.music_has_album, db.music_has_artist, " +
                    "db.music_has_music_genre, db.people, db.people_function, db.top, db.top_has_book, " +
                    "db.top_has_film, db.top_has_music, db.user, db.user_likes_book_genre, db.user_likes_film_genre," +
                    " db.user_likes_music_genre, db.user_viewed_book, db.user_viewed_film, db.user_viewed_music" +
                    " RESTART IDENTITY CASCADE "
        connection.createStatement().executeUpdate(query)
    }

}

