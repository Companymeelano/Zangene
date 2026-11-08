package ir.derakhshan.wholesale;

/** Run with the JDK; no Android runtime is needed for the URL allowlist. */
public final class ContactLinksTest {
    private static void allowedNumber(String url, String number) {
        if (!number.equals(ContactLinks.dialNumber(url))) throw new AssertionError("Expected allowed phone: " + url);
    }
    private static void blocked(String url) {
        if (ContactLinks.dialNumber(url) != null || ContactLinks.isDeveloperWebsite(url)) {
            throw new AssertionError("Unexpected allowed link: " + url);
        }
    }
    public static void main(String[] args) {
        allowedNumber("tel:09163099404", "09163099404");
        allowedNumber("tel:09169165947", "09169165947");
        allowedNumber("tel:09120219593", "09120219593");
        allowedNumber("TEL:09120219593", "09120219593");
        if (!ContactLinks.isDeveloperWebsite("http://www.Meelano.ir")
                || !ContactLinks.isDeveloperWebsite("https://www.meelano.ir/")) {
            throw new AssertionError("The developer's homepage should be allowed");
        }
        for (String value : new String[] { null, "tel:09163099405", "tel:09120219593;123", "tel:09120219593?x=1", "sms:09120219593", "javascript:alert(1)", "intent://www.meelano.ir", "http://www.meelano.ir.evil.example", "http://www.meelano.ir@evil.example", "http://user@www.meelano.ir", "http://www.meelano.ir:80", "http://www.meelano.ir/other", "http://www.meelano.ir/?x=1", "http://www.meelano.ir/#section", "https://evil.example/", "not a url" }) blocked(value);
        System.out.println("Android contact allowlist: PASS");
    }
}
