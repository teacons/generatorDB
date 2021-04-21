package ru.dbcatalog.dbcatalog

// TODO: 21.04.2021 Объеденить генерацию book_series и film_series

import org.intellij.lang.annotations.Language
import java.sql.Timestamp
import java.sql.Types.INTEGER
import java.sql.Types.VARCHAR
import java.util.*
import kotlin.random.Random

class Randomizer {

    enum class ContentType {
        Film, Music, Book
    }

    private val adjectiveList = readingFile("adjectiveList.txt")
    private val nounList = readingFile("nounList.txt")
    private val adverbList = readingFile("adverbList.txt")
    private val verbList = readingFile("verbList.txt")
    private val phraseologicalList = readingFile("phraseologicalList.txt")
    private val fullNameList = readingFile("fullNameList.txt")
    private val forWhomList = readingFile("topForWhomList.txt")
    private val placeList = readingFile("placeList.txt")
    private val soloArtistList = readingFile("soloArtistList.txt")
    private val groupNameList = readingFile("groupNameList.txt")

    private val db = DB()

    private fun readingFile(name: String): List<String> {
        val inputStream = this::class.java.classLoader.getResourceAsStream(name)
        val list = mutableListOf<String>()
        inputStream!!.bufferedReader().forEachLine { list.add(it) }
        return list
    }

    /** =================================    МУЗЫКА И ВСЁ, ЧТО С НЕЙ СВЯЗАНО    ================================ **/

    fun addMusic(): Int {
        // Генерация названия песни
        val name = getName()

        // Генерация продолжительности песни
        val duration = Random.nextInt(2, 15)

        // Генерация альбома и года для песни
        var musicAlbumId: Int?
        var musicYear: Int
        val musicAlbumExist = !(Random.nextBoolean() && Random.nextBoolean())
        if (musicAlbumExist) {
            val musicAlbumPair = randomMusicAlbum()
            musicAlbumId = musicAlbumPair.first
            musicYear = musicAlbumPair.second
        } else {
            musicAlbumId = null
            musicYear = Random.nextInt(1500, 2022)
        }

        // Запись песни в бд (таблица Music)
        val music = Music(name, musicYear, duration) // TODO: Игорь добавляет запись в бд (таблица music)

        @Language("PostgreSQL")
        val query = "INSERT INTO db.music (name, year, duration) VALUES (?, ?, ?) RETURNING id;"
        val musicId = db.query(db.getConnect().prepareStatement(query).apply {
            setString(1, name)
            setInt(2, musicYear)
            setInt(3, duration)
        })[0]["id"] as Int  // TODO: Игорь выдирает из бд id добавленной песни

        // Генерация исполнителей
        when (Random.nextInt(0, 3)) { //0 - соло, 1 - группа, 2 - несколько исполнителей
            0 -> {
                val peopleId = randomPeople(musicYear)
                val soloArtistId = randomArtist(0)
                // TODO: Игорь заполняет таблицу music_has_artist. Данные - musicId, soloArtistId
                @Language("PostgreSQL")
                var query = "INSERT INTO db.music_has_artist (music_id, artist_id) VALUES (?, ?);"
                db.query(db.getConnect().prepareStatement(query).apply {
                    setInt(1, musicId)
                    setInt(2, soloArtistId)
                })
                // TODO: Игорь заполняет таблицу artist_is_people. Данные - soloArtistId, peopleId
                @Language("PostgreSQL")
                val query1 = "INSERT INTO db.artist_is_people (artist_id, people_id) VALUES (?, ?);"
                db.query(db.getConnect().prepareStatement(query1).apply {
                    setInt(1, soloArtistId)
                    setInt(2, peopleId)
                })
            }
            1 -> {
                val groupId = randomArtist(1)
                val group = mutableListOf<Int>()
                var groupNumber = Random.nextInt(2, 21)
                while (groupNumber != 0) {
                    val peopleId = randomPeople(musicYear)
                    val memberId = randomArtist(0)
                    group.add(memberId)
                    groupNumber--
                }
                // TODO: Игорь заполняет таблицу artist_has_artist. Данные - groupId, memberId (из group)
                @Language("PostgreSQL")
                var query = "INSERT INTO db.artist_has_artist (artist_group_id, artist_id) VALUES (?, ?);"
                db.query(db.getConnect().prepareStatement(query).apply {
                    setInt(1, groupId)
//                    setInt(2, )
                })
                // TODO: Игорь заполняет таблицу music_has_artist. Данные - musicId, groupId
                @Language("PostgreSQL")
                var query1 = "INSERT INTO db.music_has_artist (music_id, artist_id) VALUES (?, ?);"
                db.query(db.getConnect().prepareStatement(query1).apply {
                    setInt(1, musicId)
                    setInt(2, groupId)
                })
            }
            else -> {
                var artistNumber = Random.nextInt(2, 6)
                val artists = mutableListOf<Int>()
                while (artistNumber != 0) {
                    val peopleId = randomPeople(musicYear)
                    val artistId = randomArtist(0)
                    // TODO: Игорь заполняет таблицу artist_is_people. Данные - artistId, peopleId
                    @Language("PostgreSQL")
                    val query = "INSERT INTO db.artist_is_people (artist_id, people_id) VALUES (?, ?);"
                    db.query(db.getConnect().prepareStatement(query).apply {
                        setInt(1, artistId)
                        setInt(2, peopleId)
                    })
                    artists.add(artistId)
                    artistNumber--
                }
                // TODO: Игорь заполняет таблицу music_has_artist. Данные - musicId, artistId (из artists)
                @Language("PostgreSQL")
                var query1 = "INSERT INTO db.music_has_artist (music_id, artist_id) VALUES (?, ?);"
                db.query(db.getConnect().prepareStatement(query1).apply {
                    setInt(1, musicId)
//                    setInt(2, art)
                })
            }
        }

        // Заполнение кросс-таблицы music_has_music_genre
        val genreEmpty = db.query(
            db.getConnect().prepareStatement("SELECT count(*) AS rowcount FROM db.music_genre;")
        )[0]["rowcount"] as Long == 0L  // TODO: Игорь выдирает из бд, пустая ли таблица music_genre
        if (genreEmpty)     // Если таблица с жанрами пустая, то заполняем её
            fillGenre(ContentType.Music)
        val lastIdGenre = db.query(
            db.getConnect().prepareStatement("SELECT max(id) FROM db.music_genre;")
        )[0]["max"] as Int // TODO: Игорь выдирает из бд последний id жанра музыки (таблица music_genre)
        val genreId = Random.nextInt(lastIdGenre + 1)
        // TODO: Игорь заполняет таблицу music_has_music_genre. Данные - musicId, genreId
        @Language("PostgreSQL")
        val q = "INSERT INTO db.music_has_music_genre (music_id, music_genre_id) VALUES (?, ?);"
        db.query(db.getConnect().prepareStatement(q).apply {
            setInt(1, musicId)
            setInt(1, genreId)
        })

        return musicId
    }

    private fun randomMusicAlbum(): Pair<Int, Int> {
        val exist = Random.nextBoolean()    // true - берём из бд, false - генерим новый
        val empty =
            db.query(
                db.getConnect().prepareStatement("SELECT count(*) AS rowcount FROM db.music_album;")
            )[0]["rowcount"] as Long == 0L       // TODO: Игорь выдирает из бд, пустая ли таблица music_album
        val id: Int
        var yearOfAlbum: Int

        if (exist && !empty) {
            val lastIdBS = db.query(
                db.getConnect().prepareStatement("SELECT max(id) FROM db.music_album;")
            )[0]["max"] as Int   // TODO: Игорь выдирает последний id из бд (таблица music_album)
            id = Random.nextInt(lastIdBS + 1)
            yearOfAlbum = db.query(
                db.getConnect().prepareStatement("SELECT year FROM db.music_album WHERE id = ?").apply {
                    setInt(
                        1,
                        id
                    )
                })[0]["year"] as Int      // TODO: Игорь выдирает year_of_album из бд (таблица music_album, выбранный id)
        } else {    // Генерация объекта песен альбома
            // Генерация названия альбома
            val name = getName()

            // Генерация постера
            val posterExist = !(Random.nextBoolean() && Random.nextBoolean())
            val poster = if (posterExist) "https://poster.com/${Random.nextInt(100000, 1000000)}" else null

            // Генерация года создания альбома
            yearOfAlbum = Random.nextInt(1500, 2022)

            // Запись альбома в бд (таблица music_album)
            val musicAlbum =
                MusicAlbum(name, yearOfAlbum, poster)     // TODO: Игорь добавляет запись в бд (таблица music_album)

            @Language("PostgreSQL")
            val query = "INSERT INTO db.music_album (name, year, poster) VALUES (?, ?) RETURNING id;"
            id =
                db.query(db.getConnect().prepareStatement(query).apply {
                    setString(1, name)
                    setInt(2, yearOfAlbum)
                    if (posterExist)
                        setString(3, poster)
                    else
                        setNull(3, VARCHAR)
                })[0]["id"] as Int
            // TODO: Игорь выдирает из бд id добавленного альбома
        }
        return Pair(id, yearOfAlbum)
    }


    private fun randomArtist(type: Int): Int {//0 - соло, 1 - группа
        //Генерация имени для артиста
        val nameList = when (type) {
            0 -> soloArtistList.union(nounList)
            else -> groupNameList
        }
        val name = nameList.random().capitalize()

        // Генерация описания артиста
        val descExist = !(Random.nextBoolean() && Random.nextBoolean() && Random.nextBoolean())
        val desc = getDescArtist(type)

        // TODO: Игорь добавляет запись в бд (таблица artist)

        @Language("PostgreSQL")
        val query = "INSERT INTO db.artist (name, description) VALUES (?, ?) RETURNING id;"
        val id = db.query(db.getConnect().prepareStatement(query).apply {
            setString(1, name)
            if (descExist)
                setString(2, desc)
            else
                setNull(2, VARCHAR)
        })[0]["id"] as Int  // TODO: Игорь выдирает из бд id добавленного артиста

        return id
    }

    /** =================================    ФИЛЬМ И ВСЁ, ЧТО С НИМ СВЯЗАНО    ================================ **/

    fun addFilm(): Int {
        // Генерация названия и описания фильма
        val nameAndDesc = getNameAndDescByTypes(ContentType.Film, false)
        val name = nameAndDesc.first
        val desc = nameAndDesc.second!!

        // Генерация постера
        val posterExist = !(Random.nextBoolean() && Random.nextBoolean())
        val poster = if (posterExist) "https://poster.com/" + Random.nextInt(100000, 1000000) else null

        // Генерация продолжительности фильма
        val duration = Random.nextInt(40, 270)

        // Генерация серии фильмов
        val filmSeriesExist = !(Random.nextBoolean() && Random.nextBoolean())
        val filmSeriesId = if (filmSeriesExist) randomFilmSeries() else null

        // Генерация книги
        val bookExist = !(Random.nextBoolean() && Random.nextBoolean())
        val bookId = if (bookExist) addBook() else null

        // Генерация года создания фильма
        val filmYear = Random.nextInt(1895, 2022)

        //Генерация съёмочной группы
        val group = mutableListOf<Int>()
        var groupNumber = Random.nextInt(8, 24)
        while (groupNumber != 0) {
            val peopleId = randomPeople(filmYear)
            group.add(peopleId)
            groupNumber--
        }

        // Запись фильма в бд (таблица Film)
        val film = Film(
            name,
            filmYear,
            duration,
            desc,
            poster,
            filmSeriesId,
            bookId
        ) // TODO: Игорь добавляет запись в бд (таблица film)

        @Language("PostgreSQL")
        val filmQuery =
            "INSERT INTO db.film (name, year, duration, description, poster, film_series_id, book_id) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id;"
        val filmId = db.query(db.getConnect().prepareStatement(filmQuery).apply {
            setString(1, name)
            setInt(2, filmYear)
            setInt(3, duration)
            setString(4, desc)
            if (posterExist)
                setString(5, poster)
            else
                setNull(5, VARCHAR)
            if (filmSeriesExist)
                setInt(6, filmSeriesId!!)
            else
                setNull(6, INTEGER)
            if (bookExist)
                setInt(7, bookId!!)
            else
                setNull(7, INTEGER)
        })[0]["id"] as Int  // TODO: Игорь выдирает из бд id добавленного фильма

        // Генерация музыки
        val musicExist = !(Random.nextBoolean() && Random.nextBoolean())
        if (musicExist) {
            var numOfMusic = Random.nextInt()
            while (numOfMusic != 0) {
                val musicId = addBook()
                // TODO: Игорь заполняет таблицу film_has_music. Данные - filmId, musicId
                @Language("PostgreSQL")
                val query = "INSERT INTO db.film_has_music (film_id, music_id) VALUES (?, ?);"
                db.query(db.getConnect().prepareStatement(query).apply {
                    setInt(1, filmId)
                    setInt(1, musicId)
                })
                numOfMusic--
            }
        } else null

        // Заполнение кросс-таблицы film_has_people
        val funcEmpty = db.query(
            db.getConnect().prepareStatement("SELECT count(*) AS rowcount FROM db.people_function;")
        )[0]["rowcount"] as Long == 0L // TODO: Игорь выдирает из бд, пустая ли таблица people_function
        if (funcEmpty)     // Если таблица с профессиями пустая, то заполняем её
            fillPeopleFunction()
        val lastIdFunc = db.query(
            db.getConnect().prepareStatement("SELECT max(id) FROM db.people_function;")
        )[0]["max"] as Int // TODO: Игорь выдирает из бд последний id профессии (таблица people_function)
        groupNumber = group.size
        while (groupNumber != 0) {
            val funcId = Random.nextInt(lastIdFunc + 1)
            // TODO: Игорь заполняет таблицу film_has_people. Данные - filmId, peopleId (из group), funcId
            @Language("PostgreSQL")
            val query = "INSERT INTO db.film_has_people (film_id, people_id, people_function_id) VALUES (?, ?, ?);"
            db.query(db.getConnect().prepareStatement(query).apply {
                setInt(1, filmId)
//                setInt(2, pe)
                setInt(3, funcId)
            })
            groupNumber--
        }

        // Заполнение кросс-таблицы film_has_film_genre
        val genreEmpty = db.query(
            db.getConnect().prepareStatement("SELECT count(*) AS rowcount FROM db.film_genre;")
        )[0]["rowcount"] as Long == 0L  // TODO: Игорь выдирает из бд, пустая ли таблица film_genre
        if (genreEmpty)     // Если таблица с жанрами пустая, то заполняем её
            fillGenre(ContentType.Film)
        val lastIdGenre = db.query(
            db.getConnect().prepareStatement("SELECT max(id) FROM db.film_genre;")
        )[0]["max"] as Int // TODO: Игорь выдирает из бд последний id жанра фильмов (таблица film_genre)
        val genreId = Random.nextInt(lastIdGenre + 1)
        // TODO: Игорь заполняет таблицу film_has_film_genre. Данные - filmId, genreId
        @Language("PostgreSQL")
        val query = "INSERT INTO db.film_has_film_genre (film_id, film_genre_id) VALUES (?, ?);"
        db.query(db.getConnect().prepareStatement(query).apply {
            setInt(1, filmId)
            setInt(1, genreId)
        })

        return filmId
    }

    private fun randomFilmSeries(): Int {
        val exist = Random.nextBoolean()    // true - берём из бд, false - генерим новый
        val empty =
            db.query(
                db.getConnect().prepareStatement("SELECT count(*) AS rowcount FROM db.film_series;")
            )[0]["rowcount"] as Long == 0L       // TODO: Игорь выдирает из бд, пустая ли таблица film_series
        val id: Int

        if (exist && !empty) {
            val lastIdBS = db.query(
                db.getConnect().prepareStatement("SELECT max(id) FROM db.film_series;")
            )[0]["max"] as Int   // TODO: Игорь выдирает последний id из бд (таблица film_series)
            id = Random.nextInt(lastIdBS + 1)
        } else {
            // Генерация названия и описания серии фильмов
            val nameAndDesc = getNameAndDescByTypes(ContentType.Film, true)
            val name = nameAndDesc.first
            val desc = nameAndDesc.second

            // Запись серии фильмов в бд (таблица film_series)
            val filmSeries = FilmSeries(name, desc)     // TODO: Игорь добавляет запись в бд (таблица film_series)

            @Language("PostgreSQL")
            val query = "INSERT INTO db.film_series (name, description) VALUES (?, ?) RETURNING id;"
            id =
                db.query(db.getConnect().prepareStatement(query).apply {
                    setString(1, name)
                    if (desc != null)
                        setString(2, desc)
                    else
                        setNull(2, VARCHAR)
                })[0]["id"] as Int
            // TODO: Игорь выдирает из бд id добавленной серии фильмов
        }
        return id
    }

    private fun fillPeopleFunction() {
        val functionList = readingFile("functionList.txt")

        @Language("PostgreSQL")
        val query = "INSERT INTO db.people_function (name) VALUES (?);"
        for (funcName in functionList) {
            db.query(db.getConnect().prepareStatement(query).apply {
                setString(1, funcName)
            })
            // TODO: Игорь добавляет funcName в бд (таблица people_function)
        }
    }

    /** =================================    КНИГА И ВСЁ, ЧТО С НЕЙ СВЯЗАНО    ================================ **/

    fun addBook(): Int {     // возврат - id книги
        // Генерация названия и описания книги
        val nameAndDesc = getNameAndDescByTypes(ContentType.Book, false)
        val bookName = nameAndDesc.first
        val bookDesc = nameAndDesc.second!!

        // Генерация постера
        val posterExist = !(Random.nextBoolean() && Random.nextBoolean())
        val bookPoster = if (posterExist) "https://poster.com/" + Random.nextInt(100000, 1000000) else null

        // Генерация серии книг
        val bookSeriesExist = !(Random.nextBoolean() && Random.nextBoolean())
        val bookSeriesId = if (bookSeriesExist) randomBookSeries() else null

        // Генерация года написания книги
        val bookYear = Random.nextInt(1500, 2021)

        // Запись книги в бд (таблица ru.db_catalog.db_catalog.Book)
        var statement = db.getConnect()
            .prepareStatement("INSERT INTO db.book (name, year, description, poster, book_series_id) VALUES (?, ?, ?, ?, ?) RETURNING id;")
            .apply {
                setString(1, bookName)
                setInt(2, bookYear)
                setString(3, bookDesc)
                if (posterExist)
                    setString(4, bookPoster)
                else
                    setNull(4, VARCHAR)
                if (bookSeriesExist)
                    setInt(5, bookSeriesId!!)
                else
                    setNull(5, INTEGER)
            }

        val bookId = db.query(statement)[0]["id"] as Int    // id добавленной книги

        // Выбор количества авторов книги: от 1 до 3
        val peopleNum = Random.nextInt(1, 4)
        val peopleIdList = mutableListOf<Int>()

        // Генерация авторов книги
        for (i in 1..peopleNum)
            peopleIdList.add(randomPeople(bookYear))

        // Заполнение кросс-таблицы book_has_people
        for (peopleId in peopleIdList) {
            statement =
                db.getConnect().prepareStatement("INSERT INTO db.book_has_people (book_id, people_id) VALUES (?, ?);")
                    .apply {
                        setInt(1, bookId)
                        setInt(2, peopleId)
                    }
            db.query(statement)
        }

        // Подготовка данных для заполнения кросс-таблицы book_has_book_genre
        var genreIds = getGenresIdsByType(ContentType.Book)   // список id жанров Я СДЕЛАЛЬ

        if (genreIds.isEmpty()) {     // Если таблица с жанрами пустая, то заполняем её
            fillGenre(ContentType.Book)
            genreIds = getGenresIdsByType(ContentType.Book)
        }

        // Генерация количества жанров (от 1 до 5, но не больше, чем есть жанров)
        val genreNum = Random.nextInt(1, minOf(genreIds.size, 5) + 1)

        // Генерация id жанров книги
        for (i in 0 until genreNum) {
            val genreId = genreIds.random()
            genreIds.remove(genreId)

            // Заполнение таблицы book_has_book_genre
            statement = db.getConnect()
                .prepareStatement("INSERT INTO db.book_has_book_genre (book_id, book_genre_id) VALUES (?, ?);").apply {
                    setInt(1, bookId)
                    setInt(2, genreId)
                }
            db.query(statement)
        }

        return bookId
    }

    private fun randomBookSeries(): Int {   // возврат - id серии книг
        // Выбор: true - берём из бд, false - генерим новый
        val exist = Random.nextBoolean()
        val id: Int
        val bookSeriesIds = mutableListOf<Int>()

        // Если берём из бд, заполняем список id существующих серий книг
        if (exist)
            db.query(db.getConnect().prepareStatement("SELECT id FROM db.book_series;"))
                .onEach { bookSeriesIds.add(it["id"] as Int) }

        if (exist && bookSeriesIds.isNotEmpty())    // Если мы берём из бд и таблица book_series не пустая
            id = bookSeriesIds.random()             // выбираем id из существующих
        else {  // Создание новой серии книг
            // Генерация названия и описания серии книг
            val nameAndDesc = getNameAndDescByTypes(ContentType.Book, true)
            val name = nameAndDesc.first
            val desc = nameAndDesc.second

            // Запись серии книг в бд (таблица book_series)
            val statement =
                db.getConnect()
                    .prepareStatement("INSERT INTO db.book_series (name, description) VALUES (?, ?) RETURNING id;")
                    .apply {
                        setString(1, name)
                        if (desc != null)
                            setString(2, desc)
                        else
                            setNull(2, VARCHAR)


                    }
            id = db.query(statement)[0]["id"] as Int
        }
        return id
    }


    /** =================================    ТОП И ВСЁ, ЧТО С НИМ СВЯЗАНО    ================================ **/

    fun addTop(type: Int = Random.nextInt(0, 3)) {
        // type: значение по умолчанию - без разницы (выбирается рандомно); 0 - фильм, 1 - музыка, 2 - книга
        // Подготовка данных
        val noun: String
        val stateForIds: String             // statement для получения id в film/music/book
        val stateForInsertInoCross: String  // statement для записи в top_has_film / top_has_music / top_has_book

        when (type) {
            0 -> {
                noun = "фильмов"
                //language=PostgreSQL
                stateForIds = "SELECT id FROM db.film;"
                //language=PostgreSQL
                stateForInsertInoCross = "INSERT INTO db.top_has_film (top_id, film_id, position) VALUES (?, ?, ?);"
            }
            1 -> {
                noun = "песен"
                //language=PostgreSQL
                stateForIds = "SELECT id FROM db.music;"
                //language=PostgreSQL
                stateForInsertInoCross = "INSERT INTO db.top_has_music (top_id, music_id, position) VALUES (?, ?, ?);"
            }
            else -> {
                noun = "книг"
                //language=PostgreSQL
                stateForIds = "SELECT id FROM db.book;"
                //language=PostgreSQL
                stateForInsertInoCross = "INSERT INTO db.top_has_book (top_id, book_id, position) VALUES (?, ?, ?);"
            }
        }

        // Генерация размера топа
        var num = Random.nextInt(5, 101)

        // Генерация имени топа
        val adj = adjectiveList.random()
        val name = "Топ-$num ${adj.substring(0, adj.length - 2)}ых $noun ${
            forWhomList.random()
        } (автор - ${fullNameList.random()})"

        // Запись топа в бд (таблица top) и получение id топа
        val topId = db.query(db.getConnect().prepareStatement("INSERT INTO db.top (name) VALUES (?) RETURNING id;")
            .apply { setString(1, name) })[0]["id"] as Int

        // Заполнение соответствующей кросс-таблицы
        // Получаем список доступных id в соответствующей таблице (film, music, book)
        val availableIds = mutableListOf<Int>()
        db.query(db.getConnect().prepareStatement(stateForIds)).onEach { availableIds.add(it["id"] as Int) }

        while (num != 0) {
            // Определение id элемента
            val elId = if (availableIds.isEmpty())  // Если использовали все существующие элементы, генерируем новые
                when (type) {
                    0 -> addFilm()
                    1 -> addMusic()
                    else -> addBook()
                }
            else                                    // Если же пока есть неиспользованные элементы, выбираем из них
                availableIds.random()

            // Запись в нужную таблицу в бд
            db.query(
                db.getConnect()
                    .prepareStatement(stateForInsertInoCross)
                    .apply {
                        setInt(1, topId)
                        setInt(2, elId)
                        setInt(3, num)
                    })

            // Если использовали существующий элемент, удаляем его из доступных
            if (availableIds.contains(elId))
                availableIds.remove(elId)
            num--
        }
    }


    /** ===============================    ПОЛЬЗОВАТЕЛЬ И ВСЁ, ЧТО С НИМ СВЯЗАНО    ============================== **/

    fun addUser() {
        // Генерация username
        val userNameWordList = readingFile("forUserName.txt")
        val stringBuilderForUserName = StringBuilder()
        while (stringBuilderForUserName.length < 6) {
            val word = userNameWordList.random()
            if (stringBuilderForUserName.length + word.length < 20)
                stringBuilderForUserName.append(word)
            else
                break
        }
        val userName = stringBuilderForUserName.toString()

        // Генерация password
        val alphabet = ('0'..'9').joinToString("") +
                ('a'..'z').joinToString("") +
                ('A'..'Z').joinToString("") +
                "!@#$%&"

        val stringBuilderForPassword = StringBuilder()

        while (stringBuilderForPassword.length < Random.nextInt(6, 32)) {
            stringBuilderForPassword.append(alphabet.random())
        }

        val password = stringBuilderForPassword.toString()

        // Генерация email
        val emailSamples = readingFile("forEmail.txt")
        val email = userName + emailSamples.random()

        // Генерация TimeStamp
        val timestamp = generateTimestamp()

        // Добавление в БД записи о пользователе
        @Language("PostgreSQL")
        val sqlQuery = "INSERT INTO db.user (username, password, email, create_time) VALUES (?, ?, ?, ?) RETURNING id;"
        val statementForAddUserEntry = db.getConnect().prepareStatement(sqlQuery).apply {
            setString(1, userName)
            setString(2, password)
            setString(3, email)
            setTimestamp(4, timestamp)
        }

        val userId = db.query(statementForAddUserEntry)[0]["id"] as Int

        // Генерация любимых жанров пользовател
        generateUsersGenres(userId, ContentType.Music)
        generateUsersGenres(userId, ContentType.Book)
        generateUsersGenres(userId, ContentType.Film)

        // Генерация просмотренного пользователем контента
        generateUsersViewedContent(userId, ContentType.Music)
        generateUsersViewedContent(userId, ContentType.Book)
        generateUsersViewedContent(userId, ContentType.Film)
    }

    private fun generateUsersViewedContent(userId: Int, contentType: ContentType) {
        var listOfContentIds = getContentIdsByType(contentType)

        if (listOfContentIds.isEmpty()) {
            when (contentType) {
                ContentType.Music -> for (i in 1..5) addMusic()
                ContentType.Book -> for (i in 1..5) addBook()
                ContentType.Film -> for (i in 1..5) addFilm()
            }
            listOfContentIds = getGenresIdsByType(contentType)
        }

        //      Рандомное добавление просмотренного контента в БД
        for (i in 1..Random.nextInt(5, listOfContentIds.size / 2)) {

            @Language("PostgreSQL")
            val insertQuery = when (contentType) {
                ContentType.Music -> "INSERT INTO db.user_viewed_music (user_id, music_id, rating, time) VALUES (?, ?, ?, ?);"
                ContentType.Book -> "INSERT INTO db.user_viewed_book (user_id, book_id, rating, time) VALUES (?, ?, ?, ?);"
                ContentType.Film -> "INSERT INTO db.user_viewed_film (user_id, film_id, rating, time) VALUES (?, ?, ?, ?);"
            }

            val randomId = listOfContentIds.random()
            listOfContentIds.remove(randomId)

            db.query(db.getConnect().prepareStatement(insertQuery).apply {
                setInt(1, userId)
                setInt(2, randomId)
                if (Random.nextBoolean())
                    setInt(3, Random.nextInt(1, 5))
                else
                    setNull(3, INTEGER)
                setTimestamp(4, generateTimestamp())
            })
        }
    }

    private fun getContentIdsByType(contentType: ContentType): MutableList<Int> {
        val listOfContentIds = mutableListOf<Int>()

        @Language("PostgreSQL")
        val selectQuery = when (contentType) {
            ContentType.Music -> "SELECT id FROM db.music;"
            ContentType.Book -> "SELECT id FROM db.book;"
            ContentType.Film -> "SELECT id FROM db.film;"
        }
        db.query(db.getConnect().prepareStatement(selectQuery)).onEach { listOfContentIds.add(it["id"] as Int) }

        return listOfContentIds
    }

    private fun generateTimestamp(): Timestamp {
        return if (Random.nextBoolean()) {
            Timestamp(Calendar.getInstance().timeInMillis)
        } else {
            val timeFrom = Calendar.getInstance().apply { set(2010, 0, 1) }.timeInMillis
            val timeTo = Calendar.getInstance().timeInMillis
            Timestamp(Random.nextLong(timeFrom, timeTo))
        }
    }

    private fun generateUsersGenres(userId: Int, contentType: ContentType) {
        var listOfGenreIds = getGenresIdsByType(contentType)

        // Проверка наличия жанров в списке из БД
        if (listOfGenreIds.isEmpty()) {
            fillGenre(contentType)
            listOfGenreIds = getGenresIdsByType(contentType)
        }

        // Рандомное добавление жанров в БД
        for (i in 1..Random.nextInt(5, listOfGenreIds.size / 2)) {

            @Language("PostgreSQL")
            val query = when (contentType) {
                ContentType.Music -> "INSERT INTO db.user_likes_music_genre (user_id, music_genre_id) VALUES (?, ?);"
                ContentType.Book -> "INSERT INTO db.user_likes_book_genre (user_id, book_genre_id) VALUES (?, ?);"
                ContentType.Film -> "INSERT INTO db.user_likes_film_genre (user_id, film_genre_id) VALUES (?, ?);"
            }

            val randomId = listOfGenreIds.random()
            listOfGenreIds.remove(randomId)

            db.query(db.getConnect().prepareStatement(query).apply {
                setInt(1, userId)
                setInt(2, randomId)
            })
        }
    }

    /** ====================================    ОБЩИЕ МЕТОДЫ    =================================== **/

    /**                                ЛЮДИ И ВСЁ, ЧТО С НИМИ СВЯЗАНО                               **/

    /** Функция генерации человека. Может как создать новую запись в таблице people, так и использовать уже
     *  существующую.
     *  Вход - year:
     *      = null (по умолчанию) - обычная генерация;
     *      = число               - поиск человека с годом рождения "рядом" с указанным годом или создание такого человека.
     *  Возврат - id_человека **/
    private fun randomPeople(year: Int? = null): Int {
        val id: Int
        val peopleIds = mutableListOf<Int>()

        // Установка границ генерации года рождения
        var from = 1500
        var until = 2005

        if (year != null) {     // Если указан year, то генерируем год "рядом" с ним
            from = year - 80
            until = year - 15
        }

        // Выбираем: true - берём из бд, false - генерим новый
        val exist = Random.nextBoolean() || Random.nextBoolean()

        // Если берём из бд, получаем список id людей из таблицы people
        if (exist)
        // Если year не указан, помещаем в список все id
            if (year == null)
                db.query(db.getConnect().prepareStatement("SELECT id FROM db.people;"))
                    .onEach { peopleIds.add(it["id"] as Int) }
            // Если year указан, помещаем в список id людей, подходящих под границы года
            else
                db.query(db.getConnect()
                    .prepareStatement("SELECT id FROM db.people WHERE ? <= year_of_birth AND year_of_birth <= ?;")  // TODO: проверить
                    .apply {
                        setInt(1, from)
                        setInt(2, until)
                    })
                    .onEach { peopleIds.add(it["id"] as Int) }

        if (exist && peopleIds.isNotEmpty()) {  // Если мы берём из бд и список интересующих id не пустой
            // Выбираем id
            id = peopleIds.random()

        } else {    // Создание нового человека
            // Генерация имени человека
            val fullName = fullNameList.random()

            // Генерация года рождения человека
            val yearExist = Random.nextBoolean() || Random.nextBoolean() || Random.nextBoolean() || Random.nextBoolean()
            val yearOfBirth = if (yearExist) Random.nextInt(from, until) else null

            // Запись человека в бд (таблица people)
            val statement =
                db.getConnect()
                    .prepareStatement("INSERT INTO db.people (fullname, year_of_birth) VALUES (?, ?) RETURNING id;")
                    .apply {
                        setString(1, fullName)
                        if (yearExist)
                            setInt(2, yearOfBirth!!)
                        else
                            setNull(2, INTEGER)
                    }
            id = db.query(statement)[0]["id"] as Int
        }
        return id
    }

    /**                                ЖАНРЫ И ВСЁ, ЧТО С НИМИ СВЯЗАНО                               **/

    /** Функция заполнения соответствующей таблицы жанров (film_genre, music_genre или book_genre) в зависимости от
     *  поданного ключа contentType **/
    private fun fillGenre(contentType: ContentType) {
        // Определение нужного файла и выражения для работы с бд
        val fileName: String
        val stateForInsert: String
        when (contentType) {
            ContentType.Film -> {
                fileName = "filmGenre.txt"
                //language=PostgreSQL
                stateForInsert = "INSERT INTO db.film_genre (name, description) VALUES (?, ?);"
            }
            ContentType.Music -> {
                fileName = "musicGenre.txt"
                //language=PostgreSQL
                stateForInsert = "INSERT INTO db.music_genre (name, description) VALUES (?, ?);"
            }
            ContentType.Book -> {
                fileName = "bookGenre.txt"
                //language=PostgreSQL
                stateForInsert = "INSERT INTO db.book_genre (name, description) VALUES (?, ?);"
            }
        }
        val genreList = readingFile(fileName)

        // Формирование данных и запись в бд
        for (genre in genreList) {
            val temp = genre.split('%', limit = 2)
            val nameGenre = temp[0]
            val descGenre = temp.getOrNull(1)

            val statement = db.getConnect().prepareStatement(stateForInsert).apply {
                setString(1, nameGenre)
                if (descGenre == null || descGenre == "")
                    setNull(2, VARCHAR)
                else
                    setString(2, descGenre)
            }
            db.query(statement)
        }
    }

    /**  Функция получения из бд списка id жанров из соответствующей таблицы (film_genre, music_genre или book_genre)
     *  в зависимости от поданного ключа contentType **/
    private fun getGenresIdsByType(contentType: ContentType): MutableList<Int> {
        val listOfGenreIds = mutableListOf<Int>()

        @Language("PostgreSQL")
        val sqlQuery = when (contentType) {
            ContentType.Music -> "SELECT id FROM db.music_genre;"
            ContentType.Book -> "SELECT id FROM db.book_genre;"
            ContentType.Film -> "SELECT id FROM db.film_genre;"
        }
        // Заполнение списка id жанров
        db.query(db.getConnect().prepareStatement(sqlQuery)).onEach { listOfGenreIds.add(it["id"] as Int) }

        return listOfGenreIds
    }

    /**                                         ГЕНЕРАЦИЯ ПОЛЕЙ                                           **/

    /** Функция генерации имени и необходимого описания.
     *  Вход contentType:
     *      BOOK или FILM - стандартная генерация имени и описания
     *      MUSIC - генерация только (!!!) описания для artist
     *  Вход series:
     *      true  - при contentType = BOOK или FILM в описании речь пойдёт о серии книг или фильмов, описание может быть null.
     *              При contentType = MUSIC в описании речь пойдёт о группе, описание может быть null;
     *      false - в описании речь пойдёт о конкретном экземпляре книги или фильма (определяет contentType), описание
     *              будет != null. При contentType = MUSIC в описании речь пойдёт о соло исполнителе, описание может быть null;
     *  Вход descExist - влияет на наличие описания:
     *      по умолчанию - используется для определения наличия описания при series = true (при contentType = BOOK или FILM)
     *                     или вне зависимости от series (при contentType = MUSIC);
     *      true  - сгенерирует описание (series не влияет);
     *      false - не сгенерирует описание при series = true или сгенерирует при series = false.
     *  Возврат - пара (сгенерированное_название, сгенерированное_описание) **/
    private fun getNameAndDescByTypes(
        contentType: ContentType, series: Boolean,
        descExist: Boolean = Random.nextBoolean()
    ): Pair<String, String?> {
        val name: StringBuilder
        val desc: StringBuilder?

        if (contentType == ContentType.Music) {     // Если указан MUSIC, значит надо генерировать только описание для artist
            name = StringBuilder("")

            if (descExist)
                if (!series) {  // Если соло артист
                    desc = StringBuilder("Этот исполнитель является кумиром для многих людей. ")
                        .append(
                            if (Random.nextBoolean())
                                "Место его рождения - ${placeList.random()}. "
                            else
                                "Родился он местечке, известном под названием ${placeList.random()}. "
                        )
                        .append("Он записал ${Random.nextInt(5, 16)} успешных альбомов и прославился по всему миру.")
                } else {        // Если группа
                    desc = StringBuilder("Эта группа является любимой у многих людей. ")
                        .append(
                            if (Random.nextBoolean())
                                "Родина этой группы - ${placeList.random()}. "
                            else
                                "Образовалась эта группа в местечке, извсетном под названием ${placeList.random()}. "
                        )
                        .append("Они записали ${Random.nextInt(5, 16)} успешных альбомов и прославились по всему миру.")
                }
            else
                desc = null

        } else {    // Стандартная генерация
            // Генерация действующего объекта (героя)
            val noun = nounList.random()

            // Генерация названия
            name = StringBuilder(
                if (Random.nextBoolean())
                    "${adjectiveList.random()} "
                else
                    ""
            )

            if (Random.nextBoolean()) {     // Генерация названия типа: *Прилагательное?* *прилагательное?* *существительное*
                name.append(
                    if (Random.nextBoolean())
                        "${adjectiveList.random()} "
                    else
                        ""
                )
                    .append(noun)

            } else {            // Генерация названия типа: *Прилагательное?* *существительное* и/!/:/-/. *Прилагательное* *существительное*
                val sep = listOf(" и", ":", " -", "!", ".").random()

                name.append(
                    "${adjectiveList.random()} $noun$sep ${adjectiveList.random().capitalize()} ${
                        nounList.random()
                    }"
                )
            }

            // Генерация описания
            if (!series || descExist) {  // Если генерируется не для серии или если нарандомили описание
                val adj = adjectiveList.random()

                desc = StringBuilder(
                    when (contentType) {
                        ContentType.Book ->
                            if (series)
                                "Эта ${adj.substring(0, adj.length - 2)}ая серия книг"
                            else
                                "Эта ${adj.substring(0, adj.length - 2)}ая книга"

                        ContentType.Film ->
                            if (series)
                                "Эта ${adj.substring(0, adj.length - 2)}ая серия фильмов"
                            else
                                "Этот ${adj.substring(0, adj.length - 2)}ый фильм"

                        else -> ""
                    }
                )
                    .append(" расскажет увлекательную историю. ")
                    .append(
                        if (!series)
                            "Место действия - ${placeList.random()}. "
                        else
                            ""
                    )
                    .append(
                        "${noun.capitalize()} хочет ${adverbList.random()} ${verbList.random().toLowerCase()} и ${
                            verbList.random().toLowerCase()
                        }, как говорится, ${phraseologicalList.random().toLowerCase()}."
                    )
            } else
                desc = null
        }
        return Pair(name.toString().capitalize(), desc.toString())
    }

    /** Небольшая "надстройка" над функцией getNameAndDescByTypes() для упрощения. Вызывает getNameAndDescByTypes()
     * для генерации только названия (описание сгенерировано не будет). **/
    private fun getName() = getNameAndDescByTypes(ContentType.Book, true, descExist = false).first

    /** Небольшая "надстройка" над функцией getNameAndDescByTypes() для упрощения получения описания исполнителя.
     *  Вызывает getNameAndDescByTypes() для генерации только описания исполнителя (название сгенерировано не будет).**/
    private fun getDescArtist(type: Int) = getNameAndDescByTypes(ContentType.Music, type != 0).second

}
