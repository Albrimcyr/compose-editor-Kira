package example.feb.domain.text

data class ContentStats(
    val words: Int,
    val chars: Int
) {
    companion object { val Empty = ContentStats(0, 0) }
}

fun computeContentStats(html: String): ContentStats {

    // ── Do not count  ────────────────────────────────────────────────────────────────────────────────────────────────
    val plain = html
        .replace(Regex("<[^>]*>"), " ")
        .replace(Regex("&[a-zA-Z#][a-zA-Z0-9]+;"), " ")
        .trim()

    val chars = plain.replace(Regex("\\s"), "").length                  // no spaces
    val words = if (plain.isBlank()) 0 else plain.trim().split(Regex("\\s+")).size   // spaces do not matter

    return ContentStats(words, chars)
}