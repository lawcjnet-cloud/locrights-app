package com.locrights.app.util

import android.text.Html
import java.util.regex.Pattern

sealed class HtmlBlock {
    data class Heading(val level: Int, val text: String) : HtmlBlock()
    data class Paragraph(val spans: List<InlineSpan>) : HtmlBlock()
    data class BulletList(val items: List<List<InlineSpan>>, val ordered: Boolean) : HtmlBlock()
    data class BlockQuote(val text: String) : HtmlBlock()
    data class Image(val url: String, val caption: String?) : HtmlBlock()
    data object Divider : HtmlBlock()
}

data class InlineSpan(
    val text: String,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val linkUrl: String? = null
)

object HtmlParser {
    private val SCRIPT_STYLE_REGEX = Regex("<(script|style)[^>]*>[\\s\\S]*?</\\1>", RegexOption.IGNORE_CASE)
    private val IMG_TAG_REGEX = Regex("<img[^>]*src=[\"']([^\"']+)[\"'][^>]*>", RegexOption.IGNORE_CASE)
    private val BLOCK_SPLIT_REGEX = Regex(
        "(<h[1-6][^>]*>[\\s\\S]*?</h[1-6]>|<blockquote[^>]*>[\\s\\S]*?</blockquote>|<ul[^>]*>[\\s\\S]*?</ul>|<ol[^>]*>[\\s\\S]*?</ol>|<figure[^>]*>[\\s\\S]*?</figure>|<hr[^>]*>|<p[^>]*>[\\s\\S]*?</p>|<div[^>]*>[\\s\\S]*?</div>)",
        RegexOption.IGNORE_CASE
    )
    private val LI_REGEX = Regex("<li[^>]*>([\\s\\S]*?)</li>", RegexOption.IGNORE_CASE)
    private val INLINE_TOKEN_REGEX = Regex("(<a\\s+[^>]*href=[\"']([^\"']+)[\"'][^>]*>([\\s\\S]*?)</a>|<(strong|b)[^>]*>([\\s\\S]*?)</\\4>|<(em|i)[^>]*>([\\s\\S]*?)</\\6>)", RegexOption.IGNORE_CASE)

    fun decodeHtml(raw: String): String {
        return Html.fromHtml(raw, Html.FROM_HTML_MODE_LEGACY).toString().trim()
    }

    fun stripToPlainText(html: String): String {
        val cleaned = html.replace(SCRIPT_STYLE_REGEX, " ")
        return decodeHtml(cleaned).replace(Regex("\\s+"), " ").trim()
    }

    fun extractFirstImageUrl(html: String): String? {
        val match = IMG_TAG_REGEX.find(html) ?: return null
        val rawUrl = match.groupValues[1].trim()
        return if (rawUrl.startsWith("http")) upscaleBloggerImage(rawUrl) else null
    }

    fun upscaleBloggerImage(url: String): String {
        return url
            .replace(Regex("/s72[^/]*/"), "/w800/")
            .replace(Regex("/w\\d+-h\\d+[^/]*/"), "/w800/")
            .replace(Regex("=s\\d+[^&]*"), "=w800")
    }

    fun estimateReadingTimeMinutes(plainText: String): Int {
        val words = plainText.split(Regex("\\s+")).count { it.isNotBlank() }
        return maxOf(1, (words + 179) / 180)
    }

    fun parseToBlocks(rawHtml: String): List<HtmlBlock> {
        val html = rawHtml.replace(SCRIPT_STYLE_REGEX, "")
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        val blocks = mutableListOf<HtmlBlock>()

        var lastIndex = 0
        for (match in BLOCK_SPLIT_REGEX.findAll(html)) {
            if (match.range.first > lastIndex) {
                val trailing = html.substring(lastIndex, match.range.first)
                addLooseSegment(trailing, blocks)
            }
            parseSingleElement(match.value, blocks)
            lastIndex = match.range.last + 1
        }

        if (lastIndex < html.length) {
            addLooseSegment(html.substring(lastIndex), blocks)
        }

        return blocks.ifEmpty {
            val plain = stripToPlainText(rawHtml)
            if (plain.isNotBlank()) listOf(HtmlBlock.Paragraph(listOf(InlineSpan(plain)))) else emptyList()
        }
    }

    private fun addLooseSegment(segment: String, out: MutableList<HtmlBlock>) {
        IMG_TAG_REGEX.findAll(segment).forEach { imgMatch ->
            val src = imgMatch.groupValues[1].trim()
            if (src.startsWith("http")) {
                out.add(HtmlBlock.Image(upscaleBloggerImage(src), null))
            }
        }
        val textOnly = segment.replace(IMG_TAG_REGEX, "")
        val spans = parseInlineSpans(textOnly)
        if (spans.any { it.text.isNotBlank() }) {
            out.add(HtmlBlock.Paragraph(spans))
        }
    }

    private fun parseSingleElement(element: String, out: MutableList<HtmlBlock>) {
        val lower = element.trimStart().lowercase()
        when {
            lower.startsWith("<hr") -> out.add(HtmlBlock.Divider)
            lower.startsWith("<h") -> {
                val level = lower.getOrNull(2)?.digitToIntOrNull()?.coerceIn(1, 6) ?: 2
                val text = stripToPlainText(element)
                if (text.isNotBlank()) out.add(HtmlBlock.Heading(level, text))
            }
            lower.startsWith("<blockquote") -> {
                val text = stripToPlainText(element)
                if (text.isNotBlank()) out.add(HtmlBlock.BlockQuote(text))
            }
            lower.startsWith("<ul") || lower.startsWith("<ol") -> {
                val ordered = lower.startsWith("<ol")
                val items = LI_REGEX.findAll(element).mapNotNull { m ->
                    val spans = parseInlineSpans(m.groupValues[1])
                    if (spans.any { it.text.isNotBlank() }) spans else null
                }.toList()
                if (items.isNotEmpty()) out.add(HtmlBlock.BulletList(items, ordered))
            }
            lower.startsWith("<figure") -> {
                val img = IMG_TAG_REGEX.find(element)?.groupValues?.getOrNull(1)?.trim()
                val caption = Regex("<figcaption[^>]*>([\\s\\S]*?)</figcaption>", RegexOption.IGNORE_CASE)
                    .find(element)?.groupValues?.getOrNull(1)?.let { stripToPlainText(it) }
                if (!img.isNullOrBlank() && img.startsWith("http")) {
                    out.add(HtmlBlock.Image(upscaleBloggerImage(img), caption))
                }
            }
            else -> {
                // <p> or <div>: extract nested images first, then inline text
                val imgs = IMG_TAG_REGEX.findAll(element).toList()
                imgs.forEach { m ->
                    val src = m.groupValues[1].trim()
                    if (src.startsWith("http")) {
                        out.add(HtmlBlock.Image(upscaleBloggerImage(src), null))
                    }
                }
                val withoutImgs = element.replace(IMG_TAG_REGEX, "")
                // If div contains nested <p> or headings, recurse
                if (lower.startsWith("<div") && Regex("<(p|h[1-6]|ul|ol|blockquote)\\b", RegexOption.IGNORE_CASE).containsMatchIn(withoutImgs)) {
                    val inner = withoutImgs.replaceFirst(Regex("^<div[^>]*>", RegexOption.IGNORE_CASE), "")
                        .replaceFirst(Regex("</div>\\s*$", RegexOption.IGNORE_CASE), "")
                    out.addAll(parseToBlocks(inner))
                } else {
                    val spans = parseInlineSpans(withoutImgs)
                    if (spans.any { it.text.isNotBlank() }) {
                        out.add(HtmlBlock.Paragraph(spans))
                    }
                }
            }
        }
    }

    private fun parseInlineSpans(fragment: String): List<InlineSpan> {
        val spans = mutableListOf<InlineSpan>()
        var cursor = 0
        for (match in INLINE_TOKEN_REGEX.findAll(fragment)) {
            if (match.range.first > cursor) {
                val plain = decodePreserveSpacing(fragment.substring(cursor, match.range.first))
                if (plain.isNotEmpty()) spans.add(InlineSpan(plain))
            }
            val full = match.value.lowercase()
            when {
                full.startsWith("<a") -> {
                    val href = match.groupValues[2].trim()
                    val label = decodePreserveSpacing(match.groupValues[3])
                    if (label.isNotBlank()) {
                        spans.add(InlineSpan(label, linkUrl = href))
                    }
                }
                full.startsWith("<strong") || full.startsWith("<b") -> {
                    val text = decodePreserveSpacing(match.groupValues[5])
                    if (text.isNotEmpty()) spans.add(InlineSpan(text, isBold = true))
                }
                full.startsWith("<em") || full.startsWith("<i") -> {
                    val text = decodePreserveSpacing(match.groupValues[7])
                    if (text.isNotEmpty()) spans.add(InlineSpan(text, isItalic = true))
                }
            }
            cursor = match.range.last + 1
        }
        if (cursor < fragment.length) {
            val tail = decodePreserveSpacing(fragment.substring(cursor))
            if (tail.isNotEmpty()) spans.add(InlineSpan(tail))
        }
        return spans
    }

    private fun decodePreserveSpacing(html: String): String {
        val noTags = html.replace(Regex("<[^>]+>"), "")
        return Html.fromHtml(noTags, Html.FROM_HTML_MODE_LEGACY).toString()
            .replace(Regex("[ \\t\\r]+"), " ")
    }
}
