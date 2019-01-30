package ir.android_developing.chargeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Mehrdad on 8/8/2017.
 */

class GetInitializeData extends AsyncTask<Void, Void, Void> {

    private String TAG = MainActivity.class.getSimpleName();
    private static String url = "https://chr724.ir/services/v3/EasyCharge/initializeData";
    private AppCompatActivity mActivity;
    private Boolean success = false;
    private OnEventListener mCallBack;

    GetInitializeData(AppCompatActivity activity, OnEventListener callback) {
        mActivity = activity;
        mCallBack = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        if (isNetworkConnected()) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null) {
                ArrayList<Package> packages;
                ArrayList<Package> giftcards;
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    JSONObject packageObj = jsonObj.getJSONObject("products").getJSONObject("internetPackage").getJSONObject("mtn");
                    packages = new ArrayList<Package>();

                    for (Iterator<String> iter = packageObj.keys(); iter.hasNext(); ) {
                        String key = iter.next();
                        JSONArray tempArray = packageObj.getJSONArray(key);
                        for (int i = 0; i < tempArray.length(); i++) {
                            JSONObject j = tempArray.getJSONObject(i);
                            Package p = new Package(j.getString("id"), j.getString("name"), j.getString("price"));
                            packages.add(p);
                        }
                    }

                    SharedPreferences sharedpreferences = mActivity.getSharedPreferences("KiooskData", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    Type listOfPackages = new TypeToken<ArrayList<Package>>() {
                    }.getType();
                    String strPackages = new Gson().toJson(packages, listOfPackages);

                    editor.putString("packages", strPackages);

                    JSONObject giftcardObj = jsonObj.getJSONObject("products").getJSONObject("giftCard");
                    giftcards = new ArrayList<>();

                    for (Iterator<String> iter = giftcardObj.keys(); iter.hasNext(); ) {
                        String key = iter.next();
                        JSONArray tempArray = giftcardObj.getJSONArray(key);
                        for (int i = 0; i < tempArray.length(); i++) {
                            JSONObject j = tempArray.getJSONObject(i);
                            Package p = new Package(j.getString("id"), j.getString("name"), j.getString("price"));
                            if (!p.getId().toLowerCase().contains("googleplay"))
                                giftcards.add(p);
                        }
                    }

                    Type listOfGiftcards = new TypeToken<ArrayList<Package>>() {
                    }.getType();
                    String strGiftcards = new Gson().toJson(giftcards, listOfGiftcards);

                    editor.putString("giftcards", strGiftcards);

                    editor.apply();
                    success = true;
                } catch (final JSONException e) {
                    Log.e("", "Json parsing error: " + e.getMessage());
                }


            } else {
                Log.e(TAG, "Couldn't get json from server.");

            }
        }
        return null;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            try {
                URL checkUrl = new URL(url);
                HttpURLConnection urlc = (HttpURLConnection) checkUrl.openConnection();
                urlc.setRequestProperty("User-Agent", "test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1000); // mTimeout is in seconds
                urlc.connect();
                return urlc.getResponseCode() == 200;
            } catch (IOException e) {
                Log.i("warning", "Error checking internet connection", e);
                return false;
            }
        }

        return false;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mCallBack != null) {
            if (success)
                mCallBack.onSuccess();
            else
                mCallBack.onFailure();
        }
    }

}