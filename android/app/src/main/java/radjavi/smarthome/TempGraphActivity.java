package radjavi.smarthome;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TempGraphActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference tempDataRef;
    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_graph);

        database = MyFirebaseDatabase.getDatabase();
        tempDataRef = database.getReference("tempData");
        tempDataRef.keepSynced(true);

        Toolbar myToolbar = findViewById(R.id.graph_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        drawGraph();
    }

    private void drawGraph() {
        Typeface firaSans = ResourcesCompat.getFont(this, R.font.fira_sans);
        int colorLightGrey = ResourcesCompat.getColor(getResources(), R.color.colorLightGrey, null);
        int colorDark = ResourcesCompat.getColor(getResources(), R.color.colorDark, null);
        int colorPrimary = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);
        chart = (LineChart) findViewById(R.id.chart);
        chart.setDescription(null);
        chart.getXAxis().setLabelCount(3);
        chart.getXAxis().setTextColor(colorLightGrey);
        chart.getXAxis().setGridColor(colorDark);
        chart.getAxisLeft().setTextColor(colorLightGrey);
        chart.getAxisLeft().setGridColor(colorDark);
        chart.getAxisRight().setTextColor(colorLightGrey);
        chart.getAxisRight().setGridColor(colorDark);
        chart.setBackgroundColor(colorPrimary);
        chart.getLegend().setTextColor(colorLightGrey);
        chart.getLegend().setTypeface(firaSans);
        chart.getAxisLeft().setTypeface(firaSans);
        chart.getAxisRight().setTypeface(firaSans);
        chart.getXAxis().setTypeface(firaSans);
        chart.getAxisLeft().setAxisMinimum(10);
        chart.getAxisLeft().setAxisMaximum(40);
        chart.getAxisRight().setAxisMinimum(0);
        chart.getAxisRight().setAxisMaximum(100);
        //chart.getAxisRight().setEnabled(false);
        chart.setDrawBorders(false);
        addData();
    }

    private void addData() {
        tempDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long reference = null;
                List<Entry> tempEntries = new ArrayList<>();
                List<Entry> humidEntries = new ArrayList<>();
                for (DataSnapshot tempSnapshot: dataSnapshot.getChildren()) {
                    Long dateLong = tempSnapshot.child("date").getValue(Long.class);
                    Integer temp = tempSnapshot.child("temp").getValue(Integer.class);
                    Integer humid = tempSnapshot.child("humid").getValue(Integer.class);
                    if (dateLong != null && temp != null && humid != null) {
                        if (reference == null) reference = dateLong;
                        Long date = dateLong - reference;
                        tempEntries.add(new Entry(date.floatValue(), temp.floatValue()));
                        humidEntries.add(new Entry(date.floatValue(), humid.floatValue()));
                    }
                }
                LineDataSet tempDataSet = new LineDataSet(tempEntries, "Temperature");
                //tempDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                tempDataSet.setColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                tempDataSet.setDrawCircles(false);
                tempDataSet.setLineWidth(2);
                LineDataSet humidDataSet = new LineDataSet(humidEntries, "Humidity");
                humidDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                humidDataSet.setColor(ResourcesCompat.getColor(getResources(), R.color.colorRed, null));
                humidDataSet.setDrawCircles(false);
                humidDataSet.setLineWidth(2);
                humidDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

                List<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(tempDataSet);
                dataSets.add(humidDataSet);
                LineData lineData = new LineData(dataSets);
                chart.setData(lineData);
                chart.getXAxis().setValueFormatter(new DayAxisValueFormatter(chart, reference));
                chart.getAxisRight().setValueFormatter(new IAxisValueFormatter() {

                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return (int)value + "%";
                    }
                });
                chart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return (int)value + "\u00b0C";
                    }
                });
                //chart.setBorderColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryLight, null));
                chart.notifyDataSetChanged();
                chart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
