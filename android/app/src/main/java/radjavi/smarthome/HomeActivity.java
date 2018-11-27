package radjavi.smarthome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private LottieAnimationView mProgressView;
    FirebaseUser user;
    private boolean connected;
    private DatabaseReference alarmRef;
    private DatabaseReference tempSensorRef;
    private DatabaseReference ledRef;
    private TextView tempText;
    private TextView humidText;
    private TextView textMaxMinTemp;
    private TextView textMaxMinHumid;
    private TextView textSync;
    private TextView armedText;
    private TextView lightText;
    private TextView textWelcomeName;
    private TextView textWelcome;
    private FrameLayout firebaseStatus;
    private FrameLayout piStatus;
    private ToggleButton btnAlarm;
    private ToggleButton btnLed;
    private LinearLayout ledControl;
    private FrameLayout contentFrame;
    private Button btnFlash;
    private Button btnSmooth;
    private Button btnBrightnessUp;
    private Button btnBrightnessDown;
    private Button btnRed;
    private Button btnOrange;
    private Button btnOrange2;
    private Button btnYellow;
    private Button btnGreen;
    private Button btnTurq;
    private Button btnBlue3;
    private Button btnBlue2;
    private Button btnBlue;
    private Button btnPurple;
    private Button btnPink;
    private Button btnWhite;
    private Boolean piOnline = false;
    private Boolean alarmOn = false;
    private Boolean ledOn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        user = FirebaseAuth.getInstance().getCurrentUser();
        // Write a message to the database
        FirebaseDatabase database = MyFirebaseDatabase.getDatabase();
        alarmRef = database.getReference("alarm");
        alarmRef.keepSynced(true);
        tempSensorRef = database.getReference("tempSensor");
        tempSensorRef.keepSynced(true);
        ledRef = database.getReference("led");
        ledRef.keepSynced(true);
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        mProgressView = findViewById(R.id.loading_progress);
        textWelcome = findViewById(R.id.textWelcome);
        textWelcomeName = findViewById(R.id.textWelcomeName);
        textWelcomeName.setText(user.getDisplayName());
        firebaseStatus = findViewById(R.id.firebaseStatus);
        piStatus = findViewById(R.id.piStatus);

        // Firebase connection status
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                connected = snapshot.getValue(Boolean.class);
                setConnectedInfo();
                if (connected) setPiInfo();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
        DatabaseReference piOnlineRef = FirebaseDatabase.getInstance().getReference("piOnline");
        piOnlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                piOnline = snapshot.getValue(Boolean.class);
                if (connected) setPiInfo();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });

        tempText = findViewById(R.id.textTemp);
        humidText = findViewById(R.id.textHumid);
        textMaxMinTemp = findViewById(R.id.textMaxMinTemp);
        textMaxMinHumid = findViewById(R.id.textMaxMinHumid);
        textSync = findViewById(R.id.textSync);
        // Read from the database
        alarmRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                alarmOn = dataSnapshot.getValue(Boolean.class);
                if (alarmOn != null) {
                    btnAlarm.setChecked(alarmOn);
                    armedText.setText(alarmOn ? "Armed" : "Disarmed");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        ledRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                ledOn = dataSnapshot.child("ledOn").getValue(Boolean.class);
                if (ledOn != null) {
                    btnLed.setChecked(ledOn);
                    lightText.setText(ledOn ? "Light is on" : "Light is off");
                    ledControl.setVisibility(ledOn ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        tempSensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Integer temp = dataSnapshot.child("temp").getValue(Integer.class);
                Integer humid = dataSnapshot.child("humid").getValue(Integer.class);
                Integer minTemp = dataSnapshot.child("minTemp").getValue(Integer.class);
                Integer maxTemp = dataSnapshot.child("maxTemp").getValue(Integer.class);
                Integer minHumid = dataSnapshot.child("minHumid").getValue(Integer.class);
                Integer maxHumid = dataSnapshot.child("maxHumid").getValue(Integer.class);
                String date = dataSnapshot.child("date").getValue(String.class);
                if (temp != null) tempText.setText(temp + "\u00b0C");
                if (humid != null) humidText.setText(humid + "%");
                if (minTemp != null && maxTemp != null) textMaxMinTemp.setText(maxTemp + "\u00b0C / " + minTemp + "\u00b0C");
                if (minHumid != null && maxHumid != null) textMaxMinHumid.setText(maxHumid + " % / " + minHumid + " %");
                if (date != null) textSync.setText("Last sync: " + date);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mDrawerLayout = findViewById(R.id.drawer_layout);
        contentFrame = findViewById(R.id.content_frame);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float slideX = drawerView.getWidth() * slideOffset;
                contentFrame.setTranslationX(slideX);
            }
        };
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionbar.setDisplayShowTitleEnabled(false);

        navigationView = findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        TextView header_name = (TextView)hView.findViewById(R.id.header_name);
        TextView header_email = (TextView)hView.findViewById(R.id.header_email);
        header_name.setText(user.getDisplayName());
        header_email.setText(user.getEmail());
        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    // set item as selected to persist highlight
                    if (menuItem.getItemId() == R.id.nav_signOut) {
                        signOut();
                    }
                    else if (menuItem.getItemId() == R.id.nav_settings) {
                        startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                    }
                    else if (menuItem.getItemId() == R.id.nav_graph) {
                        startActivity(new Intent(HomeActivity.this, TempGraphActivity.class));
                    }
                    // close drawer when item is tapped
                    mDrawerLayout.closeDrawers();

                    // Add code here to update the UI based on the item selected
                    // For example, swap UI fragments here

                    return true;
                }
            });
        armedText = findViewById(R.id.textArmed);
        btnAlarm = findViewById(R.id.alarmButton);
        btnAlarm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean checked = btnAlarm.isChecked();
                alarmRef.setValue(checked);
                //armedText.setText(checked ? "Armed" : "Disarmed");
            }
        });
        btnLed = findViewById(R.id.ledButton);
        lightText = findViewById(R.id.lightText);
        ledControl = findViewById(R.id.ledControl);
        final ViewGroup transitionsContainer = (ViewGroup) ledControl;
        btnLed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TransitionManager.beginDelayedTransition(transitionsContainer);
                boolean checked = btnLed.isChecked();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("ledOn", btnLed.isChecked());
                childUpdates.put("rgbCode", checked ? "BTN_ON" : "BTN_OFF");
                ledRef.updateChildren(childUpdates);
                //lightText.setText(checked ? "Light is on" : "Light is off");
                //ledControl.setVisibility(checked ? View.VISIBLE : View.GONE);
            }
        });

        btnFlash = findViewById(R.id.BTN_FLASH);
        btnSmooth = findViewById(R.id.BTN_SMOOTH);
        btnBrightnessUp = findViewById(R.id.BTN_BRIGHTNESS_UP);
        btnBrightnessDown = findViewById(R.id.BTN_BRIGHTNESS_DOWN);
        btnRed = findViewById(R.id.BTN_RED);
        btnOrange = findViewById(R.id.BTN_ORANGE);
        btnOrange2 = findViewById(R.id.BTN_ORANGE2);
        btnYellow = findViewById(R.id.BTN_YELLOW);
        btnGreen = findViewById(R.id.BTN_GREEN);
        btnTurq = findViewById(R.id.BTN_TURQ);
        btnBlue3 = findViewById(R.id.BTN_BLUE3);
        btnBlue2 = findViewById(R.id.BTN_BLUE2);
        btnBlue = findViewById(R.id.BTN_BLUE);
        btnPurple = findViewById(R.id.BTN_PURPLE);
        btnPink = findViewById(R.id.BTN_PINK);
        btnWhite = findViewById(R.id.BTN_WHITE);
        set_light_buttons();
    }

    private void setConnectedInfo() {
        firebaseStatus.setBackground(connected ? getResources().getDrawable(R.drawable.greencircle) : getResources().getDrawable(R.drawable.redcircle));
        if (connected) {
            mProgressView.setVisibility(View.GONE);
            btnAlarm.setEnabled(true);
            btnLed.setEnabled(true);
            btnAlarm.setChecked(alarmOn);
            btnLed.setChecked(ledOn);
            ledControl.setVisibility(ledOn ? View.VISIBLE : View.GONE);
            lightText.setText(ledOn ? "Light is on" : "Light is off");
        } else {
            piStatus.setBackground(getResources().getDrawable(R.drawable.orangecircle));
            mProgressView.setVisibility(View.VISIBLE);
            mProgressView.playAnimation();
            btnAlarm.setEnabled(false);
            btnLed.setEnabled(false);
            btnAlarm.setChecked(false);
            btnLed.setChecked(false);
            ledControl.setVisibility(View.GONE);
            lightText.setText("Light is off");
        }
    }

    private void setPiInfo() {
        piStatus.setBackground(piOnline ? getResources().getDrawable(R.drawable.greencircle) : getResources().getDrawable(R.drawable.redcircle));
        if (piOnline) {
            btnAlarm.setEnabled(true);
            btnLed.setEnabled(true);
            btnAlarm.setChecked(alarmOn);
            btnLed.setChecked(ledOn);
            ledControl.setVisibility(ledOn ? View.VISIBLE : View.GONE);
            lightText.setText(ledOn ? "Light is on" : "Light is off");
        } else {
            btnAlarm.setEnabled(false);
            btnLed.setEnabled(false);
            btnAlarm.setChecked(false);
            btnLed.setChecked(false);
            ledControl.setVisibility(View.GONE);
            lightText.setText("Light is off");
        }
    }

    private void set_light_buttons() {
        btnFlash.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnFlash);
            }
        });

        btnSmooth.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnSmooth);
            }
        });

        btnBrightnessUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnBrightnessUp);
            }
        });

        btnBrightnessDown.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnBrightnessDown);
            }
        });

        btnRed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnRed);
            }
        });

        btnOrange.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnOrange);
            }
        });

        btnOrange2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnOrange2);
            }
        });

        btnYellow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnYellow);
            }
        });

        btnGreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnGreen);
            }
        });

        btnTurq.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnTurq);
            }
        });

        btnBlue3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnBlue3);
            }
        });

        btnBlue2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnBlue2);
            }
        });

        btnBlue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnBlue);
            }
        });

        btnPurple.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnPurple);
            }
        });

        btnPink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnPink);
            }
        });

        btnWhite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                send_rgb(btnWhite);
            }
        });
    }

    private void send_rgb(Button btn) {
        ledRef.child("rgbCode").setValue(getResources().getResourceEntryName(btn.getId()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(HomeActivity.this, "Signed out.", Toast.LENGTH_SHORT).show();
        FirebaseMessaging.getInstance().unsubscribeFromTopic("all");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

}
