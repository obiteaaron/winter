package tech.obiteaaron.winter.common.tools.id;

public class TimestampGenerator {

    private long lastId = System.currentTimeMillis();

    public long generate() {
        synchronized (this) {
            while (true) {
                long l = System.currentTimeMillis();
                if (l > lastId) {
                    return l;
                }
            }
        }
    }
}
