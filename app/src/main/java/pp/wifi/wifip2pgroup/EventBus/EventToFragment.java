package pp.wifi.wifip2pgroup.EventBus;

import java.util.Objects;

/**
 * Created by gal on 16/11/2016.
 */

public class EventToFragment {
    public final String fragmentTag;
    public final int event;
    public final Object data;

    public static final int SEND_PEERS = 1000;
    public static final int MAKE_CONNECTION = 1001;

    public EventToFragment(String fragmentTag, int event, Object data) {
        this.fragmentTag = fragmentTag;
        this.event = event;
        this.data = data;
    }
}
