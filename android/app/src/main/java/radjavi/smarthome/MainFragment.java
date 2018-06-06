package radjavi.smarthome;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // GLOBAL VARIABLES //
    private DatabaseReference alarmRef;
    private DatabaseReference tempSensorRef;
    private DatabaseReference ledRef;
    private Switch ledSwitch;
    private Switch alarmSwitch;
    private TextView tempText;
    private TextView humidText;
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

    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        // Write a message to the database
        FirebaseDatabase database = MyFirebaseDatabase.getDatabase();
        alarmRef = database.getReference("alarm");
        alarmRef.keepSynced(true);
        tempSensorRef = database.getReference("tempSensor");
        tempSensorRef.keepSynced(true);
        ledRef = database.getReference("led");
        ledRef.keepSynced(true);
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        ledSwitch = (Switch) view.findViewById(R.id.ledSwitch);
        alarmSwitch = (Switch) view.findViewById(R.id.alarmSwitch);
        tempText = (TextView) view.findViewById(R.id.temperature);
        humidText = (TextView) view.findViewById(R.id.humidity);
        btnFlash = view.findViewById(R.id.BTN_FLASH);
        btnSmooth = view.findViewById(R.id.BTN_SMOOTH);
        btnBrightnessUp = view.findViewById(R.id.BTN_BRIGHTNESS_UP);
        btnBrightnessDown = view.findViewById(R.id.BTN_BRIGHTNESS_DOWN);
        btnRed = view.findViewById(R.id.BTN_RED);
        btnOrange = view.findViewById(R.id.BTN_ORANGE);
        btnOrange2 = view.findViewById(R.id.BTN_ORANGE2);
        btnYellow = view.findViewById(R.id.BTN_YELLOW);
        btnGreen = view.findViewById(R.id.BTN_GREEN);
        btnTurq = view.findViewById(R.id.BTN_TURQ);
        btnBlue3 = view.findViewById(R.id.BTN_BLUE3);
        btnBlue2 = view.findViewById(R.id.BTN_BLUE2);
        btnBlue = view.findViewById(R.id.BTN_BLUE);
        btnPurple = view.findViewById(R.id.BTN_PURPLE);
        btnPink = view.findViewById(R.id.BTN_PINK);
        btnWhite = view.findViewById(R.id.BTN_WHITE);

        // Read from the database
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
        });
        tempSensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Integer temp = dataSnapshot.child("temp").getValue(Integer.class);
                Integer humid = dataSnapshot.child("humid").getValue(Integer.class);
                if (temp != null) tempText.setText(temp + "\u00b0C");
                if (humid != null) humidText.setText(humid + "%");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

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

        ledSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("ledOn", ledSwitch.isChecked());
                if (ledSwitch.isChecked()) {
                    childUpdates.put("rgbCode", "BTN_ON");
                }
                else childUpdates.put("rgbCode", "BTN_OFF");
                ledRef.updateChildren(childUpdates);
            }
        });

        alarmSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                alarmRef.setValue(alarmSwitch.isChecked());
            }
        });
    }

    private void send_rgb(Button btn) {
        ledRef.child("rgbCode").setValue(getResources().getResourceEntryName(btn.getId()));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
