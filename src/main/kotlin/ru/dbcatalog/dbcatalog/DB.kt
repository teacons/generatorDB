package ru.dbcatalog.dbcatalog

import java.sql.*
import java.util.*

class DB {
    private val url = "jdbc:postgresql://10.0.0.100/db_catalog"
    private val props = Properties().apply {
        setProperty("user", "db_catalog")
        setProperty("password", "B8RCcsgy0")
        setProperty("ssl", "false")
    }
    var connection: Connection = DriverManager.getConnection(url, props)

    fun query(statement: PreparedStatement): List<Map<String, Any?>> {
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
        } finally {
            closeConnection()
        }
    }

    fun getConnect(): Connection {
        if (connection.isClosed) openConnection()
        return connection
    }

    private fun openConnection() {
        connection = DriverManager.getConnection(url, props)
    }

    private fun closeConnection() {
        connection.close()
    }

}

