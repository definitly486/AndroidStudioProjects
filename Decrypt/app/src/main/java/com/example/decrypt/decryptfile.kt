@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.example.decrypt

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.GeneralSecurityException

/**Decrypts one file to a second file using a key derived from a passphrase: */
@Throws(GeneralSecurityException::class, IOException::class)
fun decryptFile(fileName: String?, pass: String) {
    val encData: ByteArray?
    var decData: ByteArray
    val inFile = File(fileName+".enc")

    //Generate the cipher using pass:
    val cipher = makeCipherver2(pass, false)

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
        decData = decData.copyOfRange(0, decData.size - padCount)
    }


    //Write the decrypted data to a new file:
    val target = FileOutputStream(File("$fileName"))
    target.write(decData)
    target.close()
}

