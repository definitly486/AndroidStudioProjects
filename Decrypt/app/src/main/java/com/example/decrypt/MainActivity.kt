package com.example.decrypt
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var counter = 0
    private lateinit var passwordid: TextView
    private lateinit var inputEditText: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Предполагается, что у вас есть layout с id activity_main

        passwordid = findViewById(R.id.passwordline) // Идентификатор TextView в layout
        inputEditText = findViewById(R.id.getpassword) // Идентификатор EditText в layout
        val incrementButton: Button = findViewById(R.id.decrypt) // Идентификатор Button в layout

        incrementButton.setOnClickListener {
            try {
                val incrementValue = inputEditText.text.toString().toInt()
                passwordid.text = incrementValue.toString()
            } catch (e: NumberFormatException) {
                inputEditText.error = "Некорректный ввод" // Выводим ошибку в EditText
            }
        }

        // Восстановление значения счетчика из SharedPreferences при запуске
        val sharedPreferences = getSharedPreferences("counter_prefs", MODE_PRIVATE)
        counter = sharedPreferences.getInt("counter_value", 0)
        passwordid.text = counter.toString()
    }


    fun decr(view: View) {

        val incrementValue = inputEditText.text.toString().toInt()
        passwordid.text = incrementValue.toString()
     //  decrypt("/storage/emulated/0/Download/com.qflair.browserq.tar.xz.enc")
     //   decryptFile("/storage/emulated/0/Download/com.qflair.browserq.tar.xz.enc",
      //      passwordid.toString()
     //   )

        decryptFile("/storage/emulated/0/Download/com.qflair.browserq.tar.xz",
                  passwordid.toString())
}

    fun encryptfile(view: View) {


        encryptFilever2("/storage/emulated/0/Download/com.qflair.browserq.tar.xz","")

    }
}

