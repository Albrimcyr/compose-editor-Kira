package example.feb.domain.model

import java.util.UUID

data class Chapter(
    val id: UUID,
    val title: String,
    val content: String
)
