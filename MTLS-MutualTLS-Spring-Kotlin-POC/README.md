
## External CA

### KeyStore
    keytool -genkeypair -keyalg RSA -keysize 2048 -alias external-ca -dname "CN=External-CA,OU=Certificate Authority External,O=Guarulhos,C=BR" -validity 3650 -ext bc:c -keystore external-ca/identity.jks -storepass secret -keypass secret -deststoretype pkcs12

### Extract .p12
    keytool -importkeystore -srckeystore external-ca/identity.jks -destkeystore external-ca/external- ca.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass secret -deststorepass secret

### Extract .pem
    openssl pkcs12 -in external-ca/external-ca.p12 -out external-ca/external-ca.pem -nokeys -passin pass:secret -passout pass:secret

### Extract .key (private key)
    openssl pkcs12 -in external-ca/external-ca.p12 -out external-ca/external-ca.key -nocerts -passin pass:secret -passout pass:secret


## Internal CA 

### KeyStore
    keytool -genkeypair -keyalg RSA -keysize 2048 -alias internal-ca -dname "CN=Internal-CA,OU=Certificate Authority Internal,O=SãoPaulo,C=BR" -validity 3650 -ext bc:c -keystore internal-ca/identity.jks -storepass secret -keypass secret -deststoretype pkcs12

### Extract .p12
    keytool -importkeystore -srckeystore internal-ca/identity.jks -destkeystore internal-ca/internal-ca.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass secret -deststorepass secret

### Extract .pem
    openssl pkcs12 -in internal-ca/internal-ca.p12 -out internal-ca/internal-ca.pem -nokeys -passin pass:secret -passout pass:secret

### Extract .key (private key)
    openssl pkcs12 -in internal-ca/internal-ca.p12 -out internal-ca/internal-ca.key -nocerts -passin pass:secret -passout pass:secret



-- KeyStore Server
keytool -genkeypair -keyalg RSA -keysize 2048 -alias server -dname "CN=Server MTLS,OU=Server MTLS POC,O=SãoPaulo,C=BR" -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -validity 3650 -keystore Server/src/main/resources/identity.jks -storepass secret -keypass secret -deststoretype pkcs12

-- .csr Server
keytool -certreq -keystore Server/src/main/resources/identity.jks -alias server -keypass secret -storepass secret -keyalg rsa -file Server/src/main/resources/server.csr

-- Get .csr signed by CA
openssl x509 -req -in Server/src/main/resources/server.csr -CA external-ca/external-ca.pem -CAkey external-ca/external-ca.key -CAcreateserial -out Server/src/main/resources/server-signed.cer -days 1825 -passin pass:secret


-- Include the new signed Certificate keystore
    -- get private key from keystore server
    keytool -importkeystore -srckeystore Server/src/main/resources/identity.jks -destkeystore Server/src/main/resources/server.p12 -srcstoretype jks -deststoretype pkcs12 -srcstorepass secret -deststorepass secret
    openssl pkcs12 -in Server/src/main/resources/server.p12 -nodes -out Server/src/main/resources/server-private.key -nocerts -passin pass:secret
    
    -- Generate new p12 with signed certificate
    openssl pkcs12 -export -in Server/src/main/resources/server-signed.cer -inkey Server/src/main/resources/server-private.key -out Server/src/main/resources/server-signed.p12 -name server -passout pass:secret

    -- Cleaning old certificates not signed
    keytool -delete -alias server -keystore Server/src/main/resources/identity.jks -storepass secret

    -- Include new p12 signed in the keystore
    keytool -importkeystore -srckeystore Server/src/main/resources/server-signed.p12 -srcstoretype PKCS12 -destkeystore Server/src/main/resources/identity.jks -srcstorepass secret -deststorepass secret


--Import External-CA Certificate Server
keytool -keystore Server/src/main/resources/truststore.jks -importcert -file external-ca/external-ca.pem -alias external-ca -storepass secret

--Import Internal-CA Certificate Server
keytool -keystore Server/src/main/resources/truststore.jks -import -file internal-ca/internal-ca.pem -alias internal-ca -storepass secret

--Import External-CA Certificate Client
keytool -keystore Client/src/main/resources/truststore.jks -importcert -file external-ca/external-ca.pem -alias external-ca -storepass secret