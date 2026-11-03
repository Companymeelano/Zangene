package com.asiyestore.admin.app;
import java.util.Arrays;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
public class CredentialRegression {
    private static void check(boolean b) { if (!b) throw new AssertionError(); }
    public static void main(String[] args) throws Exception {
        KeyGenerator gen = KeyGenerator.getInstance("AES"); gen.init(256); SecretKey key = gen.generateKey();
        byte[] one = CredentialCipher.seal(key, "openai", "test-value");
        byte[] two = CredentialCipher.seal(key, "openai", "test-value");
        check(!Arrays.equals(one, two));
        check(CredentialCipher.open(key, "openai", one).equals("test-value"));
        try { CredentialCipher.open(key, "groq", one); throw new AssertionError(); } catch (javax.crypto.AEADBadTagException expected) { }
        one[one.length - 1] ^= 1;
        try { CredentialCipher.open(key, "openai", one); throw new AssertionError(); } catch (javax.crypto.AEADBadTagException expected) { }
        try { CredentialCipher.open(key, "openai", new byte[12]); throw new AssertionError(); } catch (IllegalStateException expected) { }
        System.out.println("Credential cipher regression checks passed.");
    }
}
