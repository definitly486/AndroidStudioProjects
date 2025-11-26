import java.io.File

object KernelSUInstaller {

    private const val MODULE_NAME = "APatch-KSU.zip"
    private const val DOWNLOAD_PATH = "/storage/emulated/0/Download"

    /**
     * Устанавливает строго APatch-KSU.zip из /sdcard/Download/
     * @return true — установлен успешно, false — файл не найден или ошибка
     */
    fun installAPatchKSU(): Boolean {
        val moduleFile = File("$DOWNLOAD_PATH/$MODULE_NAME")

        if (!moduleFile.exists()) {
            println("APatch-KSU.zip не найден в $DOWNLOAD_PATH")
            return false
        }

        if (!moduleFile.canRead()) {
            println("Нет доступа на чтение к $MODULE_NAME")
            return false
        }

        println("Устанавливаю $MODULE_NAME (${moduleFile.length() / 1024} КБ)...")
        return installViaKsud(moduleFile.absolutePath)
    }

    private fun installViaKsud(modulePath: String): Boolean {
        var process: Process? = null
        var os: java.io.DataOutputStream? = null

        try {
            process = Runtime.getRuntime().exec("su -mm")
            os = java.io.DataOutputStream(process.outputStream).apply {
                writeBytes("ksud module install \"$modulePath\"\n")
                writeBytes("exit \$?\n")
                flush()
            }

            val exitCode = process.waitFor()
            return exitCode == 0
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            os?.close()
            process?.destroy()
        }
    }
}