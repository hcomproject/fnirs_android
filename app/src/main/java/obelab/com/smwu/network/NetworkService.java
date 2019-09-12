package obelab.com.smwu.network;

import java.util.ArrayList;

import obelab.com.smwu.dataclass.ReportData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface NetworkService {

    String baseURL = "http://ec2-13-209-48-0.ap-northeast-2.compute.amazonaws.com:7777";

    // 레포트 전체보기
    @GET("/reportdatas")
    Call<ArrayList<ReportData>> getReport(@Header("Content-Type") String content_type);

    // 레포트 상세보기
    @GET("/reportdatas/{KEY}")
    Call<ArrayList<ReportData>> getReportDetail(@Header("Content-Type") String content_type,
                                                                            @Path("KEY") String KEY);
}
