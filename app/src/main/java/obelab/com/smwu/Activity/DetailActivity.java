package obelab.com.smwu.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import obelab.com.smwu.R;
import obelab.com.smwu.dataclass.ReportData;
import obelab.com.smwu.network.NetworkService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class DetailActivity extends AppCompatActivity {

    NetworkService networkService;
    Context ctx;
    String KEY;

    TextView tvDate;
    TextView tvStudyTime;
    TextView tvFocusedTime;
    TextView tvFocusedRatio;
    TextView tvScore;
    TextView tvResultScore;
    TextView tvGraphAnalysis;
    ImageView ivScoreGraph;
    String score_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        KEY = intent.getStringExtra("key");


        tvDate = (TextView) findViewById(R.id.tv_detail_date);
        tvStudyTime = (TextView) findViewById(R.id.tv_detail_study_time);
        tvFocusedTime = (TextView) findViewById(R.id.tv_detail_focused_time);
        tvFocusedRatio = (TextView) findViewById(R.id.tv_detail_focused_ratio);
        tvScore = (TextView) findViewById(R.id.tv_detail_score);
        tvResultScore = (TextView) findViewById(R.id.tv_detail_result_score);
        tvGraphAnalysis = (TextView) findViewById(R.id.tv_detail_graph_analysis);
        ivScoreGraph = (ImageView) findViewById(R.id.iv_detail_score_graph);

        try {
            getReportDetailResponse();
        }catch(Exception e){
            Log.e("현주: Detail ", e.getMessage());
        }

        ivScoreGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(ctx, PhotoZoomInActivity.class);
                    intent.putExtra("imageURL", score_img);
                    ctx.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));
                }catch (Exception e){
                    Log.e("현주", "startActivity 실패"+ e.getMessage());
                }
            }
        });
    }
        public void getReportDetailResponse(){
            // 서버 통신
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(NetworkService.baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            networkService = retrofit.create(NetworkService.class);

            // 레포트 상세 보기 서버 통신
            ctx = getApplicationContext();
            networkService.getReportDetail("application/json",  KEY)
                    .enqueue(new Callback<ArrayList<ReportData>>() {
                        @Override
                        public void onResponse(Call<ArrayList<ReportData>> call, Response<ArrayList<ReportData>> response) {
                            if (response.isSuccessful()){
                                ArrayList<ReportData> body = response.body();
                                ReportData data = body.get(0);
                                if (data != null){
                                    tvDate.setText(data.getDate_info());
                                    tvStudyTime.setText(data.getStudy_time());
                                    tvFocusedTime.setText(data.getFocused_time());
                                    tvFocusedRatio.setText(data.getFocused_ratio());
                                    tvScore.setText(Integer.toString(data.getScore()));
                                    tvResultScore.setText(Integer.toString(data.getScore()));
                                    tvGraphAnalysis.setText(data.getGraph_analysis());
                                    Glide.with(getApplicationContext())
                                            .load(data.getScore_img())
                                            .into(ivScoreGraph);
                                    score_img =  data.getScore_img();
                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<ArrayList<ReportData>> call, Throwable t) {
                            Log.e("DetailActivity", "레포트 상세보기 서버 통신 실패"+ t.toString());
                        }
                    });
        }
}

