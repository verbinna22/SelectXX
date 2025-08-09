package ru.yandex.mylogininya

data class RegularVertex(val oldVertex: Int, val state: AutomatonState) {
    val enflow: MutableSet<RegularVertex> = mutableSetOf()
    val exflow: MutableSet<RegularVertex> = mutableSetOf()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegularVertex

        if (oldVertex != other.oldVertex) return false
        if (state != other.state) return false

        return true
    }

    override fun hashCode(): Int {
        var result = oldVertex
        result = 31 * result + state.hashCode()
        return result
    }
}
