package slam.tronic;

import android.content.Context;
import android.os.Handler;
import java.io.IOException;
import android.content.*;
import android.os.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

public class TEMPLE {

    private Context context;
    private Handler handler;
    private  final int STATE_WAITING=1;
   private  final int STATE_MESSAGE_RECEIVED=2;
    //===========
    public TEMPLE(Context ctx, Handler h) {
        context = ctx;
        handler = h;
        //adapter = BluetoothAdapter.getDefaultAdapter();
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
       
    }
    //============
    
    

