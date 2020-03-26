package spring.studies.mtls.poc.service;

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import java.io.Reader
import java.math.BigInteger
import java.security.KeyStore
import java.security.cert.Certificate
import java.util.*

interface CertificateSigningManager {

    fun signCSR(pemcsr: Reader, validity: Int, keystore: KeyStore, alias: String, password: CharArray)

    fun signCSR(csr: PKCS10CertificationRequest, certId: BigInteger, notBefore: Date, notAfter: Date,
                csrX500NameOverride: X500Name?, extensions: List<Extension>?): Certificate
