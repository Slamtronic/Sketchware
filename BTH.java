package slam.tronic;

import android.bluetooth.*;
import android.content.*;
import android.os.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaCodec;
import java.nio.ByteBuffer;

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
 //   public static final int STATE_MESSAGE_RECEIVED = 5;
    public static final int STATE_SCAN_FINISHED = 6;
    //public static final int STATE_FILE_RECEIVED = 7;
    public static final int STATE_DATA_RECEIVED = 5;
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
    
//==============broad data========
public void broadcastData(byte[] data){

    if(clientList.isEmpty() || data == null) return;

    Iterator<SendReceive> it = clientList.iterator();

    while(it.hasNext()){
        SendReceive sr = it.next();

        try{
            sr.write(data);
        }catch(Exception e){
            it.remove();
        }
    }
}
//================private data===/===
public void privateData(BluetoothDevice device, byte[] data){

    if(device == null || data == null) return;

    SendReceive sr = clients.get(device.getAddress());

    if(sr != null){
        try{
            sr.write(data);
        }catch(Exception e){
            clients.remove(device.getAddress());
        }
    }
}
///===========ملفات الصوت==================
///=============================
///=============================
public void streamMp3( BluetoothDevice dvc, String path){

    new Thread(() -> {

        try{

            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(path);

            MediaFormat format = extractor.getTrackFormat(0);
            extractor.selectTrack(0);

            MediaCodec codec = MediaCodec.createDecoderByType(
                    format.getString(MediaFormat.KEY_MIME)
            );

            codec.configure(format, null, null, 0);
            codec.start();

            ByteBuffer[] inputBuffers = codec.getInputBuffers();
            ByteBuffer[] outputBuffers = codec.getOutputBuffers();

            boolean isEOS = false;

            while(true){

                if(!isEOS){
                    int inIndex = codec.dequeueInputBuffer(10000);
                    if(inIndex >= 0){
                        ByteBuffer buffer = inputBuffers[inIndex];

                        int sampleSize = extractor.readSampleData(buffer, 0);

                        if(sampleSize < 0){
                            codec.queueInputBuffer(inIndex, 0, 0, 0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEOS = true;
                        }else{
                            codec.queueInputBuffer(inIndex, 0, sampleSize,
                                    extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }

                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                int outIndex = codec.dequeueOutputBuffer(info, 10000);

                if(outIndex >= 0){

                    ByteBuffer outBuffer = outputBuffers[outIndex];

                    byte[] chunk = new byte[info.size];
                    outBuffer.get(chunk);
                    outBuffer.clear();

                    // 🔥 إرسال PCM
                   // broadcastData(chunk);
                  privateData(dvc ,chunk);

                    codec.releaseOutputBuffer(outIndex, false);
                }

                if((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                    break;
                }
            }

            codec.stop();
            codec.release();
            extractor.release();

        }catch(Exception e){
            e.printStackTrace();
        }

    }).start();
}
        //=========================
      //==========================
    // ===== استقبال الرسائل والملفات =====
    private class SendReceive extends Thread {

    private BluetoothSocket socket;
    private InputStream input;
    private OutputStream output;
    private boolean running = true;

    public SendReceive(BluetoothSocket s){
        socket = s;
        try{
            input = socket.getInputStream();
            output = socket.getOutputStream();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){

        byte[] buffer = new byte[1024];
        int bytes;

        try{

            while(running && (bytes = input.read(buffer)) != -1){

                // 🔥 نرسل raw bytes مباشرة للـ handler
                byte[] data = Arrays.copyOf(buffer, bytes);

                if(handler != null){
                    handler.obtainMessage(
                        STATE_DATA_RECEIVED,
                        bytes,
                        -1,
                        data
                    ).sendToTarget();
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            close();
        }
    }

    // ===== إرسال =====
    public void write(byte[] bytes){
        try{
            if(output != null){
                output.write(bytes);
                output.flush();
            }
        }catch(IOException e){
            e.printStackTrace();
            close();
        }
    }

    // ===== غلق الاتصال =====
    public void close(){
        running = false;

        try{
            if(socket != null) socket.close();
        }catch(Exception ignored){}
    }
}
}