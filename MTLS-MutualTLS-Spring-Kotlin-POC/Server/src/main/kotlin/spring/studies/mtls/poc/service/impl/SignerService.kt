package spring.studies.mtls.poc.service.impl

import sun.security.x509.*
import java.security.Key
import java.security.PrivateKey
import java.text.MessageFormat
import java.util.*


class SignerService {
    @Throws(Exception::class)
    private fun doSelfCert(alias: String, dname: String?, sigAlgName: String) {
        var alias: String? = alias
        var sigAlgName: String? = sigAlgName
        if (alias == null) {
            alias = keyAlias
        }
        val objs: Pair<Key, CharArray> = recoverKey(alias, storePass, keyPass)
        val privKey = objs.fst as PrivateKey
        if (keyPass == null) keyPass = objs.snd
        // Determine the signature algorithm
        if (sigAlgName == null) {
            sigAlgName = getCompatibleSigAlgName(privKey.algorithm)
        }
        // Get the old certificate
        val oldCert: Certificate = keyStore.getCertificate(alias)
        if (oldCert == null) {
            val form = MessageFormat(rb.getString("alias.has.no.public.key"))
            val source = arrayOf<Any?>(alias)
            throw Exception(form.format(source))
        }
        if (oldCert !is X509Certificate) {
            val form = MessageFormat(rb.getString("alias.has.no.X.509.certificate"))
            val source = arrayOf<Any?>(alias)
            throw Exception(form.format(source))
        }
        // convert to X509CertImpl, so that we can modify selected fields
        // (no public APIs available yet)
        val encoded: ByteArray = oldCert.getEncoded()
        val certImpl = X509CertImpl(encoded)
        val certInfo = certImpl[X509CertImpl.NAME
                + "." +
                X509CertImpl.INFO] as X509CertInfo
        // Extend its validity
        val firstDate: Date = getStartDate(startDate)
        val lastDate = Date()
        lastDate.time = firstDate.time + validity * 1000L * 24L * 60L * 60L
        val interval = CertificateValidity(firstDate,
                lastDate)
        certInfo[X509CertInfo.VALIDITY] = interval
        // Make new serial number
        certInfo[X509CertInfo.SERIAL_NUMBER] = CertificateSerialNumber(
                Random().nextInt() and 0x7fffffff)
        // Set owner and issuer fields
        val owner: X500Name
        if (dname == null) { // Get the owner name from the certificate
            owner = certInfo[X509CertInfo.SUBJECT + "." +
                    CertificateSubjectName.DN_NAME] as X500Name
        } else { // Use the owner name specified at the command line
            owner = X500Name(dname)
            certInfo[X509CertInfo.SUBJECT + "." +
                    CertificateSubjectName.DN_NAME] = owner
        }
        // Make issuer same as owner (self-signed!)
        certInfo[X509CertInfo.ISSUER + "." +
                CertificateIssuerName.DN_NAME] = owner
        // The inner and outer signature algorithms have to match.
        // The way we achieve that is really ugly, but there seems to be no
        // other solution: We first sign the cert, then retrieve the
        // outer sigalg and use it to set the inner sigalg
        var newCert = X509CertImpl(certInfo)
        newCert.sign(privKey, sigAlgName)
        val sigAlgid = newCert[X509CertImpl.SIG_ALG] as AlgorithmId
        certInfo[CertificateAlgorithmId.NAME + "." +
                CertificateAlgorithmId.ALGORITHM] = sigAlgid
        certInfo[X509CertInfo.VERSION] = CertificateVersion(CertificateVersion.V3)
        val ext: CertificateExtensions = createV3Extensions(
                null,
                certInfo[X509CertInfo.EXTENSIONS] as CertificateExtensions,
                v3ext,
                oldCert.getPublicKey(),
                null)
        certInfo[X509CertInfo.EXTENSIONS] = ext
        // Sign the new certificate
        newCert = X509CertImpl(certInfo)
        newCert.sign(privKey, sigAlgName)
        // Store the new certificate as a single-element certificate chain
        keyStore.setKeyEntry(alias, privKey,
                if (keyPass != null) keyPass else storePass, arrayOf<Certificate>(newCert))
        if (verbose) {
            System.err.println(rb.getString("New.certificate.self.signed."))
            System.err.print(newCert.toString())
            System.err.println()
        }
    }
}
