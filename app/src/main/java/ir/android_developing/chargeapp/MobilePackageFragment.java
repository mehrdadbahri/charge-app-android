package ir.android_developing.chargeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MobilePackageFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private RadioGroup rgPackageTime, rgPackageCustomerType;
    private ExpandableHeightGridView gridview;
    private View view;
    private FragmentManager fm;
    private Boolean loadingStatus = false;

    public MobilePackageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_mobile_package, container, false);

        fm = getParentFragment().getFragmentManager();
        fm.popBackStack();

        ArrayList<Package> packages = getPackagesList();
        if (packages != null) setupGridView(packages);
        else view.findViewById(R.id.ll_error_getting_packages).setVisibility(View.VISIBLE);

        rgPackageTime = (RadioGroup) view.findViewById(R.id.rg_time_period);
        rgPackageTime.setOnCheckedChangeListener(this);

        rgPackageCustomerType = (RadioGroup) view.findViewById(R.id.rg_customer_type);
        rgPackageCustomerType.setOnCheckedChangeListener(this);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.refresh_packages_btn);
        Drawable refreshIcon = MaterialDrawableBuilder.with(getContext())
                .setIcon(MaterialDrawableBuilder.IconValue.REFRESH)
                .setColor(Color.WHITE)
                .build();
        fab.setImageDrawable(refreshIcon);
        fab.setOnClickListener(this);

        return view;
    }

    private void setupGridView(ArrayList<Package> packages) {
        view.findViewById(R.id.ll_error_getting_packages).setVisibility(View.GONE);
        gridview = (ExpandableHeightGridView) view.findViewById(R.id.grid_view_packages);
        gridview.setAdapter(new PackageCardAdapter(getContext(), fm, packages));
        gridview.setExpanded(true);
        gridview.setFocusable(false);

        JSONObject filters = new JSONObject();
        try {
            filters.put("customerType", "prepaid");
            filters.put("timeRange", "monthly");
            String filterStr = filters.toString();
            ((PackageCardAdapter) gridview.getAdapter()).getFilter().filter(filterStr);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Package> getPackagesList() {
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("KiooskData", Context.MODE_PRIVATE);
        if (sharedpreferences.contains("packages")) {
            Type listOfPackages = new TypeToken<ArrayList<Package>>() {
            }.getType();
            String strPackages = sharedpreferences.getString("packages", "");
            ArrayList<Package> packages = new Gson().fromJson(strPackages, listOfPackages);
            if (packages == null)
                return null;
            for (int i = 0; i < packages.size(); i++) {
                if (packages.get(i).getName().contains("شگفت انگیز"))
                    packages.remove(i);
            }
            return packages;
        }
        return null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (view != null && view.findViewById(R.id.ll_error_getting_packages).getVisibility() == View.VISIBLE) {
                ArrayList<Package> packs = getPackagesList();
                if (packs != null) {
                    setupGridView(packs);
                }
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        String customerType = "";
        String timeRange = "";
        switch (rgPackageCustomerType.getCheckedRadioButtonId()) {
            case R.id.rb_customer_prepaid:
                customerType = "prepaid";
                break;
            case R.id.rb_customer_postpaid:
                customerType = "postpaid";
                break;
            case R.id.rb_customer_td_lte:
                customerType = "TDLTE";
        }
        switch (rgPackageTime.getCheckedRadioButtonId()) {
            case R.id.rb_hourly:
                timeRange = "hourly";
                break;
            case R.id.rb_daily:
                timeRange = "daily";
                break;
            case R.id.rb_weekly:
                timeRange = "weekly";
                break;
            case R.id.rb_monthly:
                timeRange = "monthly";
                break;
        }

        JSONObject filters = new JSONObject();
        try {
            filters.put("customerType", customerType);
            filters.put("timeRange", timeRange);
            String filterStr = filters.toString();
            ((PackageCardAdapter) gridview.getAdapter()).getFilter().filter(filterStr);

        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh_packages_btn:
                if (!loadingStatus) {
                    loadingStatus = true;
                    FABProgressCircle fabProgressCircle = (FABProgressCircle) view.findViewById(R.id.fabProgressCircle);
                    fabProgressCircle.show();
                    ArrayList<Package> packs = getPackagesList();
                    if (packs != null) {
                        setupGridView(packs);
                        fabProgressCircle.hide();
                        loadingStatus = false;

                    } else {
                        GetInitializeData dataGetter = new GetInitializeData(
                                (AppCompatActivity) getActivity(),
                                new OnEventListener() {
                                    @Override
                                    public void onSuccess() {
                                        fabProgressCircle.beginFinalAnimation();
                                        loadingStatus = false;
                                        fabProgressCircle.attachListener(() -> {
                                            ArrayList<Package> packs = getPackagesList();
                                            if (packs != null) {
                                                setupGridView(packs);
                                                fabProgressCircle.hide();
                                                loadingStatus = false;
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure() {
                                        Handler handler = new Handler();
                                        Runnable runnableCode = () -> {
                                            handler.postDelayed(() -> {
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
                                            }, 2000);
                                        };

                                        handler.post(runnableCode);
                                    }
                                }
                        );
                        dataGetter.execute();
                    }
                }
                break;
        }
    }
}
