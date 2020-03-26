package spring.studies.mtls.poc.utils

import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.security.spec.EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec


object IOUtil {

    fun generateSpec(fileName: String, isPublic: Boolean): EncodedKeySpec {
        val file = File(fileName)
        val fileInputStream = FileInputStream(file)
        val dataInputStream = DataInputStream(fileInputStream)
        val keyBytes = ByteArray(file.length() as Int)
        dataInputStream.readFully(keyBytes)
        dataInputStream.close()
        return if (isPublic) X509EncodedKeySpec(keyBytes) else PKCS8EncodedKeySpec(keyBytes) // TODO Why should private key be returned as PKCS8
    }


    /*
    *
    * PKCS#8 is one of the PKCS (Public Key Cryptography Standards) devised and published by RSA Security.
    * PKCS#8 is designed as the Private-Key Information Syntax Standard. It is used to store private keys.
    * When writing a private key in PKCS#8 format in a file, it needs to stored in either DER encoding or PEM encoding. DER and PEM encodings are describes in other chapters in this book.
    * */

    /*

    * PKCS#12 is one of the PKCS (Public Key Cryptography Standards) devised and published by RSA Security. PKCS#12 is designed as the Personal Information Exchange Syntax Standard.
    * PKCS#12 can be used in the same way as JKS (Java KeyStore) to store private keys and certificates together in a single file. In fact, the Java SE "keytool" supports two keystore types: "jks" and "pkcs12".
    *
    * */

    /*References - http://www.herongyang.com/crypto/Key_Formats_PKCS8_PKCS12.html*/
}
