package ir.derakhshan.wholesale;

import java.net.URI;
import java.net.URISyntaxException;

/** Only the explicitly supplied contact links can leave the offline WebView. */
final class ContactLinks {
    private static final String[] PHONE_NUMBERS = {
            "09163099404", "09169165947", "09120219593"
    };

    private ContactLinks() { }

    static String dialNumber(String value) {
        if (value == null) return null;
        for (String number : PHONE_NUMBERS) {
            if (("tel:" + number).equalsIgnoreCase(value)) return number;
        }
        return null;
    }

    static boolean isDeveloperWebsite(String value) {
        if (value == null) return false;
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            String path = uri.getRawPath();
            return !uri.isOpaque()
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && "www.meelano.ir".equalsIgnoreCase(uri.getHost())
                    && uri.getUserInfo() == null && uri.getPort() == -1
                    && (path == null || path.isEmpty() || "/".equals(path))
                    && uri.getRawQuery() == null && uri.getRawFragment() == null;
        } catch (URISyntaxException error) {
            return false;
        }
    }
}
