package ru.yandex.mylogininya

import java.io.File
import kotlin.collections.mutableMapOf
import kotlin.math.absoluteValue

//class Graph(val ribs: ArrayList<Rib>, val vertexNumberToRibs: ArrayList<ArrayList<Int>>) {
//    private fun dfs(v: Int, used: MutableSet<Int>, functor: (currentRib: Rib) -> Unit) {
//        used.add(v)
//        for (rib in vertexNumberToRibs[v]) {
//            functor(ribs[rib])
//            if (!used.contains(ribs[rib].secondVertex)) {
//                dfs(ribs[rib].secondVertex, used, functor)
//            }
//        }
//    }
//
//    fun dfs(functor: (currentRib: Rib) -> Unit) {
//        val used = mutableSetOf<Int>()
//        for (v in 1..<vertexNumberToRibs.size) {
//            if (!used.contains(v)) {
//                dfs(v, used, functor)
//            }
//        }
//    }
//}

//typealias Graph = List<Rib>

data class Graph(val ribs: List<Rib>, val vertexesNumber: Int)

fun readGraphFromFile(ribsFilename: String, vertexMappingFilename: String): Pair<Graph, MutableMap<Int, MutableMap<Int, MutableSet<Int>>>> {
    val ribs: ArrayList<Rib> = arrayListOf()
    val numberOfVertexes = File(vertexMappingFilename).bufferedReader().lines().count().toInt()
    val functionalContextToContextToVertexSet = mutableMapOf<Int, MutableMap<Int, MutableSet<Int>>>()
    data class LineItem(val label: LabelType, val realType: LabelType, val context: Int = 0, val functionId: Int = 0)
    File(ribsFilename).bufferedReader().forEachLine {
        val graphRibItems = it.split(" ", "\t")
        val firstVertex = graphRibItems[0].toInt()
        val secondVertex = graphRibItems[1].toInt()
        val (label, realType, context, funId) = when (graphRibItems[2]) {
            "alloc" -> LineItem(LabelType.ALLOC, LabelType.ALLOC) //Triple(LabelType.ALLOC, 0, LabelType.ALLOC)
            "alloc_r" -> LineItem(LabelType.ALLOC_R, LabelType.ALLOC_R)
            "assign" -> LineItem(LabelType.ASSIGN, LabelType.ASSIGN)
            "load_i" -> LineItem(LabelType.ASSIGN, LabelType.LOAD)
            "assign_r" -> LineItem(LabelType.ASSIGN_R, LabelType.ASSIGN_R)
            "load_r_i" -> LineItem(LabelType.ASSIGN_R, LabelType.LOAD_R)
            "store_i" -> LineItem(LabelType.STORE, LabelType.STORE)
            "store_r_i" -> LineItem(LabelType.STORE, LabelType.STORE_R)
            else -> {
                var (label, rest) = when {
                    graphRibItems[2].startsWith("assign_") -> {
                        val label = graphRibItems[2].removePrefix("assign_")
                        if (label.startsWith("r_")) {
                            Pair(LabelType.ASSIGN_R, label.removePrefix("r_"))
                        } else {
                            Pair(LabelType.ASSIGN, label)
                        }
                    }

                    else -> throw IllegalArgumentException("Must start with assign")
                }
                val (funIdString, contextNumberString, contextEdgeType) = rest.split("_")
                val contextNumber = contextNumberString.toInt() * (if (contextEdgeType == "open") 1 else -1)
                LineItem(label, label, contextNumber, funIdString.toInt())
            }
        }
        val fieldId = if (realType in LabelType.STORE..LabelType.STORE_R) graphRibItems[3].toInt() else -1
        if (context != 0) {
            if (!functionalContextToContextToVertexSet.contains(funId)) {
                functionalContextToContextToVertexSet[funId] = mutableMapOf()
            }
            if (!functionalContextToContextToVertexSet[funId]!!.contains(context.absoluteValue)) {
                functionalContextToContextToVertexSet[funId]!![context.absoluteValue] = mutableSetOf()
            }
            functionalContextToContextToVertexSet[funId]!![context.absoluteValue]!!.add(firstVertex)
            functionalContextToContextToVertexSet[funId]!![context.absoluteValue]!!.add(secondVertex)
        }
        ribs.add(Rib(firstVertex, secondVertex, Label(context, label, funId, realType, fieldId)))
    }
    return Pair(Graph(ribs, numberOfVertexes), functionalContextToContextToVertexSet)
}

fun dumpGraphToFile(filename: String, graph: Graph, exclude: Set<Int>, renumeration: MutableMap<Int, MutableMap<Int, Int>>) {
    fun calculateContextNumber(label: Label): Int {
        return renumeration[label.funId]!![label.contextId.absoluteValue]!!
    }
    File(filename).bufferedWriter().use { file ->
        graph.ribs.forEach { r ->
            file.write("${r.firstVertex}\t${r.secondVertex}\t")
            file.write(
                when (r.label.realType) {
                    LabelType.ASSIGN -> if (r.label.hasContext() && !(exclude.contains(r.firstVertex) && exclude.contains(r.secondVertex)))
                                            "assign_${calculateContextNumber(r.label)}_${r.label.openCloseString()}"
                                        else
                                            "assign"
                    LabelType.ASSIGN_R -> if (r.label.hasContext() && !(exclude.contains(r.firstVertex) && exclude.contains(r.secondVertex)))
                                              "assign_r_${calculateContextNumber(r.label)}_${r.label.openCloseString()}"
                                          else
                                              "assign_r"
                    LabelType.ALLOC -> "alloc"
                    LabelType.ALLOC_R -> "alloc_r"
                    LabelType.STORE -> "store_i ${r.label.fieldId}"
                    LabelType.LOAD -> "load_i ${r.label.fieldId}"
                    LabelType.LOAD_R -> "load_r_i ${r.label.fieldId}"
                    LabelType.STORE_R -> "store_r_i ${r.label.fieldId}"
                    else -> throw IllegalArgumentException("incorrect rib for the general graph")
                }
            )
            file.newLine()
        }
        file.flush()
        file.close()
    }
}
