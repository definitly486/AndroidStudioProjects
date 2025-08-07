package com.example.decrypt

import com.example.decrypt.FileEncryptor.makeCipher
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun encryptFilever2(fileName: String, pass: String) {
    val decData: ByteArray?
    val encData: ByteArray
    val inFile = File(fileName)
    //Generate the cipher using pass:
    val cipher = makeCipherver2(pass, true)

    //Read in the file:
    val inStream = FileInputStream(inFile)

    val blockSize = 8
    //Figure out how many bytes are padded
    val paddedCount = blockSize - (inFile.length().toInt() % blockSize)

    //Figure out full size including padding
    val padded = inFile.length().toInt() + paddedCount

    decData = ByteArray(padded)


    inStream.read(decData)

    inStream.close()

    //Write out padding bytes as per PKCS5 algorithm
    for (i in inFile.length().toInt()..<padded) {
        decData[i] = paddedCount.toByte()
    }

    //Encrypt the file data:
    encData = cipher.doFinal(decData)


    //Write the encrypted data to a new file:
    val outStream = FileOutputStream(File("$fileName.encrypted"))
    outStream.write(encData)
    outStream.close()
}