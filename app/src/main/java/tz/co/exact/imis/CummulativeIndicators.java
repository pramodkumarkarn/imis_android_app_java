package tz.co.exact.imis;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.exact.CallSoap.CallSoap;
import com.exact.general.General;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static tz.co.exact.imis.MainActivity.global;

public class CummulativeIndicators extends AppCompatActivity {

    private General _General = new General(AppInformation.DomainInfo.getDomain());
    Boolean ClickedFrom = false;
    Boolean ClickedTo = false;

    Calendar myCalendar;

    Button DateFrom;
    Button DateTo;
    Button btnGet;

    TextView NPC;
    TextView RPC;
    TextView EPC;
    TextView SPC;
    TextView CCC;

    private ProgressDialog pd;
    RelativeLayout CommulativeReport;

    String commulative = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cummulative_indicators);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DateFrom= (Button) findViewById(R.id.DateFrom);
        DateTo= (Button) findViewById(R.id.DateTo);
        btnGet= (Button) findViewById(R.id.btnGet);

        NPC= (TextView) findViewById(R.id.NPC);
        RPC= (TextView) findViewById(R.id.RPC);
        EPC= (TextView) findViewById(R.id.EPC);
        SPC= (TextView) findViewById(R.id.SPC);
        CCC= (TextView) findViewById(R.id.CCC);

        CommulativeReport = (RelativeLayout) findViewById(R.id.CommulativeReport);

        myCalendar = Calendar.getInstance();

        DateFrom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ClickedTo = false;
                ClickedFrom = true;
                // TODO Auto-generated method stub
                new DatePickerDialog(CummulativeIndicators.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        DateTo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ClickedFrom = false;
                ClickedTo = true;
                // TODO Auto-generated method stub
                new DatePickerDialog(CummulativeIndicators.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!DateFrom.getText().equals("Date From") && !DateTo.getText().equals("Date To")){
                    GetCommulativeIndicators(String.valueOf(DateFrom.getText()),String.valueOf(DateTo.getText()));
                }else{
                    Toast.makeText(getApplicationContext(), "Pick dates   then Get.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        if(ClickedFrom == true){
            DateFrom.setText(String.valueOf(sdf.format(myCalendar.getTime())));
        }else{
            DateTo.setText(String.valueOf(sdf.format(myCalendar.getTime())));
        }
    }
    public void GetCommulativeIndicators(final String DateFrom, final String DateTo) {
        if(_General.isNetworkAvailable(this)){
            pd = ProgressDialog.show(CummulativeIndicators.this, "", getResources().getString(R.string.GetingCummulativeReport));
            try {
                new Thread() {
                    public void run() {
                        getCommulativeIndicators(DateFrom, DateTo);
                        pd.dismiss();
                    }
                }.start();

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public void getCommulativeIndicators(String DateFrom, String DateTo){
        //global = (Global) mContext.getApplicationContext();
        CallSoap cs = new CallSoap();
        cs.setFunctionName("GetCumulativeIndicators");
        try {
            commulative = cs.GetCummulativeIndicators(DateFrom, DateTo, String.valueOf(global.getOfficerId()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(commulative.length() != 0){
                        try {
                            JSONArray SnapshotData = new JSONArray(commulative);

                            for(int j = 0;j < SnapshotData.length();j++){
                                JSONObject ob = SnapshotData.getJSONObject(j);
                                NPC.setText(String.valueOf(ob.getInt("NewPolicies")));
                                RPC.setText(String.valueOf(ob.getInt("RenewedPolicies")));
                                EPC.setText(String.valueOf(ob.getInt("ExpiredPolicies")));
                                SPC.setText(String.valueOf(ob.getInt("SuspendedPolicies")));
                                CCC.setText(String.valueOf(ob.getInt("CollectedContribution")));

                                CommulativeReport.setVisibility(View.VISIBLE);
                            }
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), "Something went wrong on the server", Toast.LENGTH_SHORT);
                        }

                    }else{
                        Toast.makeText(getApplicationContext(), "Something went wrong on the server", Toast.LENGTH_SHORT);
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                super.onOptionsItemSelected(item);
        }
        return false;

    }

}
