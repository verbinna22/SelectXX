package ru.yandex.mylogininya

import java.io.File

fun main() {
    File("graphs.txt").bufferedReader().forEachLine {
        val (graphFilename, vertexFilename, resultFile) = it.split(" ")
        val graph = readGraphFromFile(graphFilename, vertexFilename)
        val (regularGraph, vertexToContextToChildren, vertexToContextToParents) = buildRegularGraph(graph)
        val insensitiveVertexes = selectX(regularGraph, vertexToContextToChildren, vertexToContextToParents)
        File(resultFile).bufferedWriter().use { file ->
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
    }
}