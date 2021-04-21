package ru.dbcatalog.dbcatalog

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
        val musicAlbumId: Int?
        val musicYear: Int
        val musicAlbumExist = !(Random.nextBoolean() && Random.nextBoolean())

        if (musicAlbumExist) {
            val musicAlbumPair = randomSeriesByType(ContentType.Music)
            musicAlbumId = musicAlbumPair.first
            musicYear = musicAlbumPair.second!!
        } else {
            musicAlbumId = null
            musicYear = Random.nextInt(1500, 2022)
        }

        // Запись песни в бд (таблица Music) и получение её id
        @Language("PostgreSQL")
        var query = "INSERT INTO db.music (name, year, duration) VALUES (?, ?, ?) RETURNING id;"
        val musicId = db.query(db.getConnect().prepareStatement(query).apply {
            setString(1, name)
            setInt(2, musicYear)
            setInt(3, duration)
        })[0]["id"] as Int

        // TODO: Где работа с таблицей music_has_album?

        // Генерация исполнителей   // TODO: как-нибудь упростить? Например, во всех ветках происходит работа с music_has_artist
        when (Random.nextInt(0, 3)) { //0 - соло, 1 - группа, 2 - несколько исполнителей
            0 -> {
                val peopleId = randomPeople(musicYear)
                val soloArtistId = randomArtist(0)
                // TODO: Игорь заполняет таблицу music_has_artist. Данные - musicId, soloArtistId
                @Language("PostgreSQL")
                query = "INSERT INTO db.music_has_artist (music_id, artist_id) VALUES (?, ?);"
                db.query(db.getConnect().prepareStatement(query).apply {
                    setInt(1, musicId)
                    setInt(2, soloArtistId)
                })
                // TODO: Игорь заполняет таблицу artist_is_people. Данные - soloArtistId, peopleId
                @Language("PostgreSQL")
                query = "INSERT INTO db.artist_is_people (artist_id, people_id) VALUES (?, ?);"
                db.query(db.getConnect().prepareStatement(query).apply {
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
                query = "INSERT INTO db.artist_has_artist (artist_group_id, artist_id) VALUES (?, ?);"
                db.query(db.getConnect().prepareStatement(query).apply {
                    setInt(1, groupId)
//                    setInt(2, )
                })
                // TODO: Игорь заполняет таблицу music_has_artist. Данные - musicId, groupId
                @Language("PostgreSQL")
                query = "INSERT INTO db.music_has_artist (music_id, artist_id) VALUES (?, ?);"
                db.query(db.getConnect().prepareStatement(query).apply {
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
                    query = "INSERT INTO db.artist_is_people (artist_id, people_id) VALUES (?, ?);"
                    db.query(db.getConnect().prepareStatement(query).apply {
                        setInt(1, artistId)
                        setInt(2, peopleId)
                    })
                    artists.add(artistId)
                    artistNumber--
                }
                // TODO: Игорь заполняет таблицу music_has_artist. Данные - musicId, artistId (из artists)
                @Language("PostgreSQL")
                query = "INSERT INTO db.music_has_artist (music_id, artist_id) VALUES (?, ?);"
                db.query(db.getConnect().prepareStatement(query).apply {
                    setInt(1, musicId)
//                    setInt(2, art)
                })
            }
        }

        // Генерация id жанра для песни     // TODO: У песни может быть несколько жанров
        val genreIds = getGenresIdsByType(ContentType.Music)    // список id жанров
        val genreId = genreIds.random()

        // Заполнение кросс-таблицы music_has_music_genre
        @Language("PostgreSQL")
        query = "INSERT INTO db.music_has_music_genre (music_id, music_genre_id) VALUES (?, ?);"
        db.query(db.getConnect().prepareStatement(query).apply {
            setInt(1, musicId)
            setInt(1, genreId)
        })

        return musicId
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

        // Добавление записи об артисте в бд (иаблица artist) и получение её id
        @Language("PostgreSQL")
        val query =
            db.getConnect().prepareStatement("INSERT INTO db.artist (name, description) VALUES (?, ?) RETURNING id;")
                .apply {
                    setString(1, name)
                    if (descExist)
                        setString(2, desc)
                    else
                        setNull(2, VARCHAR)
                }

        return db.query(query)[0]["id"] as Int
    }

    /** =================================    ФИЛЬМ И ВСЁ, ЧТО С НИМ СВЯЗАНО    ================================ **/

    fun addFilm(): Int {
        // Генерация названия и описания фильма
        val nameAndDesc = getNameAndDescByTypes(ContentType.Film, false)
        val name = nameAndDesc.first
        val desc = nameAndDesc.second!!

        // Генерация постера
        val posterExist = !(Random.nextBoolean() && Random.nextBoolean())
        val poster = if (posterExist) "https://poster.com/${Random.nextInt(100000, 1000000)}" else null

        // Генерация продолжительности фильма
        val duration = Random.nextInt(40, 270)

        // Генерация серии фильмов
        val filmSeriesExist = !(Random.nextBoolean() && Random.nextBoolean())
        val filmSeriesId = if (filmSeriesExist) randomSeriesByType(ContentType.Film).first else null

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

        // Добавление записи о фильме в бд (таблица film) и получение его id
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
        })[0]["id"] as Int

        // Генерация музыки
        val musicExist = !(Random.nextBoolean() && Random.nextBoolean())
        if (musicExist) {
            var numOfMusic = Random.nextInt()

            while (numOfMusic != 0) {
                val musicId = addMusic()    // TODO: было написано musicId = addBook(). Корректно ли исправление?

                // Заполнение кросс-таблицы film_has_music
                @Language("PostgreSQL")
                val query = "INSERT INTO db.film_has_music (film_id, music_id) VALUES (?, ?);"
                db.query(db.getConnect().prepareStatement(query).apply {
                    setInt(1, filmId)
                    setInt(1, musicId)
                })
                numOfMusic--
            }
        }

        // Получение списка существующих id в таблице people_function
        val funcIds = mutableListOf<Int>()
        db.query(db.getConnect().prepareStatement("SELECT id FROM db.people_function;"))
            .onEach { funcIds.add(it["id"] as Int) }

        // Если таблица с профессиями пустая, то заполняем её и повторно заполняем список
        if (funcIds.isEmpty()) {
            fillPeopleFunction()
            db.query(db.getConnect().prepareStatement("SELECT id FROM db.people_function;"))
                .onEach { funcIds.add(it["id"] as Int) }
        }

        for (peopleIdInGroup in group) {
            val funcId = funcIds.random()

            // Заполнение кросс-таблицы film_has_people
            // TODO: Игорь заполняет таблицу film_has_people. Данные - filmId, peopleId (из group), funcId
            @Language("PostgreSQL")
            val query = "INSERT INTO db.film_has_people (film_id, people_id, people_function_id) VALUES (?, ?, ?);"
            db.query(db.getConnect().prepareStatement(query).apply {
                setInt(1, filmId)
                setInt(2, peopleIdInGroup)  // TODO: это ли подразумевалось под peopleId (из group)?
                setInt(3, funcId)
            })

        }

        // Генерация id жанра для фильма   // TODO: У фильма может быть несколько жанров
        val genreIds = getGenresIdsByType(ContentType.Film)
        val genreId = genreIds.random()

        // Заполнение кросс-таблицы film_has_film_genre
        @Language("PostgreSQL")
        val query = "INSERT INTO db.film_has_film_genre (film_id, film_genre_id) VALUES (?, ?);"
        db.query(db.getConnect().prepareStatement(query).apply {
            setInt(1, filmId)
            setInt(1, genreId)
        })

        return filmId
    }

    private fun fillPeopleFunction() {
        val functionList = readingFile("functionList.txt")

        @Language("PostgreSQL")
        val query = "INSERT INTO db.people_function (name) VALUES (?);"

        for (funcName in functionList)
            db.query(db.getConnect().prepareStatement(query).apply {
                setString(1, funcName)
            })
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
        val bookSeriesId = if (bookSeriesExist) randomSeriesByType(ContentType.Book).first else null

        // Генерация года написания книги
        val bookYear = Random.nextInt(1500, 2021)

        // Запись книги в бд (таблица Book)
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
        val genreIds = getGenresIdsByType(ContentType.Book)   // список id жанров

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

    /** =================================    ТОП И ВСЁ, ЧТО С НИМ СВЯЗАНО    ================================ **/

    fun addTop(type: Int = Random.nextInt(0, 3)) {
        // type: значение по умолчанию - без разницы (выбирается рандомно); 0 - фильм, 1 - музыка, 2 - книга
        // Подготовка данных
        val noun: String
        val table: String             // таблица для общения с бд

        when (type) {
            0 -> {
                noun = "фильмов"
                table = "film"
            }
            1 -> {
                noun = "песен"
                table = "music"
            }
            else -> {
                noun = "книг"
                table = "book"
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
        db.query(db.getConnect().prepareStatement("SELECT id FROM db.$table;"))
            .onEach { availableIds.add(it["id"] as Int) }

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
                    .prepareStatement("INSERT INTO db.top_has_$table (top_id, ${table}_id, position) VALUES (?, ?, ?);")
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

            val table = when (contentType) {
                ContentType.Music -> "music"
                ContentType.Book -> "book"
                ContentType.Film -> "film"
            }

            val randomId = listOfContentIds.random()
            listOfContentIds.remove(randomId)

            db.query(
                db.getConnect()
                    .prepareStatement("INSERT INTO db.user_viewed_$table (user_id, ${table}_id, rating, time) VALUES (?, ?, ?, ?);")
                    .apply {
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

        val table = when (contentType) {
            ContentType.Music -> "db.music"
            ContentType.Book -> "db.book"
            ContentType.Film -> "db.film"
        }
        db.query(db.getConnect().prepareStatement("SELECT id FROM $table;"))
            .onEach { listOfContentIds.add(it["id"] as Int) }

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

            val table = when (contentType) {
                ContentType.Music -> "music_genre"
                ContentType.Book -> "book_genre"
                ContentType.Film -> "film_genre"
            }

            val randomId = listOfGenreIds.random()
            listOfGenreIds.remove(randomId)

            db.query(
                db.getConnect()
                    .prepareStatement("INSERT INTO db.user_likes_$table (user_id, ${table}_id) VALUES (?, ?);").apply {
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
                    .prepareStatement("SELECT id FROM db.people WHERE ? <= year_of_birth AND year_of_birth <= ?;")
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
        val table: String
        when (contentType) {
            ContentType.Film -> {
                fileName = "filmGenre.txt"
                table = "db.film_genre"
            }
            ContentType.Music -> {
                fileName = "musicGenre.txt"
                table = "db.music_genre"
            }
            ContentType.Book -> {
                fileName = "bookGenre.txt"
                table = "db.book_genre"
            }
        }
        val genreList = readingFile(fileName)

        // Формирование данных и запись в бд
        for (genre in genreList) {
            val temp = genre.split('%', limit = 2)
            val nameGenre = temp[0]
            val descGenre = temp.getOrNull(1)

            val statement =
                db.getConnect().prepareStatement("INSERT INTO $table (name, description) VALUES (?, ?);")
                    .apply {
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

        val table = when (contentType) {
            ContentType.Music -> "db.music_genre"
            ContentType.Book -> "db.book_genre"
            ContentType.Film -> "db.film_genre"
        }
        // Заполнение списка id жанров
        db.query(db.getConnect().prepareStatement("SELECT id FROM $table;"))
            .onEach { listOfGenreIds.add(it["id"] as Int) }

        // Если соответствующая таблица в бд не заполнена, заполняем её и список
        if (listOfGenreIds.isEmpty()) {
            fillGenre(contentType)
            db.query(db.getConnect().prepareStatement("SELECT id FROM $table;"))
                .onEach { listOfGenreIds.add(it["id"] as Int) }
        }

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


    /**                                         ГЕНЕРАЦИЯ СЕРИИ (АЛЬБОМА)                                    **/

    /** Функция генерации серии (альбома) фильмов/книг/музыки и её занесения в соответствующую таблицу в бд в
     *  зависимости от поданного ключа contentType.
     *  Вход contentType:
     *      BOOK или FILM - генерация и запись в бд соответствующей серии. На выходе - пара (id, null);
     *      MUSIC - генерация и запись в бд музыкального альбома. На выходе - пара (id, year).
     *  Возврат - пара (id_сгенерированной_серии, null / год_музыкального_альбома). **/
    private fun randomSeriesByType(contentType: ContentType): Pair<Int, Int?> {   // возврат - id интересующей серии
        val table = when (contentType) {
            ContentType.Film -> "db.film_series"
            ContentType.Book -> "db.book_series"
            ContentType.Music -> "db.music_album"
        }

        // Выбор: true - берём из бд, false - генерим новый
        val exist = Random.nextBoolean()
        val id: Int
        val yearOfAlbum: Int?
        val seriesIds = mutableListOf<Int>()

        // Если берём из бд, заполняем список id существующих серий из соответствующей таблицы
        if (exist)
            db.query(db.getConnect().prepareStatement("SELECT id FROM $table;"))
                .onEach { seriesIds.add(it["id"] as Int) }

        if (exist && seriesIds.isNotEmpty()) {    // Если мы берём из бд и соответстующая таблица не пустая
            // Выбор id из существующих
            id = seriesIds.random()
            // Получение из бд года альбома, если создаётся альбом, иначе null
            yearOfAlbum = if (contentType == ContentType.Music)
                db.query(db.getConnect().prepareStatement("SELECT year FROM $table WHERE id = ?").apply {
                    setInt(1, id)
                })[0]["year"] as Int
            else null
        } else {  // Создание новой серии
            // Генерация названия и описания серии
            val nameAndDesc = when (contentType) {
                ContentType.Film -> getNameAndDescByTypes(ContentType.Film, true)
                ContentType.Book -> getNameAndDescByTypes(ContentType.Book, true)
                ContentType.Music -> Pair(getName(), null)
            }
            val name = nameAndDesc.first
            val desc = nameAndDesc.second
            val poster: String?

            if (contentType == ContentType.Music) {
                // Генерация постера
                val posterExist = !(Random.nextBoolean() && Random.nextBoolean())
                poster = if (posterExist) "https://poster.com/${Random.nextInt(100000, 1000000)}" else null
                // Генерация года создания альбома
                yearOfAlbum = Random.nextInt(1500, 2022)
            } else {
                poster = null
                yearOfAlbum = null
            }

            // Запись серии в бд (соответствующая таблица)
            val statement = if (contentType != ContentType.Music) {
                db.getConnect().prepareStatement("INSERT INTO $table (name, description) VALUES (?, ?) RETURNING id;")
                    .apply {
                        setString(1, name)
                        if (desc != null)
                            setString(2, desc)
                        else
                            setNull(2, VARCHAR)
                    }
            } else {
                db.getConnect()
                    .prepareStatement("INSERT INTO $table (name, year, poster) VALUES (?, ?, ?) RETURNING id;")
                    .apply {
                        setString(1, name)
                        setInt(2, yearOfAlbum!!)
                        if (poster != null)
                            setString(3, poster)
                        else
                            setNull(3, VARCHAR)
                    }
            }

            id = db.query(statement)[0]["id"] as Int
        }
        return Pair(id, yearOfAlbum)
    }

}
