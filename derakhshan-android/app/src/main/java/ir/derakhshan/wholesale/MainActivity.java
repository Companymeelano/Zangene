package ir.derakhshan.wholesale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Offline Android shell around the same storefront used by the web build. */
public final class MainActivity extends Activity {
    private static final String LOCAL_HOST = "appassets.androidplatform.net";
    private static final String ASSET_PREFIX = "/assets/";
    private static final String HOME_URL = "https://" + LOCAL_HOST + ASSET_PREFIX + "index.html";

    private WebView webView;
    private FrameLayout rootLayout;
    private View splashView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(8, 43, 27));
        getWindow().setNavigationBarColor(Color.rgb(8, 43, 27));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(8, 43, 27));
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        // Local files are served by OfflineClient instead of exposing device files to JavaScript.
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.setWebViewClient(new OfflineClient());

        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.rgb(8, 43, 27));
        rootLayout.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        splashView = createSplash();
        rootLayout.addView(splashView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(rootLayout);
        // Even on an unusually slow or broken WebView, the launch screen must not trap the user.
        splashView.postDelayed(new Runnable() {
            @Override public void run() { dismissSplash(); }
        }, 6000);
        webView.loadUrl(HOME_URL);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private View createSplash() {
        LinearLayout splash = new LinearLayout(this);
        splash.setOrientation(LinearLayout.VERTICAL);
        splash.setGravity(Gravity.CENTER);
        splash.setPadding(dp(24), dp(24), dp(24), dp(24));
        splash.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] { Color.rgb(22, 83, 51), Color.rgb(9, 55, 33), Color.rgb(5, 31, 20) }));

        ImageView emblem = new ImageView(this);
        int art = getResources().getIdentifier("splash_emblem", "drawable", getPackageName());
        if (art != 0) emblem.setImageResource(art);
        GradientDrawable emblemShape = new GradientDrawable();
        emblemShape.setShape(GradientDrawable.OVAL);
        emblemShape.setColor(Color.rgb(8, 43, 27));
        emblem.setBackground(emblemShape);
        emblem.setClipToOutline(true);
        emblem.setContentDescription("Nute Store");
        splash.addView(emblem, new LinearLayout.LayoutParams(dp(160), dp(160)));

        TextView title = new TextView(this);
        title.setText("Nute Store");
        title.setTextColor(Color.rgb(239, 225, 183));
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(18);
        splash.addView(title, titleParams);

        TextView subtitle = new TextView(this);
        subtitle.setText("فروشگاه آجیل و خشکبار درخشان");
        subtitle.setTextColor(Color.rgb(198, 211, 190));
        subtitle.setTextSize(13);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = dp(8);
        splash.addView(subtitle, subtitleParams);
        return splash;
    }

    private void dismissSplash() {
        if (splashView == null) return;
        final View departing = splashView;
        splashView = null;
        departing.animate().alpha(0f).setDuration(300).withEndAction(new Runnable() {
            @Override public void run() {
                if (rootLayout != null) rootLayout.removeView(departing);
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (webView == null) {
            super.onBackPressed();
            return;
        }
        // Return through checkout, cart and product dialogs before leaving the shop.
        String script = "(function(){"
                + "var q=document.getElementById('checkout-drawer');"
                + "if(q&&q.open){var x=q.querySelector('[data-close-checkout]');if(x){x.click();return true;}}"
                + "var d=document.getElementById('cart-drawer');"
                + "if(d&&d.open){var b=d.querySelector('[data-close-cart]');if(b){b.click();return true;}}"
                + "var p=document.getElementById('product-modal');"
                + "if(p&&p.open){var c=p.querySelector('[data-close-product]');if(c){c.click();return true;}}"
                + "var s=document.getElementById('app-scroll');"
                + "if((s&&s.scrollTop>10)||window.scrollY>10){var h=document.querySelector('[data-home]');if(h){h.click();return true;}}"
                + "return false;})()";
        webView.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String handled) {
                if (!"true".equals(handled)) navigateBack();
            }
        });
    }

    private void navigateBack() {
        if (isFinishing()) return;
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            if (rootLayout != null) rootLayout.removeView(webView);
            webView.destroy();
            webView = null;
        }
        splashView = null;
        rootLayout = null;
        super.onDestroy();
    }

    /** Open only the provided contact numbers or developer site in a system app. */
    private void openContact(Uri uri) {
        if (uri == null) return;
        String number = ContactLinks.dialNumber(uri.toString());
        Intent intent;
        if (number != null) {
            // ACTION_DIAL lets the user choose whether to place a call, without CALL_PHONE permission.
            intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
        } else if (ContactLinks.isDeveloperWebsite(uri.toString())) {
            intent = new Intent(Intent.ACTION_VIEW, uri);
        } else {
            return;
        }
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException error) {
            Toast.makeText(this, "برنامه تلفن یا مرورگر در دسترس نیست.", Toast.LENGTH_LONG).show();
        }
    }

    /** Map a single virtual HTTPS origin to APK assets and deny all other requests. */
    private final class OfflineClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (isHomeDocument(Uri.parse(url))) dismissSplash();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            if (isHomeDocument(uri)) return false;
            // Some WebView versions omit the gesture flag for genuine link taps.
            // The handler itself accepts only our known contacts and never places a call.
            if (request.isForMainFrame()) openContact(uri);
            return true;
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            if (isHomeDocument(uri)) return false;
            // On Android 6 this older callback does not expose gesture information.
            openContact(uri);
            return true;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (!"GET".equalsIgnoreCase(request.getMethod())) return errorResponse(403, "Forbidden");
            return openAsset(request.getUrl());
        }

        @Override
        @SuppressWarnings("deprecation")
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return openAsset(Uri.parse(url));
        }

        private boolean isHomeDocument(Uri uri) {
            return isLocal(uri) && (ASSET_PREFIX + "index.html").equals(uri.getPath());
        }

        private boolean isLocal(Uri uri) {
            if (uri == null || !"https".equalsIgnoreCase(uri.getScheme())
                    || !LOCAL_HOST.equalsIgnoreCase(uri.getHost()) || uri.getPort() != -1
                    || uri.getUserInfo() != null) return false;
            String path = uri.getPath();
            if (path == null || !path.startsWith(ASSET_PREFIX) || path.indexOf('\\') >= 0) return false;
            String relative = path.substring(ASSET_PREFIX.length());
            if (relative.isEmpty()) return false;
            for (String part : relative.split("/", -1)) {
                if (part.isEmpty() || ".".equals(part) || "..".equals(part)) return false;
            }
            return true;
        }

        private WebResourceResponse openAsset(Uri uri) {
            if (!isLocal(uri)) return errorResponse(403, "Forbidden");
            String path = uri.getPath().substring(ASSET_PREFIX.length());
            try {
                InputStream stream = getAssets().open(path, AssetManager.ACCESS_STREAMING);
                String mime = mimeType(path);
                String encoding = (mime.startsWith("text/") || mime.equals("application/javascript")
                        || mime.equals("image/svg+xml")) ? "UTF-8" : null;
                Map<String, String> headers = new HashMap<>();
                headers.put("Cache-Control", "no-cache");
                headers.put("X-Content-Type-Options", "nosniff");
                return new WebResourceResponse(mime, encoding, 200, "OK", headers, stream);
            } catch (IOException error) {
                return errorResponse(404, "Not Found");
            }
        }

        private String mimeType(String path) {
            String name = path.toLowerCase(Locale.ROOT);
            if (name.endsWith(".html")) return "text/html";
            if (name.endsWith(".css")) return "text/css";
            if (name.endsWith(".js")) return "application/javascript";
            if (name.endsWith(".svg")) return "image/svg+xml";
            if (name.endsWith(".webp")) return "image/webp";
            if (name.endsWith(".png")) return "image/png";
            if (name.endsWith(".woff2")) return "font/woff2";
            if (name.endsWith(".txt") || name.endsWith(".md")) return "text/plain";
            return "application/octet-stream";
        }

        private WebResourceResponse errorResponse(int code, String reason) {
            return new WebResourceResponse("text/plain", "UTF-8", code, reason,
                    new HashMap<String, String>(), new ByteArrayInputStream(new byte[0]));
        }
    }
}
