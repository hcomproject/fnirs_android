package obelab.com.smwu.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import obelab.com.smwu.Adapter.ReportsAllRVAdapter;
import obelab.com.smwu.R;
import obelab.com.smwu.dataclass.ReportData;
import obelab.com.smwu.network.NetworkService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResultFragment extends Fragment {

    NetworkService networkService;
    ArrayList<ReportData> dataList = new ArrayList<ReportData>();
    ReportsAllRVAdapter reportsAllRVAdapter;
    RecyclerView rv_report_all_list;
    SwipeRefreshLayout sl_result_report_refresh;
    Context ctx;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_result, container, false);
        rv_report_all_list = v.findViewById(R.id.rv_report_all_list);
        sl_result_report_refresh = v.findViewById(R.id.sl_result_report_refresh);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sl_result_report_refresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getReportsResponse();
                        sl_result_report_refresh.setRefreshing(false);
                    }
                }
        );

        getReportsResponse();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void getReportsResponse() {
        // 서버 통신
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NetworkService.baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        networkService = retrofit.create(NetworkService.class);


        // 레포트 전체 보기 서버 통신
        ctx = getActivity().getApplicationContext();
        networkService.getReport("application/json")
                .enqueue(new Callback<ArrayList<ReportData>>() {
                   @Override
                    public void onResponse(Call<ArrayList<ReportData>> call, Response<ArrayList<ReportData>> response) {
                        if (response.isSuccessful()) {
                            ArrayList<ReportData> body = response.body();
                            reportsAllRVAdapter= new ReportsAllRVAdapter(ctx, body);

                            if (body != null) {
                                // 서버 통신을 위한 recyclerview
                                rv_report_all_list.setAdapter(reportsAllRVAdapter);
                                rv_report_all_list.setLayoutManager(new LinearLayoutManager(ctx));
                                rv_report_all_list.setItemAnimator(new DefaultItemAnimator());
                                reportsAllRVAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ArrayList<ReportData>> call, Throwable t) {
                        Log.e("ResultFragment", "서버 통신 실패"+t.toString());
                    }
                });
    }

}


