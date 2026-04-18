 package slam.tronic;

import android.bluetooth.*;
import android.content.*;
import android.os.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public class BTH {

    private Context context;
    private Handler handler;
    private BluetoothAdapter adapter;
    private BluetoothServerSocket serverSocket;

    private UUID uuid, MY_UUID0 = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
   private UUID getUUIDFromTag(String tag) {
    return UUID.nameUUIDFromBytes(tag.getBytes());
}

    public static final int STATE_LISTENING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTION_FAILED = 4;
    public static final int STATE_MESSAGE_RECEIVED = 5;
    public static final int STATE_SCAN_FINISHED = 6;
    public static final int STATE_FILE_RECEIVED = 7;
    public static String  filopath="/storage/emulated/0/bthapp/";
    public static double scanDl=4000;

    private Map<String, SendReceive> clients = new HashMap<>();
    private List<SendReceive> clientList = new ArrayList<>();
    private final int MAX_CLIENTS = 7;

    public List<BluetoothDevice> visibleDevices = new ArrayList<>();

    // ===== استقبال ملفات =====
    //private boolean receivingFile = false;
    //private FileOutputStream fileOut;
    //private long remainingBytes = 0;
    
    public void setPathFile(String pth){
        filopath=pth; 
    }
    public void setScanDelay(double dl){
        scanDl=dl;
    }
//==========Constructors====
       public BTH(Context ctx) {
        context = ctx;
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public BTH(Context ctx, Handler h) {
        context = ctx;
        handler = h;
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    // ===== تمكين / تعطيل =====
    public void enable() {
        if (!adapter.isEnabled()) {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(i);
        }
    }
    public void disable() {
        if (adapter.isEnabled()) adapter.disable();
    }
    public boolean isEnabled() {
        return adapter != null && adapter.isEnabled();
    }

    // ===== مسح الأجهزة =====
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) visibleDevices.add(device);
            }
        }
    };

    public void startScan() {
        if (adapter.isDiscovering()) adapter.cancelDiscovery();
        visibleDevices.clear();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(receiver, filter);
        adapter.startDiscovery();

        new Handler().postDelayed(() -> {
            try {
                adapter.cancelDiscovery();
                context.unregisterReceiver(receiver);
            } catch (Exception ignored) {}
            handler.obtainMessage(STATE_SCAN_FINISHED).sendToTarget();
        },(int)scanDl);
    }
    public List<BluetoothDevice> getScannedDevices() { return visibleDevices; }

    // ===== أزواج =====
    public List<BluetoothDevice> getPairedDevicesAsList() {
        List<BluetoothDevice> list = new ArrayList<>();
        if (adapter == null || !adapter.isEnabled()) return list;
        Set<BluetoothDevice> paired = adapter.getBondedDevices();
        if (paired != null) list.addAll(paired);
        return list;
    }

    public boolean pair(BluetoothDevice device) {
        try { if (device.getBondState() != BluetoothDevice.BOND_BONDED) return device.createBond(); }
        catch (Exception ignored) {}
        return false;
    }
    
    public boolean unpair
    (BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond");
            method.invoke(device);
            return true;
        } catch (Exception e) { return false; }
    }
    
    public boolean ispaired
    (BluetoothDevice device) {
        return device != null && device.getBondState() == BluetoothDevice.BOND_BONDED;
    }

    // ===== سيرفر =====
    public void startServer(String name) {
        uuid=MY_UUID0;
        if (adapter.isDiscovering()) adapter.cancelDiscovery();

        new Thread(() -> {
            try {
                handler.obtainMessage(STATE_LISTENING).sendToTarget();
                serverSocket = adapter.listenUsingRfcommWithServiceRecord(name, uuid);

                //while (true) 
                
               while (!Thread.currentThread().isInterrupted())
                {
                    BluetoothSocket socket = serverSocket.accept();
                    if (socket != null && clientList.size() < MAX_CLIENTS) {
                        BluetoothDevice device = socket.getRemoteDevice();
                        SendReceive sr = new SendReceive(socket);
                        sr.start();
                        clients.put(device.getAddress(), sr);
                        clientList.add(sr);
                        handler.obtainMessage(STATE_CONNECTED).sendToTarget();
                    }
                }

            } catch (IOException e) {
                handler.obtainMessage(STATE_CONNECTION_FAILED).sendToTarget();
            }
        }).start();
    }
    //=≈========================
   public void startServer(String name,String tag) {
    if(adapter.isDiscovering()) adapter.cancelDiscovery();

     uuid = getUUIDFromTag(tag);

    new Thread(() -> {
        try {
            handler.obtainMessage(STATE_LISTENING).sendToTarget();
            serverSocket = adapter.listenUsingRfcommWithServiceRecord(name, uuid);

            //while(true) 
            
           while (!Thread.currentThread().isInterrupted())
            {
                BluetoothSocket socket = serverSocket.accept();
                if(socket!=null && clientList.size()<MAX_CLIENTS) {
                    BluetoothDevice device = socket.getRemoteDevice();
                    SendReceive sr = new SendReceive(socket);
                    sr.start();
                    clients.put(device.getAddress(), sr);
                    clientList.add(sr);
                    handler.obtainMessage(STATE_CONNECTED).sendToTarget();
                }
            }

        } catch(IOException e){
            handler.obtainMessage(STATE_CONNECTION_FAILED).sendToTarget();
        }
    }).start();
}

    // ===== كليان =====
    public void connect(BluetoothDevice device) {
          uuid=MY_UUID0;
        new Thread(() -> {
            try {
                if (adapter.isDiscovering()) adapter.cancelDiscovery();
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
                socket.connect();
                SendReceive sr = new SendReceive(socket);
                sr.start();
                clients.put(device.getAddress(), sr);
                clientList.add(sr);
                handler.obtainMessage(STATE_CONNECTED).sendToTarget();
            } catch (IOException e) {
                handler.obtainMessage(STATE_CONNECTION_FAILED).sendToTarget();
            }
        }).start();
    }
    //==================================
public void connect(BluetoothDevice device, String tag){
    new Thread(() -> {
        try {
            if(adapter.isDiscovering()) adapter.cancelDiscovery();
            UUID uuid = getUUIDFromTag(tag);
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            SendReceive sr = new SendReceive(socket);
            sr.start();
            clients.put(device.getAddress(), sr);
            clientList.add(sr);
            handler.obtainMessage(STATE_CONNECTED).sendToTarget();
        } catch(IOException e){
            handler.obtainMessage(STATE_CONNECTION_FAILED).sendToTarget();
        }
    }).start();
}


    // ===== ارسال رسائل ===================
    public void sendText(String msg){
        if(clientList.isEmpty()) return;
        byte[] data = ("MSG|" + msg +"\n" ).getBytes(); // علامة MSG
        //byte[] data = (msg).getBytes(); // علامة MSG
        for(SendReceive sr : clientList) if(sr != null) sr.write(data);
    }

   // public void sendBroadcast(String msg) { sendText(msg); }
    
    public void sendPrivate(BluetoothDevice device, String msg){
        
        if(device==null) return;
       // SendReceive sr = clients.get(device.getAddress());
        SendReceive sr = clients.get(device.getAddress());
        if(sr!=null) sr.write(("MSG|" + msg +"\n").getBytes());
    }

    
   public void sendFilePath(BluetoothDevice device, String path){

    SendReceive sr = clients.get(device.getAddress());
    if(sr == null) return;

    new Thread(() -> {

        try{

            File file = new File(path);
            if(!file.exists()) return;

            FileInputStream fis = new FileInputStream(file);

            String header = "FILE|" + file.getName() + "|" + file.length() + "\n";
            sr.write(header.getBytes("UTF-8"));

            byte[] buffer = new byte[4096];
            int bytes;

            while((bytes = fis.read(buffer)) != -1){
                sr.write(Arrays.copyOf(buffer, bytes));
            }

            fis.close();

        }catch(Exception e){
            e.printStackTrace();
        }

    }).start();
}
    
   //===================
   public void sendFile(String filePath){

    File file = new File(filePath);
    if(!file.exists()) return;

    for(SendReceive sr : clientList){

        if(sr == null) continue;

        new Thread(() -> {

            try{

                FileInputStream fis = new FileInputStream(file);

                String header = "FILE|" + file.getName() + "|" + file.length() + "\n";
                sr.write(header.getBytes("UTF-8"));

                byte[] buffer = new byte[4096];
                int bytes;

                while((bytes = fis.read(buffer)) != -1){
                    sr.write(Arrays.copyOf(buffer, bytes));
                }

                fis.close();

            }catch(Exception ignored){}

        }).start();
    }
}

//===============ارسال لموديل مثل الصوت//=====
public void sendRawFileTo(BluetoothDevice device, String path){

    SendReceive sr = clients.get(device.getAddress());
    if(sr == null) return;

    new Thread(() -> {

        try{

            File file = new File(path);
            if(!file.exists()) return;

            FileInputStream fis = new FileInputStream(file);

            byte[] buffer = new byte[4096];
            int bytes;

            while((bytes = fis.read(buffer)) != -1){
                sr.write(Arrays.copyOf(buffer, bytes));
            }

            fis.close();

        }catch(Exception e){
            e.printStackTrace();
        }

    }).start();
}

    // ===== استقبال الرسائل والملفات =====
    private class SendReceive extends Thread {
        
        private boolean receivingFile = false;
        private FileOutputStream fileOut;
        private long remainingBytes = 0;
        private BluetoothSocket socket;
        private InputStream input;
        private OutputStream output;

        public SendReceive(BluetoothSocket s){
            socket = s;
            try{
                input = socket.getInputStream();
                output = socket.getOutputStream();
            }catch(IOException ignored){}
        }
        
        
           //===============run========== 
            
          @Override
public void run(){

    try{

        BufferedInputStream bis = new BufferedInputStream(input);

        StringBuilder buffer = new StringBuilder();
        byte[] temp = new byte[1024];
        int bytes;

        while((bytes = bis.read(temp)) != -1){

            String chunk = new String(temp, 0, bytes);
            buffer.append(chunk);

            int index;

            // 🔥 فصل الرسائل حسب \n
            while((index = buffer.indexOf("\n")) != -1){

                String line = buffer.substring(0, index).trim();
                buffer.delete(0, index + 1);

                // ===== FILE =====
                if(line.startsWith("FILE|")){

                    String[] parts = line.split("\\|");
                    String name = parts[1];
                    long size = Long.parseLong(parts[2]);

                    File dir = new File(filopath);
                    if(!dir.exists()) dir.mkdirs();

                    File file = new File(dir, name);
                    FileOutputStream fos = new FileOutputStream(file);

                    byte[] fileBuffer = new byte[4096];
                    long remaining = size;

                    while(remaining > 0){

                        int read = bis.read(fileBuffer, 0, (int)Math.min(fileBuffer.length, remaining));

                        if(read == -1) break;

                        fos.write(fileBuffer, 0, read);
                        remaining -= read;
                    }

                    fos.close();

                    handler.obtainMessage(
                        STATE_FILE_RECEIVED,
                        0,
                        0,
                        file.getAbsolutePath()
                    ).sendToTarget();
                }

                // ===== MESSAGE (أي شيء) =====
                else{

                    handler.obtainMessage(
                        STATE_MESSAGE_RECEIVED,
                        line.length(),
                        -1,
                        line.getBytes()
                    ).sendToTarget();
                }
            }
        }

    }catch(Exception e){
        e.printStackTrace();
    }
}
           
        
//=================write=========
        public void write(byte[] bytes){
            try{ output.write(bytes); output.flush(); } catch(IOException ignored){}
        }
    }

    // ===== متابعة طلب الاقتران =====
    public void listenToPaireRequest(){
        BroadcastReceiver pairReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context c, Intent intent){
                if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())){
                    int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    switch(state){
                        case BluetoothDevice.BOND_BONDED: handler.obtainMessage(8).sendToTarget(); break;
                        case BluetoothDevice.BOND_BONDING: handler.obtainMessage(9).sendToTarget(); break;
                        case BluetoothDevice.BOND_NONE: handler.obtainMessage(10).sendToTarget(); break;
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(pairReceiver, filter);
    }
}
