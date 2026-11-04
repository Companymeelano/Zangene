package com.asiyestore.admin.app;
public class MarketPriceRegression {
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    public static void main(String[] args) throws Exception {
        long now=1700000000000L;
        String response="{\"offers\":["+
          "{\"source\":\"A\",\"title\":\"tea\",\"url\":\"https://a.example/p\",\"price_toman\":100,\"observed_at_ms\":"+now+"},"+
          "{\"source\":\"B\",\"title\":\"tea\",\"url\":\"https://b.example/p\",\"price_toman\":200,\"observed_at_ms\":"+now+"},"+
          "{\"source\":\"duplicate\",\"title\":\"tea\",\"url\":\"https://a.example/p\",\"price_toman\":1,\"observed_at_ms\":"+now+"},"+
          "{\"source\":\"stale\",\"title\":\"tea\",\"url\":\"https://c.example/p\",\"price_toman\":1,\"observed_at_ms\":"+(now-86400001L)+"},"+
          "{\"source\":\"bad\",\"title\":\"tea\",\"url\":\"http://d.example/p\",\"price_toman\":1,\"observed_at_ms\":"+now+"}]}";
        MarketPrice.Offer[] offers=MarketPrice.parse(response,now); check(offers.length==2);
        check(MarketPrice.average(offers,new boolean[]{true,true})==150);
        try { MarketPrice.average(offers,new boolean[]{true,false}); throw new AssertionError(); } catch (IllegalArgumentException expected) { }
        for (String invalid:new String[]{"http://test.example", "https://user:pass@test.example", "https://test.example/?key=x"}) {
            try { MarketPrice.endpoint(invalid); throw new AssertionError(); } catch (IllegalArgumentException expected) { }
        }
        check(MarketPrice.endpoint("https://test.example/v1/prices").equals("https://test.example/v1/prices"));
        System.out.println("Price parsing, freshness and explicit selection passed.");
    }
}
