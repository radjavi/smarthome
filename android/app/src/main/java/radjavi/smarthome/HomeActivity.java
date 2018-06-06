package radjavi.smarthome;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    FirebaseUser user;
    private DatabaseReference alarmRef;
    private DatabaseReference tempSensorRef;
    private DatabaseReference ledRef;
    private TextView tempText;
    private TextView humidText;
    private TextView tempUpdated;


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

        tempText = findViewById(R.id.textTemp);
        humidText = findViewById(R.id.textHumid);
        tempUpdated = findViewById(R.id.tempUpdated);
        // Read from the database
        /*
        alarmRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Boolean alarmOn = dataSnapshot.getValue(Boolean.class);
                if (alarmOn != null) alarmSwitch.setChecked(alarmOn);
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
                Boolean ledOn = dataSnapshot.child("ledOn").getValue(Boolean.class);
                if (ledOn != null) ledSwitch.setChecked(ledOn);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        }); */
        tempSensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Integer temp = dataSnapshot.child("temp").getValue(Integer.class);
                Integer humid = dataSnapshot.child("humid").getValue(Integer.class);
                if (temp != null) tempText.setText(temp + "\u00b0C");
                if (humid != null) humidText.setText(humid + "%");
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                tempUpdated.setText("Last updated: " + currentDateTimeString);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mDrawerLayout = findViewById(R.id.drawer_layout);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        TextView textWelcome = findViewById(R.id.textWelcome);
        textWelcome.setText(user.getDisplayName());

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
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                        }
                        else if (menuItem.getItemId() == R.id.nav_oldActivity) {
                            startActivity(new Intent(HomeActivity.this, MainActivity.class));
                        }
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });


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

}
