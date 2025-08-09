package ru.yandex.mylogininya

data class RegularGraph(val ribs: MutableList<RegularRib>, val numberOfVertexes: Int)

fun buildRegularGraph(graph: Graph): Triple<RegularGraph, MutableMap<RegularVertex, MutableMap<Int, MutableSet<RegularVertex>>>, MutableMap<RegularVertex, MutableMap<Int, MutableSet<RegularVertex>>>> {
    val newEdges = mutableListOf<RegularRib>()
    val vertexes = mutableMapOf<Int, MutableMap<AutomatonState, RegularVertex>>() // TODO reuse it
    fun getVertex(oldVertex: Int, state: AutomatonState): RegularVertex {
        if (vertexes.containsKey(oldVertex)) {
            if (vertexes[oldVertex]!!.containsKey(state)) {
                return vertexes[oldVertex]!![state]!!
            }
            vertexes[oldVertex]!![state] = RegularVertex(oldVertex, state)
            return vertexes[oldVertex]!![state]!!
        }
        vertexes[oldVertex] = mutableMapOf()
        vertexes[oldVertex]!![state] = RegularVertex(oldVertex, state)
        return vertexes[oldVertex]!![state]!!
    }
    val vertexToContextToChildren: MutableMap<RegularVertex, MutableMap<Int, MutableSet<RegularVertex>>> =
        mutableMapOf()
    val vertexToContextToParents: MutableMap<RegularVertex, MutableMap<Int, MutableSet<RegularVertex>>> =
        mutableMapOf()
    for (rib in graph.ribs) {
        val added =
            when (rib.label.labelType) {
                LabelType.ALLOC ->
                    RegularRib(
                        getVertex(rib.firstVertex, AutomatonState.OPEN),
                        getVertex(rib.secondVertex, AutomatonState.FLOW),
                        rib.label
                    )


                LabelType.ALLOC_R ->
                    RegularRib(
                        getVertex(rib.firstVertex, AutomatonState.POINTS),
                        getVertex(rib.secondVertex, AutomatonState.OPEN),
                        rib.label
                    )

                LabelType.ASSIGN ->
                    RegularRib(
                        getVertex(rib.firstVertex, AutomatonState.FLOW),
                        getVertex(rib.secondVertex, AutomatonState.FLOW),
                        rib.label
                    )

                LabelType.ASSIGN_R ->
                    RegularRib(
                        getVertex(rib.firstVertex, AutomatonState.POINTS),
                        getVertex(rib.secondVertex, AutomatonState.POINTS),
                        rib.label
                    )

                LabelType.STORE ->
                    RegularRib(
                        getVertex(rib.firstVertex, AutomatonState.FLOW),
                        getVertex(rib.secondVertex, AutomatonState.POINTS),
                        rib.label
                    )

                else -> throw IllegalArgumentException("invalid graph")
            }
        newEdges.add(added)
        if (rib.label.hasContext()) {
            if (!vertexToContextToChildren.containsKey(added.firstVertex)) {
                vertexToContextToChildren[added.firstVertex] = mutableMapOf()
            }
            val vertexToChildren = vertexToContextToChildren[added.firstVertex]
            val context = rib.label.contextId
            if (!vertexToChildren!!.containsKey(context)) {
                vertexToChildren[context] = mutableSetOf()
            }
            vertexToChildren[context]!!.add(added.secondVertex)
            if (!vertexToContextToParents.containsKey(added.secondVertex)) {
                vertexToContextToParents[added.secondVertex] = mutableMapOf()
            }
            val vertexToParents = vertexToContextToParents[added.secondVertex]
            if (!vertexToParents!!.containsKey(context)) {
                vertexToParents[context] = mutableSetOf()
            }
            vertexToParents[context]!!.add(added.firstVertex)
        }
    }
    return Triple(RegularGraph(newEdges, graph.vertexesNumber), vertexToContextToChildren, vertexToContextToParents)
}
