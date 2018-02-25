package radjavi.smarthome;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

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
    private ToggleButton ipToggle;
    private TextView tempText;
    private TextView humidText;
    private TextView connectionStatusText;

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
        mSocket.on("tempSensor", tempListen);
        mSocket.on("connect", socketConnect);
        mSocket.on("disconnect", socketDisconnect);
    }

    private void disconnectSocket() {
        mSocket.disconnect();
        mSocket.off("led", ledListen);
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
                    tempText.setText("Temperature: -\u00b0C");
                    humidText.setText("Humidity: -%");
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
                        tempText.setText("Temperature: " + temp + "\u00b0C");
                        humidText.setText("Humidity: " + humid + "%");
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
        getActivity().setTitle("Dashboard");
        mSocket = createSocket(intIp);
        connectSocket();

        ipToggle = (ToggleButton) view.findViewById(R.id.ipToggle);
        ledSwitch = (Switch) view.findViewById(R.id.ledSwitch);
        tempText = (TextView) view.findViewById(R.id.temperature);
        humidText = (TextView) view.findViewById(R.id.humidity);
        connectionStatus = (FrameLayout) view.findViewById(R.id.connectionStatus);
        connectionStatusText = (TextView) view.findViewById(R.id.connectionStatusText);

        ledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                JSONObject json = new JSONObject();
                try {
                    json.put("value", isChecked);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("led", json);
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
