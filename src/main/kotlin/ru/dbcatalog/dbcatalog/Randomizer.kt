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

    private val db = DB()

    private fun readingFile(name: String): List<String> {
        val inputStream = this::class.java.classLoader.getResourceAsStream(name)
        val list = mutableListOf<String>()
        inputStream!!.bufferedReader().forEachLine { list.add(it) }
        return list
    }

    private fun randomPeople(): Pair<Int, Int?> {    // возврат - (id, yearOfBirth)
        // Выбираем: true - берём из бд, false - генерим новый
        val exist = Random.nextBoolean()
        // Если берём из бд, получаем список доступных id людей
        val peopleIds = mutableListOf<Int>()
        if (exist)
            db.query(db.getConnect().prepareStatement("SELECT id FROM db.people;"))
                .onEach { peopleIds.add(it["id"] as Int) }

        val id: Int
        val yearOfBirth: Int?

        if (exist && peopleIds.isNotEmpty()) {  // Если мы берём из бд и таблица people не пустая
            // Выбираем id
            id = peopleIds.random()
            // Узнаём год рождения выбранного человека
            yearOfBirth = db.query(db.getConnect()
                .prepareStatement("SELECT year_of_birth FROM db.people WHERE id = ?;")
                .apply { setInt(1, id) })[0]["year_of_birth"] as Int?

        } else {    // Создание нового человека
            // Генерация имени человека
            val fullName = fullNameList.random()

            // Генерация года рождения человека
            val yearExist =
                !(Random.nextBoolean() && Random.nextBoolean() && Random.nextBoolean() && Random.nextBoolean())
            yearOfBirth = if (yearExist) Random.nextInt(1500, 2005) else null

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
        return Pair(id, yearOfBirth)
    }

    /* private fun randomPeopleFunction(): ru.db_catalog.db_catalog.PeopleFunction {
         // TODO: 15.04.2021 Реализовать генерацию объекта ru.db_catalog.db_catalog.PeopleFunction
         return ru.db_catalog.db_catalog.PeopleFunction("Programmer")
     }*/

    private fun randomMusicAlbum(): MusicAlbum {
        // TODO: 15.04.2021 Реализовать генерацию объекта ru.db_catalog.db_catalog.MusicAlbum
        return MusicAlbum("Imposter of us", 2020, null)
    }

    private fun randomFilmSeries(): FilmSeries {
        // TODO: 15.04.2021 Реализовать генерацию объекта ru.db_catalog.db_catalog.FilmSeries
        return FilmSeries("Harry Potter", null)
    }

    fun addFilm(): Int {
        // TODO: 15.04.2021 Реализовать генерацию объекта ru.db_catalog.db_catalog.Film
        /*return ru.db_catalog.db_catalog.Film(
            "Зеленый слоник",
            1999,
            5160,
            "Два младших офицера, сидя в одной камере на гауптвахте, вынуждены решать острые социальные и психологические вопросы в небольшом пространстве.",
            null,
            null,
            null
        )*/
        return 0
    }

    fun addMusic(): Int {
        // TODO: 15.04.2021 Реализовать генерацию объекта ru.db_catalog.db_catalog.Music
        //return ru.db_catalog.db_catalog.Music("Believer", 2017, 203)
        return 0
    }

    fun addTop(type: Int = Random.nextInt(0, 3)) {
        // type: значение по умолчанию - без разницы (выбирается рандомно); 0 - фильм, 1 - музыка, 2 - книга
        // Подготовка данных
        val noun: String
        val stateForIds: String             // statement для получения id в film/music/book
        val stateForInsertInoCross: String  // statement для записи в top_has_film / top_has_music / top_has_book

        when (type) {
            0 -> {
                noun = "фильмов"
                stateForIds = "SELECT id FROM db.film;"
                stateForInsertInoCross = "INSERT INTO db.top_has_film (top_id, film_id, position) VALUES (?, ?, ?);"
            }
            1 -> {
                noun = "песен"
                stateForIds = "SELECT id FROM db.music;"
                stateForInsertInoCross = "INSERT INTO db.top_has_music (top_id, music_id, position) VALUES (?, ?, ?);"
            }
            else -> {
                noun = "книг"
                stateForIds = "SELECT id FROM db.book;"
                stateForInsertInoCross = "INSERT INTO db.top_has_book (top_id, book_id, position) VALUES (?, ?, ?);"
            }
        }

        // Генерация размера топа
        var num = Random.nextInt(5, 101)

        // Генерация имени топа
        val adj = adjectiveList.random()
        val name = "Топ-$num " + adj.substring(0, adj.length - 2) + "ых " + noun + " " + forWhomList.random() +
                " (автор - " + fullNameList.random() + ")"

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

    fun addUser() {
//      Генерация username

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

//      Генерация password

        val alphabet = ('0'..'9').joinToString("") +
                ('a'..'z').joinToString("") +
                ('A'..'Z').joinToString("") +
                "!@#$%&"

        val stringBuilderForPassword = StringBuilder()

        while (stringBuilderForPassword.length < Random.nextInt(6, 32)) {
            stringBuilderForPassword.append(alphabet.random())
        }

        val password = stringBuilderForPassword.toString()

//      Генерация email

        val emailSamples = readingFile("forEmail.txt")

        val email = userName + emailSamples.random()

//      Генерация TimeStamp

        val timestamp = generateTimestamp()

//      Добавление в БД записи о пользователе

        @Language("PostgreSQL")
        val sqlQuery = "INSERT INTO db.user (username, password, email, create_time) VALUES (?, ?, ?, ?) RETURNING id;"
        val statementForAddUserEntry = db.getConnect().prepareStatement(sqlQuery).apply {
            setString(1, userName)
            setString(2, password)
            setString(3, email)
            setTimestamp(4, timestamp)
        }

        val userId = db.query(statementForAddUserEntry)[0]["id"] as Int

//      Генерация любимых жанров пользователя

//        generateUsersGenres(userId, ContentType.Music)
        generateUsersGenres(userId, ContentType.Book)
//        generateUsersGenres(userId, ContentType.Film)

//      Генерация просмотренного пользователем контента

//        generateUsersViewedContent(userId, ContentType.Music)
        generateUsersViewedContent(userId, ContentType.Book)
//        generateUsersViewedContent(userId, ContentType.Film)
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

//      Проверка наличия жанров в списке из БД
        if (listOfGenreIds.isEmpty()) {
            fillGenre(contentType)
            listOfGenreIds = getGenresIdsByType(contentType)
        }

//      Рандомное добавление жанров в БД
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

    private fun getGenresIdsByType(contentType: ContentType): MutableList<Int> {
        val listOfGenreIds = mutableListOf<Int>()

        @Language("PostgreSQL")
        val sqlQuery = when (contentType) {
            ContentType.Music -> "SELECT id FROM db.music_genre;"
            ContentType.Book -> "SELECT id FROM db.book_genre;"
            ContentType.Film -> "SELECT id FROM db.film_genre;"
        }
//      Заполнение списка id жанров
        db.query(db.getConnect().prepareStatement(sqlQuery)).onEach { listOfGenreIds.add(it["id"] as Int) }

        return listOfGenreIds
    }

    private fun fillGenre(contentType: ContentType) {  // type: 0 - фильм, 1 - музыка, else - книга
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

    fun addBook(): Int {     // возврат - id книги
        // Генерация действующего объекта (героя) книги
        val noun = nounList.random()

        // Генерация названия книги
        val bookName = (adjectiveList.random() + " " + adjectiveList.random() + " " + noun).capitalize()

        // Генерация описания книги
        val adj = adjectiveList.random()
        val bookDesc = "Эта " + adj.substring(0, adj.length - 2) + "ая книга расскажет увлекательную историю. " +
                (noun + " хочет " + adverbList.random() + " " + verbList.random() + ", как говорится, " +
                        phraseologicalList.random() + ".").toLowerCase().capitalize()

        // Генерация постера
        val posterExist = !(Random.nextBoolean() && Random.nextBoolean())
        val bookPoster = if (posterExist) "https://poster.com/" + Random.nextInt(100000, 1000000) else null

        // Генерация серии книг
        val bookSeriesExist = !(Random.nextBoolean() && Random.nextBoolean())
        val bookSeriesId = if (bookSeriesExist) randomBookSeries() else null

        // Генерация авторов книги
        val peopleNum = Random.nextInt(1, 4)    // Выбираем количество авторов: от 1 до 3
        val peopleIdList = mutableListOf<Int>()
        val peopleYearList = mutableListOf<Int?>()

        for (i in 1..peopleNum) {
            val people = randomPeople()
            peopleIdList.add(people.first)
            peopleYearList.add(people.second)
        }

        // Генерация года написания книги
        val maxYear = peopleYearList.maxByOrNull { it != null }
        val bookYear = if (maxYear != null)
            Random.nextInt(
                maxYear + 15,
                maxYear + 71
            )    // год написания книги: не раньше, чем в 15 лет, и не старше, чем в 70 (по самому младшему)
        else
            Random.nextInt(1500, 2021)

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
        val exist = Random.nextBoolean()    // true - берём из бд, false - генерим новый
        val empty =
            db.query(
                db.getConnect().prepareStatement("SELECT count(*) AS rowcount FROM db.book_series;")
            )[0]["rowcount"] as Long == 0L
        var id: Int

        if (exist && !empty) {
            val lastIdBS =
                db.query(db.getConnect().prepareStatement("SELECT max(id) FROM db.book_series;"))[0]["max"] as Int
            do {
                id = Random.nextInt(1, lastIdBS + 1)
            } while (!(db.query(
                    db.getConnect().prepareStatement("SELECT EXISTS( SELECT * FROM db.book_series WHERE id = ?);")
                        .apply { setInt(1, id) })[0]["exists"] as Boolean)
            )
        } else {
            // Генерация действующего объекта (героя) серии книг
            val noun = nounList.random()

            // Генерация названия серии книг
            val name = (adjectiveList.random() + " " + adjectiveList.random() + " " + noun).capitalize()

            // Генерация описания серии книг
            val descExist = !(Random.nextBoolean() && Random.nextBoolean() && Random.nextBoolean())
            val desc = if (descExist) {
                val adj = adjectiveList.random()

                "Эта " + adj.substring(0, adj.length - 2) + "ая серия книг расскажет увлекательную историю. " +
                        (noun + " хочет " + adverbList.random() + " " + verbList.random() + ", как говорится, " +
                                phraseologicalList.random() + ".").toLowerCase().capitalize()
            } else
                null

            // Запись серии книг в бд (таблица book_series)
            val statement =
                db.getConnect()
                    .prepareStatement("INSERT INTO db.book_series (name, description) VALUES (?, ?) RETURNING id;")
                    .apply {
                        setString(1, name)
                        if (descExist)
                            setString(2, desc)
                        else
                            setNull(2, VARCHAR)


                    }
            id =
                db.query(statement)[0]["id"] as Int
        }
        return id
    }
}