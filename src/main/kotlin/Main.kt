package ru.yandex.mylogininya

import java.io.File
import kotlin.collections.mutableMapOf
import kotlin.math.max

fun main() {
    File("graphs.txt").bufferedReader().forEachLine {
        val (graphFilename, vertexFilename, resultFile) = it.split(" ")
        println(graphFilename)
        val (graph, functionalContextToContextToVertexSet) = readGraphFromFile(graphFilename, vertexFilename)
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
        val functionalContextToContextToNewContext = mutableMapOf<Int, MutableMap<Int, Int>>()
        var maxContextNumber = 0
        var maxOldNumber = 0
        functionalContextToContextToVertexSet.forEach { (funId, contextToVertexSet) ->
            var newContextId = 0
            contextToVertexSet.forEach { (context, set) ->
                maxOldNumber = max(maxOldNumber, context)
                if (!set.all { value -> insensitiveVertexes.contains(value) }) {
                    if (!functionalContextToContextToNewContext.contains(funId)) {
                        functionalContextToContextToNewContext[funId] = mutableMapOf()
                    }
                    functionalContextToContextToNewContext[funId]!![context] = ++newContextId
                }
            }
            maxContextNumber = max(newContextId, maxContextNumber)
        }
        File("$resultFile.ctxn").bufferedWriter().use { file ->
            file.write("$maxContextNumber")
        }
        println("Decrease is ${maxOldNumber - maxContextNumber}")
        dumpGraphToFile("$resultFile.g", graph, insensitiveVertexes, functionalContextToContextToNewContext)
    }
}