package example.feb.presentation

data class ContentStats(
    val words: Int,
    val chars: Int
) {
    companion object { val Empty = ContentStats(0, 0) }
}

fun computeContentStats(markdown: String): ContentStats {
    return computeContentStatsFromPlainText(markdownToPlainText(markdown))
}

fun computeContentStatsFromPlainText(text: String): ContentStats {
    val normalized = text.trim()

    val chars = normalized
        .replace(Regex("\\s"), "")
        .length

    val words = if (normalized.isBlank()) { 0 } else {
        normalized.split(Regex("\\s+")).size
    }

    return ContentStats(words = words, chars = chars)
}

fun markdownToPlainText(markdown: String): String {
    return markdown
        .replace("\r\n", "\n")
        .lines()
        .joinToString("\n") { line ->
            line
                .replace(Regex("""^\s{0,3}#{1,6}\s+"""), "")
                .replace(Regex("""^\s{0,3}>\s?"""),      "")
                .replace(Regex("""^\s*[-*+]\s+"""),      "")
                .replace(Regex("""^\s*\d+\.\s+"""),      "")
        }
        .replace("**", "")
        .replace("__", "")
        .replace("~~", "")
        .replace("==", "")
        .replace("`", "")
}