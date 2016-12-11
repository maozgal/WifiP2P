package pp.wifi.wifip2pgroup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import pp.wifi.wifip2pgroup.EventBus.ChooserToMainActivity;
import pp.wifi.wifip2pgroup.EventBus.EventToFragment;
import pp.wifi.wifip2pgroup.EventBus.PeersToChooser;

/**
 * Created by gal on 14/11/2016.
 */

public class ChooseFragment extends DialogFragment {
    private int selectedItemIndex = -1;
    private RecyclerView recyclerView;
    private PeersAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    public static final String TAG = "ChooseFragment";

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView= (RecyclerView) view.findViewById(R.id.choose_rv);
        layoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        adapter=new PeersAdapter(new ArrayList<String>());
        recyclerView.setAdapter(adapter);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle("Choose Peer")
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int whichButton) {
                                dialogInterface.dismiss();
                            }
                        }
                ).setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(selectedItemIndex != -1) {
                            EventBus.getDefault().post(new ChooserToMainActivity(ChooserToMainActivity.PEER_HAS_BEEN_SELECTED, selectedItemIndex));
                            dialogInterface.dismiss();
                        }
                    }
                });

        // call default fragment methods and set view for dialog
        View view = onCreateDialogView(getActivity().getLayoutInflater(), null, null);
        onViewCreated(view, null);
        dialogBuilder.setView(view);

        return dialogBuilder.create();
    }

    private View onCreateDialogView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.choose_fragment, container); // inflate here
    }


    /*--- Recycler Stuff---*/
    public class PeersAdapter extends RecyclerView.Adapter<PeersAdapter.PeersViewHolder> {
        private ArrayList<String> peersList;

        public PeersAdapter(ArrayList<String> peersList) {
            this.peersList = peersList;
        }

        @Override
        public PeersAdapter.PeersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.peer_row,parent,false);
            PeersViewHolder viewHolder=new PeersViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final PeersAdapter.PeersViewHolder holder, final int position) {
            holder.text.setText(peersList.get(position).toString());
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.view.setBackgroundColor(Color.GRAY);
                    selectedItemIndex = position;
                }
            });
        }

        @Override
        public int getItemCount() {
            return peersList.size();
        }

        public void reloadData(Object[] newPeers){
            selectedItemIndex = -1;
            peersList = new ArrayList<String>();
            for(int i = 0;i<newPeers.length;i++){
                peersList.add(((WifiP2pDevice)newPeers[i]).deviceName);
            }
            notifyDataSetChanged();
        }



        public class PeersViewHolder extends RecyclerView.ViewHolder{
            protected TextView text;
            protected LinearLayout view;
            public PeersViewHolder(View itemView) {
                super(itemView);
                text= (TextView) itemView.findViewById(R.id.text_id);
                view = (LinearLayout) itemView.findViewById(R.id.ll_row_chooser);
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PeersToChooser eventToFragment){
        if(eventToFragment.fragmentTag.equals(TAG) && eventToFragment.event == EventToFragment.SEND_PEERS){
            WifiP2pDeviceList wifiP2pDeviceList = ((WifiP2pDeviceList)(eventToFragment.data));
            adapter.reloadData(wifiP2pDeviceList.getDeviceList().toArray());

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
