import java.io.File
import java.io.InputStream
import java.sql.Timestamp
import java.util.*
import kotlin.random.Random

class Randomizer {

    private fun readingFile(name: String): List<String> {
        val uri = this.javaClass.getResource("/$name").toURI()
        val inputStream: InputStream = File(uri).inputStream()
        val list = mutableListOf<String>()
        inputStream.bufferedReader().forEachLine { list.add(it) }
        return list
    }

    private val adjectiveList = readingFile("adjectiveList.txt")
    private val nounList = readingFile("nounList.txt")
    private val adverbList = readingFile("adverbList.txt")
    private val verbList = readingFile("verbList.txt")
    private val phraseologicalList = readingFile("phraseologicalList.txt")
    private val nameList = readingFile("nameList.txt")
    private val surnameList = readingFile("surnameList.txt")

    fun randomPeople(): Pair<Int, Int?> {    // возврат - id, yearOfBirth
        val exist = Random.nextBoolean()    // true - берём из бд, false - генерим новый
        val empty = false //TODO: Игорь выдирает из бд, пустая ли таблица
        val id: Int
        val yearOfBirth: Int?

        if (exist && !empty) {
            id = 0          //TODO: Игорь выдирает id из бд
            yearOfBirth = 1500     //TODO: Игорь выдирает yearOfBirth из бд
        } else {
            val yearExist =
                !(Random.nextBoolean() && Random.nextBoolean() && Random.nextBoolean() && Random.nextBoolean())
            val fullname = surnameList[Random.nextInt(surnameList.size)] + " " + nameList[Random.nextInt(nameList.size)]
            yearOfBirth = if (yearExist) Random.nextInt(1500, 2005) else null

            val people = People(fullname, yearOfBirth)
            id = 0   // TODO: Игорь добавляет в бд и выдирает id
        }
        return Pair(id, yearOfBirth)
    }

    fun randomPeopleFunction(): PeopleFunction {
        // TODO: 15.04.2021 Реализовать генерацию объекта PeopleFunction
        return PeopleFunction("Programmer")
    }

    fun randomMusicAlbum(): MusicAlbum {
        // TODO: 15.04.2021 Реализовать генерацию объекта MusicAlbum
        return MusicAlbum("Imposter of us", 2020, null)
    }

    fun randomFilmSeries(): FilmSeries {
        // TODO: 15.04.2021 Реализовать генерацию объекта FilmSeries
        return FilmSeries("Harry Potter", null)
    }

    fun randomFilm(): Film {
        // TODO: 15.04.2021 Реализовать генерацию объекта Film
        return Film(
            "Зеленый слоник",
            1999,
            5160,
            "Два младших офицера, сидя в одной камере на гауптвахте, вынуждены решать острые социальные и психологические вопросы в небольшом пространстве.",
            null,
            null,
            null
        )
    }

    fun randomMusic(): Music {
        // TODO: 15.04.2021 Реализовать генерацию объекта Music
        return Music("Believer", 2017, 203)
    }

/*    fun randomFilmGenre(): FilmGenre {
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

    fun randomTop(): Top {
        // TODO: 15.04.2021 Реализовать генерацию объекта Top
        return Top("Топ 100 по версии Кинопоиск 2018")
    }

/*    fun randomMusicGenre(): MusicGenre {
        // TODO: 15.04.2021 Реализовать генерацию объекта MusicGenre (неточно)
        return MusicGenre()
    }*/

    fun randomUser(): User {
        // TODO: 15.04.2021 Реализовать генерацию объекта User
        return User("Teacons", "123321", "kek@gmail.com", Timestamp(Calendar.getInstance().timeInMillis))
    }

/*    fun randomBookGenre(): BookGenre {
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

    fun randomBook(): Int { // возврат - id книги
        val noun = nounList[Random.nextInt(nounList.size)]
        val name =
            (adjectiveList[Random.nextInt(adjectiveList.size)] + " " + adjectiveList[Random.nextInt(adjectiveList.size)] + " " + noun).capitalize()
        val adj = adjectiveList[Random.nextInt(adjectiveList.size)]
        val desc =
            "Эта " + adj.substring(0, adj.length - 2) + "ая" + " книга расскажет увлекательную историю. " +
                    (noun + " хочет " + adverbList[Random.nextInt(adverbList.size)] + " " + verbList[Random.nextInt(
                        verbList.size
                    )] +
                            ", как говорится, " + phraseologicalList[Random.nextInt(phraseologicalList.size)] + ".").toLowerCase()
                        .capitalize()

        val posterExist = !(Random.nextBoolean() && Random.nextBoolean())
        val poster = if (posterExist) "https://poster.com/" + Random.nextInt(100000, 1000000) else null

        val bookSeriesExist = !(Random.nextBoolean() && Random.nextBoolean())
        val bookSeriesId = if (bookSeriesExist) randomBookSeries() else null

        val people = randomPeople()
        val peopleId = people.first
        val peopleYear = people.second
        val year =
            if (peopleYear != null) Random.nextInt(peopleYear + 15, peopleYear + 60) else Random.nextInt(1500, 2021)

        val book = Book(name, year, desc, poster, bookSeriesId) // TODO: Игорь заносит в бд
        val bookId = 0  // TODO: Игорь выдирает из бд id добавленной книги

        // TODO: Игорь заполняет таблицу book_has_people. Данные - peopleId, bookId

        val lastIdGenre = 10 // TODO: Игорь выдирает из бд последний id жанра книг (таблица book_genre)
        val genreId = Random.nextInt(lastIdGenre + 1)
        // TODO: Игорь заполняет таблицу book_has_book_genre. Данные - bookId, genreId

        return bookId
    }

    fun randomBookSeries(): Int {   // Возврат - id серии книг
        val exist = Random.nextBoolean()    // true - берём из бд, false - генерим новый
        val empty = false //TODO: Игорь выдирает из бд, пустая ли таблица
        val id: Int

        if (exist && !empty) {
            id = 0 //TODO: Игорь выдирает id из бд
        } else {
            val descExist = !(Random.nextBoolean() && Random.nextBoolean() && Random.nextBoolean())
            val noun = nounList[Random.nextInt(nounList.size)]
            val name =
                adjectiveList[Random.nextInt(adjectiveList.size)] + " " + adjectiveList[Random.nextInt(adjectiveList.size)] + " " + noun
            name.capitalize()

            val adj = adjectiveList[Random.nextInt(adjectiveList.size)]
            val desc = if (descExist)
                "Эта " + adj.substring(0, adj.length - 2) + "ая" + " серия книг расскажет увлекательную историю. " +
                        (noun + " хочет " + adverbList[Random.nextInt(adverbList.size)] + " " + verbList[Random.nextInt(
                            verbList.size
                        )] +
                                ", как говорится, " + phraseologicalList[Random.nextInt(phraseologicalList.size)] + ".").toLowerCase()
                            .capitalize()
            else null
            val bookSeries = BookSeries(name, desc)
            id = 0   // TODO: Игорь добавляет в бд и выдирает id
        }
        return id
    }
}
// Эта *книга, фильм, песня" расскажет увлекательную историю. *Существительное, палка* хочет *начерчие, увлекательно* *глагол, плыть*, как говорпится, *фразеологизм*