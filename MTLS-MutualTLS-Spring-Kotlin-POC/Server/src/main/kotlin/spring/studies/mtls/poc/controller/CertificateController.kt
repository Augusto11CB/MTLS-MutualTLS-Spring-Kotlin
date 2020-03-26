package spring.studies.mtls.poc.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class CertificateController() {

    fun requestSigningClientCertificate(): ResponseEntity<*> {

        return ResponseEntity.ok("OK")
    }

}