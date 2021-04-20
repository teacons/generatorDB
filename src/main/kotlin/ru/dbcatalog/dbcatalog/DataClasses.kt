package ru.dbcatalog.dbcatalog

import java.sql.Timestamp

data class User(
    val userName: String,
    val password: String,
    val email: String,
    val createTime: Timestamp
)

data class People(
    val fullName: String,
    val yearOfBirth: Int?
)

data class PeopleFunction(
    val name: String
)

data class MusicAlbum(
    val name: String,
    val year: Int,
    val poster: String?
)

data class FilmSeries(
    val name: String,
    val description: String?
)

data class Film(
    val name: String,
    val year: Int,
    val duration: Int,
    val description: String,
    val poster: String?,
    val filmSeriesId: Int?,
    val bookId: Int?
)

data class Music(
    val name: String,
    val year: Int,
    val duration: Int
)

data class FilmGenre(
    val name: String,
    val description: String?
)

data class Top(
    val name: String
)

data class MusicGenre(
    val name: String,
    val description: String?
)

data class BookGenre(
    val name: String,
    val description: String?
)

data class Book(
    val name: String,
    val year: Int,
    val description: String,
    val poster: String?,
    val bookSeriesId : Int?
)

data class BookSeries(
    val name: String,
    val description: String?
)
