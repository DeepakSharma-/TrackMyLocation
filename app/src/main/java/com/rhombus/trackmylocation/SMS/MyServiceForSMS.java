package com.rhombus.trackmylocation.SMS;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rhombus.trackmylocation.Modle.MessageDetails;
import com.rhombus.trackmylocation.UtilityTrackLocation.SharedPreferenceTrack;
import com.rhombus.trackmylocation.UtilityTrackLocation.TrackConstant;
import com.rhombus.trackmylocation.database.AndroidSQLiteOpenHelper;
import com.rhombus.trackmylocation.database.Database;
import com.rhombus.trackmylocation.location.LocationResult;
import com.rhombus.trackmylocation.location.MyLocation;

import java.util.ArrayList;

/**
 * Created by deepak.kumar on 10/28/2015.
 */
public class MyServiceForSMS extends Service implements LocationResult {
    String number,strMessage="";
    Context context;
double latilude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatilude() {
        return latilude;
    }

    public void setLatilude(double latilude) {
        this.latilude = latilude;
    }

    double longitude;
    ArrayList<MessageDetails> messageDetailsesList;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.context = this;
        // Let it continue running until it is stopped.
        if (intent!=null && intent.hasExtra("track_me")){
            checkNumberInDatabaseTrackMe(this);
        }else{
            this.stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
     //   Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    public boolean checkNumberInDatabaseTrackMe(Context context) {
        Database database = Database.getInstance(context);
        messageDetailsesList = null;
        messageDetailsesList = new ArrayList<MessageDetails>();
        String query = "SELECT * FROM " + AndroidSQLiteOpenHelper.TRACK_ME;
        Cursor cursor = database.rawQueryMy(query);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            if (cursor != null && cursor.getCount() > 0) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    MessageDetails messageDetails = new MessageDetails();
                    messageDetails.setMessageNumber(cursor.getString(cursor.getColumnIndex(AndroidSQLiteOpenHelper.NUMBER)));
                    messageDetails.setMessageText(cursor.getString(cursor.getColumnIndex(AndroidSQLiteOpenHelper.MESSAGE)) + " http://maps.google.com/?daddr=%replat,%replong");

                    messageDetailsesList.add(messageDetails);
                    strMessage +="nMessage will be send to \n\n NAME : " + cursor.getString(cursor.getColumnIndex(AndroidSQLiteOpenHelper.NUMBER)) + "\n MESSAGE TO :" + cursor.getString(cursor.getColumnIndex(AndroidSQLiteOpenHelper.NUMBER)) + "\n MESSAGE TEXT :" + cursor.getString(cursor.getColumnIndex(AndroidSQLiteOpenHelper.MESSAGE))+"\nhttp://maps.google.com/?daddr=%replat,%replong\n";
//                    Toast.makeText(context, "Message will be send to \n\n NAME : " + cursor.getString(1) + "\n MESSAGE TO :" + cursor.getString(2) + "\n MESSAGE TEXT :" + cursor.getString(3)+"\nhttp://maps.google.com/?daddr="+latilude+","+longitude, Toast.LENGTH_LONG).show();
                    //http://maps.google.com/?daddr=56.3546,34.322546"
                 //   Log.e("@@@@@@@@","--------"+strMessage);
                    cursor.moveToNext();
                }
            }
            getMyCurrentLocation();

        } else {
            // Toast.makeText(context, "No Shared Contact added", Toast.LENGTH_LONG).show();
            SharedPreferenceTrack.setValueBooleanPreference(context, TrackConstant.IS_TRACKING_ON,false);
            this.stopSelf();
        }
        return true;
    }

    /**
     * Get User Current location after checking is any location provider is
     * available or not if not then go to setting screen else get user Current
     * Location.
     */
    public void getMyCurrentLocation() {
        boolean isLocationProviderActive = ((LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE))
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        boolean isNetworkProviderActive = ((LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE))
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

//        if (!isLocationProviderActive && !isNetworkProviderActive) {
//            getLocationSettingsDialog(this);
//        } else {

            MyLocation location = new MyLocation();
            location.getLocation(this, this);
//        }
    }

    public static void getLocationSettingsDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage("Your Location Settings are turned off. Do you want to turn them on?");
        builder.setTitle("Warning");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent myIntent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(myIntent);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    @Override
    public void gotLocation(Location location) {
        if (location != null) {

            Log.e("Location",
                    location.getLatitude() + " " + location.getLongitude());
            latilude = location.getLatitude();
            longitude = location.getLongitude();
            setLatilude(latilude);
            setLongitude(longitude);
            strMessage = strMessage.replace("%replat",""+latilude);
            strMessage = strMessage.replace("%replong",""+longitude);
         //  handler.sendEmptyMessage(2);
            //start send message thread

          //  Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();
            SMStoMultiplepeople sms = new SMStoMultiplepeople();
            sms.startSendMessages(context,messageDetailsesList,getLatilude(),getLongitude());
         //   this.stopSelf();
        } else {
            Log.e("Location", "NO LOCATION");
//            Toast.makeText(this,
//                    "Error while fetching location", Toast.LENGTH_SHORT).show();
            getMyCurrentLocation();
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    //checkNumberInDatabase(context, number);
                    break;
                case 2:
                   // Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();
                    Log.e("@@@@@@@@", "--------" + strMessage);
                    break;
                default:
                    break;
            }
        }

        ;
    };
}
