package com.asiyestore.admin.app;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.json.JSONObject;

/** Device-local override; embedded credentials remain extractable from the APK. */
public final class StudioCredentials {
    private static final String PREFS = "ai_credentials_v1", ALIAS = "asiye_ai_credentials_v1";
    private StudioCredentials() { }

    private static KeyStore store() throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore"); ks.load(null); return ks;
    }
    private static SecretKey getKey(boolean create) throws Exception {
        KeyStore ks = store();
        if (ks.containsAlias(ALIAS)) return (SecretKey) ks.getKey(ALIAS, null);
        if (!create) throw new IllegalStateException("device key unavailable");
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
        return generator.generateKey();
    }
    private static void checkId(String id) {
        if (id == null || !id.matches("[a-z_]{2,32}")) throw new IllegalArgumentException("invalid provider");
    }
    public static boolean hasOverride(Context ctx, String id) {
        checkId(id); return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(id);
    }
    /** Prior device overrides are retained but superseded for newly supplied package credentials. */
    public static boolean isOverrideActive(Context ctx, String id) {
        checkId(id);
        if (!hasOverride(ctx, id) || ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("disabled_" + id, false)) return false;
        int revision = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("revision_" + id, 0);
        return CredentialRevision.isOverrideActive(id, revision);
    }
    public static void save(Context ctx, String id, String input) throws Exception {
        checkId(id); String value = input.trim();
        if (value.isEmpty() || value.length() > 4096 || value.contains("\n") || value.contains("\r")) throw new IllegalArgumentException("invalid value");
        byte[] payload = CredentialCipher.seal(getKey(true), id, value);
        if (!ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(id, Base64.encodeToString(payload, Base64.NO_WRAP)).putInt("revision_" + id, CredentialRevision.CURRENT).remove("disabled_" + id).commit())
            throw new IllegalStateException("storage failed");
    }
    /** Switch without deleting the device override; reactivation is always explicit. */
    public static void setOverrideEnabled(Context ctx, String id, boolean enabled) {
        checkId(id);
        if (!hasOverride(ctx, id)) throw new IllegalStateException("no device override");
        android.content.SharedPreferences.Editor edit = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        edit.putBoolean("disabled_" + id, !enabled);
        if (enabled) edit.putInt("revision_" + id, CredentialRevision.CURRENT);
        if (!edit.commit()) throw new IllegalStateException("storage failed");
    }
    public static void clear(Context ctx, String id) {
        checkId(id);
        if (!ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(id).remove("revision_" + id).remove("disabled_" + id).commit()) throw new IllegalStateException("storage failed");
    }
    public static String get(Context ctx, String id) throws Exception {
        checkId(id);
        if (isOverrideActive(ctx, id)) {
            byte[] payload = Base64.decode(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(id, ""), Base64.NO_WRAP);
            return CredentialCipher.open(getKey(false), id, payload);
        }
        try (InputStream in = ctx.getAssets().open("studio_provider_keys.json"); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] b = new byte[4096]; int n, count = 0;
            while ((n = in.read(b)) != -1) { count += n; if (count > 16384) throw new IllegalArgumentException("asset too large"); out.write(b, 0, n); }
            return new JSONObject(new String(out.toByteArray(), StandardCharsets.UTF_8)).optString(id, "").trim();
        }
    }
}
