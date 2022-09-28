package flashcards

import flashcards.model.InfoAboutTheCard
import java.io.File // работа с файлами

/*
Key things:
-Extension function
-Data class
-Working with file system
-Working with Map
-Simple string parsing

simple console application

you can add "cards" manually or from the file.
file have to contain strings like this:
"CardKey CardDefinition CountOfMistakes".

also you can  get the log of the game with "log" command
 */

var loggingFromConsole = ""
val cardPool = mutableMapOf<String, InfoAboutTheCard>()
val cardsWithMistakesForRepeating = mutableListOf<String>()

fun main(args: Array<String>) {

    if(args.contains("-import")) importFromFile(searchPath("-import", args))

    while(true) {

        println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")

        when(readLine()!!) {
            "add" -> addCard()
            "remove" -> removeCard()
            "import" -> importFromFile()
            "export" -> exportToFile()
            "ask" -> askRandom()
            "log" -> logging()
            "hardest card" -> showHardest()
            "reset stats" -> breakMistakes()

            "exit" -> {
                println("Bye bye!")

                if(args.contains("-export")) exportToFile(searchPath("-export", args))
                break
            }
        }
    }
}

fun searchPath(command: String, args: Array<String>): String {
    var found = false

    when(command) {
        "-import" -> {
            for(unit in args) {
                if(found) return unit
                if(unit == "-import") found = true
            }
        }
        "-export" -> {
            for(unit in args) {
                if(found) return unit
                if(unit == "-export") found = true
            }
        }
    }

    return "fail\n"
}

fun println(str: String) {
    loggingFromConsole += "$str\n"

    kotlin.io.println(str)
}

fun readLine(): String? {
    val inputString = kotlin.io.readLine()

    loggingFromConsole += "$inputString\n"

    return inputString
}

fun addCard() {

    println("The card:")
    val card = readLine()!!

    if (cardPool.containsKey(card)) {
        println("The card \"$card\" already exists.\n")
        return
    }

    println("The definition of the card:")
    val definition = readLine()!!

    cardPool[card] = InfoAboutTheCard(definition)
    println("The pair (\"$card\":\"$definition\") has been added.\n")
}

fun removeCard() {
    println("Which card?")
    val card = readLine()!!

    if(cardPool.containsKey(card)) {
        cardPool.remove(card)
        println("The card has been removed.\n")
    } else {
        println("Can't remove \"$card\": there is no such card.\n")
    }
}

fun importFromFile(name: String = "") {
    var fileName = name
    var addedCardCounter = 0

    if(name == "") {
        println("File name:")
        fileName = readLine()!!
    }

    if (!File(fileName).exists()) {
        println("File not found.\n")
        return
    }

    val bufList = File(fileName).readLines()

    for (unit in bufList) {

        val card = unit.substringBefore(' ')

        if (cardPool.containsKey(card)) {
            println("The card \"$card\" already exists.\n")
            continue
        }

        val definition = InfoAboutTheCard(unit.substringAfter(' ').substringBefore(' '))
        val mistakes = try {
            unit.substringAfterLast(' ').toInt()
        } catch (e: Exception) {
            0
        }

        cardPool[card] = definition
        cardPool[card]!!.cardMistakes += mistakes
        addToHardest(card)

        addedCardCounter++
    }

    println("$addedCardCounter cards have been loaded.\n")
}

fun exportToFile(name: String = "") {
    var fileName = name

    if(name == "") {
        println("File name:")
        fileName = readLine()!!
    }

    val exportFile = File(fileName)

    var output = ""

    for ((k,v) in cardPool) {
        output += "$k $v\n"
    }

    exportFile.writeText(output)
    println("${exportFile.readLines().size} cards have been saved.\n")
}

fun askRandom() {

    var userAnswer = ""
    println("How many times to ask?")

    val countOfAsks = readLine()!!.toInt()

    for(i in 1..countOfAsks) {
        val pair = cardPool.random()
        println("Print the definition of \"${pair.key}\":")

        userAnswer = readLine()!!

        if(userAnswer == pair.value.cardDefinition) {
            println("Correct!\n")
        }

        else if(cardPool.containsValue(InfoAboutTheCard(userAnswer))) {

            println("Wrong. The right answer is \"${pair.value.cardDefinition}\", but your definition is correct for \"${findByValue(userAnswer)}\".\n")
            pair.value.cardMistakes++
            addToHardest(pair.key)

        } else {

            println("Wrong. The right answer is \"${pair.value.cardDefinition}\".\n")
            pair.value.cardMistakes++
            addToHardest(pair.key)

        }
    }
}

fun findByValue(findValue: String): String {

    var answer = "nothing"

    for ((card, definition) in cardPool) {
        if(definition.cardDefinition == findValue) {
            answer = card
            break
        }
    }

    return answer
}

fun addToHardest(key: String) {

    if(cardPool[key]!!.cardMistakes == 0) return

    if(cardsWithMistakesForRepeating.contains(key)) return

    if(cardsWithMistakesForRepeating.isEmpty()) {
        cardsWithMistakesForRepeating.add(key)
        return
    }

    if(cardPool[key]!!.cardMistakes > cardPool[cardsWithMistakesForRepeating.first()]!!.cardMistakes) {
        cardsWithMistakesForRepeating.clear()
        cardsWithMistakesForRepeating.add(key)
        return
    }

    if(cardPool[key]!!.cardMistakes == cardPool[cardsWithMistakesForRepeating.first()]!!.cardMistakes) {
        cardsWithMistakesForRepeating.add(key)
    }
}

fun logging() {

    println("File name:")

    File(readLine()!!).writeText(loggingFromConsole)

    println("The log has been saved.")
}

fun showHardest() {

    if(cardsWithMistakesForRepeating.isEmpty()) {
        println("There are no cards with errors.\n")
        return
    }

    var outputWithHardestWords = ""

    for(card in cardsWithMistakesForRepeating) {
        outputWithHardestWords += "\"$card\""
        if(card != cardsWithMistakesForRepeating.last()) outputWithHardestWords += ", "
    }

    println("The hardest card${if(cardsWithMistakesForRepeating.size == 1) " is" else "s are"}" +
            " $outputWithHardestWords. You have ${cardPool[cardsWithMistakesForRepeating.first()]!!.cardMistakes} errors answering" +
            " ${if(cardsWithMistakesForRepeating.size == 1) "it" else "them"}.\n")

}

fun breakMistakes() {

    for(item in cardPool) {
        item.value.cardMistakes = 0
    }

    cardsWithMistakesForRepeating.clear()

    println("Card statistics have been reset.\n")
}

