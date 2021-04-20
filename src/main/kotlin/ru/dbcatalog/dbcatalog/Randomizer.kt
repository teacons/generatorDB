package ru.dbcatalog.dbcatalog

import java.io.File
import java.io.InputStream
import java.sql.Timestamp
import java.util.*
import kotlin.random.Random

class Randomizer {

    private val adjectiveList = readingFile("adjectiveList.txt")
    private val nounList = readingFile("nounList.txt")
    private val adverbList = readingFile("adverbList.txt")
    private val verbList = readingFile("verbList.txt")
    private val phraseologicalList = readingFile("phraseologicalList.txt")
    private val fullNameList = readingFile("fullNameList.txt")
    private val forWhomList = readingFile("topForWhomList.txt")
    private val separatorList = readingFile("separatorList.txt")
    private val placeList = readingFile("placeList.txt")
    private val soloArtistList = readingFile("soloArtistList.txt")
    private val groupNameList = readingFile("groupNameList.txt")

    private fun readingFile(name: String): List<String> {
        val uri = this.javaClass.getResource("/$name").toURI()
        val inputStream: InputStream = File(uri).inputStream()
        val list = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { list.add(it) }
        return list
    }

    private fun randomPeople(year: Int): Pair<Int, Int?> {    // возврат - (id, yearOfBirth)
        var from = 1500
        var until = 2005
        if (year != -1) {
            from = year - 80
            until = year - 15
        }

        val exist = Random.nextBoolean()    // true - берём из бд, false - генерим новый
        val empty = false           // TODO: Игорь выдирает из бд, пустая ли таблица people
        val id: Int
        val yearOfBirth: Int?

        if (exist && !empty) {
            val lastIdPeople = 10   // TODO: Игорь выдирает последний id из бд (таблица people)
            id = Random.nextInt(lastIdPeople + 1)
            yearOfBirth = from      // TODO: Игорь выдирает year_of_birth из бд (таблица people, выбранный id)
        } else {
            // Генерация имени человека
            val fullName = fullNameList.random()

            // Генерация года рождения человека
            val yearExist =
                !(Random.nextBoolean() && Random.nextBoolean() && Random.nextBoolean() && Random.nextBoolean())
            yearOfBirth = if (yearExist) Random.nextInt(from, until) else null

            // Запись человека в бд (таблица people)
            val people = People(fullName, yearOfBirth)  // TODO: Игорь добавляет запись в бд (таблица people)
            id = 0                  // TODO: Игорь выдирает из бд id добавленного человека
        }
        return Pair(id, yearOfBirth)
    }

    private fun fillPeopleFunction() {
        val functionList = readingFile("functionList.txt")
        for (funcName in functionList) {
            // TODO: Игорь добавляет funcName в бд (таблица people_function)
        }
     }

    private fun randomMusicAlbum(): Pair<Int, Int> {
        val exist = Random.nextBoolean()    // true - берём из бд, false - генерим новый
        val empty = false       // TODO: Игорь выдирает из бд, пустая ли таблица music_album
        val id: Int
        var yearOfAlbum: Int

        if (exist && !empty) {
            val lastIdBS = 10   // TODO: Игорь выдирает последний id из бд (таблица music_album)
            id = Random.nextInt(lastIdBS + 1)
            yearOfAlbum = 1500      // TODO: Игорь выдирает year_of_album из бд (таблица music_album, выбранный id)
        } else {
            // Генерация объекта песен альбома
            val noun = nounList.random()
            val withAdj = Random.nextBoolean()

            // Генерация названия альбома
            val name = if (withAdj) (adjectiveList.random() + " " + noun).capitalize()
                       else noun.capitalize()

            // Генерация постера
            val posterExist = !(Random.nextBoolean() && Random.nextBoolean())
            val poster = if (posterExist) "https://poster.com/" + Random.nextInt(100000, 1000000) else null

            // Генерация года создания альбома
            yearOfAlbum = Random.nextInt(1500, 2022)

            // Запись альбома в бд (таблица music_album)
            val musicAlbum = MusicAlbum(name, yearOfAlbum, poster)     // TODO: Игорь добавляет запись в бд (таблица music_album)
            id = 0              // TODO: Игорь выдирает из бд id добавленного альбома
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
        val desc = if (descExist) {
            when (type) {
                0 -> {"Этот исполнитель является кумиром для многих людей. " +
                        "Место рождения исполнителя " + name + " - " + placeList.random() + ". " +
                        "Он записал " + Random.nextInt(5, 16) +
                        " успешных альбомов и прославился по всему миру."}
                else -> {"Эта группа является любимой у многих людей. " +
                        "Родина" + name + " - это " + placeList.random() + ". " +
                        "Они записали " + Random.nextInt(5, 16) +
                        " успешных альбомов и прославились по всему миру"}
            }
        }
        else null

        // TODO: Игорь добавляет запись в бд (таблица artist)
        val id = 0  // TODO: Игорь выдирает из бд id добавленного артиста

        return id
    }

    private fun randomFilmSeries(): Int {
        val exist = Random.nextBoolean()    // true - берём из бд, false - генерим новый
        val empty = false       // TODO: Игорь выдирает из бд, пустая ли таблица film_series
        val id: Int

        if (exist && !empty) {
            val lastIdBS = 10   // TODO: Игорь выдирает последний id из бд (таблица film_series)
            id = Random.nextInt(lastIdBS + 1)
        } else {
            // Генерация действующего объекта (героя) серии фильмов
            val noun = nounList.random()

            //Прил+Сущ и/:/-/!/. прил+сущ
            // Генерация названия серии фильмов
            val sep = separatorList.random()
            val name = (adjectiveList.random() + " " + noun).capitalize() + sep + " " +
                    if (sep.trim().compareTo("!") == 0 || sep.trim()
                            .compareTo(".") == 0
                    ) (adjectiveList.random() + " " + nounList.random()).capitalize()
                    else (adjectiveList.random() + " " + nounList.random()).toLowerCase()

            // Генерация описания серии фильмов
            val descExist = !(Random.nextBoolean() && Random.nextBoolean() && Random.nextBoolean())
            val desc = if (descExist) {
                val adj = adjectiveList.random()
                "Эта " + adj.substring(0, adj.length - 2) + "ая серия фильмов повествует увлекательную историю. " +
                        (noun + " хочет " + adverbList.random() + " " + verbList.random() + "и " + verbList.random() +
                                ", как говорится, " + phraseologicalList.random() + ".").toLowerCase().capitalize()
            } else
                null

            // Запись серии фильмов в бд (таблица film_series)
            val filmSeries = FilmSeries(name, desc)     // TODO: Игорь добавляет запись в бд (таблица film_series)
            id = 0              // TODO: Игорь выдирает из бд id добавленной серии фильмов
        }
        return id
    }

    fun addFilm(): Int {
        // Генерация действующего объекта (героя) фильма
        val noun = nounList.random()

        //Прил+Сущ и/:/-/!/. прил+сущ
        // Генерация названия фильма
        val sep = separatorList.random()
        val name = (adjectiveList.random() + " " + noun).capitalize() + sep +" "+
                if (sep.trim().compareTo("!") == 0 || sep.trim()
                        .compareTo(".") == 0
                ) (adjectiveList.random() + " " + nounList.random()).capitalize()
                else (adjectiveList.random() + " " + nounList.random()).toLowerCase()

        // Генерация описания фильма
        val desc = "Этот " + adjectiveList.random() + " фильм повествует увлекательную историю. " +
                "Место действия данного фильма - " + placeList.random() + ". " +
                (noun + " хочет " + adverbList.random() + " " + verbList.random() + " и " + verbList.random() +
                        ", как говорится, " + phraseologicalList.random() + ".").toLowerCase().capitalize()

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

        // Генерация музыки
        val musicExist = !(Random.nextBoolean() && Random.nextBoolean())
        if (musicExist) {
            var numOfMusic = Random.nextInt()
            while (numOfMusic != 0){
                val musicId = addBook()
                // TODO: Игорь заполняет таблицу film_has_music. Данные - filmId, musicId
                numOfMusic--
            }
        } else null

        // Генерация года создания фильма
        val filmYear = Random.nextInt(1895, 2022)

        //Генерация съёмочной группы
        val group = mutableListOf<Int>()
        var groupNumber = Random.nextInt(8, 24)
        while (groupNumber != 0){
            val people = randomPeople(filmYear)
            val peopleId = people.first
            group.add(peopleId)
            groupNumber--
        }

        // Запись фильма в бд (таблица Film)
        val film = Film(name, filmYear, duration, desc, poster, filmSeriesId, bookId) // TODO: Игорь добавляет запись в бд (таблица film)
        val filmId = 0  // TODO: Игорь выдирает из бд id добавленного фильма

        // Заполнение кросс-таблицы film_has_people
        val funcEmpty = false // TODO: Игорь выдирает из бд, пустая ли таблица people_function
        if (funcEmpty)     // Если таблица с профессиями пустая, то заполняем её
            fillPeopleFunction()
        val lastIdFunc = 10 // TODO: Игорь выдирает из бд последний id профессии (таблица people_function)
        groupNumber = group.size
        while (groupNumber != 0) {
            val funcId = Random.nextInt(lastIdFunc + 1)
            // TODO: Игорь заполняет таблицу film_has_people. Данные - filmId, peopleId (из group), funcId
            groupNumber--
        }

        // Заполнение кросс-таблицы film_has_film_genre
        val genreEmpty = false  // TODO: Игорь выдирает из бд, пустая ли таблица film_genre
        if (genreEmpty)     // Если таблица с жанрами пустая, то заполняем её
            fillGenre(0)
        val lastIdGenre = 10 // TODO: Игорь выдирает из бд последний id жанра фильмов (таблица film_genre)
        val genreId = Random.nextInt(lastIdGenre + 1)
        // TODO: Игорь заполняет таблицу film_has_film_genre. Данные - filmId, genreId

        return filmId
    }


    fun addMusic(): Int {
        // Генерация объекта песни
        val noun = nounList.random()

        // Генерация названия песни
        val name = (adjectiveList.random() + " " + adjectiveList.random() + " " + noun).capitalize()

        // Генерация продолжительности песни
        val duration = Random.nextInt(2, 15)

        // Генерация альбома и года для песни
        var musicAlbumId:Int?
        var musicYear:Int
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
        val musicId = 0  // TODO: Игорь выдирает из бд id добавленной песни

        // Генерация исполнителей
        when (Random.nextInt(0,3)) { //0 - соло, 1 - группа, 2 - несколько исполнителей
            0 -> {
                val people = randomPeople(musicYear)
                val peopleId = people.first
                val soloArtistId = randomArtist(0)
                // TODO: Игорь заполняет таблицу music_has_artist. Данные - musicId, soloArtistId
                // TODO: Игорь заполняет таблицу artist_is_people. Данные - soloArtistId, peopleId
            }
            1 -> {
                val groupId = randomArtist(1)
                val group = mutableListOf<Int>()
                var groupNumber = Random.nextInt(2, 21)
                while (groupNumber != 0) {
                    val people = randomPeople(musicYear)
                    val peopleId = people.first
                    val memberId = randomArtist(0)
                    group.add(memberId)
                    groupNumber--
                }
                // TODO: Игорь заполняет таблицу artist_has_artist. Данные - groupId, memberId (из group)
                // TODO: Игорь заполняет таблицу music_has_artist. Данные - musicId, groupId
            }
            else -> {
                var artistNumber = Random.nextInt(2, 6)
                val artists = mutableListOf<Int>()
                while (artistNumber != 0) {
                    val people = randomPeople(musicYear)
                    val peopleId = people.first
                    val artistId = randomArtist(0)
                    // TODO: Игорь заполняет таблицу artist_is_people. Данные - artistId, peopleId
                    artists.add(artistId)
                    artistNumber--
                }
                // TODO: Игорь заполняет таблицу music_has_artist. Данные - musicId, artistId (из artists)
            }
        }

        // Заполнение кросс-таблицы music_has_music_genre
        val genreEmpty = false  // TODO: Игорь выдирает из бд, пустая ли таблица music_genre
        if (genreEmpty)     // Если таблица с жанрами пустая, то заполняем её
            fillGenre(1)
        val lastIdGenre = 10 // TODO: Игорь выдирает из бд последний id жанра музыки (таблица music_genre)
        val genreId = Random.nextInt(lastIdGenre + 1)
        // TODO: Игорь заполняет таблицу music_has_music_genre. Данные - musicId, genreId

        return musicId
    }

    fun addTop(type: Int = Random.nextInt(0, 3)) { // type: значение по умолчанию - без разницы (выбирается рандомно); 0 - фильм, 1 - музыка, 2 - книга
        // Подготовка данных
        val noun: String
        val lastId: Int
        val numId: Int

        when (type) {
            0 -> {
                noun = "фильмов"
                lastId = 10     // TODO: Игорь выдирает из бд последний id (таблица film)
                numId = 10      // TODO: Игорь выдирает из бд количество записей в таблице film
            }
            1 -> {
                noun = "песен"
                lastId = 10     // TODO: Игорь выдирает из бд последний id (таблица music)
                numId = 10      // TODO: Игорь выдирает из бд количество записей в таблице music
            }
            else -> {
                noun = "книг"
                lastId = 10     // TODO: Игорь выдирает из бд последний id (таблица book)
                numId = 10      // TODO: Игорь выдирает из бд количество записей в таблице book
            }
        }

        // Генерация размера топа
        var num = Random.nextInt(5, 101)

        // Генерация имени топа
        val adj = adjectiveList.random()
        //val filmList = listOf("IMDb", "Киноафиши", "сайта kinonews.ru", "Кинопоиска", "Netflix", "сайта MyShows", "IVI", "YouTube")
        val name = "Топ-$num " + adj.substring(0, adj.length - 2) + "ых " + noun + " " + forWhomList.random() +
                " (автор - " + fullNameList.random() + ")"

        // Запись топа в бд (таблица top)
        val top = Top(name)     // TODO: Игорь добавляет запись в бд (таблица top)
        val topId = 0           // TODO: Игорь выдирает из бд id добавленного топа

        // Заполнение кросс-таблиц
        val usedId = mutableListOf<Int>()

        while (num != 0) {
            // Определение id элемента
            val elId = if (usedId.size >= numId)
                when (type) {
                    0 -> addFilm()
                    1 -> addMusic()
                    else -> addBook()
                }
            else
                 Random.nextInt(0, lastId + 1)

            // Запись в нужную таблицу в бд
            val check: Boolean
            when (type) {
                0 -> {
                    if (true) {         // TODO: Игорь проверяет, что выбранный id есть в таблице top_has_film
                        // TODO: Игорь добавляет запись в бд (таблица top_has_film). Данные - topId, elId, place
                        check = true    // TODO: Игорь заполняет индикатор (true - запись добавлена, false - нет)
                        TODO()
                    } else
                        check = false
                }
                1 -> {
                    if (true) {         // TODO: Игорь проверяет, что выбранный id есть в таблице top_has_music
                        // TODO: Игорь добавляет запись в бд (таблица top_has_music). Данные - topId, elId, place
                        check = true    // TODO: Игорь заполняет индикатор (true - запись добавлена, false - нет)
                        TODO()
                    } else
                        check = false
                }
                else -> {
                    if (true) {         // TODO: Игорь проверяет, что выбранный id есть в таблице top_has_book
                        // TODO: Игорь добавляет запись в бд (таблица top_has_book). Данные - topId, elId, place
                        check = true    // TODO: Игорь заполняет индикатор (true - запись добавлена, false - нет)
                        TODO()
                    } else
                        check = false
                }
            }

            // Проверка корректного добавления записи в бд
            if (check) {
                usedId.add(elId)
                num--
            }
        }
    }

    fun addUser(): User {
        // TODO: 15.04.2021 Реализовать генерацию объекта User
        return User("Teacons", "123321", "kek@gmail.com", Timestamp(Calendar.getInstance().timeInMillis))
    }

    fun fillGenre(type: Int) {  // type: 0 - фильм, 1 - музыка, 2 - книга
        // Определение нужного файла
        val fileName = when (type) {
            0 -> "filmGenre.txt"
            1 -> "musicGenre.txt"
            else -> "bookGenre.txt"
        }
        val genreList = readingFile(fileName)

        // Формирование данных и запись в бд
        for (genre in genreList) {
            val temp = genre.split('%', limit = 2)
            val nameGenre = temp[0]
            val descGenre = temp[1]

            when (type) {
                0 -> {
                    // TODO: Игорь добавляет запись в бд (таблица film_genre). Данные - nameGenre, descGenre.
                }
                1 -> {
                    // TODO: Игорь добавляет запись в бд (таблица music_genre). Данные - nameGenre, descGenre.
                }
                else -> {
                    // TODO: Игорь добавляет запись в бд (таблица book_genre). Данные - nameGenre, descGenre.
                }
            }
        }
    }

    fun addBook(): Int {     // возврат - id книги
        // Генерация действующего объекта (героя) книги
        val noun = nounList.random()

        // Генерация названия книги
        val name = (adjectiveList.random() + " " + adjectiveList.random() + " " + noun).capitalize()

        // Генерация описания книги
        val adj = adjectiveList.random()
        val desc = "Эта " + adj.substring(0, adj.length - 2) + "ая книга расскажет увлекательную историю. " +
                (noun + " хочет " + adverbList.random() + " " + verbList.random() + ", как говорится, " +
                phraseologicalList.random() + ".").toLowerCase().capitalize()

        // Генерация постера
        val posterExist = !(Random.nextBoolean() && Random.nextBoolean())
        val poster = if (posterExist) "https://poster.com/" + Random.nextInt(100000, 1000000) else null

        // Генерация серии книг
        val bookSeriesExist = !(Random.nextBoolean() && Random.nextBoolean())
        val bookSeriesId = if (bookSeriesExist) randomBookSeries() else null

        // Генерация автора книги
        val people = randomPeople(-1)
        val peopleId = people.first
        val peopleYear = people.second

        // Генерация года написания книги
        val year = if (peopleYear != null)
            Random.nextInt(peopleYear + 15, peopleYear + 70)    // год написания книги: не раньше, чем в 15 лет, и не старше, чем в 70
        else
            Random.nextInt(1500, 2021)

        // Запись книги в бд (таблица Book)
        val book = Book(name, year, desc, poster, bookSeriesId) // TODO: Игорь добавляет запись в бд (таблица book)
        val bookId = 0  // TODO: Игорь выдирает из бд id добавленной книги

        // Заполнение кросс-таблицы book_has_people
        // TODO: Игорь заполняет таблицу book_has_people. Данные - peopleId, bookId

        // Заполнение кросс-таблицы book_has_book_genre
        val genreEmpty = false  // TODO: Игорь выдирает из бд, пустая ли таблица book_genre
        if (genreEmpty)     // Если таблица с жанрами пустая, то заполняем её
            fillGenre(2)
        val lastIdGenre = 10 // TODO: Игорь выдирает из бд последний id жанра книг (таблица book_genre)
        val genreId = Random.nextInt(lastIdGenre + 1)
        // TODO: Игорь заполняет таблицу book_has_book_genre. Данные - bookId, genreId

        return bookId
    }

    private fun randomBookSeries(): Int {   // возврат - id серии книг
        val exist = Random.nextBoolean()    // true - берём из бд, false - генерим новый
        val empty = false       // TODO: Игорь выдирает из бд, пустая ли таблица book_series
        val id: Int

        if (exist && !empty) {
            val lastIdBS = 10   // TODO: Игорь выдирает последний id из бд (таблица book_series)
            id = Random.nextInt(lastIdBS + 1)
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
            val bookSeries = BookSeries(name, desc)     // TODO: Игорь добавляет запись в бд (таблица book_series)
            id = 0              // TODO: Игорь выдирает из бд id добавленной серии книг
        }
        return id
    }
}
