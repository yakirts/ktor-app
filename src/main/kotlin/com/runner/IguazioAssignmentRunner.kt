package com.runner

import com.mathcer.CaseStringMatcher
import com.mathcer.StringMatcher
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class IguazioAssignmentRunner(
    inputFilePath: String,
    outputFilePath: String,
    private val endPoint1: String,
    private val endPoint2: String
) : Closeable {

    companion object {
        val LOG: Logger = LogManager.getLogger(IguazioAssignmentRunner.javaClass)
    }

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
            LOG.debug("Sending $chunkSize AsyncRequests...")

            chunk.forEach { entry -> sendAsyncRequest(entry, queue) }

            LOG.debug("Finish sending $chunkSize AsyncRequests...")

            LOG.debug("Start processing response and write to disk...")

            while (qIter.hasNext()) handleResponses(qIter)

            LOG.debug("Done processing response and write to disk")
        }

        val endTime = System.currentTimeMillis() - startTime
        LOG.debug("Total time sending ${inputMap.size} request , and writing to disk [took ${endTime}ms]")
    }

    private suspend fun handleResponses(qIter: MutableIterator<BiDataHolder>) {
        val biDataHolder = qIter.withIndex().next()
        val response1Text = biDataHolder.value.dh1.deferred.await()
        val response2Text = biDataHolder.value.dh2.deferred.await()
        outFile.appendText("${stringMatcher.isMatch(response1Text, response2Text)}\n")
        if (biDataHolder.index % 10_000 == 0) LOG.debug("${biDataHolder.value.dh1.offset} : $response1Text , $response2Text = ${response1Text == response2Text}")
        qIter.remove()
    }

    private fun CoroutineScope.sendAsyncRequest(
        entry: Map.Entry<Int, String>,
        queue: ArrayDeque<BiDataHolder>
    ) {
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
        queue.addLast(BiDataHolder(dd1, dd2))
    }

    override fun close() {
        httpClient.close()
    }

}


private suspend fun sendRequest(client: HttpClient, url: String, param: String): String {
    return client.post {
        url(url)
        contentType(ContentType.Application.Json)
        body = Content(seed = param)
    }
}

data class BiDataHolder(val dh1: DeferredData, val dh2: DeferredData) {
    data class DeferredData(
        val deferred: Deferred<String>,
        val offset: Int
    )
}

data class Content(val seed: String)