# MTLS-MutualTLS-Spring-Kotlin
Mutual TLS authentication with SpringBoot using Kotlin sample


# TLS - HTTPS - CAs - Certificates


## TLS vs SSL
Both TLS (Transport Layer Security) and SSL (Secure Sockets Layer) are cryptographic protocols that provide security over a network. The TLS is the evolution (the most updated version) of SSL which now is deprecated since 2015 by the IETF (Internet Engineering Task Force).

## HTTPS
HTTPS is the HTTP procotol running over TLS, in other words is the secured version of HTTP. By using HTTPS **every packet** transferred between client and server is **encrypted** using public or private key cryptography.

### What does HTTPS ensure?
1. **Privacy** - It means that no one can eavesdrop on your messages
2. **Integrity** - It means that the message is not manipulated on the way to its destination
3. **Identification** - It means that the site that are been visited is indeed the one that should be visited not a fake one.

### Encryptation Algorithms
1. **Symmetric Key Algorithm** 
There is only one key to encrypt and decrypt a message. The Encryption key is mixed in with the message, so even if the encryption algorithm is known, whithout the key, the message is still nonsense. 

The bad point of Symmetric keys is  that they are hard to share.

2. **Asymmetric Keys Algorithms**
In this algorithm there are two keys,  one is public and the other one is private. The public key is used by the sender to encrypt its message and the private key is used by the receiver to decrypt the message.

**public key cryptography**
Any message encrypted with Bob's public key can only be decrypted with Bob's private key.

**signature**
Anyone with access to Alice's public key can verify that a message (signature) could only have been created by someone with access to Alice"s private key

## Digital Certificates
[TO STUDY](https://www.geeksforgeeks.org/digital-signatures-certificates/)
## Digital Signatures
[TO STUDY](https://www.geeksforgeeks.org/digital-signatures-certificates/)

## CA - Certification Authority
How to make sure that the party we are talking to is actually who they claim they are? This is where the certificate authority comes in.

A certificate authority (CA) is a third-party organization with 3 main **objectives**.
1. Issuing Certificates
2. Confirming the identity of the certificate owner
3. Provide proofthat the certificate is valid

**Type of certificates**
1. Domain Validated: This certificate just verifies the domain name, and nothing else.
2. Organization Validated: The certificate requires the validation and manual verification of the organization behind the certificate
3. Extended Validation: The certificate requires an exhaustive verification of the business 

### How Do Certificates Get Validated?  It is done by the "Chain of trust"
When a CA issues a certificate, they sign the certificate with their root certificate pre-installed in the root store (A root store is basically a database of trusted CAs).

**PS:** Most of the time it's an intermediate certificate signed with a root certificate.

* The browser connects to a site via HTTPS and downloads the certificate. 
* The certificate is not a root certificate. 
* The browser downloads the certificate that was used to sign the certificate on the site, but this certificate is still not the root certificate (Mid Certificat). 
* The browser once more looks up the certificate that signed the intermediate certificate. 
* Now it is the **root certificate**. 
* The entire certificate chain is trusted, and thus the site certificate is trusted as well.

### How to get a certificate from a CA?
1. Create a **Certificate Signing Request** with the key pair (public and private key)
2. Ask a **CA to sign** the provided certificate from step 1 **with its private key** **(anyone who has the public key of the CA can verify that it was actually signed by the CA)**

 Now that the requester has its certificate signed by a CA, everyone who needs to verify its identity checks if the certified provided is signed by a CA by using the CA public key to authenticate. If it was a valid certificate (the one signed by a CA), we can securely use the public key "attached" in the certificate to send our secret key. From now on the communication will be encrypted. 

## The HTTPS Handshake
The **process to established a secure connection** to transmit messages is called **handshake**. Here some agreements are made in order to determine how to communicate securely.

1. The client send a list of SSL/TLS  versions and encryption algorithms (**Cypher Suite**) that it can works. 
2. The server choose the best SSL/TLS version and encryption algorithm and reply with its certificate which includes public key, so they can verify the server identity.
3. Client verifies the received certificate against a CA using it's public key. If the validation is consistent, the client generates a "pre master key" that is encrypted by the server public key. In the end the client send a request to the server with the generated content.
4. The server decrypt the "pre master key".
5. Both server and client have generated the same "shared secret" that they are going to use as a symmetric key

## Keystore and Truststore
[TO STUDY](https://www.educative.io/edpresso/keystore-vs-truststore)

### Keystore
Keystore is used to store private key and identity certificates that a specific program should present to both parties (server or client) for verification.

**Used to store the server keys (both public and private) along with signed cert**

Nubank Quote: "Keystore não pode ter um certificado da CA interna, pois estes não são válidos para tais clientes externos. É necessário um certificado externo, que para simplificar, pode ser o mesmo para todos os serviços."

### Truststore
Truststore is  is used to store certificates from Certified Authorities (CA) that verify the certificate presented by the server in SSL connection.

Nubank Quote: "A truststore armazena os certificados de confiança usados para verificar os certificados de clientes recebidos. Todos os certificados dos troncos e da raiz da CA interna ficam na truststore."

* represents the list of trusted parties you intend to communicate with

### Handshake Exemplification
During the SSL handshake,

1.  A client tries to access https://
    
2.  And thus, Server responds by providing a SSL certificate (which is stored in its keyStore)
    
3.  Now, the client receives the SSL certificate and verifies it via trustStore (i.e the client's trustStore already has pre-defined set of certificates which it trusts.). Its like : Can I trust this server ? Is this the same server whom I am trying to talk to ? No middle man attacks ?
    
4.  Once, the client verifies that it is talking to server which it trusts, then SSL communication can happen over a shared secret key.

**WARNING:** If a server wants to do a client authentication too, then the server also maintains a trustStore to verify client.

<<<<<<< Updated upstream
=======
## Files extensions Definitions
* PKCS12 - <name>..p12 
The .p12 contains both the private and the public key, and also information about the owner (name, email address, etc. ) all being certified by a third party. With such certificate, a user can identify himself and authenticate himself to any organization trusting the third party.

### Create a keystore with public and private key
*server*

`keytool -genkeypair -keyalg RSA -keysize 2048 -alias server -dname "CN=Hakan,OU=Amsterdam,O=Thunderberry,C=NL" -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -validity 3650 -keystore server/src/main/resources/identity.jks -storepass secret -keypass secret -deststoretype pkcs12`

*client*

`keytool -genkeypair -keyalg RSA -keysize 2048 -alias client -dname "CN=Suleyman,OU=Altindag,O=Altindag,C=NL" -validity 3650 -keystore client/src/main/resources/identity.jks -storepass secret -keypass secret -deststoretype pkcs12`

### Export certificate

*server*

`keytool -exportcert -keystore server/src/main/resources/identity.jks -storepass secret -alias server -rfc -file server/src/main/resources/server.cer`

*client*

`keytool -exportcert -keystore client/src/main/resources/identity.jks -storepass secret -alias client -rfc -file client/src/main/resources/client.cer`


### Create trusststore 

*server*
`keytool -keystore server/src/main/resources/truststore.jks -importcert -file client/src/test/resources/client.cer -alias client -storepass secret`

*client*

`keytool -keystore client/src/main/resources/truststore.jks -importcert -file server/src/main/resources/server.cer -alias server -storepass secret`


---


### Creating a Certificate Authority

 Here we will create your own Certificate Authority and sign the Client and Server certificate with it. 

`keytool -genkeypair -keyalg RSA -keysize 2048 -alias root-ca -dname "CN=Root-CA,OU=Certificate Authority,O=Thunderberry,C=NL" -validity 3650 -ext bc:c -keystore root-ca/identity.jks -storepass secret -keypass secret -deststoretype pkcs12`

### Creating a Certificate Signing Request
Before Get the certificates signed by the C.A a Certificate Signing Request **(.csr)** must be created. 
**PS:**The Certificate Authority need these csr files to be able to sign it. The next step will be signing the requests.

*server*

`keytool -certreq -keystore MTLS-MutualTLS-Spring-Kotlin-POC/server/src/main/resources/identity.jks -alias server -keypass secret -storepass secret -keyalg rsa -file MTLS-MutualTLS-Spring-Kotlin-POC/server/src/main/resources/server.csr`


*client*

`keytool -certreq -keystore MTLS-MutualTLS-Spring-Kotlin-POC/client/src/main/resources/identity.jks -alias client -keypass secret -storepass secret -keyalg rsa -file MTLS-MutualTLS-Spring-Kotlin-POC/client/src/main/resources/client.csr`


## Generate Important Files Before Signing the certificate with the Certificate Signing Request (.csr)

The .csr file can be signed with a *pem* file and the *CA private key*. 

PS: The *pem* file is a container format that might include: public certificate, CA certificates files, may include an entire certificate chain including public key, private key, and root certificates.

### extract the pem and key file from the identity.jks 

#### CA - Converting CA keystore to a p12 file

`keytool -importkeystore -srckeystore root-ca/identity.jks -destkeystore root-ca/root-ca.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass secret -deststorepass secret`

#### Create pem file from p12 file

`openssl pkcs12 -in root-ca/root-ca.p12 -out root-ca/root-ca.pem -nokeys -passin pass:secret -passout pass:secret`

#### Create a key file from a p12 file

`openssl pkcs12 -in root-ca/root-ca.p12 -out root-ca/root-ca.key -nocerts -passin pass:secret -passout pass:secret`

### Signing the client certificate

`openssl x509 -req -in MTLS-MutualTLS-Spring-Kotlin-POC/client/src/main/resources/client.csr -CA root-ca/root-ca.pem -CAkey root-ca/root-ca.key -CAcreateserial -out MTLS-MutualTLS-Spring-Kotlin-POC/client/src/main/resources/client-signed.cer -days 1825 -passin pass:secret`

### Signing the server certificate

`openssl x509 -req -in MTLS-MutualTLS-Spring-Kotlin-POC/server/src/main/resources/server.csr -CA root-ca/root-ca.pem -CAkey root-ca/root-ca.key -CAcreateserial -out MTLS-MutualTLS-Spring-Kotlin-POC/server/src/main/resources/server-signed.cer -sha256 -extfile server/src/main/resources/extensions/v3.ext -days 1825 -passin pass:secret`


## Replace Self-Signed Certificates by the CA-Signed Certificates (:D)


*server*
1. Extractiong .p12 files 

`keytool -importkeystore -srckeystore server/src/main/resources/identity.jks -destkeystore server/src/main/resources/server.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass secret -deststorepass secret`

### How do I import a PKCS12 certificate into a java keystore?
> keytool -v -importkeystore -srckeystore alice.p12 -srcstoretype PKCS12 -destkeystore bob.jks -deststoretype JKS
>>>>>>> Stashed changes

## References 
[How Http Works]([https://howhttps.works/the-handshake/](https://howhttps.works/the-handshake/))
[Trust Store vs Key Store - creating with keytool](https://stackoverflow.com/questions/6340918/trust-store-vs-key-store-creating-with-keytool)](https://stackoverflow.com/questions/6340918/trust-store-vs-key-store-creating-with-keytool)

