package ru.yandex.mylogininya

data class RegularRib(val firstVertex: RegularVertex, val secondVertex: RegularVertex, val label: Label) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegularRib

        if (firstVertex != other.firstVertex) return false
        if (secondVertex != other.secondVertex) return false
        if (label != other.label) return false

        return true
    }

    override fun hashCode(): Int {
        var result = firstVertex.hashCode()
        result = 31 * result + secondVertex.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }

}
