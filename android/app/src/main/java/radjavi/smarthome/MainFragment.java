package radjavi.smarthome;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;


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
    private String intIp = "http://192.168.0.101:8080"; // Internal IP
    private String extIp = "http://95.80.50.198:8080"; // External IP
    private Socket mSocket;
    private FrameLayout connectionStatus;
    private Switch ledSwitch;
    private Switch alarmSwitch;
    private ToggleButton ipToggle;
    private TextView tempText;
    private TextView humidText;
    private TextView connectionStatusText;
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

    private Socket createSocket(String ip) {
        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.reconnectionAttempts = Integer.MAX_VALUE;
            options.reconnectionDelay = 500;
            return IO.socket(ip, options);
        } catch (java.net.URISyntaxException e) {return null;}
    }

    private void connectSocket() {
        mSocket.connect();
        mSocket.on("led", ledListen);
        mSocket.on("alarm", alarmListen);
        mSocket.on("tempSensor", tempListen);
        mSocket.on("connect", socketConnect);
        mSocket.on("disconnect", socketDisconnect);
    }

    private void disconnectSocket() {
        mSocket.disconnect();
        mSocket.off("led", ledListen);
        mSocket.off("alarm", alarmListen);
        mSocket.off("tempSensor", tempListen);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private Emitter.Listener socketConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectionStatusText.setText("Connected");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        connectionStatus.setBackground(getResources().getDrawable(R.drawable.greencircle));
                    }
                    else {
                        connectionStatus.setBackgroundColor(getResources().getColor(R.color.connected));
                    }
                }
            });
        }
    };

    private Emitter.Listener socketDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectionStatusText.setText("Disconnected");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        connectionStatus.setBackground(getResources().getDrawable(R.drawable.redcircle));
                    }
                    else {
                        connectionStatus.setBackgroundColor(getResources().getColor(R.color.disconnected));
                    }
                    tempText.setText("0\u00b0C");
                    humidText.setText("0%");
                }
            });
        }
    };

    private Emitter.Listener ledListen = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Boolean value;
                    try {
                        value = data.getBoolean("value");
                        ledSwitch.setChecked(value);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener alarmListen = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Boolean value;
                    try {
                        value = data.getBoolean("value");
                        alarmSwitch.setChecked(value);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener tempListen = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // To fix problem with foreground notification clicks
                    connectionStatusText.setText("Connected");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        connectionStatus.setBackground(getResources().getDrawable(R.drawable.greencircle));
                    }
                    else {
                        connectionStatus.setBackgroundColor(getResources().getColor(R.color.connected));
                    }

                    JSONObject data0 = (JSONObject) args[0];
                    JSONObject data1 = (JSONObject) args[1];
                    int temp, humid;
                    try {
                        temp = data0.getInt("value");
                        humid = data1.getInt("value");
                        tempText.setText(temp + "\u00b0C");
                        humidText.setText(humid + "%");
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

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
        mSocket = createSocket(intIp);
        connectSocket();
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        ipToggle = (ToggleButton) view.findViewById(R.id.ipToggle);
        ledSwitch = (Switch) view.findViewById(R.id.ledSwitch);
        alarmSwitch = (Switch) view.findViewById(R.id.alarmSwitch);
        tempText = (TextView) view.findViewById(R.id.temperature);
        humidText = (TextView) view.findViewById(R.id.humidity);
        connectionStatus = (FrameLayout) view.findViewById(R.id.connectionStatus);
        connectionStatusText = (TextView) view.findViewById(R.id.connectionStatusText);
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
                JSONObject json = new JSONObject();
                try {
                    if (ledSwitch.isChecked()) {
                        json.put("value", "BTN_ON");
                    }
                    else json.put("value", "BTN_OFF");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("led", json);
            }
        });

        alarmSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                JSONObject json = new JSONObject();
                try {
                    json.put("value", alarmSwitch.isChecked());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("alarm", json);
            }
        });

        ipToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    disconnectSocket();
                    mSocket = createSocket(extIp);
                    connectSocket();
                }
                else {
                    disconnectSocket();
                    mSocket = createSocket(intIp);
                    connectSocket();
                }
            }
        });
    }

    private void send_rgb(Button btn) {
        JSONObject json = new JSONObject();
        try {
            json.put("value", getResources().getResourceEntryName(btn.getId()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("led", json);
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

        disconnectSocket();
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
