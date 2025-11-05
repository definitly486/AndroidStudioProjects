package com.example.app.fragments
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SixthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sixth, container, false)

        // Ищем элемент UI, где вводится ссылка на репозиторий
        val repoUrlField = view.findViewById<EditText>(R.id.repo_url_field)
        // Ищем кнопку для начала процесса клонирования
        val buttonClone = view.findViewById<Button>(R.id.button_clone)

        // Назначаем слушатель кликов на кнопке
        buttonClone.setOnClickListener {
            cloneGIT(repoUrlField.text.toString())
        }

        return view
    }

    // Корутинный метод для клонирования репозитория
    private fun cloneGIT(repoUrl: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val gitCloneInstance = GitClone2(null)
            gitCloneInstance.setRepositoryUrl(repoUrl)
            val downloadDir = "/storage/emulated/0/Android/data/com.example.app/files/Download/"
            gitCloneInstance.setLocalPath(downloadDir)

            // Выполнение клонирования в фоновом потоке
            val result = withContext(Dispatchers.IO) {
                gitCloneInstance.cloneRepository().isSuccess
            }

            if (result) {
                Toast.makeText(requireContext(), "Репозиторий успешно клонирован.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Клонирование завершилось с ошибкой.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}