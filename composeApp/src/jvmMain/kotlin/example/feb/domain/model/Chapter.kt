package example.feb.domain.model

import java.util.UUID

data class Chapter(
    val id: UUID,
    val title: String,
    val content: String,
    val zoomPercent: Int = 100,
) {
    companion object {
        const val DEFAULT_TITLE = "New Chapter"
    }
}
