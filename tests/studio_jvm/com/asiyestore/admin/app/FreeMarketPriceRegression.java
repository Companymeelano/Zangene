package com.asiyestore.admin.app;
public class FreeMarketPriceRegression {
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    public static void main(String[] args) throws Exception {
        String dk="{\"data\":{\"products\":["+
          "{\"id\":123,\"title_fa\":\"چای ۵۰۰ گرم\",\"status\":\"marketable\",\"default_variant\":{\"price\":{\"selling_price\":425036000},\"seller\":{\"title\":\"فروشنده\"}}},"+
          "{\"id\":124,\"title_fa\":\"ناموجود\",\"status\":\"out_of_stock\",\"default_variant\":{\"price\":{\"selling_price\":1000}}},"+
          "{\"id\":125,\"title_fa\":\"بدون قیمت\",\"status\":\"marketable\",\"default_variant\":null}]}}";
        MarketPrice.Offer[] d=FreeMarketPrice.digikala(dk,1234); check(d.length==1 && d[0].toman==42503600 && d[0].observedAt==1234 && d[0].url.equals("https://www.digikala.com/product/dkp-123/"));
        String torob="{\"results\":[{\"random_key\":\"a919036f-3658-48a6-88d9-f3cdac48d594\",\"name1\":\"چای\",\"price\":72099000,\"price_prefix\":\"cheapest\",\"price_text_mode\":\"active\",\"is_adv\":true},{\"random_key\":\"a919036f-3658-48a6-88d9-f3cdac48d594\",\"name1\":\"چای\",\"price\":72099000,\"price_prefix\":\"cheapest\",\"price_text_mode\":\"active\"},{\"random_key\":\"bad\",\"name1\":\"bad\",\"price\":1,\"price_prefix\":\"cheapest\",\"price_text_mode\":\"active\"}]}";
        MarketPrice.Offer[] t=FreeMarketPrice.torob(torob,1234); check(t.length==1 && t[0].toman==72099000 && t[0].source.contains("تبلیغ"));
        try { FreeMarketPrice.torob("{}",1234); throw new AssertionError(); } catch (org.json.JSONException expected) { }
        System.out.println("Unofficial search parsing, stock, unit conversion and deduplication passed.");
    }
}
