package pp.wifi.wifip2pgroup;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import pp.wifi.wifip2pgroup.EventBus.ChooserToMainActivity;
import pp.wifi.wifip2pgroup.EventBus.EventToFragment;
import pp.wifi.wifip2pgroup.EventBus.EventToMainActivity;
import pp.wifi.wifip2pgroup.EventBus.MessageToFragmentEvent;
import pp.wifi.wifip2pgroup.rx.network.RxNetworkSession;

public class MainActivity extends AppCompatActivity {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    WifiP2pDeviceList mWifiP2pDeviceList;


    String host;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                EventBus.getDefault().post(new MessageToFragmentEvent("PeersAvailable",RegFragment.TAG));
                mWifiP2pDeviceList = wifiP2pDeviceList;
                EventBus.getDefault().post(new EventToFragment(RegFragment.TAG,EventToFragment.SEND_PEERS,wifiP2pDeviceList));
            }
        }, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo) {

                if(wifiP2pInfo.groupOwnerAddress != null){
                    if(!RxNetworkSession.isOn()) {
                        host = wifiP2pInfo.groupOwnerAddress.toString();
                        final RxNetworkSession rxNetworkSession = RxNetworkSession.getInstance();
                        rxNetworkSession.openServer();
                        EventBus.getDefault().post(new MessageToFragmentEvent("***** Server Opened *****",RegFragment.TAG));
                        String ip = getDottedDecimalIP(getLocalIPAddress());
                        String myIp = ip.replace("/","");
                        EventBus.getDefault().post(new MessageToFragmentEvent("***** my IP : "+ip+" \nowner : "+wifiP2pInfo.groupOwnerAddress.toString()+" *****",RegFragment.TAG));
                        if(!myIp.equals(wifiP2pInfo.groupOwnerAddress.toString().replace("/",""))) {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    rxNetworkSession.openNotOwnerClient(wifiP2pInfo.groupOwnerAddress.toString().replace("/",""));
                                    EventBus.getDefault().post(new MessageToFragmentEvent("***** Client Opened *****", RegFragment.TAG));

                                    //EventBus.getDefault().post(new MessageToFragmentEvent("***** my IP : "+ip+" \nowner : "+wifiP2pInfo.groupOwnerAddress.toString()+" *****",RegFragment.TAG));
                                }
                            });
                            t.start();
                        }



                    }
//                    Socket socket = new Socket();
//                    String myIp = socket.getLocalAddress().toString().replace("/","");
//                    if(!myIp.equals(wifiP2pInfo.groupOwnerAddress.getHostAddress())){
//                        if(RxNetworkSession.isOn()) {
//                            RxNetworkSession rxNetworkSession = RxNetworkSession.getInstance();
//                            //rxNetworkSession.openServer();
//                            rxNetworkSession.openNotOwnerClient(wifiP2pInfo.groupOwnerAddress.getHostAddress());
//                        }
//                    }
                }
                else{
                    EventBus.getDefault().post(new MessageToFragmentEvent("ConnectionInfoAvailable",RegFragment.TAG));
                }
            }
        });

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        Fragment mainFragment = new MainFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.main_frag, mainFragment);
        transaction.commit();

        if(notConnected()) {
            Fragment regFragment = new RegFragment();
            transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.main_frag, regFragment);
            transaction.addToBackStack("aa");
            transaction.commit();
        }
    }


    private boolean notConnected() {
        return true;
    }

    public WifiP2pManager getmManager() {
        return mManager;
    }

    public WifiP2pManager.Channel getmChannel() {
        return mChannel;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        EventBus.getDefault().register(this);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ChooserToMainActivity chooserToMainActivity){
        int peerIndex = chooserToMainActivity.IndexOfPeer;
        WifiP2pDevice device = ((WifiP2pDevice)mWifiP2pDeviceList.getDeviceList().toArray()[peerIndex]);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        EventBus.getDefault().post(new EventToFragment(RegFragment.TAG,EventToFragment.MAKE_CONNECTION,config));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventToMainActivity eventToMainActivity){
        if(eventToMainActivity.event == EventToMainActivity.OPEN_CLIENT){
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    RxNetworkSession.getInstance().openClient(host.replace("/",""));
                }
            });
            t.start();

        }
    }







    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }

    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }
}
