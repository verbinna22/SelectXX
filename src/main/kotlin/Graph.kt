package ru.yandex.mylogininya

import java.io.File
import java.util.SortedMap
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

fun readGraphFromFile(ribsFilename: String, vertexMappingFilename: String): Pair<Graph, SortedMap<Int, MutableSet<Int>>> {
    val ribs: ArrayList<Rib> = arrayListOf()
    val numberOfVertexes = File(vertexMappingFilename).bufferedReader().lines().count().toInt()
    val contextToVertexSet = sortedMapOf<Int, MutableSet<Int>>()
//    val vertexNumberToRibs: ArrayList<ArrayList<Int>> = ArrayList(Collections.nCopies(numberOfVertexes, arrayListOf()))
    File(ribsFilename).bufferedReader().forEachLine {
        val graphRibItems = it.split(" ")
        val firstVertex = graphRibItems[0].toInt()
        val secondVertex = graphRibItems[1].toInt()
        val (label, context, realType) = when (graphRibItems[2]) {
            "alloc" -> Triple(LabelType.ALLOC, 0, LabelType.ALLOC)
            "alloc_r" -> Triple(LabelType.ALLOC_R, 0, LabelType.ALLOC_R)
            "assign" -> Triple(LabelType.ASSIGN, 0, LabelType.ASSIGN)
            "load_i" -> Triple(LabelType.ASSIGN, 0, LabelType.LOAD)
            "assign_r" -> Triple(LabelType.ASSIGN_R, 0, LabelType.ASSIGN_R)
            "load_r_i" -> Triple(LabelType.ASSIGN_R, 0, LabelType.LOAD_R)
            "store_i" -> Triple(LabelType.STORE, 0, LabelType.STORE)
            "store_r_i" -> Triple(LabelType.STORE, 0, LabelType.STORE_R)
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
                val (contextNumberString, contextEdgeType) = rest.split("_")
                val contextNumber = contextNumberString.toInt() * (if (contextEdgeType == "open") 1 else -1)
                Triple(label, contextNumber, label)
            }
        }
        val fieldId = if (realType in LabelType.STORE..LabelType.STORE_R) graphRibItems[3].toInt() else -1
        if (!contextToVertexSet.contains(context.absoluteValue)) {
            contextToVertexSet[context.absoluteValue] = mutableSetOf()
        }
        contextToVertexSet[context.absoluteValue]!!.add(firstVertex)
        contextToVertexSet[context.absoluteValue]!!.add(secondVertex)
        ribs.add(Rib(firstVertex, secondVertex, Label(context, label, realType, fieldId)))
//        vertexNumberToRibs[firstVertex].add(secondVertex)
    }
    return Pair(Graph(ribs, numberOfVertexes), contextToVertexSet)
}

fun dumpGraphToFile(filename: String, graph: Graph, exclude: Set<Int>) {
    File(filename).bufferedWriter().use { file ->
        graph.ribs.forEach { r ->
            file.write("${r.firstVertex} ${r.secondVertex}")
            file.write(
                when (r.label.realType) {
                    LabelType.ASSIGN -> if (r.label.hasContext() && !exclude.contains(r.firstVertex) && !exclude.contains(r.secondVertex))
                                            "assign"
                                        else
                                            "assign"
                    LabelType.ASSIGN_R -> if (r.label.hasContext() && !exclude.contains(r.firstVertex) && !exclude.contains(r.secondVertex))
                                            "assign_r"
                                          else "assign_r"
                    LabelType.ALLOC -> "alloc"
                    LabelType.ALLOC_R -> "alloc_r"
                    LabelType.STORE -> "store ${r.label.fieldId}"
                    LabelType.LOAD -> "load ${r.label.fieldId}"
                    LabelType.LOAD_R -> "load_r ${r.label.fieldId}"
                    LabelType.STORE_R -> "store_r ${r.label.fieldId}"
                    else -> throw IllegalArgumentException("incorrect rib for the general graph")
                }
            )
            file.newLine()
        }
        file.flush()
        file.close()
    }
}
