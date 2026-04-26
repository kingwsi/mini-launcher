package com.mico.launcher

object ShellUtils {

    fun execute(command: String): Boolean {
        System.out.println("MicoLauncher: Direct Execute -> $command")
        return try {
            val process = Runtime.getRuntime().exec(command)
            
            // 非阻塞读取错误流
            Thread {
                try {
                    val msg = process.errorStream.bufferedReader().readText()
                    if (msg.isNotEmpty()) System.out.println("MicoLauncher: Direct Error -> $msg")
                } catch (e: Exception) {}
            }.start()

            val result = process.waitFor()
            System.out.println("MicoLauncher: Direct Result Code -> $result")
            result == 0
        } catch (e: Exception) {
            System.out.println("MicoLauncher: Direct Exception -> ${e.message}")
            false
        }
    }
}
