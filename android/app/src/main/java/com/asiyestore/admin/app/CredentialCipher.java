package com.asiyestore.admin.app;

import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** AES-GCM binds each encrypted value to its provider ID; keys are supplied by Android Keystore. */
public final class CredentialCipher {
    private CredentialCipher() { }
    public static byte[] seal(SecretKey key, String id, String value) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        cipher.updateAAD(id.getBytes(StandardCharsets.UTF_8));
        byte[] nonce = cipher.getIV(), encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        if (nonce.length != 12) throw new IllegalStateException("invalid nonce");
        byte[] payload = new byte[nonce.length + encrypted.length];
        System.arraycopy(nonce, 0, payload, 0, nonce.length);
        System.arraycopy(encrypted, 0, payload, nonce.length, encrypted.length);
        return payload;
    }
    public static String open(SecretKey key, String id, byte[] payload) throws Exception {
        if (payload.length < 29 || payload.length > 8192) throw new IllegalStateException("unreadable credential");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, payload, 0, 12));
        cipher.updateAAD(id.getBytes(StandardCharsets.UTF_8));
        return new String(cipher.doFinal(payload, 12, payload.length - 12), StandardCharsets.UTF_8);
    }
}
