package com.asiyestore.admin.app;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

/** Read-only price adapter: an integrator supplies current public offers in toman. */
public final class MarketPrice {
    private MarketPrice() { }
    public static final class Offer {
        public final String source, title, url; public final long toman, observedAt;
        Offer(String source, String title, String url, long toman, long observedAt) {
            this.source=source; this.title=title; this.url=url; this.toman=toman; this.observedAt=observedAt;
        }
    }
    public static String endpoint(String raw) throws Exception {
        URL u = new URL(raw.trim());
        if (!"https".equalsIgnoreCase(u.getProtocol()) || u.getHost().isEmpty() || u.getUserInfo()!=null || u.getQuery()!=null || u.getRef()!=null) throw new IllegalArgumentException("invalid endpoint");
        return u.toString();
    }
    public static Offer[] parse(String raw, long now) throws Exception {
        JSONArray offers = new JSONObject(raw).getJSONArray("offers");
        if (offers.length() > 30) throw new IllegalArgumentException("too many offers");
        ArrayList<Offer> valid = new ArrayList<>(); HashSet<String> seen = new HashSet<>();
        for (int i=0;i<offers.length();i++) {
            JSONObject o = offers.optJSONObject(i); if (o==null) continue;
            String source=o.optString("source", "").trim(), title=o.optString("title", "").trim(), url=o.optString("url", "").trim();
            Object rawPrice=o.opt("price_toman"), rawTime=o.opt("observed_at_ms");
            if (!(rawPrice instanceof Number) || !(rawTime instanceof Number)) continue;
            long price=((Number)rawPrice).longValue(), time=((Number)rawTime).longValue();
            if (((Number)rawPrice).doubleValue() != (double)price || ((Number)rawTime).doubleValue() != (double)time) continue;
            if (price<=0 || price>100000000000L || time<=0 || time>now+300000L || now-time>86400000L) continue;
            if (source.isEmpty() || source.length()>80 || title.isEmpty() || title.length()>250 || url.length()>2048) continue;
            try { URL u=new URL(url); if (!"https".equalsIgnoreCase(u.getProtocol()) || u.getHost().isEmpty() || u.getUserInfo()!=null) continue; }
            catch (Exception e) { continue; }
            if (!seen.add(url)) continue;
            valid.add(new Offer(source,title,url,price,time));
        }
        return valid.toArray(new Offer[0]);
    }
    public static long average(Offer[] offers, boolean[] selected) {
        if (offers.length != selected.length) throw new IllegalArgumentException("selection mismatch");
        long sum=0; int count=0;
        for (int i=0;i<offers.length;i++) if (selected[i]) { sum += offers[i].toman; count++; }
        if (count<2) throw new IllegalArgumentException("two sources required");
        return Math.round((double)sum/count);
    }
}
