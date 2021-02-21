import com.runner.SimpleHttpRunner


suspend fun main() {
    SimpleHttpRunner(
            inputFilePath = "input file here",
            outputFilePath = "./output.txt",
            endPoint1 = "http://localhost:8081/",
            endPoint2 = "http://localhost:8082/"
    ).run()
}

