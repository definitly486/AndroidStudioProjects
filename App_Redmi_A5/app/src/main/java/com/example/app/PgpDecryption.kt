@file:Suppress("SpellCheckingInspection")


import android.content.Context
import androidx.appcompat.app.AlertDialog

@Suppress("unused")
fun decryptPGPFileWithPassword(context: Context){

    val builder = AlertDialog.Builder(context)
    builder.setTitle("Предупреждение ")
    builder.setMessage("Функция не реализована  ")
    builder.setPositiveButton("Продолжить") { dialog, _ ->
        dialog.dismiss()
    }
    builder.show()

}

