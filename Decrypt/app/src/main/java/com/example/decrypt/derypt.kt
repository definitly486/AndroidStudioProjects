package com.example.decrypt

import android.annotation.SuppressLint
import com.example.decrypt.FileEncryptor.makeCipher
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.security.spec.KeySpec
import javax.crypto.Cipher
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.Arrays
import javax.crypto.CipherInputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
/**Decrypts one file to a second file using a key derived from a passphrase: */
@Throws(GeneralSecurityException::class, IOException::class)
fun decryptFile(fileName: String?, pass: String) {
    val encData: ByteArray?
    var decData: ByteArray
    val inFile = File(fileName)

    //Generate the cipher using pass:
    val cipher = makeCipher(pass, false)

    //Read in the file:
    val inStream = FileInputStream(inFile)
    encData = ByteArray(inFile.length().toInt())
    inStream.read(encData)
    inStream.close()
    //Decrypt the file data:
    decData = cipher.doFinal(encData)

    //Figure out how much padding to remove
    val padCount = decData[decData.size - 1].toInt()

    //Naive check, will fail if plaintext file actually contained
    //this at the end
    //For robust check, check that padCount bytes at the end have same value
    if (padCount >= 1 && padCount <= 8) {
        decData = Arrays.copyOfRange(decData, 0, decData.size - padCount)
    }


    //Write the decrypted data to a new file:
    val target = FileOutputStream(File(fileName + ".decrypted.txt"))
    target.write(decData)
    target.close()
}

