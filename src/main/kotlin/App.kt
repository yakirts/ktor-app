import com.runner.IguazioAssignmentRunner


suspend fun main() {
    IguazioAssignmentRunner(
            inputFilePath = "input path here",
            outputFilePath = "./output.txt",
            endPoint1 = "http://localhost:8081/",
            endPoint2 = "http://localhost:8082/"
    ).run()
}

