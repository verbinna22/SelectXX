package ru.yandex.mylogininya

import java.io.File
import java.util.Collections

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

fun readGraphFromFile(ribsFilename: String, vertexMappingFilename: String): Graph {
    val ribs: ArrayList<Rib> = arrayListOf()
    val numberOfVertexes = File(vertexMappingFilename).bufferedReader().lines().count().toInt()
//    val vertexNumberToRibs: ArrayList<ArrayList<Int>> = ArrayList(Collections.nCopies(numberOfVertexes, arrayListOf()))
    File(ribsFilename).bufferedReader().forEachLine {
        val graphRibItems = it.split(" ")
        val firstVertex = graphRibItems[0].toInt()
        val secondVertex = graphRibItems[1].toInt()
        val (label, context) = when (graphRibItems[2]) {
            "alloc" -> Pair(LabelType.ALLOC, 0)
            "alloc_r" -> Pair(LabelType.ALLOC_R, 0)
            "assign", "load_i" -> Pair(LabelType.ASSIGN, 0)
            "assign_r", "load_r_i" -> Pair(LabelType.ASSIGN_R, 0)
            "store_i", "store_r_i" -> Pair(LabelType.STORE, 0)
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
                Pair(label, contextNumber)
            }
        }
        ribs.add(Rib(firstVertex, secondVertex, Label(context, label)))
//        vertexNumberToRibs[firstVertex].add(secondVertex)
    }
    return Graph(ribs, numberOfVertexes)
}
