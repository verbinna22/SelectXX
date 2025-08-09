package ru.yandex.mylogininya

fun selectX(
    graph: RegularGraph,
    vertexToContextToChildren: Map<RegularVertex, MutableMap<Int, MutableSet<RegularVertex>>>,
    vertexToContextToParents: Map<RegularVertex, MutableMap<Int, MutableSet<RegularVertex>>>
): Set<Int> {
    var changed = true
    val balancedRibs: MutableSet<RegularRib> = mutableSetOf()
    val regularVertexes: MutableSet<RegularVertex> = mutableSetOf()

    fun addToEnflow(owner: RegularVertex, addable: RegularVertex) {
        changed = true
        owner.enflow.add(addable)
        val contextToChildren = vertexToContextToChildren[owner]
        if (contextToChildren === null) {
            return
        }
        for (context in contextToChildren.keys) {
            if (context < 0) {
                val contextToParents = vertexToContextToParents[addable]
                if (contextToParents != null) {
                    val parents = contextToParents[-context]
                    if (parents != null) {
                        for (parent in parents) {
                            for (child in contextToChildren[context]!!) {
                                val rib = RegularRib(
                                    parent, child, Label(0, LabelType.BALANCED)
                                )
                                if (!balancedRibs.contains(rib)) {
                                    balancedRibs.add(rib)
                                    graph.ribs.add(rib)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    fun addToExflow(owner: RegularVertex, addable: RegularVertex) {
        changed = true
        owner.exflow.add(addable)
        val contextToChildren = vertexToContextToChildren[addable]
        if (contextToChildren === null) {
            return
        }
        for (context in contextToChildren.keys) {
            if (context < 0) {
                val contextToParents = vertexToContextToParents[owner]
                if (contextToParents != null) {
                    val parents = contextToParents[-context]
                    if (parents != null) {
                        for (parent in parents) {
                            for (child in contextToChildren[context]!!) {
                                val rib = RegularRib(
                                    parent, child, Label(0, LabelType.BALANCED)
                                )
                                if (!balancedRibs.contains(rib)) {
                                    balancedRibs.add(rib)
                                    graph.ribs.add(rib)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    while (changed) {
        changed = false
        for (i in 0..<graph.ribs.size) {
            val rib = graph.ribs[i]
            if (!regularVertexes.contains(rib.firstVertex)) {
                regularVertexes.add(rib.firstVertex)
            }
            if (!regularVertexes.contains(rib.secondVertex)) {
                regularVertexes.add(rib.secondVertex)
            }
            if (rib.label.hasContext()) {
                if (rib.label.isContextOpen()) {
                    if (!rib.secondVertex.enflow.contains(rib.secondVertex)) {
                        addToEnflow(rib.secondVertex, rib.secondVertex)
                    }
                } else {
                    if (!rib.firstVertex.exflow.contains(rib.firstVertex)) {
                        addToExflow(rib.firstVertex, rib.firstVertex)
                    }
                }
            } else {
                for (vertex in rib.firstVertex.enflow) {
                    if (!rib.secondVertex.enflow.contains(vertex)) {
                        addToEnflow(rib.secondVertex, vertex)
                    }
                }
                for (vertex in rib.secondVertex.exflow) {
                    if (!rib.firstVertex.exflow.contains(vertex)) {
                        addToExflow(rib.firstVertex, vertex)
                    }
                }
            }
        }
    }
    val sensitiveVertexes = regularVertexes.filter { it.enflow.isNotEmpty() && it.exflow.isNotEmpty() }.map { it.oldVertex }.toSet()
    return (0..<graph.numberOfVertexes).filter { !sensitiveVertexes.contains(it) }.toSet()
}