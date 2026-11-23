package com.locrights.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.locrights.app.util.HtmlBlock
import com.locrights.app.util.HtmlParser
import com.locrights.app.util.InlineSpan

@Composable
fun NativeHtmlRenderer(
    html: String,
    fontScale: Float,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val blocks = remember(html) { HtmlParser.parseToBlocks(html) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground

    SelectionContainer(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            blocks.forEach { block ->
                when (block) {
                    is HtmlBlock.Heading -> {
                        val size = when (block.level) {
                            1 -> 24f
                            2 -> 21f
                            3 -> 18f
                            else -> 16f
                        } * fontScale
                        Text(
                            text = block.text,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = size.sp,
                                lineHeight = (size * 1.35f).sp
                            ),
                            color = textColor,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    is HtmlBlock.Paragraph -> {
                        val annotated = buildInlineAnnotatedString(block.spans, primaryColor)
                        ClickableText(
                            text = annotated,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = textColor,
                                fontSize = (16f * fontScale).sp,
                                lineHeight = (26f * fontScale).sp
                            ),
                            onClick = { offset ->
                                annotated.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                    .firstOrNull()?.let { onLinkClick(it.item) }
                            }
                        )
                    }

                    is HtmlBlock.BulletList -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            block.items.forEachIndexed { idx, itemSpans ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = if (block.ordered) "${idx + 1}." else "•",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = (16f * fontScale).sp
                                        ),
                                        color = primaryColor
                                    )
                                    val annotated = buildInlineAnnotatedString(itemSpans, primaryColor)
                                    ClickableText(
                                        text = annotated,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = textColor,
                                            fontSize = (16f * fontScale).sp,
                                            lineHeight = (25f * fontScale).sp
                                        ),
                                        modifier = Modifier.weight(1f),
                                        onClick = { offset ->
                                            annotated.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                                .firstOrNull()?.let { onLinkClick(it.item) }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    is HtmlBlock.BlockQuote -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = block.text,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontStyle = FontStyle.Italic,
                                    fontSize = (15.5f * fontScale).sp,
                                    lineHeight = (24f * fontScale).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is HtmlBlock.Image -> {
                        Column {
                            AsyncImage(
                                model = block.url,
                                contentDescription = block.caption ?: "Article image",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            if (!block.caption.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = block.caption,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is HtmlBlock.Divider -> {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun buildInlineAnnotatedString(
    spans: List<InlineSpan>,
    linkColor: androidx.compose.ui.graphics.Color
): AnnotatedString = buildAnnotatedString {
    spans.forEach { span ->
        val start = length
        append(span.text)
        val end = length
        if (span.isBold) {
            addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
        }
        if (span.isItalic) {
            addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
        }
        if (!span.linkUrl.isNullOrBlank()) {
            addStyle(
                SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Medium
                ),
                start,
                end
            )
            addStringAnnotation(tag = "URL", annotation = span.linkUrl, start = start, end = end)
        }
    }
}
