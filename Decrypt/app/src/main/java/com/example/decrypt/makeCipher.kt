package com.example.decrypt

//noinspection SuspiciousImport
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

fun makeCipherver2(pass: String, decryptMode: Boolean): Cipher {
  val passwd = "639639"
    val encryptedFile = File("/storage/emulated/0/Download/com.qflair.browserq.tar.xz.encrypted")
    val fileBytes = encryptedFile.readBytes()

    // Assuming "Salted__" header and 8-byte salt
    val headerSize = 8 // "Salted__".toByteArray().size
    val saltSize = 8
    val expectedHeader = "Salted__".toByteArray(Charsets.US_ASCII)

    if (!fileBytes.copyOfRange(0, headerSize).contentEquals(expectedHeader)) {
        println("File does not have expected OpenSSL 'Salted__' header.")

    }

    val salt = fileBytes.copyOfRange(headerSize, headerSize + saltSize)

    // Derive key and IV (adjust iterations, keyLength, ivSize as per OpenSSL encryption)
    val iterations = 100000 // Example, match OpenSSL's -pbkdf2 iterations
    val keyLength = 256 // AES-256
    val ivSize = 16 // AES block size for IV
    //   Security.addProvider( BouncyCastleProvider())
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val spec: KeySpec = PBEKeySpec(passwd.toCharArray(), salt, iterations, keyLength + ivSize * 8)
    val tmp = factory.generateSecret(spec)
    val derivedBytes = tmp.encoded

    val key = derivedBytes.copyOfRange(0, keyLength / 8)
    val iv = derivedBytes.copyOfRange(keyLength / 8, keyLength / 8 + ivSize)

    val secretKey = SecretKeySpec(key, "AES")
    val ivParameterSpec = IvParameterSpec(iv)

    val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding") // Or appropriate mode/padding

passToFile(pass)

    //Set the cipher mode to decryption or encryption:
    if (decryptMode) {
        cipher.init(Cipher.ENCRYPT_MODE,secretKey , ivParameterSpec)
    } else {
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
    }

    return cipher
}

fun passToFile(pass:String ) {
    try {
        val keyFile = File("/storage/emulated/0/Download/keyfile.txt")
        val keyStream = FileWriter(keyFile)
    //    val encodedKey = "\n" + "Encoded version of key:  " + pass.encoded.toString()
        keyStream.write(pass)
        keyStream.write(pass)
        keyStream.close()
    } catch (e: IOException) {
        System.err.println("Failure writing key to file")
        e.printStackTrace()
    }
}