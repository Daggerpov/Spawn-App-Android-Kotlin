package com.danielagapov.spawn.core.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Phone input the way iOS UserDetailsInputView shows it: the field holds digits (or "+" and digits),
 * and a 10-digit North American number displays as "(604) 555-0123". International numbers stay as typed.
 */
object Phone {
    /** What to keep from typed or pasted text. */
    fun clean(input: String): String {
        val plus = input.trimStart().startsWith("+")
        return (if (plus) "+" else "") + input.filter { it.isDigit() }.take(15)
    }

    /** E.164 for the backend; a bare 10-digit number is assumed to be +1. */
    fun e164(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            raw.startsWith("+") -> "+$digits"
            digits.length == 10 -> "+1$digits"
            else -> "+$digits"
        }
    }

    fun isComplete(raw: String): Boolean {
        val digits = raw.filter { it.isDigit() }
        return if (raw.startsWith("+")) digits.length >= 8 else digits.length == 10 || (digits.length == 11 && digits.startsWith("1"))
    }

    val visualTransformation = VisualTransformation { text ->
        val d = text.text
        if (d.startsWith("+") || d.length > 10 || d.length < 4) {
            TransformedText(text, OffsetMapping.Identity)
        } else {
            val out = buildString {
                append('(').append(d, 0, 3).append(") ")
                append(d, 3, minOf(6, d.length))
                if (d.length > 6) append('-').append(d, 6, d.length)
            }
            val mapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = when {
                    offset <= 3 -> offset + 1
                    offset <= 6 -> offset + 3
                    else -> offset + 4
                }.coerceAtMost(out.length)

                override fun transformedToOriginal(offset: Int) = when {
                    offset <= 1 -> 0
                    offset <= 4 -> offset - 1
                    offset <= 6 -> 3
                    offset <= 9 -> offset - 3
                    offset == 10 -> 6
                    else -> offset - 4
                }.coerceIn(0, d.length)
            }
            TransformedText(AnnotatedString(out), mapping)
        }
    }
}
