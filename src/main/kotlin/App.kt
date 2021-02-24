import com.runner.SimpleHttpClientRunner


suspend fun main() {
    SimpleHttpClientRunner(
            inputFilePath = "input here",
            outputFilePath = "./output.txt",
            endPoint1 = "http://localhost:8081/",
            endPoint2 = "http://localhost:8081/"
    ).run()
}

