package com.asiyestore.admin.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ProductImageRegistryTest {
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    public static void main(String[] args) {
        check(ProductImageRegistry.PLACEHOLDER.equals(ProductImageRegistry.drawableName(null)), "null placeholder");
        check(ProductImageRegistry.PLACEHOLDER.equals(ProductImageRegistry.drawableName("")), "empty placeholder");
        check(ProductImageRegistry.PLACEHOLDER.equals(ProductImageRegistry.drawableName("unknown")), "unknown placeholder");
        check(ProductImageRegistry.PLACEHOLDER.equals(ProductImageRegistry.drawableName("generic")), "generic placeholder");
        check("img_gatieau_whitening".equals(ProductImageRegistry.drawableName("gatieau_whitening")), "existing key");
        for (ReferenceDraftMigration.Draft draft : ReferenceDraftMigration.drafts()) {
            String resource = ProductImageRegistry.drawableName(draft.imageKey);
            if ("generic".equals(draft.imageKey)) {
                check(ProductImageRegistry.PLACEHOLDER.equals(resource), "unknown product has placeholder");
            } else {
                Path file=Paths.get(args[0],resource+".webp");
                check(Files.isRegularFile(file), "missing reference image " + file);
            }
        }
        try { ProductImageRegistry.mappings().clear(); throw new AssertionError("mutable mappings"); }
        catch (UnsupportedOperationException expected) { }
        System.out.println("Product image keys and safe unknown-image fallback verified.");
    }
}
