package com.asiyestore.admin.app;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;

/** Unofficial read-only website search APIs; results require human product matching. */
public final class FreeMarketPrice {
    private FreeMarketPrice() { }
    static long number(Object n) {
        if (!(n instanceof Number)) return 0;
        long x = ((Number)n).longValue();
        return ((Number)n).doubleValue() == (double)x && x > 0 && x <= 1000000000000L ? x : 0;
    }
    public static MarketPrice.Offer[] digikala(String json, long receivedAt) throws Exception {
        JSONArray products = new JSONObject(json).getJSONObject("data").getJSONArray("products");
        ArrayList<MarketPrice.Offer> out = new ArrayList<>(); HashSet<Long> seen = new HashSet<>();
        for (int i=0; i<products.length() && i<100; i++) {
            JSONObject p=products.optJSONObject(i); if (p==null || !"marketable".equals(p.optString("status"))) continue;
            long id=number(p.opt("id")); String title=p.optString("title_fa", "").trim();
            JSONObject variant=p.optJSONObject("default_variant"); JSONObject price=variant==null ? null : variant.optJSONObject("price");
            long rial=price==null ? 0 : number(price.opt("selling_price"));
            if (id==0 || title.isEmpty() || title.length()>250 || rial==0 || rial%10!=0 || !seen.add(id)) continue;
            JSONObject seller=variant.optJSONObject("seller"); String shop=seller==null ? "" : seller.optString("title", "").trim();
            if (shop.length()>60) shop=shop.substring(0,60);
            out.add(new MarketPrice.Offer("دیجی‌کالا"+(shop.isEmpty()?"":" · "+shop),title,"https://www.digikala.com/product/dkp-"+id+"/",rial/10,receivedAt));
        }
        return out.toArray(new MarketPrice.Offer[0]);
    }
    public static MarketPrice.Offer[] torob(String json, long receivedAt) throws Exception {
        JSONArray products=new JSONObject(json).getJSONArray("results");
        ArrayList<MarketPrice.Offer> out=new ArrayList<>(); HashSet<String> seen=new HashSet<>();
        for (int i=0; i<products.length() && i<100; i++) {
            JSONObject p=products.optJSONObject(i); if (p==null) continue;
            String id=p.optString("random_key", ""), title=p.optString("name1", "").trim(); long price=number(p.opt("price"));
            if (!id.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}") || title.isEmpty() || title.length()>250 || price==0 || price>100000000000L || !"active".equals(p.optString("price_text_mode")) || !"cheapest".equals(p.optString("price_prefix")) || !seen.add(id)) continue;
            String label="ترب · از قیمت"+(p.optBoolean("is_adv",false)?" · تبلیغ":"");
            out.add(new MarketPrice.Offer(label,title,"https://torob.com/p/"+id+"/",price,receivedAt));
        }
        return out.toArray(new MarketPrice.Offer[0]);
    }
    public static String get(boolean digikala, String query) throws Exception {
        String q=URLEncoder.encode(query, "UTF-8");
        String base=digikala ? "https://api.digikala.com/v1/search/?page=1&has_selling_stock=1&q=" : "https://api.torob.com/v4/base-product/search/?page=0&size=24&sort=popularity&source=next_desktop&q=";
        HttpURLConnection c=(HttpURLConnection)new URL(base+q).openConnection();
        try {
            c.setConnectTimeout(8000); c.setReadTimeout(12000); c.setInstanceFollowRedirects(false);
            c.setRequestProperty("Accept", "application/json"); c.setRequestProperty("Accept-Language", "fa-IR,fa;q=0.9");
            if (c.getResponseCode()!=200) throw new java.io.IOException("HTTP " + c.getResponseCode());
            try (InputStream in=c.getInputStream(); ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                byte[] buf=new byte[8192]; int n,total=0;
                while ((n=in.read(buf))!=-1) { total+=n; if (total>1024*1024) throw new java.io.IOException("response too large"); out.write(buf,0,n); }
                return new String(out.toByteArray(),StandardCharsets.UTF_8);
            }
        } finally { c.disconnect(); }
    }
}
