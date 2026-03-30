package slam.tronic;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Handler;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageView;
import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.security.MessageDigest;

public class TEMPLE {

    private Context context;
    private static Handler handler;
    private static String qrstr = "";

    private static final int STATE_WAITING = 1;
    private static final int STATE_MESSAGE_RECEIVED = 2;

    public TEMPLE(Context ctx, Handler h) {
        context = ctx;
        handler = h;
    }

    public TEMPLE(Context ctx) {
        context = ctx;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent == null || intent.getAction() == null) return;

            switch (intent.getAction()) {
                case "action example":
                    break;
                default:
                    break;
            }
        }
    };

    public static void delai(int dl) {
        try {
            Thread.sleep(dl);
        } catch (Exception ex) {
        }
    }

    public static void onDelay(int dl) {
        delai(dl);
        if (handler != null) {
            handler.obtainMessage(STATE_WAITING).sendToTarget();
        }
    }

    public static void toHandler(String str) {
        if (handler != null && str != null) {
            handler.obtainMessage(
                STATE_MESSAGE_RECEIVED,
                str.length(),
                -1,
                str.getBytes()
            ).sendToTarget();
        }
    }

    public static void test_Merror(String str) {
        delai(1500);
        toHandler(str);
    }

    ////


}
