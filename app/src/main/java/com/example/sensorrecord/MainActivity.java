package com.example.sensorrecord;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.sensorrecord.fragment.DetectFragment;
import com.example.sensorrecord.fragment.NewFragment;
import com.example.sensorrecord.fragment.RawFragment;
import com.example.sensorrecord.fragment.RecordFragment;
import com.example.sensorrecord.fragment.UserInfoFragment;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MainActivity";
    //Set the specific sensors to be used throughout the app
    public final static short TYPE_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    public final static short TYPE_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
    public final static short TYPE_MAGNETIC = Sensor.TYPE_MAGNETIC_FIELD;
    public final static short TYPE_GRAVITY = Sensor.TYPE_GRAVITY;
    public final static short TYPE_ORIENTATION = Sensor.TYPE_ORIENTATION;
    //App flags
    public static Boolean dataRecordStarted;
    public static Boolean heightUnitSpinnerTouched;
    public static Boolean androidVersionSpinner;
    public static Boolean subCreated;
    public NavigationView navigationView;
    public DrawerLayout drawer;
    public ActionBarDrawerToggle hamburger;
    Toolbar toolbar;
    LayoutInflater inflater;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        String pathToExternalStorage = Environment.getExternalStorageDirectory().toString();
        File logFileDir = new File(pathToExternalStorage, "/SensorRecord/");

        fragmentManager = getSupportFragmentManager();

        addFragment(new NewFragment(), true);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        hamburger = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(hamburger);
        hamburger.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_start).setEnabled(false);

        dataRecordStarted = false;
        androidVersionSpinner = false;
        heightUnitSpinnerTouched = false;
        subCreated = false;
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (fragmentManager.getBackStackEntryCount() != 0) {
                fragmentManager.popBackStack();
            }
        }
    }

    @Override
    public void recreate() {
        super.recreate();
        SharedPreferences.Editor editor = getSharedPreferences("pref", MODE_PRIVATE).edit();

        editor.remove("isLogin");
        editor.remove("name");
        editor.remove("age");
        editor.remove("job");
        editor.remove("gender");
        editor.remove("device");

        editor.commit();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_new:
                if (!subCreated) {
                    addFragment(new NewFragment(), true);
                } else {
                    addFragment(new UserInfoFragment(), true);
                }
                break;
            case R.id.nav_start:
                addFragment(new RecordFragment(), true);
                break;
            case R.id.nav_detected:
                addFragment(new DetectFragment(), true);
                break;
            case R.id.nav_raw:
                addFragment(new RawFragment(), true);
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void addFragment(Fragment fragment, Boolean addToBackStack) {
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            //Check getFragments() == null to prevent the initial blank
            //fragment (before 'New' fragment is displayed) from being added to the backstack
            if (fragmentManager.getFragments() == null || !addToBackStack) {
                fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
                        .commit();
            } else {
                fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(null)
                        .commit();
            }
        }
    }
}
