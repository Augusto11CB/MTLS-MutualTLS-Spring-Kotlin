package spring.studies.mtls.poc.service.impl

import spring.studies.mtls.poc.service.CertificateSigningManager

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.crypto.params.AsymmetricKeyParameter
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.bc.BcECContentSignerBuilder
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder
import org.bouncycastle.pkcs.PKCS10CertificationRequest
import java.io.ByteArrayInputStream

import java.security.cert.Certificate
import java.security.cert.X509Certificate

import java.util.*
import java.math.BigInteger
import java.security.cert.CertificateFactory

abstract class CertificateSigningManagerImpl(
        val sigAlgoName: String,
        val hashAlgoName: String,
        val sigAlgoId: AlgorithmIdentifier,
        val digAlgoId: AlgorithmIdentifier,
        val caPrivKeyParam: AsymmetricKeyParameter,
        val caCertificate: X509Certificate?
        ) : CertificateSigningManager {

    override fun signCSR(
            clientCSR: PKCS10CertificationRequest,
            certificateID: BigInteger,
            notBefore: Date,
            notAfter: Date,
            csrX500NameOverride: X500Name?,
            extensions: List<Extension>?
    ): Certificate {

        val vp = JcaContentVerifierProviderBuilder().build(clientCSR.subjectPublicKeyInfo)

        //Validating the signature on the PKCS10 certification request in the clientCSR.
        if (!clientCSR.isSignatureValid(vp)) {
            throw SecurityException("ERROR: CSR signature verification failure.")
        }

        var csrName = csrX500NameOverride ?: clientCSR.subject // It is dangerous to use subject from csr


        val caName = if (caCertificate != null) {
            X500Name(caCertificate.subjectX500Principal.name)
        } else {
            // Selfsig mode
            csrName
        }

        val certBuilder = X509v3CertificateBuilder(
                caName, certificateID,
                notBefore, notAfter,
                csrName,
                clientCSR.subjectPublicKeyInfo
        )

        if (extensions != null) {
            for (ext in extensions) {
                certBuilder.addExtension(ext)
            }
        }

        val sigGen: ContentSigner = when (sigAlgoName) {
            "RSA" -> BcRSAContentSignerBuilder(sigAlgoId, digAlgoId)
            "ECDSA" -> BcECContentSignerBuilder(sigAlgoId, digAlgoId)
            else -> throw IllegalArgumentException("Error! Unsupported sigAlgoName")
        }.build(caPrivKeyParam)

        val certHolder = certBuilder.build(sigGen);
        val certStructure = certHolder.toASN1Structure();

        val certFactory = CertificateFactory.getInstance("X509", "BC");

        return certFactory.generateCertificate(ByteArrayInputStream(certStructure.encoded))
    }
}