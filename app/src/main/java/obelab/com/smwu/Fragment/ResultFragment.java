package obelab.com.smwu.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import obelab.com.smwu.Adapter.ResultOverviewRecyclerViewAdapter;
import obelab.com.smwu.Data.ResultOverviewData;
import obelab.com.smwu.R;

public class ResultFragment extends Fragment {
    private ArrayList<ResultOverviewData> resultList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ResultOverviewRecyclerViewAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_result, container, false);

        //recyclerview
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        mAdapter = new ResultOverviewRecyclerViewAdapter(resultList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareData();
    }

    //데이터 준비(최종적으로는 동적으로 추가하거나 삭제할 수 있어야 한다. 이 데이터를 어디에 저장할지 정해야 한다.)
    private void prepareData() {
        resultList.add(new ResultOverviewData("1","5월10일","04:10:07", "04:00:02",85));
        resultList.add(new ResultOverviewData("2","5월11일","05:10:07", "05:00:02",82));
        resultList.add(new ResultOverviewData("3","5월13일","03:10:07", "02:00:02",62));
    }
}

