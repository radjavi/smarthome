package radjavi.smarthome;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by philipp on 02/06/16.
 */
public class DayAxisValueFormatter implements IAxisValueFormatter
{

    private final String[] mMonths = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private final BarLineChartBase<?> chart;
    private Long reference;

    public DayAxisValueFormatter(BarLineChartBase<?> chart, Long reference) {
        this.chart = chart;
        this.reference = reference;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // convertedTimestamp = originalTimestamp - referenceTimestamp
        long convertedTimestamp = (long) value;

        // Retrieve original timestamp
        long originalTimestamp = reference + convertedTimestamp;

        Date date = new Date(originalTimestamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int monthName = calendar.get(Calendar.MONTH);
        int yearName = calendar.get(Calendar.YEAR);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // > 6 months
        if (chart.getVisibleXRange() > (long)6*30*24*60*60*1000) return monthName + " " + yearName;
        // > 1 day
        else if (chart.getVisibleXRange() > 24*60*60*1000) return mMonths[monthName] + " " + dayOfMonth;
        // <= 1 day
        else return mMonths[monthName] + " " + dayOfMonth + " - " + hourOfDay + ":00";

    }
}
