package com.runner

import com.mathcer.CaseStringMatcher
import com.mathcer.StringMatcher
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException

class SimpleHttpRunner(
    inputFilePath: String,
    outputFilePath: String,
    private val endPoint1: String,
    private val endPoint2: String
) : Closeable {

    private val inFile = File(inputFilePath)
    private val outFile = File(outputFilePath)

    private val httpClient = HttpClient(CIO) {
        engine {
            endpoint.connectAttempts = 40
        }
        install(JsonFeature) {
            serializer = GsonSerializer {
                // Configurable .GsonBuilder
                serializeNulls()
                disableHtmlEscaping()
            }
        }
    }

    private val stringMatcher = StringMatcher.createMatcher(CaseStringMatcher::javaClass.name)

    suspend fun run(chunkSize: Int = 100_000) = coroutineScope {
        if (!inFile.exists()) throw FileNotFoundException()

        // read all lines of file and map to with offset(line number)
        val inputMap = inFile.readLines().mapIndexed { index, line -> index to line }.toMap()

        val queue = ArrayDeque<BiDataHolder>(chunkSize)
        val qIter = queue.iterator()

        val startTime = System.currentTimeMillis()

        inputMap.entries.chunked(chunkSize).forEach { chunk ->
            println("Sending $chunkSize AsyncRequests...")

            chunk.forEach { entry ->
                val offset = entry.key
                val dataParam = entry.value
                //async requests here
                val dd1 = BiDataHolder.DeferredData(
                    offset = offset,
                    deferred = async(Dispatchers.IO) { sendRequest(httpClient, endPoint1, dataParam) }
                )

                val dd2 = BiDataHolder.DeferredData(
                    offset = offset,
                    deferred = async(Dispatchers.IO) { sendRequest(httpClient, endPoint2, dataParam) }
                )
                queue.addLast(BiDataHolder(dd1,dd2))
            }

            println("Finish sending $chunkSize AsyncRequests...")

            println("Start processing response and write to disk...")
            while (qIter.hasNext()) {
                val biDataHolder = qIter.next()
                val response1Text = biDataHolder.dh1.deferred.await()
                val response2Text = biDataHolder.dh2.deferred.await()
                outFile.appendText(
                    "${biDataHolder.dh1.offset} : $response1Text , $response2Text = ${
                        stringMatcher.isMatch(
                            response1Text,
                            response2Text
                        )
                    }\n"
                )
                println("${biDataHolder.dh1.offset} : $response1Text , $response2Text = ${response1Text == response2Text}")
                qIter.remove()
            }
            println("Done processing response and write to disk")
        }

        val endTime = System.currentTimeMillis() - startTime
        println("Total time sending ${inputMap.size} request , and writing to disk [took ${endTime}ms]")
    }

    override fun close() {
        httpClient.close()
    }

}


private suspend fun sendRequest(client: HttpClient, url: String, param: String): String {
    return client.post {
        url(url)
        contentType(ContentType.Application.Json)
        body  = Content(seed = param)
    }
}

data class BiDataHolder(val dh1: DeferredData, val dh2: DeferredData) {
    data class DeferredData(
        val deferred: Deferred<String>,
        val offset: Int
    )
}


data class Content(val seed:String)