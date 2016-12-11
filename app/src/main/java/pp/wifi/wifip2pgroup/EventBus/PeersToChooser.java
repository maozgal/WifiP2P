package pp.wifi.wifip2pgroup.EventBus;

/**
 * Created by gal on 16/11/2016.
 */

public class PeersToChooser {
    public final String fragmentTag;
    public final int event;
    public final Object data;

    public static final int SEND_PEERS = 1000;

    public PeersToChooser(String fragmentTag, int event, Object data) {
        this.fragmentTag = fragmentTag;
        this.event = event;
        this.data = data;
    }
}
