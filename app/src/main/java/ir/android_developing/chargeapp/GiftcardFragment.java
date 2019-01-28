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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class GiftcardFragment extends Fragment implements View.OnClickListener {

    private View view;
    private ExpandableHeightGridView gridview;
    private FragmentManager fm;
    private Boolean loadingStatus = false;


    public GiftcardFragment() {
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
        view =  inflater.inflate(R.layout.fragment_giftcatd, container, false);

        fm = getFragmentManager();
        fm.popBackStack();

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle("خرید گیفت کارت");

        ArrayList<Package> giftcards = getGiftcardsList();
        if (giftcards != null) {
            setupGridView(giftcards);
        }
        else view.findViewById(R.id.ll_error_getting_packages).setVisibility(View.VISIBLE);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.refresh_packages_btn);
        Drawable refreshIcon = MaterialDrawableBuilder.with(getContext())
                .setIcon(MaterialDrawableBuilder.IconValue.REFRESH)
                .setColor(Color.WHITE)
                .build();
        fab.setImageDrawable(refreshIcon);
        fab.setOnClickListener(this);

        return view;
    }

    private void setupGridView(ArrayList<Package> giftcards) {
        view.findViewById(R.id.ll_error_getting_packages).setVisibility(View.GONE);
        gridview = (ExpandableHeightGridView) view.findViewById(R.id.grid_view_giftcard);
        gridview.setAdapter(new GiftcardCardAdapter(getContext(), fm, giftcards));
        gridview.setExpanded(true);
    }

    private ArrayList<Package> getGiftcardsList(){
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("KiooskData", Context.MODE_PRIVATE);
        if (sharedpreferences.contains("giftcards")) {
            Type listOfGiftcards = new TypeToken<ArrayList<Package>>() {
            }.getType();
            String strGiftcards = sharedpreferences.getString("giftcards", "");
            ArrayList<Package> giftcards = new Gson().fromJson(strGiftcards, listOfGiftcards);
            return giftcards;
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.refresh_packages_btn:
                if (!loadingStatus) {
                    loadingStatus = true;
                    FABProgressCircle fabProgressCircle = (FABProgressCircle) view.findViewById(R.id.fabProgressCircle);
                    fabProgressCircle.show();
                    ArrayList<Package> packs = getGiftcardsList();
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
                                            ArrayList<Package> packs = getGiftcardsList();
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
                                                dialog.setOnDismissListener(dialog1 -> {
                                                    fabProgressCircle.hide();
                                                    loadingStatus = false;
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
