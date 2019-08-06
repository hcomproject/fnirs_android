package obelab.com.smwu.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import obelab.com.smwu.R;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_detail_result);
        Intent intent = getIntent();

        TextView textViewDate = (TextView)findViewById(R.id.txt_detail_date);
        String date = intent.getStringExtra("selectedDate");
        textViewDate.setText(date);

        TextView textViewTimer = (TextView)findViewById(R.id.txt_detail_timer);
        String timer = intent.getStringExtra("selectedTimer");
        textViewTimer.setText(timer);

        TextView textViewRtimer = (TextView)findViewById(R.id.txt_detail_realtimer);
        String Rtimer = intent.getStringExtra("selectedRTimer");
        textViewRtimer.setText(Rtimer);

        TextView textViewScore = (TextView)findViewById(R.id.txt_detail_score);
        String score = intent.getStringExtra("selectedScore");
        textViewScore.setText(score);

        TextView textViewRScore = (TextView)findViewById(R.id.txt_detail_resultscore);
        textViewRScore.setText(score);

        //TextView textViewPercent = (TextView)findViewById(R.id.txt_detail_percent);
        //int t = Integer.parseInt(timer);
        //int r = Integer.parseInt(Rtimer);
        //int Percent = (r / t)*100;
        //textViewPercent.setText(Integer.toString(Percent));
    }
}
