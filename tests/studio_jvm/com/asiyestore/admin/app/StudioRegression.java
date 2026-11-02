package com.asiyestore.admin.app;
public class StudioRegression {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        AiProductStudio.Analysis a = AiProductStudio.analyze("چای سیاه", "ساقدوش", null);
        check(a.detectedTitle.equals("چای سیاه"), "name changed");
        check(a.detectedBrand.equals("ساقدوش"), "brand changed");
        check(a.category.equals("چای و نوشیدنی"), "tea misclassified");
        check(a.confidence == 0, "fabricated confidence");
        AiProductStudio.Draft d = AiProductStudio.compose(a, "چای سیاه", "ساقدوش");
        check(d.brand.equals("ساقدوش"), "brand missing");
        check(d.category.equals("چای و نوشیدنی"), "draft category");
        check(d.instagramCaption.contains("ساقدوش") && !d.instagramCaption.contains("پوست"), "wrong caption");
        check(d.ingredients.isEmpty() && d.country.isEmpty() && d.volume.isEmpty(), "fabricated facts");
        check(d.suggestedPrice == 0 && d.stock == 0 && d.imageKey.equals("generic"), "fabricated commercial info");
        a = AiProductStudio.analyze("چای سبز", "گاتیو", null);
        check(!a.category.contains("پوست"), "brand confused with category");
        a = AiProductStudio.analyze("unknown thing", "unknown brand", null);
        check(a.category.equals("سایر محصولات"), "unknown should not be beauty");
        check(AiProductStudio.compose(a, "unknown thing", "unknown brand").description.contains("unknown brand"), "brand lost");
        System.out.println("Studio regression checks passed");
    }
}
