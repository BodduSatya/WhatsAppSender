package com.example.satya;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.net.URLEncoder;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG="FCMService";
    public static final String FCM_CHANNEL_ID="FCM_CHANNEL_ID";
    private static final String FILEPATH = "filepath";
    private static final String MOBILE_NUMBER = "mobile_number";
    private static final String ACTION_WHATSAPP = "action_whatsapp";
    private static final String MESSAGE = "message";
    private static final String COUNT = "count";
    private static final String DELAY = "delay";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG,"onMessageReceived called");
        Log.d(TAG,"message received from "+message.getFrom());

        if( message.getNotification()!=null ){
            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();

            Log.d(TAG,"onMessageReceived title ="+title);
            Log.d(TAG,"onMessageReceived body="+body);

//            sendNotification(message.getFrom(),message.getNotification().getBody());
            sendNotification2(title,body);
            sendWAMsg(title,body);
//            handleActionWA( body, getNumbers(title), "/Internal Storage/Download/images.jpg", "" );
        }

        message.getData();
        if(message.getData().size()>0){
            Log.d(TAG,"message received with data="+message.getData());
        }
    }

    private void sendNotification2(String title, String body){
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,
                intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this,FCM_CHANNEL_ID)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setColor(Color.BLUE);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel fcmChannel = new NotificationChannel(FCM_CHANNEL_ID, "FCM_Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(fcmChannel);
        }
        notificationManager.notify(0,notificationBuilder.build());

    }

    private void sendNotification(String from, String body){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyFirebaseMessagingService.this.getApplicationContext(),
                        from+"-"+body,Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showToast(String msg){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyFirebaseMessagingService.this.getApplicationContext(),
                        msg,Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d(TAG,"onDeletedMessages called");
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG,"onNewToken called = "+token);
    }

    public void sendWAMsg( String phNo, String msg ) {
        try {
            PackageManager packageManager = getApplicationContext().getPackageManager();
            String url = "whatsapp://send?phone="+phNo+"&text="+ URLEncoder.encode(msg,"UTF-8");
            System.out.println("url = " + url);
            Intent whatappIntent = new Intent(Intent.ACTION_VIEW);
            whatappIntent.setPackage("com.whatsapp");
            whatappIntent.setData(Uri.parse(url));
            whatappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(whatappIntent.resolveActivity(packageManager)!=null) {
                getApplicationContext().startActivity(whatappIntent);
                Thread.sleep(5000);
                showToast("whatapp send ");
            }else{
                showToast("whatapp not installed ");
            }
        }catch (Exception e){
            showToast("whatsapp sending failed! ");
        }
    }

    private String[] getNumbers(String phNo){
        phNo = phNo.replace("+","").replaceAll(" ","");
        String[] numbers = phNo.split(",");
        for(int i=0;i< numbers.length;i++){
            if( numbers[i].length()<=10)
                numbers[i]="91"+numbers[i];
        }
        return numbers;
    }

    public void handleActionWA( String message, String[] phNos, String filepath, String delay ) {
        int delayN = 10000;
        if(!delay.equals("")){
            delayN = Integer.parseInt(delay);
        }
        if(filepath!=null){
            try {
                PackageManager packageManager = getApplicationContext().getPackageManager();
                if (phNos.length > 0) {
                    for( int j=0;j<phNos.length;j++){
                        String number = phNos[j];
                        Intent sendIntent = new Intent("android.intent.action.MAIN");
                        System.out.println("filepath = " + filepath);
//                        Uri fileData = FileProvider.getUriForFile(getApplicationContext(),getApplicationContext().
//                                getApplicationContext().getPackageName()+".provider",new File(filepath));
                        Uri fileData = Uri.parse("https://aapyaya.com/assets/img/home_img.jpg");
                        System.out.println("fileData = " + fileData);
                        sendIntent.putExtra(Intent.EXTRA_STREAM,fileData);
                        sendIntent.putExtra("jid",number+"@s.whatsapp.net");
                        sendIntent.putExtra(Intent.EXTRA_TEXT,message);
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        sendIntent.setPackage("com.whatsapp");
                        sendIntent.setType(getMimeType(fileData));
                        if( sendIntent.resolveActivity(packageManager)!=null){
                            getApplicationContext().startActivity(sendIntent);
                            Thread.sleep(delayN);
                            showToast("Result: whatapp msg sent to "+number);
                        }
                        else {
                            showToast("Result: whatapp not installed ");
                        }
                    }
                }
            }catch(Exception e){
                showToast("whatsapp sending failed! ");
                e.printStackTrace();
            }

        }else{
            try {
                PackageManager packageManager = getApplicationContext().getPackageManager();
                if (phNos.length > 0) {
                    for (int j = 0; j < phNos.length; j++) {
                        String number = phNos[j];
                        String url = "whatsapp://send?phone=" + number + "&text=" + URLEncoder.encode(message, "UTF-8");
                        System.out.println("url = " + url);
                        Intent whatappIntent = new Intent(Intent.ACTION_VIEW);
                        whatappIntent.setPackage("com.whatsapp");
                        whatappIntent.setData(Uri.parse(url));
                        whatappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (whatappIntent.resolveActivity(packageManager) != null) {
                            getApplicationContext().startActivity(whatappIntent);
                            Thread.sleep(delayN);
                            showToast("whatapp send ");
                        } else {
                            showToast("whatapp not installed ");
                        }
                    }
                }
            }catch(Exception e){
                showToast("whatsapp sending failed! ");
            }
        }

    }

    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = MyFirebaseMessagingService.this.getApplicationContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        System.out.println("mimeType = " + mimeType);
        return mimeType;
    }

    public static void startActionWHATSAPP(Context applicationContext,
                                           String message,
                                           String textcount,
                                           String[] numbers,
                                           String file_path,
                                           String delay){
        Intent intent = new Intent(applicationContext,MyFirebaseMessagingService.class);
        intent.setAction(ACTION_WHATSAPP);
        intent.putExtra(MESSAGE,message);
        intent.putExtra(COUNT,textcount);
        intent.putExtra(MOBILE_NUMBER,numbers);
        intent.putExtra(FILEPATH,file_path);
        intent.putExtra(DELAY,delay);
        applicationContext.startService(intent);
//        onHandleIntent(intent);

    }

    protected void onHandleIntent(Intent intent){
        if( intent!=null ){
            final String action = intent.getAction();
            if(ACTION_WHATSAPP.equals(action)){
                final String message = intent.getStringExtra(MESSAGE);
                final String count = intent.getStringExtra(COUNT);
                final String delay = intent.getStringExtra(DELAY);
                final String filepath = intent.getStringExtra(FILEPATH);
                final String[] mobile_number = intent.getStringArrayExtra(MOBILE_NUMBER);
                handleActionWA(message,mobile_number,filepath,delay);
            }
        }
    }
}

