package ru.yandex.mylogininya

import kotlin.math.absoluteValue

data class Label(val contextId: Int, val labelType: LabelType, val funId: Int = 0, val realType: LabelType = LabelType.BALANCED, val fieldId: Int = -1) {
    fun hasContext(): Boolean = contextId != 0
    fun isContextOpen(): Boolean = when (contextId) {
        0 -> throw IllegalArgumentException("Need context to have")
        in 1..Int.MAX_VALUE -> true
        else -> false
    }
    fun isContextClose(): Boolean = !isContextOpen()
    fun context(): Int = contextId.absoluteValue
    fun openCloseString(): String = if (isContextOpen()) "open" else "close"
}
