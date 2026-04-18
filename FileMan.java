package slam.tronic;

import android.content.Context;
import android.os.Handler;
import java.io.IOException;
import android.content.*;
import android.os.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public class FileMan {

    private Context context;
    private Handler handler;
    private  final int STATE_WAITING=1;
   private  final int STATE_MESSAGE_RECEIVED=2;
    //===========
    public FileMan(Context ctx, Handler h) {
        context = ctx;
        handler = h;
        //adapter = BluetoothAdapter.getDefaultAdapter();
    }
    //===========
   //===========
    public FileMan(Context ctx) {
        context = ctx;
        }
    //===========
    
    

   private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
          //  String str=Intent.getAction();
            switch (intent.getAction()){ 
             case "action example":
               //do something 
               break;
               
               
               
               
               default:
               
               break;}
               
            
        }
    };
    //==============
    public void delai(int dl){
       try {Thread.sleep((int)dl); // إعادة المحاولة بعد ثانية
	                } catch (Exception ex) { 
                      // handler.obtainMessage(STATE_WAITING).sendToTarget();}
                      }}
     public void onDelay(int dl){                 
        delai(dl);
       handler.obtainMessage(STATE_WAITING).sendToTarget();  }
       
      //=================
      public void toHandler(String str){
         handler.obtainMessage(
                        STATE_MESSAGE_RECEIVED,
                        str.length(),
                        -1,
                        str.getBytes()
                    ).sendToTarget();
      } 
      
    //=================
    public void test_Merror(String str){
        delai(1500);toHandler(str);
    }   
    //==============
public static byte[] textToBytes(String text){
    if(text == null) return new byte[0];
    return text.getBytes();
}
//================
  public static byte[] fileToBytes(String path){

    try{
        File file = new File(path);
        if(!file.exists()) return null;

        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int bytes;

        while((bytes = fis.read(buffer)) != -1){
            bos.write(buffer, 0, bytes);
        }

        fis.close();
        return bos.toByteArray();

    }catch(Exception e){
        e.printStackTrace();
        return null;
    }
}
//============
  public static String bytesToText(byte[] data){
    if(data == null) return "";
    return new String(data);
}
//=============
  public static boolean bytesToFile(byte[] data, String path){

    try{
        if(data == null) return false;

        File file = new File(path);

        // إنشاء الفولدر إذا ماكانش
        File parent = file.getParentFile();
        if(parent != null && !parent.exists()){
            parent.mkdirs();
        }

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.flush();
        fos.close();

        return true;

    }catch(Exception e){
        e.printStackTrace();
        return false;
    }
}
//============/////
public  static File getFilebypath(String pFile)
{
    
   File file = new File(pFile);
            if(!file.exists()) return null;
            else return file;
}
public static FileInputStream getFisbyFile(File file) {
    
        if (!file.exists()) return null;
       FileInputStream fis=null;
try {
         fis = new FileInputStream(file);
        //return fis;

    } catch (IOException e) {
        e.printStackTrace();
    }

    return fis; // ✅ الحل هنا
}
public  static FileInputStream skipFirst44byte (FileInputStream fis)
{  try{ 
    fis.skip(44);
    } catch (IOException e) {
        e.printStackTrace();
    }
    return fis;} 
//=================
public static byte[] getbytebyFis(FileInputStream fis) {

    byte[] buffer = new byte[1024];
    int bytes;
    byte[] chunk = new byte[0]; // ✅ تهيئة

    try {
        while ((bytes = fis.read(buffer)) != -1) {
            chunk = Arrays.copyOf(buffer, bytes);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    return chunk;
}
    }
    //============
    
    

