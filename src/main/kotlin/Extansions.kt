package flashcards

import java.util.*

fun <T,U> Map<T,U>.random(): Map.Entry<T,U>{
    return entries.elementAt(Random().nextInt(size))
}