package ru.yandex.mylogininya

data class Label(val contextId: Int, val labelType: LabelType) {
    fun hasContext(): Boolean = contextId != 0
    fun isContextOpen(): Boolean = when (contextId) {
        0 -> throw IllegalArgumentException("Need context to have")
        in 1..Int.MAX_VALUE -> true
        else -> false
    }
    fun isContextClose(): Boolean = !isContextOpen()
}
