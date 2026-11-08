package ir.derakhshan.wholesale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.webkit.JavascriptInterface;
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

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Offline Android shell around the same storefront used by the web build. */
public final class MainActivity extends Activity {
    private static final String LOCAL_HOST = "appassets.androidplatform.net";
    private static final String ASSET_PREFIX = "/assets/";
    private static final String HOME_URL = "https://" + LOCAL_HOST + ASSET_PREFIX + "index.html";
    private static final String STATE_PENDING_TEXT = "pending_inquiry_text";
    private static final int CREATE_DOCUMENT = 1001;
    private static final int MAX_TEXT_LENGTH = 100_000;

    private WebView webView;
    private FrameLayout rootLayout;
    private View splashView;
    private String pendingText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            pendingText = savedInstanceState.getString(STATE_PENDING_TEXT);
        }

        getWindow().setStatusBarColor(Color.rgb(41, 69, 51));
        getWindow().setNavigationBarColor(Color.rgb(41, 69, 51));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(248, 246, 237));
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
        webView.addJavascriptInterface(new NativeBridge(), "DerakhshanAndroid");

        rootLayout = new FrameLayout(this);
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
                new int[] { Color.rgb(35, 65, 43), Color.rgb(12, 34, 23) }));

        ImageView emblem = new ImageView(this);
        int art = getResources().getIdentifier("splash_emblem", "drawable", getPackageName());
        if (art != 0) emblem.setImageResource(art);
        GradientDrawable emblemShape = new GradientDrawable();
        emblemShape.setShape(GradientDrawable.OVAL);
        emblemShape.setColor(Color.rgb(16, 40, 27));
        emblem.setBackground(emblemShape);
        emblem.setClipToOutline(true);
        emblem.setContentDescription("نشان درخشان");
        splash.addView(emblem, new LinearLayout.LayoutParams(dp(160), dp(160)));

        TextView title = new TextView(this);
        title.setText("درخشان");
        title.setTextColor(Color.rgb(239, 225, 183));
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(18);
        splash.addView(title, titleParams);

        TextView subtitle = new TextView(this);
        subtitle.setText("آجیل و خشکبار عمده");
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
    public void onSaveInstanceState(Bundle outState) {
        if (pendingText != null) outState.putString(STATE_PENDING_TEXT, pendingText);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView == null) {
            super.onBackPressed();
            return;
        }
        // Close an open product, quote or mobile menu before navigating the WebView history.
        String script = "(function(){"
                + "var p=document.getElementById('product-modal');"
                + "if(p&&p.open){var c=p.querySelector('[data-close-product]');if(c){c.click();return true;}}"
                + "var d=document.getElementById('quote-drawer');"
                + "if(d&&d.open){var b=d.querySelector('[data-close-quote]');if(b){b.click();return true;}}"
                + "var n=document.getElementById('mobile-nav');"
                + "if(n&&!n.hidden){var t=document.querySelector('.mobile-menu-toggle');if(t){t.click();return true;}}"
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CREATE_DOCUMENT) return;
        String text = pendingText;
        pendingText = null;
        if (resultCode != RESULT_OK || data == null || data.getData() == null || text == null) {
            showFeedback("ذخیره فایل لغو شد.");
            return;
        }
        try {
            OutputStream stream = getContentResolver().openOutputStream(data.getData());
            if (stream == null) throw new IOException("No output stream");
            try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                writer.write('\ufeff');
                writer.write(text);
            }
            showFeedback("فایل درخواست ذخیره شد.");
        } catch (IOException | SecurityException error) {
            showFeedback("ذخیره فایل ممکن نشد؛ می‌توانید متن را کپی کنید.");
        }
    }

    private void showFeedback(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        if (webView != null) {
            webView.evaluateJavascript("(function(){var e=document.getElementById('copy-feedback');"
                    + "if(e)e.textContent=" + JSONObject.quote(message) + ";})()", null);
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("DerakhshanAndroid");
            if (rootLayout != null) rootLayout.removeView(webView);
            webView.destroy();
            webView = null;
        }
        splashView = null;
        rootLayout = null;
        super.onDestroy();
    }

    /** Only these two methods are exposed to the app's bundled page. */
    public final class NativeBridge {
        @JavascriptInterface
        public void copyText(final String text) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (text == null || text.length() > MAX_TEXT_LENGTH) {
                        showFeedback("کپی متن ممکن نشد.");
                        return;
                    }
                    try {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard == null) throw new IllegalStateException("Clipboard unavailable");
                        clipboard.setPrimaryClip(ClipData.newPlainText("استعلام درخشان", text));
                    } catch (RuntimeException error) {
                        showFeedback("کپی متن ممکن نشد؛ آن را به‌صورت دستی انتخاب کنید.");
                    }
                }
            });
        }

        @JavascriptInterface
        public void saveText(final String text) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (text == null || text.length() > MAX_TEXT_LENGTH) {
                        showFeedback("متن برای ذخیره‌سازی معتبر نیست.");
                        return;
                    }
                    pendingText = text;
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TITLE, "derakhshan-inquiry.txt");
                    try {
                        startActivityForResult(intent, CREATE_DOCUMENT);
                    } catch (ActivityNotFoundException | SecurityException error) {
                        pendingText = null;
                        showFeedback("پنجره ذخیره فایل در دسترس نیست؛ می‌توانید متن را کپی کنید.");
                    }
                }
            });
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
            return !isHomeDocument(request.getUrl());
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return !isHomeDocument(Uri.parse(url));
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
