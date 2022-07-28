package com.example.satya;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import java.util.List;

public class WhatsAppAccessibilityService extends AccessibilityService {
    private static final String TAG = "WhatsAppAccessibilityService";
    /*@Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if(getRootInActiveWindow()==null){
            return;
        }

        //getting root node
        AccessibilityNodeInfoCompat rootNodeInfo = AccessibilityNodeInfoCompat.wrap(getRootInActiveWindow());

        if( rootNodeInfo == null )
            return;

//        System.out.println("rootNodeInfo` = " + rootNodeInfo);
        //get edit text if message from whats app
        List<AccessibilityNodeInfoCompat> messageNodeList = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.whatsapp:id/entry");
        if( messageNodeList == null || messageNodeList.isEmpty() ) {
            System.out.println("Suspect - 1 *********************" );
            return;
        }

        //checking if message field if filled with text and ending with our suffix
        AccessibilityNodeInfoCompat messageField = messageNodeList.get(0);
        if(messageField== null || messageField.getText().length() ==0 || messageField.getText().toString().endsWith("   ")) {
            System.out.println("Suspect - 2 *********************" );
            return;
        }

        //get whatsapp send message button node list
        List<AccessibilityNodeInfoCompat> sendMessageNodeList = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send");
        if( sendMessageNodeList == null || sendMessageNodeList.isEmpty() ) {
            System.out.println("Suspect - 3 *********************" );
            return;
        }

        AccessibilityNodeInfoCompat sendMessage = sendMessageNodeList.get(0);
        if(!sendMessage.isVisibleToUser())
            return;

        //fire send button
        sendMessage.performAction(AccessibilityNodeInfo.ACTION_CLICK);

        //go back to our app by clicking back button twice

        try{
            Thread.sleep(2000);
            performGlobalAction(GLOBAL_ACTION_BACK);
            Thread.sleep(2000);
        }catch (InterruptedException ignored ){
            System.out.println("ignored = " + ignored);
        }
        performGlobalAction(GLOBAL_ACTION_BACK);

    }*/


    @SuppressLint("LongLogTag")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event.getEventType()) {
            Log.e(TAG, "Acc:onAccessiblityEvent: event=" + event);

            AccessibilityNodeInfo nodeInfo = event.getSource();
            Log.e(TAG, "Acc:onAccessiblityEvent: nodeInfo = " + nodeInfo);
            if (nodeInfo == null) {
                return;
            }


            //get whatsapp send message button node list
            List<AccessibilityNodeInfo> sendMessageNodeList = nodeInfo.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send");
            for (AccessibilityNodeInfo node : sendMessageNodeList) {
                Log.e(TAG, "Acc:onAccessiblityEvent: send_button =" + node);
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                //go back to our app by clicking back button twice

                try {
                    Thread.sleep(2000);
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    Thread.sleep(2000);
                    performGlobalAction(GLOBAL_ACTION_BACK);
                } catch (InterruptedException ignored) {
                    System.out.println("ignored = " + ignored);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }
}
