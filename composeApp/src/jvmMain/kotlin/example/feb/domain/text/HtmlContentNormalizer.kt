package example.feb.domain.text

import kotlin.text.iterator

object HtmlContentNormalizer {

    fun normalizeToHtml(stored: String): String {
        val s = stored.trim()
        if (s.isEmpty()) return ""

        val looksLikeHtml = s.startsWith("<") && (
                s.contains("</") ||
                        s.contains("<p") || s.contains("<div") || s.contains("<br") ||
                        s.contains("<ul") || s.contains("<ol") || s.contains("<li") ||
                        s.contains("<h1") || s.contains("<h2") || s.contains("<a ")
                )

        if (looksLikeHtml) return stored

        val normalized = stored.replace("\r\n", "\n")
        val paragraphs = normalized.split("\n\n")

        return paragraphs.joinToString(separator = "") { p ->
            val escapedLines = p
                .split("\n")
                .joinToString("<br>") { line -> escapeHtml(line) }
            "<p>$escapedLines</p>"
        }
    }

    private fun escapeHtml(text: String): String = buildString(text.length) {
        for (ch in text) {
            when (ch) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(ch)
            }
        }
    }
}