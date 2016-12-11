package pp.wifi.wifip2pgroup.EventBus;

/**
 * Created by gal on 20/11/2016.
 */

public class ChooserToMainActivity {
    public final int event;
    public final int IndexOfPeer;

    public static final int PEER_HAS_BEEN_SELECTED = 1000;

    public ChooserToMainActivity(int event, int IndexOfPeer) {
        this.event = event;
        this.IndexOfPeer = IndexOfPeer;
    }
}
