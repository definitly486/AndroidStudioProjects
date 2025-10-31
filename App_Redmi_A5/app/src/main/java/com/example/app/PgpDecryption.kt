import android.content.Context
import androidx.appcompat.app.AlertDialog
import org.bouncycastle.openpgp.*
import org.bouncycastle.openpgp.operator.jcajce.*
import java.io.*

fun decryptPGPFileWithPassword(context: Context){

    val builder = AlertDialog.Builder(context)
    builder.setTitle("Предупреждение ")
    builder.setMessage("Функция не реализована  ")
    builder.setPositiveButton("Продолжить") { dialog, _ ->
        dialog.dismiss()
    }
    builder.show()

}

