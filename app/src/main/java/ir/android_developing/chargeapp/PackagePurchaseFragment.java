package ir.android_developing.chargeapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.Gson;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class PackagePurchaseFragment extends Fragment implements View.OnClickListener {

    private AppCompatButton buyBtn;
    private EditText editTextPhone;
    private SharedPreferences sharedpreferences;
    private String selectedPackageId;
    private SmoothProgressBar progressBar;
    private Boolean progressBarStatus = false;
    private View view;

    public PackagePurchaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("خرید بسته اینترنتی");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_package_purchase, container, false);

        sharedpreferences = getContext().getSharedPreferences("KiooskData", Context.MODE_PRIVATE);

        Bundle bundle = this.getArguments();
        String strPackage = bundle.getString("selectedPackage");
        Package p = new Gson().fromJson(strPackage, Package.class);
        selectedPackageId = p.getId();

        ((TextView) view.findViewById(R.id.tv_selected_pacakge_price)).setText(p.getPrice() + " " + "تومان");

        String customerType = getCustomerType(p.getName());
        ((TextView) view.findViewById(R.id.tv_selected_package_customer_type)).setText(customerType);

        String traffic = getTraffic(p.getName());
        ((TextView) view.findViewById(R.id.tv_selected_package_traffic)).setText(traffic);

        String time = getTime(p.getName());
        ((TextView) view.findViewById(R.id.tv_selected_package_time)).setText(time);

        String detail = getDetail(p.getName());
        ((TextView) view.findViewById(R.id.tv_selected_pacakge_detail)).setText(detail);

        buyBtn = (AppCompatButton) view.findViewById(R.id.buy_selected_package_btn);
        Drawable cartIcon = MaterialDrawableBuilder.with(getContext())
                .setIcon(MaterialDrawableBuilder.IconValue.CART)
                .setColor(Color.WHITE)
                .build();
        buyBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(cartIcon, null, null, null);
        buyBtn.setOnClickListener(this);

        ImageView searchContact = (ImageView) view.findViewById(R.id.btn_search__selected_package);
        searchContact.setOnClickListener(this);

        editTextPhone = (EditText) view.findViewById(R.id.phone_number_selected_package);
        if (sharedpreferences.contains("phoneNumber"))
            editTextPhone.setText(sharedpreferences.getString("phoneNumber", ""));

        progressBar = (SmoothProgressBar) view.findViewById(R.id.pb_topup);

        AndroidNetworking.initialize(getContext());

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            String transitionName = getArguments().getString("transitionName");
            view.findViewById(R.id.cv_selected_package_card).setTransitionName(transitionName);
        }
    }

    private String getCustomerType(String name) {
        if (name.contains("دائمی")) {
            return "ویژه مشترکین دائمی";
        }
        if (name.contains("TDLTE")) {
            return "ویژه مشترکین TDLTE";
        }
        if (name.contains("شگفت انگیز")) {
            return "ویژه مشترکین اعتباری و دائمی";
        }
        return "ویژه مشترکین اعتباری";
    }

    private String getDetail(String name) {
        if (name.contains("ظهر")) {
            return "6 صبح تا 12 ظهر";
        }
        name = name.replaceAll("اینترنت ایرانسل ((ساعتی)|(روزانه)|(هفتگی)|(ماهانه)) - ", "");
        name = name.replace("اینترنت ثابت TDLTE - ", "");
        name = name.replace("(مشترکین دائمی)", "");
        name = name.replaceAll("\\(|\\)", "");
        name = name.replaceAll("\\d+ تومانی", "");
        name = name.replace("مشترکین اعتباری", "");
        name = name.replace("یک", "1");
        name = name.replace("دو", "2");
        name = name.replace("سه", "3");
        name = name.replace("شش", "6");
        name = name.replaceAll("(\\d+)\\s*((روزه)|(ماهه)|(ساعته))", "");
        return name;
    }

    private String getTime(String name) {
        if (name.contains("روزانه")) {
            if (!name.contains("روزه")) {
                return "یک روزه";
            }
        }

        if (name.contains("ماهانه")) {
            if (!name.contains("ماهه")) {
                return "یک ماهه";
            }
        }

        if (name.contains("هفتگی")) {
            return "یک هفته‌ای";
        }

        name = name.replace("یک", "1");
        name = name.replace("دو", "2");
        name = name.replace("سه", "3");
        name = name.replace("شش", "6");

        final Pattern pattern = Pattern.compile("(\\d+)\\s*((روزه)|(ماهه)|(ساعته))");
        Matcher m = pattern.matcher(name);
        if (m.find()) {
            return m.group(1) + " " + m.group(2);
        } else
            return "";

    }

    private String getTraffic(String name) {
        final Pattern pattern = Pattern.compile("(\\d+\\.*\\d*)\\s*((مگابایت)|(گیگابایت)|(گیگ))");
        Matcher m = pattern.matcher(name);
        double traffic = 0.;
        String unit = "", trafficStr;
        while (m.find()) {
            traffic += Double.parseDouble(m.group(1));
            unit = m.group(2);
        }
        if (traffic != 0) {
            trafficStr = String.valueOf(traffic).replace(".0", "") + " " + unit;
        } else {
            trafficStr = "نامحدود";
        }
        return trafficStr;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buy_selected_package_btn:
                v.setEnabled(false);
                String phoneNumber = editTextPhone.getText().toString();
                if (isphoneNumber(phoneNumber)) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("phoneNumber", phoneNumber);
                    editor.apply();
                } else {
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText("خطا");
                    dialog.setContentText("شماره تلفن وارد شده صحیح نمی باشد.");
                    dialog.setConfirmText("OK");
                    dialog.setOnShowListener(dialog1 -> {
                        SweetAlertDialog alertDialog = (SweetAlertDialog) dialog1;
                        ((TextView) alertDialog.findViewById(R.id.content_text))
                                .setTextColor(getResources().getColor(R.color.colorPrimaryText));
                        ((TextView) alertDialog.findViewById(R.id.title_text))
                                .setTextColor(getResources().getColor(R.color.colorDanger));
                    });
                    dialog.setOnDismissListener(dialog1 ->  {
                        progressBar.progressiveStop();
                        progressBarStatus = false;
                        buyBtn.setEnabled(true);
                    });
                    dialog.show();
                    return;
                }
                progressBar.setIndeterminate(true);
                progressBar.progressiveStart();
                progressBarStatus = true;

                String scriptVersion = "Android";
                buyPackage(selectedPackageId, phoneNumber, "", scriptVersion);
                break;

            case R.id.btn_search_topup:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, 0);
                break;
        }
    }

    @Override
    public void onResume() {
        Handler handler = new Handler();
        Runnable runnableCode = () -> {
            handler.postDelayed(() -> {
                if (progressBar != null) {
                    progressBar.progressiveStop();
                    progressBarStatus = false;
                }
            }, 100);
        };
        handler.post(runnableCode);
        buyBtn.setEnabled(true);
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser){
            if (view != null && progressBarStatus){
                progressBar.progressiveStop();
                progressBarStatus = false;
            }
        }
    }

    private void buyPackage(String selectedPackageId, String phoneNumber, String selectedGateway, String scriptVersion) {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("packageId", selectedPackageId);
            jsonData.put("cellphone", phoneNumber);
            jsonData.put("issuer", selectedGateway);
            jsonData.put("scriptVersion", scriptVersion);
            jsonData.put("firstOutputType", "json");
            jsonData.put("secondOutputType", "view");
            jsonData.put("redirectToPage", "False");
            String webserviceID = "587ceaef-4ee0-46dd-a64e-31585bef3768";
            jsonData.put("webserviceId", webserviceID);
            String url = "https://chr724.ir/services/v3/EasyCharge/internetRecharge";
            AndroidNetworking.post(url)
                    .addJSONObjectBody(jsonData)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getString("status").equals("Success")) {
                                    String paymentUrl = response.getJSONObject("paymentInfo").getString("url");
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(paymentUrl));
                                    startActivity(i);
                                } else {
                                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                                    dialog.setTitleText("خطا");
                                    dialog.setContentText(response.getString("errorMessage"));
                                    dialog.setConfirmText("OK");
                                    dialog.setOnShowListener(dialog1 -> {
                                        SweetAlertDialog alertDialog = (SweetAlertDialog) dialog1;
                                        ((TextView) alertDialog.findViewById(R.id.content_text))
                                                .setTextColor(getResources().getColor(R.color.colorPrimaryText));
                                        ((TextView) alertDialog.findViewById(R.id.title_text))
                                                .setTextColor(getResources().getColor(R.color.colorDanger));
                                    });
                                    dialog.show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError error) {
                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                            dialog.setTitleText("خطا");
                            dialog.setContentText("خطا در اتصال به سرور! لطفا از اتصال به اینترنت اطمینال حاصل نمایید سپس مجددا امتحان کنید.");
                            dialog.setConfirmText("OK");
                            dialog.setOnShowListener(dialog1 -> {
                                SweetAlertDialog alertDialog = (SweetAlertDialog) dialog1;
                                ((TextView) alertDialog.findViewById(R.id.content_text))
                                        .setTextColor(getResources().getColor(R.color.colorPrimaryText));
                                ((TextView) alertDialog.findViewById(R.id.title_text))
                                        .setTextColor(getResources().getColor(R.color.colorDanger));
                            });
                            dialog.show();
                        }
                    });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {

            if (resultCode == Activity.RESULT_OK) {

                Uri contactUri = data.getData();

                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                Cursor cursor = this.getActivity().getContentResolver()
                        .query(contactUri, projection, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(column);
                editTextPhone.setText(number.replace("+98", "0"));
                cursor.close();
            }
        }

    }

    private String getOperator(String number) {
        if (number.startsWith("093") || number.startsWith("090")) {
            return "MTN";
        } else if (number.startsWith("094")) {
            return "WiMax";
        } else if (number.startsWith("091") || number.startsWith("0990")) {
            return "MCI";
        } else if (number.startsWith("0921") || number.startsWith("0922")) {
            return "RTL";
        }
        return null;
    }

    private boolean isphoneNumber(String phoneNumber) {
        return getOperator(phoneNumber) != null && phoneNumber.length() == 11;
    }
}
