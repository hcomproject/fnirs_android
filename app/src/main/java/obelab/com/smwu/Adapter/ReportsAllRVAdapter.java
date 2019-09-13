package obelab.com.smwu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import obelab.com.smwu.Activity.DetailActivity;
import obelab.com.smwu.R;
import obelab.com.smwu.dataclass.ReportData;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class ReportsAllRVAdapter extends RecyclerView.Adapter<ReportsAllRVAdapter.MyViewHolder>{
    private Context ctx;
    private ArrayList<ReportData> mDataset;
    String clickedKey;
    int myscore;
    public ReportsAllRVAdapter(Context context, ArrayList<ReportData> myData){
        this.ctx = context;
        this.mDataset = myData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.rv_item_result_overview, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportsAllRVAdapter.MyViewHolder holder, int position) {

        holder.date.setText(mDataset.get(position).getDate_info());
        holder.start_time.setText(mDataset.get(position).getTime_info());
        holder.study_time.setText(mDataset.get(position).getStudy_time());
        holder.focused_time.setText(mDataset.get(position).getFocused_time());
        holder.score.setText(String.valueOf(mDataset.get(position).getScore()));

        myscore = mDataset.get(position).getScore();
        final ReportData data_turn = mDataset.get(position);


        holder.btnReportDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d("현주", data_turn.getKey());
                    Intent intent = new Intent(ctx, DetailActivity.class);
                    intent.putExtra("key", data_turn.getKey());
                    ctx.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));
                }catch (Exception e){
                    Log.e("현주", "startActivity 실패"+ e.getMessage());
                }
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView date, start_time, study_time, focused_time, score;
        public LinearLayout btnReportDetail;
        ProgressBar rv_progressBar;

        //ViewHolder
        public MyViewHolder(View view) {
            super(view);
            btnReportDetail = (LinearLayout) view.findViewById(R.id.btn_rv_report_detail);
            date = (TextView) view.findViewById(R.id.tv_rv_item_product_overview_date);
            start_time = (TextView) view.findViewById(R.id.tv_rv_item_product_overview_time);
            study_time = (TextView) view.findViewById(R.id.tv_rv_item_product_overview_study_time);
            focused_time = (TextView) view.findViewById(R.id.tv_rv_item_product_overview_real_focus_time);
            score = (TextView) view.findViewById(R.id.txt_rv_item_product_overview_score);
            rv_progressBar = (ProgressBar)view.findViewById(R.id.rv_progressBar) ;

            // 문자열을 숫자로 변환.
            if (myscore >= 0 && myscore <25 ) {
                score.setTextColor(Color.parseColor("#2196F3"));
                rv_progressBar.setProgressDrawable(view.getResources().getDrawable(R.drawable.score_circle_bar_blue));
            }
            else if (myscore >= 25 && myscore < 50 ){
                score.setTextColor(Color.parseColor("#8BC34A"));
                rv_progressBar.setProgressDrawable(view.getResources().getDrawable(R.drawable.score_circle_bar_green));
            }
            else if (myscore >= 50 && myscore < 79 ) {
                score.setTextColor(Color.parseColor("#FFC107"));
                rv_progressBar.setProgressDrawable(view.getResources().getDrawable(R.drawable.score_circle_bar_yellow));
            }
            rv_progressBar.setProgress(myscore) ;
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

