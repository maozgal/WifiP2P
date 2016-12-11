package pp.wifi.wifip2pgroup.EventBus;

import android.app.Fragment;

/**
 * Created by gal on 06/11/2016.
 */

public class MessageToFragmentEvent {

    public final String message;
    public final String fragmentTag;

    public MessageToFragmentEvent(String message, String fragmentTag) {
        this.message = message;
        this.fragmentTag = fragmentTag;
    }
}
