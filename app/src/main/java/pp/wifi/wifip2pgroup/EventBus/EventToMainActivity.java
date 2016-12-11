package pp.wifi.wifip2pgroup.EventBus;

/**
 * Created by gal on 20/11/2016.
 */

public class EventToMainActivity {
    public final int event;

    public static final int OPEN_CLIENT = 1000;

    public EventToMainActivity(int event) {
        this.event = event;
    }
}
