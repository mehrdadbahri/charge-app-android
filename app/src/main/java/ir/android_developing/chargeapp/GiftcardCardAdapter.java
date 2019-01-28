package ir.android_developing.chargeapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;

/**
 * Created by mehrdad on 7/31/17.
 */

public class GiftcardCardAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Package> giftcards;
    private FragmentManager fragmentManager;

    // Constructor
    public GiftcardCardAdapter(Context c, FragmentManager fm, ArrayList<Package> giftcardsList){
        mContext = c;
        giftcards = giftcardsList;
        fragmentManager = fm;
    }

    public int getCount() {
        return giftcards.size();
    }

    public Object getItem(int position) {
        return giftcards.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.giftcard_template, parent, false);
        }

        final Package p = (Package) this.getItem(position);

        ((TextView) view.findViewById(R.id.tv_giftcard_price)).setText(p.getPrice() + " " + "تومان");

        ((TextView) view.findViewById(R.id.tv_giftcard_detail)).setText(p.getName());

        ((ImageView) view.findViewById(R.id.iv_giftcard_logo)).setImageDrawable(getGiftcardDrawable(p.getId()));

        CardView cv = (CardView) view.findViewById(R.id.cv_giftcards);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            String transitionName = "card" + p.getId();
            cv.setTransitionName(transitionName);
        }
        cv.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            String strPackage = new Gson().toJson(p, Package.class);
            bundle.putString("selectedGiftcard", strPackage);
            Fragment fragment = new GiftcardPurchaseFragment();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Transition moveTransition = TransitionInflater.from(mContext).inflateTransition(android.R.transition.move);
                fragment.setSharedElementEnterTransition(moveTransition);
                fragment.setSharedElementReturnTransition(moveTransition);
                bundle.putString("transitionName", "card" + p.getId());
                fragment.setArguments(bundle);
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.main_container_wrapper);
                fragmentManager.beginTransaction()
                        .hide(currentFragment)
                        .add(R.id.main_container_wrapper, fragment)
                        .addToBackStack(null)
                        .addSharedElement(cv, "card" + p.getId())
                        .commit();

            } else {
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .replace(R.id.main_container_wrapper, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private Drawable getGiftcardDrawable(String name) {
        MaterialDrawableBuilder drawableBuilder = MaterialDrawableBuilder.with(mContext);
        if (name.toLowerCase().contains("itunes")) {
            drawableBuilder.setIcon(MaterialDrawableBuilder.IconValue.ITUNES);
            drawableBuilder.setColor(mContext.getResources().getColor(R.color.itunesColor));

        }
        else if (name.toLowerCase().contains("googleplay")) {
            drawableBuilder.setIcon(MaterialDrawableBuilder.IconValue.GOOGLE);
            drawableBuilder.setColor(mContext.getResources().getColor(R.color.googlePlayColor));
        }
        else if (name.toLowerCase().contains("microsoft")) {
            drawableBuilder.setIcon(MaterialDrawableBuilder.IconValue.MICROSOFT);
            drawableBuilder.setColor(mContext.getResources().getColor(R.color.microsoftColor));
        }
        else if (name.toLowerCase().contains("amazon")) {
            drawableBuilder.setIcon(MaterialDrawableBuilder.IconValue.AMAZON);
            drawableBuilder.setColor(mContext.getResources().getColor(R.color.amazonColor));

        }
        else if (name.toLowerCase().contains("xbox")) {
            drawableBuilder.setIcon(MaterialDrawableBuilder.IconValue.XBOX);
            drawableBuilder.setColor(mContext.getResources().getColor(R.color.xboxColor));
        }
        else if (name.toLowerCase().contains("playstationplus")) {
            drawableBuilder.setIcon(MaterialDrawableBuilder.IconValue.PLAYSTATION);
            drawableBuilder.setColor(mContext.getResources().getColor(R.color.playstationPlusColor));
        }
        else if (name.toLowerCase().contains("playstation")) {
            drawableBuilder.setIcon(MaterialDrawableBuilder.IconValue.PLAYSTATION);
            drawableBuilder.setColor(mContext.getResources().getColor(R.color.playstationColor));
        }
        else if (name.toLowerCase().contains("steam")) {
            drawableBuilder.setIcon(MaterialDrawableBuilder.IconValue.STEAM);
            drawableBuilder.setColor(mContext.getResources().getColor(R.color.steamColor));
        }

        return drawableBuilder.build();
    }
}
