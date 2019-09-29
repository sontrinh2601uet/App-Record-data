package com.example.sensorrecord;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MainActivity";
    //Set the specific sensors to be used throughout the app
    public final static short TYPE_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    public final static short TYPE_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
    public final static short TYPE_MAGNETIC = Sensor.TYPE_MAGNETIC_FIELD;
    //App flags
    public static Boolean dataRecordStarted;
    public static Boolean dataRecordCompleted;
    public static Boolean heightUnitSpinnerTouched;
    public static Boolean subCreated;
    public Logger logger;
    NavigationView navigationView;
    Menu optionsMenu;
    Toolbar toolbar;
    DrawerLayout drawer;
    LayoutInflater inflater;
    ActionBarDrawerToggle hamburger;
    FragmentManager fragmentManager;
    SensorManager mSensorManager;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        String pathToExternalStorage = Environment.getExternalStorageDirectory().toString();
        File logFileDir = new File(pathToExternalStorage, "/SensorRecord/");
        logger = new Logger(logFileDir);

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
        navigationView.getMenu().findItem(R.id.nav_save).setEnabled(false);

        dbHelper = DBHelper.getInstance(this);

        dataRecordStarted = false;
        dataRecordCompleted = false;
        heightUnitSpinnerTouched = false;
        subCreated = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_settings, menu);
        optionsMenu = menu;
        return true;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        dbHelper.closeDB();
        super.onDestroy();
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
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_new:
                if (!subCreated) {
                    addFragment(new NewFragment(), true);
                } else {
                    addFragment(new SubjectInfoFragment(), true);
                }
                break;
            case R.id.nav_start:
                addFragment(new StartFragment(), true);
                break;
            case R.id.nav_save:
                addFragment(new SaveFragment(), true);
                break;
            case R.id.nav_raw:
                addFragment(new RawFragment(), true);
                break;
            case R.id.nav_accelerometer:
                addFragment(new AccelerometerFragment(), true);
                break;
            case R.id.nav_gyroscope:
                addFragment(new GyroscopeFragment(), true);
                break;
            case R.id.nav_magnetometer:
                addFragment(new MagnetometerFragment(), true);
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    public String getSensorAvailable(short sensor_type) {
        Sensor curSensor = mSensorManager.getDefaultSensor(sensor_type);
        if (curSensor != null) {
            return ("Yes  " + "(" + curSensor.getVendor() + ")");
        } else {
            return ("No");
        }
    }
}
