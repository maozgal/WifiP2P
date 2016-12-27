package pp.wifi.wifip2pgroup.EventBus;

/**
 * Created by gal on 20/11/2016.
 */

public class EventToMainActivity {
    public static final int PING = 1001;
    public static final int OPEN_CLIENT = 1000;

    public final int event;

    public EventToMainActivity(int event) {
        this.event = event;
    }
}
