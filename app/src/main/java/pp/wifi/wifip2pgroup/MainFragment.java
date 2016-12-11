package pp.wifi.wifip2pgroup;

import android.app.Fragment;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by gal on 02/11/2016.
 */

public class MainFragment extends Fragment  {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment, container, false);


        return view;
    }


}
