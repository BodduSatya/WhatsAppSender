package com.example.satya;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    public static final String FCM_CHANNEL_ID="FCM_CHANNEL_ID";
    private static final String TAG = "MainActivity";
    private EditText txt_message;
    private EditText txt_number;
    private EditText txt_count;
    private EditText txt_delay;
    private Button btn_manual;
    private Button btn_sms;
    private Button btn_attachment;
    private Button btn_whatsapp;

    private String file_path="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_message = findViewById(R.id.txt_message);
        txt_number = findViewById(R.id.txt_mobile);
        txt_count= findViewById(R.id.txt_count);
//        txt_delay= findViewById(R.id.txt_delay);

        btn_manual = findViewById(R.id.btn_manual);
        btn_sms = findViewById(R.id.btn_sms);
        btn_attachment = findViewById(R.id.btn_attachment);
        btn_whatsapp = findViewById(R.id.btn_whatsapp);

        Dexter.withContext(this)
                .withPermission(Manifest.permission.SEND_SMS)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {/* ... */}
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {/* ... */}
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();

//        createNotificationChannel();
        subscribeTopics();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }
                String token = task.getResult();
                Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                subscribeTopics();
            }
        });

        if(!isAccessibilitySettingsOn(getApplicationContext())){
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        btn_sms.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                try {
                    if( txt_number.length()!=0  ) {
                        for (int i = 0; i < Integer.parseInt(txt_count.getText().toString()); i++) {
                            System.out.println("i = " + i);
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(txt_number.getText().toString(), null, txt_message.getText().toString(), null, null);
                            Toast.makeText(MainActivity.this, "SMS send " + (i + 1), Toast.LENGTH_SHORT).show();
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(MainActivity.this,"SMS sending failed!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_manual.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

            }
        });

        btn_attachment.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ChooserDialog dialog2 = new ChooserDialog(v.getContext())
                        .enableOptions(true)
                        .withFilter(false,true)
                        .withStartFile(Environment.getExternalStorageDirectory().getAbsolutePath())
                        .withChosenListener(new ChooserDialog.Result(){
                            @Override
                            public void onChoosePath(String path, File pathFile){
                                file_path = path;

                                }
                        })
                        .withOnCancelListener(new DialogInterface.OnCancelListener(){
                            @Override
                            public void onCancel(DialogInterface dialog){
                                Log.d("CANCEL","CANCEL");
                                dialog.dismiss();
                            }
                        });
                dialog2.build();
                dialog2.show();
            }
        });

        btn_whatsapp.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View view) {

//                MySMSservice.startActionSMS(getApplicationContext(),txt_message.getText().toString(),
//                        txt_count.getText().toString(),results);

                /*try {
                    PackageManager packageManager = getApplicationContext().getPackageManager();
//                    if( txt_number.length()!=0  ) {
                    System.out.println("expr = " + Integer.parseInt(txt_count.getText().toString()));
//                        for (int i = 0; i < Integer.parseInt(txt_count.getText().toString()); i++) {
//                            System.out.println("i = " + i);
//                            String url = "https://api.whatsapp.com?phone="+txt_number.getText().toString()+"&text="+ URLEncoder.encode(txt_message.getText().toString(),"UTF-8");
                    String url = "whatsapp://send?phone="+txt_number.getText().toString()+"&text="+ URLEncoder.encode(txt_message.getText().toString(),"UTF-8");
                    System.out.println("url = " + url);
                    Intent whatappIntent = new Intent(Intent.ACTION_VIEW);
                    whatappIntent.setPackage("com.whatsapp");
                    whatappIntent.setData(Uri.parse(url));
                    whatappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    System.out.println("@@@");
                    if(whatappIntent.resolveActivity(packageManager)!=null) {
                        System.out.println("####");
                        getApplicationContext().startActivity(whatappIntent);
                        Thread.sleep(5000);
                        Toast.makeText(MainActivity.this, "whatapp send " , Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "whatapp not installed " , Toast.LENGTH_SHORT).show();
                    }
//                                sendBroadcastMessage("");
//                            System.out.println("i = " + i);
//                            SmsManager smsManager = SmsManager.getDefault();
//                            smsManager.sendTextMessage(txt_number.getText().toString(), null, txt_message.getText().toString(), null, null);
//                            Toast.makeText(MainActivity.this, "SMS send " + (i + 1), Toast.LENGTH_SHORT).show();
//                        }
//                    }
                }catch (Exception e){
                    Toast.makeText(MainActivity.this,"whatsapp sending failed!",Toast.LENGTH_SHORT).show();
                }*/
                System.out.println("file_path = " + file_path);
//                MyFirebaseMessagingService.startActionWHATSAPP(getApplicationContext(),
//                        txt_message.getText().toString(),"1",
//                        new String[]{"919014434569"},file_path,"");

                handleActionWA(
                        txt_message.getText().toString(),
                        new String[]{"919014434569"},file_path,"");

            }
        });
    }

    // To check if service is enabled
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + WhatsAppAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel fcmChannel = new NotificationChannel(FCM_CHANNEL_ID, "FCM_Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(fcmChannel);
        }
    }

    private void subscribeTopics() {
        // [START subscribe_topics]
        FirebaseMessaging.getInstance().subscribeToTopic("whatsappUAT")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        // [END subscribe_topics]
    }

    public void handleActionWA( String message, String[] phNos, String filepath, String delay ) {
        int delayN = 10000;
        if(!delay.equals("")){
            delayN = Integer.parseInt(delay);
        }
        filepath = "  ";
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
                        Uri fileData = Uri.parse("http://www.africau.edu/images/default/sample.pdf");
//                        Uri fileData =  Uri.parse( "https://aapyaya.com/assets/img/home_img.jpg" );
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

    private void showToast(String msg){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this.getApplicationContext(),
                        msg,Toast.LENGTH_SHORT).show();
            }
        });

    }

    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = MainActivity.this.getApplicationContext().getContentResolver();
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

}