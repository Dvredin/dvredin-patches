import java.util.ArrayList;
import java.util.List;
/** Version-pinned fragment-manager reflection fixture, excluded from production. */
public class cc {
    public final List<Object> fragments = new ArrayList<>();
    public List<Object> n() { return fragments; }
    public static class Host {
        public cc child;
        public Host(cc child) { this.child = child; }
    }
}
