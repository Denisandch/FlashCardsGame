package flashcards.model

data class InfoAboutTheCard(val cardDefinition: String, var cardMistakes: Int = 0) {
    override fun toString(): String {
        return "$cardDefinition $cardMistakes"
    }
}