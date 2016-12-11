package pp.wifi.wifip2pgroup.rx.network;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import pp.wifi.wifip2pgroup.EventBus.MessageToFragmentEvent;
import pp.wifi.wifip2pgroup.RegFragment;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by gal on 23/11/2016.
 */

public class RxNetworkSession {
    private static RxNetworkSession instance;
    private Socket client;

    public static RxNetworkSession getInstance() {
        if(instance != null) {
            return instance;
        }
        else return new RxNetworkSession();
    }

    public static boolean isOn(){
        return instance != null;
    }

    public void openServer(){
        Subscription socketServerObservable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    ServerSocket serverSocket = new ServerSocket(8888);
                    Socket client = serverSocket.accept();
                    EventBus.getDefault().post(new MessageToFragmentEvent("***** CLIENT IN SERVER *****", RegFragment.TAG));
                    InputStream inputStream = client.getInputStream();
                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder total = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        total.append(line).append('\n');
                    }
                    EventBus.getDefault().post(new MessageToFragmentEvent("***** "+total.toString()+" *****", RegFragment.TAG));
                    subscriber.onNext(total.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String total) {
                        if(total.toString().startsWith(Statics.MESSAGE_BEGIN)){
                            String msg = getMsg(total);
                            String msgArray[] = msg.split(Statics.COLON);
                            if(msgArray[0]!= null && msgArray[0].equals(Statics.CLIENT_HANDSHAKE)){
                                EventBus.getDefault().post(new MessageToFragmentEvent("***** HAND SHAKE SERVER SIDE *****", RegFragment.TAG));
                                openClient(msgArray[1].replace("/",""));
                            }
                        }
                    }
                });
    }

    public Socket openClient(String host) {

        Socket socket = null;
        try {
            socket = new Socket();
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, 8888)), 500);

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("gal".getBytes());
            EventBus.getDefault().post(new MessageToFragmentEvent("***** CLENT SET MSG *****", RegFragment.TAG));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }

    private String getMsg(String total) {
        String msg = total.replace(Statics.MESSAGE_BEGIN,"").replace(Statics.MESSAGE_END,"");
        return msg;
    }

    public void openNotOwnerClient(String host) {
        try {
            String str = Statics.SERVER_HANDSHAKE;
            client = openClient(host.replace("/",""));
            OutputStream outputStream = client.getOutputStream();
            outputStream.write(str.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getClient() {
        return client;
    }
}
