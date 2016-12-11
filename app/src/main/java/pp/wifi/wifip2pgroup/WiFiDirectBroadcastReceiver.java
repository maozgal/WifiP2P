package pp.wifi.wifip2pgroup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

import org.greenrobot.eventbus.EventBus;

import pp.wifi.wifip2pgroup.EventBus.MessageToFragmentEvent;

/**
 * Created by gal on 06/11/2016.
 */

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager.PeerListListener myPeerListListener;
    private WifiP2pManager.ConnectionInfoListener myInfoListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       WifiP2pManager.PeerListListener myPeerListListener,WifiP2pManager.ConnectionInfoListener myInfoListener) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.myPeerListListener = myPeerListListener;
        this.myInfoListener = myInfoListener;
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
            } else {
                // Wi-Fi P2P is not enabled
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }


        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            EventBus.getDefault().post(new MessageToFragmentEvent("Got Something",RegFragment.TAG));
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()



            if (mManager != null) {
                mManager.requestPeers(mChannel, myPeerListListener);
                mManager.requestConnectionInfo(mChannel,myInfoListener);
            }
        }
    }


}
