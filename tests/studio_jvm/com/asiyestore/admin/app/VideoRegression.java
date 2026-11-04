package com.asiyestore.admin.app;
import org.json.JSONArray;
import org.json.JSONObject;
public class VideoRegression {
    static void check(boolean b) { if (!b) throw new AssertionError(); }
    static JSONObject out(String type, String url) { return new JSONObject().put("type", type).put("url", url); }
    public static void main(String[] args) throws Exception {
        JSONObject wrong = new JSONObject().put("id", "other").put("status", "succeeded").put("outputs", new JSONArray().put(out("video", "https://example.org/wrong.mp4")));
        JSONObject right = new JSONObject().put("id", "wanted").put("status", "succeeded").put("outputs", new JSONArray().put(out("image", "https://example.org/image.png")).put(out("video", "https://example.org/right.mp4")));
        JSONObject response = new JSONObject().put("code", 0).put("data", new JSONArray().put(wrong).put(right));
        String[] result = AiVideo.parseStatus("kling", "wanted", response);
        check(result[0].equals("succeeded") && result[1].equals("https://example.org/right.mp4"));
        right.put("status", "processing");
        check(AiVideo.parseStatus("kling", "wanted", response)[1].isEmpty());
        right.put("status", "succeeded");
        right.put("outputs", new JSONArray().put(out("image", "https://example.org/image.png")));
        check(AiVideo.parseStatus("kling", "wanted", response)[1].isEmpty());
        try { AiVideo.parseStatus("kling", "not-found", response); throw new AssertionError(); } catch (IllegalArgumentException expected) { }
        right.put("outputs", new JSONArray().put(out("video", "http://example.org/a.mp4")));
        try { AiVideo.parseStatus("kling", "wanted", response); throw new AssertionError(); } catch (IllegalArgumentException expected) { }
        JSONObject pi = new JSONObject().put("code", 200).put("data", new JSONObject().put("task_id", "p1").put("status", "completed").put("output", new JSONObject().put("video", "").put("video_url", "https://example.org/p.mp4")));
        check(AiVideo.parseStatus("piapi", "p1", pi)[1].equals("https://example.org/p.mp4"));
        try { AiVideo.parseStatus("piapi", "p2", pi); throw new AssertionError(); } catch (IllegalArgumentException expected) { }
        System.out.println("Video status and output validation regression checks passed.");
    }
}
