package com.asiyestore.admin.app;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TransportRegression {
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    public static void main(String[] args) throws Exception {
        check(Arrays.equals(AiProviderOrder.forPreference("openai"), new String[]{"openai", "gemini", "groq"}));
        check(Arrays.equals(AiProviderOrder.forPreference("groq"), new String[]{"groq", "gemini", "openai"}));
        check(Arrays.equals(AiProviderOrder.forPreference("unknown"), new String[]{"gemini", "openai", "groq"}));
        ServerSocket server = new ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"));
        final String[] requests = new String[4];
        Thread worker = new Thread(new Runnable() { public void run() {
            try {
                for (int i=0; i<4; i++) {
                    Socket socket = server.accept(); socket.setSoTimeout(3000);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder headers = new StringBuilder(); String line; int len=0;
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {
                        headers.append(line).append('\n');
                        if (line.toLowerCase().startsWith("content-length:")) len=Integer.parseInt(line.substring(line.indexOf(':')+1).trim());
                    }
                    char[] body = new char[len]; int got=0; while (got<len) { int n=reader.read(body,got,len-got); if (n<0) break; got+=n; }
                    requests[i] = headers.toString() + new String(body, 0, got);
                    String[] status = {"200 OK", "401 Unauthorized", "302 Found", "200 OK"};
                    String[] data = {"{}", "secret-error-body", "redirect", "oversized"};
                    byte[] bytes = data[i].getBytes(StandardCharsets.UTF_8);
                    OutputStream out = socket.getOutputStream();
                    out.write(("HTTP/1.1 " + status[i] + "\r\nContent-Length: " + bytes.length + "\r\nLocation: /other\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    out.write(bytes); out.flush(); socket.close();
                }
            } catch (Exception e) { throw new RuntimeException(e); }
        }}); worker.start();
        String url = "http://127.0.0.1:" + server.getLocalPort() + "/test";
        check("{}".equals(AiHttpTransport.post(url,"Authorization","Bearer test-credential","{}",100)));
        for (int status : new int[]{401,302}) {
            try { AiHttpTransport.post(url,"Authorization","Bearer test-credential","{}",100); throw new AssertionError(); }
            catch (DirectAiStudio.ProviderHttpException e) { check(e.status == status); check(!e.getMessage().contains("secret")); }
        }
        try { AiHttpTransport.post(url,"x-goog-api-key","test-credential","{}",2); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { check(expected.getMessage().equals("response too large")); }
        worker.join(4000); server.close(); check(!worker.isAlive());
        check(requests[0].startsWith("POST /test "));
        check(requests[0].toLowerCase().contains("authorization: bearer test-credential"));
        check(requests[3].toLowerCase().contains("x-goog-api-key: test-credential"));
        check(requests[0].endsWith("{}"));
        System.out.println("Provider ordering and HTTP transport regression checks passed.");
    }
}
