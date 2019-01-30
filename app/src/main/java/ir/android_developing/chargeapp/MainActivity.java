package ir.android_developing.chargeapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentManager fragmentManager;
    private Fragment fragment = null;
    private ActionBarDrawerToggle toggle;
    private Menu navigationMenu;
    private int prevMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("خرید شارژ");

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        actionBar.setDisplayHomeAsUpEnabled(false);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        MaterialDrawableBuilder iconBuilder = MaterialDrawableBuilder.with(this)
                .setColor(Color.DKGRAY);
        navigationMenu = ((NavigationView) findViewById(R.id.nav_view)).getMenu();

        iconBuilder.setIcon(MaterialDrawableBuilder.IconValue.CREDIT_CARD_PLUS);
        navigationMenu.findItem(R.id.item_charge).setIcon(iconBuilder.build());

        iconBuilder.setIcon(MaterialDrawableBuilder.IconValue.PACKAGE_DOWN);
        navigationMenu.findItem(R.id.item_packages).setIcon(iconBuilder.build());

        iconBuilder.setIcon(MaterialDrawableBuilder.IconValue.WALLET_GIFTCARD);
        navigationMenu.findItem(R.id.item_giftcard).setIcon(iconBuilder.build());

//        iconBuilder.setIcon(MaterialDrawableBuilder.IconValue.COUNTER);
//        navigationMenu.findItem(R.id.item_pay_bill).setIcon(iconBuilder.build());

        iconBuilder.setIcon(MaterialDrawableBuilder.IconValue.STAR_CIRCLE);
        navigationMenu.findItem(R.id.item_comment).setIcon(iconBuilder.build());

        iconBuilder.setIcon(MaterialDrawableBuilder.IconValue.PHONE_CLASSIC);
        navigationMenu.findItem(R.id.item_support).setIcon(iconBuilder.build());

        fragmentManager = getSupportFragmentManager();

        toolbar.setNavigationOnClickListener(v -> {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                onBackPressed();
            }
            else if (drawer.isDrawerOpen(GravityCompat.START))
                drawer.closeDrawer(GravityCompat.START);
            else
                drawer.openDrawer(GravityCompat.START);
        });

        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new ChargeFragment();
        prevMenuItem = R.id.item_charge;
        fragmentTransaction.replace(R.id.main_container_wrapper, fragment);
        fragmentTransaction.commit();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        new GetInitializeData(this, null).execute();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (fragmentManager.getBackStackEntryCount() == 0) {
                SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
                dialog.setTitleText("خروج");
                dialog.setContentText("آیا می خواهید از برنامه خارج شوید؟");
                dialog.setConfirmText("بله");
                dialog.setCancelText("خیر");
                dialog.setConfirmClickListener(sweetAlertDialog -> {
                    dialog.dismissWithAnimation();
                    finish();
                });
                dialog.setCancelClickListener(SweetAlertDialog::dismissWithAnimation);
                dialog.setOnShowListener(dialog1 -> {
                    SweetAlertDialog alertDialog = (SweetAlertDialog) dialog1;
                    TextView text = (TextView) alertDialog.findViewById(R.id.content_text);
                    text.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                });
                dialog.show();
            }
            else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                toggle.setDrawerIndicatorEnabled(true);
                toggle.syncState();
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        boolean switchFragment = false;
        switch (item.getItemId()){
            case R.id.item_charge:
                fragment = new ChargeFragment();
                prevMenuItem = R.id.item_charge;
                switchFragment = true;
                break;
            case R.id.item_packages:
                fragment = new PackageFragment();
                prevMenuItem = R.id.item_packages;
                switchFragment = true;
                break;
            case R.id.item_giftcard:
                fragment = new GiftcardFragment();
                prevMenuItem = R.id.item_giftcard;
                switchFragment = true;
                break;
//            case R.id.item_pay_bill:
//                prevMenuItem = R.id.item_pay_bill;
//                break;
            case R.id.item_support:
                SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
                dialog.setTitleText("پشتیبانی");
                dialog.setContentText("در صورت نیاز, برای پیگیری تراکنش های انجام شده (فقط تراکنش های آنلاین) می توانید با واحد پشتیبانی به شماره 88019574 - 021 تماس بگیرید.");
                dialog.setOnDismissListener(dialog1 -> navigationMenu.findItem(prevMenuItem).setChecked(true));
                dialog.setOnShowListener(dialog1 -> {
                    SweetAlertDialog alertDialog = (SweetAlertDialog) dialog1;
                    TextView text = (TextView) alertDialog.findViewById(R.id.content_text);
                    text.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                });
                dialog.show();
                break;
            case  R.id.item_comment:
                navigationMenu.findItem(prevMenuItem).setChecked(true);
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setData(Uri.parse("bazaar://details?id=" + "ir.android_developing.chargeapp"));
                intent.setPackage("com.farsitel.bazaar");
                startActivity(intent);
                break;

        }

        if (switchFragment) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_container_wrapper, fragment);
            transaction.commit();
        }

        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        return true;
    }
}
