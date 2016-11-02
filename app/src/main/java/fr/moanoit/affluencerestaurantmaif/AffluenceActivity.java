package fr.moanoit.affluencerestaurantmaif;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.moanoit.ws.moneweb.Affluence;

import static fr.moanoit.affluencerestaurantmaif.R.id.activity_affluence;

public class AffluenceActivity extends AppCompatActivity {

    private static String URL = "https://maif-solutions.moneweb.fr/Communication/Afficheur/RefreshAffluence";
    List<PieEntry> pieEntries = null;
    /**
     * View elements
     */
    private TextView mTauxOccupationTextView = null;
    private TextView mNbPlacesOccupeesTextView = null;
    private TextView mNbPlacesTotalesTextView = null;
    private Button mRefreshButton = null;
    private PieChart mPieChart = null;
    private Affluence affluence = null;

    public AffluenceActivity() {
        super();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affluence);

        affluence = Affluence.getInstance();

        mTauxOccupationTextView = (TextView) findViewById(R.id.tauxOccupation_textView);
        mNbPlacesOccupeesTextView = (TextView) findViewById(R.id.nbPlacesOccupees_textView);
        mNbPlacesTotalesTextView = (TextView) findViewById(R.id.nbPlacesTotales_textView);
        mRefreshButton = (Button) findViewById(R.id.resfresh_button);
        mPieChart = (PieChart) findViewById(R.id.occupation_chart);

        pieEntries = new ArrayList<PieEntry>();

        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshAffluence();
            }
        });

        refreshAffluence();

        // customize legends
        Legend l = mPieChart.getLegend();
        l.setTextColor(Color.DKGRAY);
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7);
        l.setYEntrySpace(5);

        // configure pie chart
        mPieChart.setUsePercentValues(true);
        Description desc = new Description();
        desc.setEnabled(false);
        desc.setText(getString(R.string.text_tauxOccupation));
        mPieChart.setDescription(desc);

        // enable hole and configure
        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleRadius(7);
        mPieChart.setTransparentCircleRadius(25);

        // enable rotation of the chart by touch
        mPieChart.setRotationAngle(0);
        mPieChart.setRotationEnabled(false);
        mPieChart.setDrawCenterText(true);
        mPieChart.setEntryLabelColor(Color.DKGRAY);

        // set a chart value selected listener
        mPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                // display msg when value selected
                if (e == null)
                    return;

                Snackbar.make(findViewById(activity_affluence), String.valueOf(h.getX()) + " %", Snackbar.LENGTH_SHORT);
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void refreshAffluence() {

        // Instantiate the RequestQueue.
        affluence.setRequestQueue(Volley.newRequestQueue(this));

        // Request a string response from the provided URL.
        JsonObjectRequest jsonRequest = null;
        jsonRequest = new JsonObjectRequest(Request.Method.POST, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (!pieEntries.isEmpty()) {
                            pieEntries.clear();
                        }
                        // the response is already constructed as a JSONObject!
                        try {
                            response = response.getJSONObject("affluence");
                            mTauxOccupationTextView.setText(response.getString("TauxOccupation"));
                            mNbPlacesOccupeesTextView.setText(response.getString("NbPlacesOcupees"));
                            mNbPlacesTotalesTextView.setText(response.getString("NbPlacesTotales"));

                            pieEntries.add(new PieEntry(response.getInt("NbPlacesOcupees"), getString(R.string.text_nbPlacesOccupees)));
                            //pieEntries.add(new PieEntry(300, getString(R.string.text_nbPlacesOccupees)));
                            pieEntries.add(new PieEntry(response.getInt("NbPlacesTotales") - response.getInt("NbPlacesOcupees"), getString(R.string.text_nbPlacesLibres)));

                            // add many colors
                            ArrayList<Integer> colors = new ArrayList<Integer>();

                            for (int c : ColorTemplate.MATERIAL_COLORS)
                                colors.add(c);

                            PieDataSet dataSet = new PieDataSet(pieEntries, null); // add entries to dataset
                            dataSet.setColors(colors);

                            PieData pieData = new PieData(dataSet);
                            pieData.setValueFormatter(new PercentFormatter());
                            pieData.setValueTextColor(Color.DKGRAY);
                            pieData.setValueTextSize(16f);

                            mPieChart.setData(pieData);
                            mPieChart.invalidate(); // refresh

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Snackbar snackbar = Snackbar
                                    .make(findViewById(activity_affluence), "Couldn't parse data", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar snackbar = Snackbar
                        .make(findViewById(activity_affluence), "That didn't work!", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() {
                String body = "{\"widgetId\":\"1\",\"exploitationId\":\"1\"}";
                return body.getBytes();
            }
        };
        // Add the request to the RequestQueue.
        affluence.getRequestQueue().add(jsonRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        affluence.getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                // do I have to cancel this?
                return true; // -> always yes
            }
        });
    }
}

