package com.asiyestore.admin.app;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Explicitly submitted asynchronous jobs; callers must never auto-retry creation. */
public final class AiVideo {
    private AiVideo() { }
    public static String submit(Context ctx, String id, String prompt) throws Exception {
        JSONObject body; String endpoint, auth, header;
        if ("kling".equals(id)) {
            endpoint = "https://api-singapore.klingai.com/text-to-video/kling-3.0";
            header = "Authorization"; auth = "Bearer " + AiMedia.key(ctx, id);
            body = new JSONObject().put("prompt", prompt).put("settings", new JSONObject().put("resolution", "720p").put("aspect_ratio", "16:9").put("duration", 5).put("audio", "off").put("multi_shot", false));
        } else if ("piapi".equals(id)) {
            endpoint = "https://api.piapi.ai/api/v1/task"; header = "x-api-key"; auth = AiMedia.key(ctx, id);
            body = new JSONObject().put("model", "kling").put("task_type", "video_generation").put("input", new JSONObject().put("prompt", prompt).put("duration", 5).put("aspect_ratio", "16:9").put("mode", "std"));
        } else throw new IllegalArgumentException("unknown provider");
        JSONObject response = new JSONObject(AiHttpTransport.post(endpoint, header, auth, body.toString(), 256 * 1024));
        if (response.optInt("code", -1) != ("kling".equals(id) ? 0 : 200)) throw new IllegalArgumentException("provider failure");
        JSONObject data = response.getJSONObject("data");
        String task = data.getString("kling".equals(id) ? "id" : "task_id");
        if (!task.matches("[A-Za-z0-9_-]{1,120}")) throw new IllegalArgumentException("invalid task");
        return task;
    }
    public static String[] status(Context ctx, String id, String task) throws Exception {
        if (!task.matches("[A-Za-z0-9_-]{1,120}")) throw new IllegalArgumentException("invalid task");
        boolean kling = "kling".equals(id);
        if (!kling && !"piapi".equals(id)) throw new IllegalArgumentException("unknown provider");
        String url = kling ? "https://api-singapore.klingai.com/tasks?task_ids=" + task : "https://api.piapi.ai/api/v1/task/" + task;
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        try {
            c.setInstanceFollowRedirects(false); c.setConnectTimeout(15000); c.setReadTimeout(35000);
            c.setRequestProperty(kling ? "Authorization" : "x-api-key", kling ? "Bearer " + AiMedia.key(ctx,id) : AiMedia.key(ctx,id));
            c.setRequestProperty("Accept", "application/json");
            int code = c.getResponseCode(); if (code != 200) throw new DirectAiStudio.ProviderHttpException(code);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = c.getInputStream()) { byte[] b = new byte[4096]; int n, total = 0; while ((n=in.read(b)) != -1) { total += n; if (total > 256*1024) throw new IllegalArgumentException("response too large"); out.write(b,0,n); } }
            return parseStatus(id, task, new JSONObject(new String(out.toByteArray(), StandardCharsets.UTF_8)));
        } finally { c.disconnect(); }
    }

    /** Parse only the requested job and only video outputs; never confuse an image for a video. */
    static String[] parseStatus(String id, String task, JSONObject response) throws Exception {
        boolean kling = "kling".equals(id);
        if (!kling && !"piapi".equals(id)) throw new IllegalArgumentException("unknown provider");
        if (response.optInt("code", -1) != (kling ? 0 : 200)) throw new IllegalArgumentException("provider failure");
        JSONObject data = null;
        if (kling) {
            JSONArray tasks = response.getJSONArray("data");
            for (int i = 0; i < tasks.length(); i++) {
                JSONObject candidate = tasks.getJSONObject(i);
                if (task.equals(candidate.optString("id", ""))) { data = candidate; break; }
            }
            if (data == null) throw new IllegalArgumentException("requested task missing");
        } else {
            data = response.getJSONObject("data");
            if (!task.equals(data.optString("task_id", ""))) throw new IllegalArgumentException("requested task missing");
        }
        String state = data.optString("status", ""); String video = "";
        if (kling) {
            JSONArray outputs = data.optJSONArray("outputs");
            if (outputs != null) for (int i = 0; i < outputs.length(); i++) {
                JSONObject output = outputs.getJSONObject(i);
                if ("video".equals(output.optString("type", "")) && !output.optString("url", "").isEmpty()) {
                    video = output.optString("url", ""); break;
                }
            }
        } else {
            JSONObject output = data.optJSONObject("output"); if (output != null) {
                video = output.optString("video", "");
                if (video.isEmpty()) video = output.optString("video_url", "");
            }
        }
        if (!state.matches("submitted|pending|processing|succeeded|completed|failed")) throw new IllegalArgumentException("invalid status");
        if (!"succeeded".equals(state) && !"completed".equals(state)) video = "";
        if (!video.isEmpty()) {
            URL parsed = new URL(video);
            if (!"https".equalsIgnoreCase(parsed.getProtocol()) || parsed.getUserInfo() != null || parsed.getHost().isEmpty())
                throw new IllegalArgumentException("unsafe url");
        }
        return new String[]{state, video};
    }
}
