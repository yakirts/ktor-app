import com.runner.IguazioAssignmentRunner


suspend fun main() {
    IguazioAssignmentRunner(
            inputFilePath = "/home/yakirt/Downloads/dha-20210216T081141Z-001/Dina- home assignment/370098-lines.txt",
            outputFilePath = "./output.txt",
            endPoint1 = "http://localhost:8081/",
            endPoint2 = "http://localhost:8082/"
    ).run()
}

