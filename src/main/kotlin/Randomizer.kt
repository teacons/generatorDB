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

    private fun readingFile(name: String): List<String> {
        val uri = this.javaClass.getResource("/$name").toURI()
        val inputStream: InputStream = File(uri).inputStream()
        val list = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { list.add(it) }
        return list
    }

    private fun randomPeople(): Pair<Int, Int?> {    // возврат - (id, yearOfBirth)
        val exist = Random.nextBoolean()    // true - берём из бд, false - генерим новый
        val empty = false           // TODO: Игорь выдирает из бд, пустая ли таблица people
        val id: Int
        val yearOfBirth: Int?

        if (exist && !empty) {
            val lastIdPeople = 10   // TODO: Игорь выдирает последний id из бд (таблица people)
            id = Random.nextInt(lastIdPeople + 1)
            yearOfBirth = 1500      // TODO: Игорь выдирает year_of_birth из бд (таблица people, выбранный id)
        } else {
            // Генерация имени человека
            val fullName = fullNameList.random()

            // Генерация года рождения человека
            val yearExist =
                !(Random.nextBoolean() && Random.nextBoolean() && Random.nextBoolean() && Random.nextBoolean())
            yearOfBirth = if (yearExist) Random.nextInt(1500, 2005) else null

            // Запись человека в бд (таблица people)
            val people = People(fullName, yearOfBirth)  // TODO: Игорь добавляет запись в бд (таблица people)
            id = 0                  // TODO: Игорь выдирает из бд id добавленного человека
        }
        return Pair(id, yearOfBirth)
    }

    /* private fun randomPeopleFunction(): PeopleFunction {
         // TODO: 15.04.2021 Реализовать генерацию объекта PeopleFunction
         return PeopleFunction("Programmer")
     }*/

    private fun randomMusicAlbum(): MusicAlbum {
        // TODO: 15.04.2021 Реализовать генерацию объекта MusicAlbum
        return MusicAlbum("Imposter of us", 2020, null)
    }

    private fun randomFilmSeries(): FilmSeries {
        // TODO: 15.04.2021 Реализовать генерацию объекта FilmSeries
        return FilmSeries("Harry Potter", null)
    }

    fun addFilm(): Int {
        // TODO: 15.04.2021 Реализовать генерацию объекта Film
        /*return Film(
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
        // TODO: 15.04.2021 Реализовать генерацию объекта Music
        //return Music("Believer", 2017, 203)
        return 0
    }

/*    private fun randomFilmGenre(): FilmGenre {
        // TODO: 15.04.2021 Реализовать генерацию объекта FilmGenre (Неточно)
        return FilmGenre(
            "Драма",
            "Драматические фильмы – один из наиболее распространенных кинематографических жанров. Как правило, эти фильмы повествуют о частной жизни и социальных конфликтах персонажей, акцентируя внимание на воплощенных в их поступках и поведении общечеловеческих противоречиях. Характерной чертой жанра является приближенная к реальности стилистика и бытовой сюжет.\n" +
                    "\n" +
                    "Драма (с древнегреческого языка – действие) понятие очень обширное, пришедшее в кино из театра и литературы. До появления кино и телевидения в театре этот термин использовался для описания типа пьесы, которая не является ни комедией, ни трагедией.\n" +
                    "\n" +
                    "В связи с этим, при описании конкретного фильма, слово драма чаще всего используется в сочетании с дополнительными терминами. Такое разделение помогает получить более конкретное представление о месте действия фильма и поднимаемых в нем вопросов.\n" +
                    "\n" +
                    "Благодаря близким многим зрителям бытовым сюжетам и реалистичности, лучшие драматические фильмы современности и прошлого века пользуются огромной популярностью. Смотреть такие фильмы можно по нескольку раз."
        )
    }*/

    fun addTop(type: Int = Random.nextInt(0, 3)) { // type: значение по умолчанию - без разницы (выбирается рандомно); 0 - книга, 1 -фильм, 2 - музыка
        // Подготовка данных
        val noun: String
        val lastId: Int
        val numId: Int

        when (type) {
            0 -> {
                noun = "книг"
                lastId = 10     // TODO: Игорь выдирает из бд последний id (таблица book)
                numId = 10      // TODO: Игорь выдирает из бд количество записей в таблице book
                // TODO: совпадают?
            }
            1 -> {
                noun = "фильмов"
                lastId = 10     // TODO: Игорь выдирает из бд последний id (таблица film)
                numId = 10      // TODO: Игорь выдирает из бд количество записей в таблице film
            }
            2 -> {
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
        val forWhomList = readingFile("topForWhomList.txt")
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
                    0 -> addBook()
                    1 -> addFilm()
                    2 -> addMusic()
                    else -> addBook()
                }
            else
                 Random.nextInt(0, lastId + 1)

            // Запись в нужную таблицу в бд
            val check: Boolean
            when (type) {
                0 -> {
                    if (true) {         // TODO: Игорь проверяет, что выбранный id есть в таблице top_has_book
                        // TODO: Игорь добавляет запись в бд (таблица top_has_book). Данные - topId, elId, place
                        check = true    // TODO: Игорь заполняет индикатор (true - запись добавлена, false - нет)
                        TODO()
                    } else
                        check = false
                }
                1 -> {
                    if (true) {         // TODO: Игорь проверяет, что выбранный id есть в таблице top_has_film
                        // TODO: Игорь добавляет запись в бд (таблица top_has_film). Данные - topId, elId, place
                        check = true    // TODO: Игорь заполняет индикатор (true - запись добавлена, false - нет)
                        TODO()
                    } else
                        check = false
                }
                2 -> {
                    if (true) {         // TODO: Игорь проверяет, что выбранный id есть в таблице top_has_book
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

/*    private fun randomMusicGenre(): MusicGenre {
        // TODO: 15.04.2021 Реализовать генерацию объекта MusicGenre (неточно)
        return MusicGenre()
    }*/

    fun addUser(): User {
        // TODO: 15.04.2021 Реализовать генерацию объекта User
        return User("Teacons", "123321", "kek@gmail.com", Timestamp(Calendar.getInstance().timeInMillis))
    }

/*    private fun randomBookGenre(): BookGenre {
        // TODO: 15.04.2021 Реализовать генерацию объекта BookGenre (неточно)
        return BookGenre(
            "Детектив", "Детекти́вный рассказ, так же уголо́вный рассказ — " +
                    "преимущественно литературный и кинематографический жанр, произведения которого описывают " +
                    "процесс исследования загадочного происшествия с целью выяснения его обстоятельств и раскрытия " +
                    "загадки. Обычно в качестве такого происшествия выступает преступление, и детектив описывает его " +
                    "расследование и определение виновных, в таком случае конфликт строится на столкновении " +
                    "справедливости с беззаконием, завершающемся победой справедливости."
        )
    }*/

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
        val people = randomPeople()
        val peopleId = people.first
        val peopleYear = people.second

        // Генерация года написания книги
        val year = if (peopleYear != null)
            Random.nextInt(
                peopleYear + 15,
                peopleYear + 70
            )    // год написания книги: не раньше, чем в 15 лет, и не старше, чем в 70
        else
            Random.nextInt(1500, 2021)

        // Запись книги в бд (таблица Book)
        val book = Book(name, year, desc, poster, bookSeriesId) // TODO: Игорь добавляет запись в бд (таблица book)
        val bookId = 0  // TODO: Игорь выдирает из бд id добавленной книги

        // Заполнение кросс-таблицы book_has_people
        // TODO: Игорь заполняет таблицу book_has_people. Данные - peopleId, bookId

        // Заполнение кросс-таблицы book_has_book_genre
        val lastIdGenre = 10 // TODO: Игорь выдирает из бд последний id жанра книг (таблица book_genre)
        val genreId = Random.nextInt(lastIdGenre + 1)
        // TODO: Игорь заполняет таблицу book_has_book_genre. Данные - bookId, genreId

        return bookId
    }

    private fun randomBookSeries(): Int {   // возврат - id серии книг
        val exist = Random.nextBoolean()    // true - берём из бд, false - генерим новый
        val empty = false       // TODO: Игорь выдирает из бд, пустая ли таблица
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
