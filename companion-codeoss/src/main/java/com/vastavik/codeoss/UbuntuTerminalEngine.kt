package com.vastavik.codeoss

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object UbuntuTerminalEngine {

    data class TerminalOutput(
        val text: String,
        val isError: Boolean = false,
        val isPrompt: Boolean = false
    )

    fun getWorkspaceDir(context: Context): File {
        val dir = File(context.filesDir, "workspace")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun saveCodeToFile(context: Context, code: String, language: String): File {
        val workspace = getWorkspaceDir(context)
        val fileName = when (language.lowercase().trim()) {
            "python", "py" -> "solution.py"
            "java" -> "Main.java"
            "c++", "cpp" -> "solution.cpp"
            "c" -> "solution.c"
            "javascript", "js", "node" -> "solution.js"
            "kotlin", "kt" -> "Solution.kt"
            "bash", "shell", "sh" -> "script.sh"
            else -> "solution.txt"
        }
        val file = File(workspace, fileName)
        file.writeText(code)
        return file
    }

    fun getRunCommandForLanguage(language: String, file: File): String {
        val name = file.name
        return when (language.lowercase().trim()) {
            "python", "py" -> "python3 $name"
            "java" -> "javac $name && java ${file.nameWithoutExtension}"
            "c++", "cpp" -> "g++ -O2 $name -o ${file.nameWithoutExtension} && ./${file.nameWithoutExtension}"
            "c" -> "gcc -O2 $name -o ${file.nameWithoutExtension} && ./${file.nameWithoutExtension}"
            "javascript", "js", "node" -> "node $name"
            "kotlin", "kt" -> "kotlinc $name -include-runtime -d ${file.nameWithoutExtension}.jar && java -jar ${file.nameWithoutExtension}.jar"
            "bash", "shell", "sh" -> "bash $name"
            else -> "cat $name"
        }
    }

    suspend fun executeCommand(
        context: Context,
        command: String,
        onOutputLine: (TerminalOutput) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val workspace = getWorkspaceDir(context)
        val prootUbuntu = File(context.filesDir, "ubuntu/rootfs")

        val actualCommand = if (prootUbuntu.exists()) {
            val prootBin = File(context.filesDir, "ubuntu/proot").absolutePath
            "$prootBin -0 -r ${prootUbuntu.absolutePath} -w /root/workspace /bin/bash -c \"$command\""
        } else {
            command
        }

        try {
            val process = ProcessBuilder("/system/bin/sh", "-c", actualCommand)
                .directory(workspace)
                .redirectErrorStream(false)
                .apply {
                    environment()["HOME"] = workspace.absolutePath
                    environment()["TERM"] = "xterm-256color"
                    environment()["USER"] = "root"
                    environment()["PATH"] = "${context.filesDir}/usr/bin:${environment()["PATH"] ?: "/system/bin:/system/xbin"}"
                }
                .start()

            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            var stdLine: String?
            while (stdoutReader.readLine().also { stdLine = it } != null) {
                stdLine?.let { onOutputLine(TerminalOutput(it, isError = false)) }
            }

            var errLine: String?
            while (stderrReader.readLine().also { errLine = it } != null) {
                errLine?.let { onOutputLine(TerminalOutput(it, isError = true)) }
            }

            val exitCode = process.waitFor()
            return@withContext exitCode
        } catch (e: Exception) {
            onOutputLine(TerminalOutput("Terminal Execution Error: ${e.localizedMessage ?: e.message}", isError = true))
            return@withContext -1
        }
    }
}
