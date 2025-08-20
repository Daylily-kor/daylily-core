package com.daylily.global.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.StringWriter;
import java.util.Base64;

@Slf4j
public class PemKeyConverter {

    /**
     * PKCS#1 형식의 PEM 키를 PKCS#8 형식으로 변환합니다.<br/>
     * GitHub App 등록 시 발급되는 PEM키는 PKCS#1 형식이고, 이를 PKCS#8 형식으로 변환해야합니다.
     */
    public static String convertPkcs1ToPkcs8(String pkcs1Pem) throws Exception {
        if (pkcs1Pem.contains("BEGIN PRIVATE KEY")) {
            return pkcs1Pem; // 이미 PKCS#8 형식이면 그대로 반환
        }

        String keyContent = pkcs1Pem
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        var rsaPrivateKey = RSAPrivateKey.getInstance(keyBytes);

        var algorithmIdentifier = new AlgorithmIdentifier(
                PKCSObjectIdentifiers.rsaEncryption,
                DERNull.INSTANCE
        );

        var privateKeyInfo = new PrivateKeyInfo(algorithmIdentifier, rsaPrivateKey);

        var stringWriter = new StringWriter();
        try (var pemWriter = new PemWriter(stringWriter)) {
            pemWriter.writeObject(new PemObject("PRIVATE KEY", privateKeyInfo.getEncoded()));
        }

        return stringWriter.toString();
    }
}
