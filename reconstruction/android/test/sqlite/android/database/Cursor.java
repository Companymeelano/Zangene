package android.database;
public class Cursor implements AutoCloseable {
    private final boolean exists;
    public Cursor(boolean exists) { this.exists=exists; }
    public boolean moveToFirst() { return exists; }
    public void close() { }
}
