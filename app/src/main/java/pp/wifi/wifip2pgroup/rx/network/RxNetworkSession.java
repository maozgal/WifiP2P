package pp.wifi.wifip2pgroup.rx.network;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
    private Socket clientSocket;
    private boolean amIOwner = false;

    public static RxNetworkSession getInstance() {
        if(instance != null) {
            return instance;
        }
        else{
            instance = new RxNetworkSession();
            return instance;
        }
    }

    public static boolean isOn(){
        return instance != null;
    }

    public void openServer(){
        Subscription socketServerObservable = Observable.create(new Observable.OnSubscribe<Socket>() {
            @Override
            public void call(Subscriber<? super Socket> subscriber) {
                try {
                    ServerSocket serverSocket = new ServerSocket(8888);
                    Socket client = serverSocket.accept();
                    EventBus.getDefault().post(new MessageToFragmentEvent("***** Observable Is On *****", RegFragment.TAG));
                    InputStream inputStream = client.getInputStream();
                    BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder total = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        if (line.equals(Statics.MESSAGE_END)) {
                            EventBus.getDefault().post(new MessageToFragmentEvent(total.toString(), RegFragment.TAG));
                        } else {
                            total.append(line).append('\n');
                            EventBus.getDefault().post(new MessageToFragmentEvent("***** Line Added *****", RegFragment.TAG));
                        }
                    }
                    EventBus.getDefault().post(new MessageToFragmentEvent("***** " + total.toString() + " *****", RegFragment.TAG));
                    //subscriber.onNext(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<Socket>() {
                    @Override
                    public void call(Socket client) {
                        boolean sessionIsOn = true;

                        try {
                        while (sessionIsOn) {
                            InputStream inputStream = client.getInputStream();
                            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

                                StringBuilder total = new StringBuilder();
                                String line;
                            EventBus.getDefault().post(new MessageToFragmentEvent("***** " + "Wait For Line" + " *****", RegFragment.TAG));
                                while ((line = r.readLine()) != null) {
                                    total.append(line).append('\n');
                                }
                               // if(total.length() > 0)
                                 EventBus.getDefault().post(new MessageToFragmentEvent("***** " + total.toString() + " *****", RegFragment.TAG));


//                                if (total.toString().startsWith(Statics.MESSAGE_BEGIN)) {
//                                    String msg = getMsg(total.toString());
//                                    String msgArray[] = msg.split(Statics.COLON);
//                                    if (msgArray[0] != null && msgArray[0].equals(Statics.CLIENT_HANDSHAKE)) {
//                                        EventBus.getDefault().post(new MessageToFragmentEvent("***** HAND SHAKE SERVER SIDE *****", RegFragment.TAG));
//                                        openClient(msgArray[1].replace("/", ""));
//                                    }
//                                }
//
//                                if (total.toString().startsWith(Statics.END_OF_SESSION)) {
//                                    sessionIsOn = false;
//                                }

                        }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * returns a connected client Socket. You should run this methid on a background thread.
     * @param host
     * @return
     */
    public Socket openClient(String host) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, 8888)), 500);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println("gal");
            out.flush();

            EventBus.getDefault().post(new MessageToFragmentEvent("***** CLENT SENT MSG *****", RegFragment.TAG));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }

    private String getMsg(String total) {
        String msg = total.replace(Statics.MESSAGE_BEGIN,"").replace(Statics.MESSAGE_END,"");
        return msg;
    }

    public void openNotOwnerClient(final String host) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String str = Statics.SERVER_HANDSHAKE;
                    clientSocket = openClient(host.replace("/",""));
                    EventBus.getDefault().post(new MessageToFragmentEvent("clientSocket : " +clientSocket, RegFragment.TAG));
//                    OutputStream outputStream = clientSocket.getOutputStream();
//                    outputStream.write(str.getBytes());
//                    outputStream.write("\n".getBytes());
//                    outputStream.();
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                    out.println(str);
                    out.println();
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void pingTheServer() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EventBus.getDefault().post(new MessageToFragmentEvent("clientSocket : " +clientSocket, RegFragment.TAG));
//                    OutputStream outputStream = clientSocket.getOutputStream();
//                    outputStream.write("gal".getBytes());
//                    outputStream.write("maoz".getBytes());
//                    outputStream.close();
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                    out.println("gal");
                    out.flush();
                    out.println(Statics.MESSAGE_END);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public void setAmIOwner(boolean amIOwner) {
        this.amIOwner = amIOwner;
    }
}
