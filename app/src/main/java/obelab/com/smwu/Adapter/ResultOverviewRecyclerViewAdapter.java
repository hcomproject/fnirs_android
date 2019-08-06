package obelab.com.smwu.Adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import obelab.com.smwu.Activity.DetailActivity;
import obelab.com.smwu.Data.ResultOverviewData;
import obelab.com.smwu.R;

public class ResultOverviewRecyclerViewAdapter extends RecyclerView.Adapter<ResultOverviewRecyclerViewAdapter.MyViewHolder>{
    private ArrayList<ResultOverviewData> mDataset;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView date, timer, real_timer, score;

        //ViewHolder
        public MyViewHolder(View view) {
            super(view);
            date = (TextView) view.findViewById(R.id.txt_rv_item_product_overview_date);
            timer = (TextView) view.findViewById(R.id.txt_rv_item_product_overview_timer_time);
            real_timer = (TextView) view.findViewById(R.id.txt_rv_item_product_overview_real_focus_time);
            score = (TextView) view.findViewById(R.id.txt_rv_item_product_overview_score);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), DetailActivity.class);

                    intent.putExtra("selectedDate", date.getText().toString());
                    intent.putExtra("selectedTimer", timer.getText().toString()); //this one on makes app crash
                    intent.putExtra("selectedRTimer", real_timer.getText().toString()); //this one on makes app crash
                    intent.putExtra("selectedScore", score.getText().toString()); //this one on makes app crash
                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    public ResultOverviewRecyclerViewAdapter(ArrayList<ResultOverviewData> myData){
        this.mDataset = myData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_result_overview, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultOverviewRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.date.setText(mDataset.get(position).getDate());
        holder.timer.setText(mDataset.get(position).getTimer());
        holder.real_timer.setText(mDataset.get(position).getReal_timer());
        holder.score.setText(String.valueOf(mDataset.get(position).getScore()));

    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
