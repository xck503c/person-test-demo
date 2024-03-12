package com.xck.jdk.keystore;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;

public class KeystoreTest {

    private static String base = "D:/work/tmp/";

    public static void main(String[] args) throws Exception {
        java.security.Security.addProvider(
                new org.bouncycastle.jce.provider.BouncyCastleProvider()
        );

        String pwd = "123456";
        KeyStore keystore= KeyStore.getInstance("pkcs12");
        keystore.load(null, pwd.toCharArray());
        parse(keystore, "baiwu", pwd, base + "smpp_tls_baiwu.pem",  base + "smpp_tls_baiwu.crt");

        parse(keystore, "dbcpass", pwd, base + "smpp_tls_dbcpass.pem",  base + "smpp_tls_dbcpass.crt");

        FileOutputStream keyFile = new FileOutputStream(base + "smpp_tls_test.keystore");
        keystore.store(keyFile, pwd.toCharArray());
        keyFile.close();
    }

    public static void parse(KeyStore keyStore, String alias, String pwd, String pemPath, String crtPath) throws Exception{
        try(PEMParser pemParser = new PEMParser(new FileReader(pemPath))) {
            Object object = pemParser.readObject();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            RSAPrivateKey result = (RSAPrivateKey)converter.getPrivateKey(((PrivateKeyInfo) object));

            FileInputStream certificateStream = new FileInputStream(crtPath);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            java.security.cert.Certificate[] chain = {};
            chain = certificateFactory.generateCertificates(certificateStream).toArray(chain);
            certificateStream.close();

            keyStore.setKeyEntry(alias, result, pwd.toCharArray(), chain);
        }

    }
}
