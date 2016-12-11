package pp.wifi.wifip2pgroup;

import android.app.Fragment;
import android.app.FragmentManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import pp.wifi.wifip2pgroup.EventBus.EventToFragment;
import pp.wifi.wifip2pgroup.EventBus.EventToMainActivity;
import pp.wifi.wifip2pgroup.EventBus.MessageToFragmentEvent;
import pp.wifi.wifip2pgroup.EventBus.PeersToChooser;
import pp.wifi.wifip2pgroup.rx.network.RxNetworkSession;

/**
 * Created by gal on 02/11/2016.
 */

public class RegFragment extends Fragment  {
    public static final String TAG = "RegFragment";
    TextView tvLog;
    Button btScan;


    WifiP2pManager.Channel mChannel;
    WifiP2pManager mManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reg_fragment, container, false);
        setViews(view);

        mManager = ((MainActivity)getActivity()).getmManager();
        mChannel = ((MainActivity)getActivity()).getmChannel();
        return view;
    }

    private void setViews(View view) {
        tvLog = (TextView) view.findViewById(R.id.reg_log_tv);
        btScan = (Button) view.findViewById(R.id.scan_bt);

        btScan.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                EventBus.getDefault().post(new EventToMainActivity(EventToMainActivity.OPEN_CLIENT));
                return true;
            }
        });

        btScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                ChooseFragment dialogFragment = new ChooseFragment();
                dialogFragment.show(fm, "Dialog Fragment");
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        log("discover peers succeed");
//                        //in case the not-owner started the session.
//                        RxNetworkSession rxNetworkSession = RxNetworkSession.getInstance();
//                        rxNetworkSession.openNotOwnerClient();
//                        // TODO: 27/11/2016 //in case the owner started the session.
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        log("discover peers failed : " + reasonCode);
                    }
                });
            }
        });
    }

    private void log(String str){
        tvLog.append(str + "\n");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageToFragmentEvent event) {
        if(event.fragmentTag.equals(TAG)){
            log(event.message);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final EventToFragment eventToFragment){
       if(eventToFragment.fragmentTag.equals(TAG)) {
           switch (eventToFragment.event) {
               case EventToFragment.SEND_PEERS:
                   Handler h = new Handler();
                   h.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           EventBus.getDefault().post(new PeersToChooser(ChooseFragment.TAG, PeersToChooser.SEND_PEERS, eventToFragment.data));
                       }
                   }, 1000);
                   break;
               //connect to peer
               case EventToFragment.MAKE_CONNECTION:
                   WifiP2pConfig config = (WifiP2pConfig) eventToFragment.data;
                   mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                       @Override
                       public void onSuccess() {
                           log("Connection Succeeded");
                       }

                       @Override
                       public void onFailure(int i) {
                           log("Connection Failed");
                       }
                   });
           }
       }
    }
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
