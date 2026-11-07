package android.database.sqlite;
import android.content.ContentValues;
import android.database.Cursor;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/** Test shim forwarding operations to a real SQLite database via Python. */
public class SQLiteDatabase implements AutoCloseable {
    private final Process process;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private boolean transaction;
    public SQLiteDatabase(String bridge) throws IOException {
        process = new ProcessBuilder("python3", "-u", bridge).redirectError(ProcessBuilder.Redirect.INHERIT).start();
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(),StandardCharsets.UTF_8));
        reader = new BufferedReader(new InputStreamReader(process.getInputStream(),StandardCharsets.UTF_8));
    }
    private String call(String command, String... args) {
        try {
            StringBuilder line = new StringBuilder(command);
            for (String a : args) line.append('\t').append(Base64.getEncoder().encodeToString(a.getBytes(StandardCharsets.UTF_8)));
            writer.write(line.toString()); writer.newLine(); writer.flush();
            String response=reader.readLine();
            if (response==null) throw new IOException("SQLite bridge exited");
            String[] parts=response.split("\\t",-1);
            String result=new String(Base64.getDecoder().decode(parts[1]),StandardCharsets.UTF_8);
            if (!parts[0].equals("OK")) throw new IllegalStateException(result);
            return result;
        } catch(IOException e) { throw new IllegalStateException(e); }
    }
    public boolean inTransaction() { return transaction; }
    public void begin() { execSQL("BEGIN"); transaction=true; }
    public void commit() { execSQL("COMMIT"); transaction=false; }
    public void rollback() { execSQL("ROLLBACK"); transaction=false; }
    public void execSQL(String sql) { call("exec",sql); }
    public Cursor rawQuery(String sql,String[] args) {
        List<String> values=new ArrayList<>(); values.add(sql);
        for (String arg : args) values.add(arg);
        return new Cursor(call("query", values.toArray(new String[0])).equals("1"));
    }
    public long insertOrThrow(String table,String nullColumnHack,ContentValues values) {
        List<String> args=new ArrayList<>(); args.add(table);
        for (Map.Entry<String,String> e : values.values.entrySet()) { args.add(e.getKey()); args.add(e.getValue()); }
        call("insert",args.toArray(new String[0])); return 1;
    }
    public int update(String table,ContentValues values,String where,String[] whereArgs) {
        List<String> args=new ArrayList<>(); args.add(table); args.add(where); args.add(Integer.toString(whereArgs.length));
        for (String arg : whereArgs) args.add(arg);
        for (Map.Entry<String,String> e : values.values.entrySet()) { args.add(e.getKey()); args.add(e.getValue()); }
        return Integer.parseInt(call("update",args.toArray(new String[0])));
    }
    public void close() throws Exception { writer.close(); if (process.waitFor()!=0) throw new IOException("bridge failed"); }
}
