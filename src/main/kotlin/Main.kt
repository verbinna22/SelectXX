package ru.yandex.mylogininya

import java.io.File

fun main() {
    File("graphs.txt").bufferedReader().forEachLine {
        val (graphFilename, vertexFilename, resultFile) = it.split(" ")
        val (graph, contextToVertexSet) = readGraphFromFile(graphFilename, vertexFilename)
        val (regularGraph, vertexToContextToChildren, vertexToContextToParents) = buildRegularGraph(graph)
        val insensitiveVertexes = selectX(regularGraph, vertexToContextToChildren, vertexToContextToParents)
        File(resultFile).bufferedWriter().use { file ->
            file.write(insensitiveVertexes.size.toString())
            file.newLine()
            file.newLine()
            insensitiveVertexes.forEach { v ->
                file.write(v.toString())
                file.newLine()
            }
            file.flush()
            file.close()
        }
        // log
        File("$resultFile.graph.dmp").bufferedWriter().use { file ->
            regularGraph.ribs.forEach { r ->
                file.write(r.toString())
                file.newLine()
            }
            file.flush()
            file.close()
        }
        val missedContextToPreviouslyMissed = sortedMapOf<Int, Int>()
        var previouslyMissed = 0
        contextToVertexSet.forEach { (context, set) ->
            if (set.all { value -> insensitiveVertexes.contains(value) }) {
                missedContextToPreviouslyMissed[context] = previouslyMissed
                previouslyMissed++
            }
        }
        dumpGraphToFile("$resultFile.g", graph, insensitiveVertexes, missedContextToPreviouslyMissed) // TODO if map is empty + dump it
    }
}