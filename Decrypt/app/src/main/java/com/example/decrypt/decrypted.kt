package com.example.decrypt

import android.annotation.SuppressLint
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec


object FileEncryptor {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            encryptFile("C:\\test.txt", "password")
            decryptFile("C:\\test.txt", "password")
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: GeneralSecurityException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    //Arbitrarily selected 8-byte salt sequence:
    private val salt = byteArrayOf(
        0x43.toByte(), 0x76.toByte(), 0x95.toByte(), 0xc7.toByte(),
       0x5b.toByte(), 0xd7.toByte(), 0x45.toByte(), 0x17.toByte()
   )

    @SuppressLint("GetInstance")
    @Throws(GeneralSecurityException::class)
    fun makeCipher(pass: String, decryptMode: Boolean): Cipher {
        //Use a KeyFactory to derive the corresponding key from the passphrase:

        val keySpec = PBEKeySpec(pass.toCharArray(), salt, 65536, 256)
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val key = keyFactory.generateSecret(keySpec)

        //Create parameters from the salt and an arbitrary number of iterations:
        val pbeParamSpec = PBEParameterSpec(salt, 42)

        /*Dump the key to a file for testing: */
        keyToFile(key)

        //Set up the cipher:
        val cipher = Cipher.getInstance("AES/ECB/NoPadding")

        //Set the cipher mode to decryption or encryption:
        if (decryptMode) {
            cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec)
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec)
        }

        return cipher
    }


    /**Encrypts one file to a second file using a key derived from a passphrase: */
    @Throws(IOException::class, GeneralSecurityException::class)
    fun encryptFile(fileName: String, pass: String) {
        val decData: ByteArray?
        val encData: ByteArray
        val inFile = File(fileName)
        //Generate the cipher using pass:
        val cipher = makeCipher(pass, true)

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


    /**Decrypts one file to a second file using a key derived from a passphrase: */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun decryptFile(fileName: String?, pass: String) {
        val encData: ByteArray?
        var decData: ByteArray
        val inFile = File("$fileName.encrypted")

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
            decData = decData.copyOfRange(0, decData.size - padCount)
        }


        //Write the decrypted data to a new file:
        val target = FileOutputStream(File("$fileName.decrypted.txt"))
        target.write(decData)
        target.close()
    }

    /**Record the key to a text file for testing: */
    private fun keyToFile(key: SecretKey) {
        try {
            val keyFile = File("/storage/emulated/0/Download/keyfile.txt")
            val keyStream = FileWriter(keyFile)
            val encodedKey = "\n" + "Encoded version of key:  " + key.encoded.toString()
            keyStream.write(key.toString())
            keyStream.write(encodedKey)
            keyStream.close()
        } catch (e: IOException) {
            System.err.println("Failure writing key to file")
            e.printStackTrace()
        }
    }
}
