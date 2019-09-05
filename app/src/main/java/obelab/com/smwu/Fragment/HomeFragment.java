package obelab.com.smwu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;

import obelab.com.nirsitsdk.NirsitData;
import obelab.com.nirsitsdk.NirsitProvider;
import obelab.com.smwu.Activity.SettingActivity;
import obelab.com.smwu.R;
import obelab.com.smwu.utils.fileUtils;


/**
 * 기존 코드들은 모두 주석처리 하였습니다.
 * 측정 전 먼저 gain calibration을 해야합니다. 현재는 버튼을 통해 gain cal을 하는 화면으로 넘어가도록 구성하였습니다.(SettingActivity.java 참고 및 UI 추가 )
 * Mbll은 측정 시작 5초 후에 켜야합니다. 1초당 8개의 데이터를 받으므로 데이터 개수를 세어 40개부터 Mbll을 키고, resetHemo도 켜야 데이터가 정상적으로 나옵니다.(코드 설명 有)
 * array를 이용해 한꺼번에 데이터를 파일로 저장할 경우에는 데이터가 많아지거나 앱이 중단될 경우 데이터가 사라집니다.
 * 파일 저장 위치 역시 context가 아닌 sdcard안에 저장해야 데이터 확인이 가능합니다. - 실시간으로 sdcard안에 파일을 만들어 쓰는 방식으로 수정하였습니다.(코드 설명 有)
 */
public class HomeFragment extends Fragment {
    public static final String PATH = Environment.getExternalStorageDirectory() + "/SMWU/DATA";
    final static int Init = 0;
    final static int Run = 1;
    final int port = 50007;
    final int TIME_OUT = 3000;
    private final String TAG = "[OpenFragment]";
    TextView txtTime;
    int cur_Status = Init;
    int myCount = 1;
    long myBaseTime;
    long myPauseTime;
    String ip = "192.168.0.1";
    TextView dataHbO2TextView;
    //TextView inputDataTextView;
    TextView dataHbRTextView;
    NirsitProvider nirsitProvider;
    double[] splittedHbO2 = new double[16];

    //ArrayList<double[]> inputData = new ArrayList();
    //double[] splittedHbR = new double[16];
    Button btnGainCal;
    Button btnUpload;
    //timer
    long outTime;
    Handler myTimer = new Handler() {
        public void handleMessage(Message msg) {
            txtTime.setText(getTimeOut());
            myTimer.sendEmptyMessage(0);    // sendEmptyMessage는 비어있는 메시지를 Handler에게 전송.
        }
    };
    // 추가한 변수들
    private int cnt = 0;
    private Timestamp ts;
    File mFile;

    //S3 upload 변수
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;
    final String MY_BUCKET = "hcom-fnirs";
    String OBJECT_KEY = "";

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ViewPager vp;
        final Button btnStart = (Button) view.findViewById(R.id.btn_start);
        txtTime = (TextView) view.findViewById(R.id.txt_time);
        vp = (ViewPager) view.findViewById(R.id.vp_main_product);
        dataHbRTextView = (TextView) view.findViewById(R.id.dataHbRTextView);
        dataHbO2TextView = (TextView) view.findViewById(R.id.dataHbO2TextView);
        //inputDataTextView = (TextView) view.findViewById(R.id.inputDataTextView);

        // gaincal로 넘어가기 위해 추가한 버튼
        btnGainCal = (Button) view.findViewById(R.id.btnGainCal);
        btnUpload = (Button) view.findViewById(R.id.btnUpload);

        nirsitProvider = new NirsitProvider(getActivity(), ip);
        nirsitProvider.setMbll(false);
        nirsitProvider.setLpf(true);
        nirsitProvider.setHeartbeat(true);
        nirsitProvider.setDataListener(new NirsitProvider.NirsitDataListener() {
            @Override
            public void onReceiveData(NirsitData data) {
                double[] splittedHbO2HbR = new double[17];
                String resultTime = String.format("%02d.%02d", (outTime / 1000), (outTime % 1000));
                for (int i = 16; i < 32; i++) {
                    splittedHbO2[i - 16] = data.getHbO2()[i];
                    //splittedHbR[i - 16] = data.getHbR()[i];
                    splittedHbO2HbR[0] = Double.parseDouble(resultTime);     //timestamp
                    splittedHbO2HbR[i - 15] = data.getHbO2()[i];    //2k+1
                    //splittedHbO2HbR[2 * i - 30] = data.getHbR()[i];     //2(k+1)
                }
                Log.d("time", data.getTimestamp());

                // 40개 count 후(5s) setMbll(true)
                // setMbll(true) 후 initiate 위해 resetHemo()도 해주어야함 - 5초대에 데이터가 0이 나와야 정상
                if (++cnt == 40) {
                    nirsitProvider.setMbll(true);
                    nirsitProvider.resetHemo();
                }

                //inputData.add(splittedHbO2HbR);
                dataHbO2TextView.setText("[d780]\n" + Arrays.toString(splittedHbO2));
                //dataHbRTextView.setText("[d850]\n" + Arrays.toString(splittedHbR));

                Log.d(TAG, "COUNT:  " + cnt + "    Mbll:  " + nirsitProvider.isMbll());

                if (cnt == 1)
                    ts = new Timestamp(System.currentTimeMillis());
                // 파일 이름: "mbll_start를 누른 시각의 timestamp"
                saveData("mbll_" + ts, cnt, splittedHbO2HbR);
                OBJECT_KEY = "mbll_" + ts + ".txt";

                Log.d(TAG, "raw:" + data.getRaw());
            }

            @Override
            public void onDisconnected() {
                Toast.makeText(getActivity(), "onDisconnected()", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceiveDemodData(NirsitData data) {
                Log.d(TAG, "raw:" + data.getRaw());
            }
        });


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*      https://rjswn0315.tistory.com/23        */
                Log.i("STATE", "Button Pressed");

                switch (cur_Status) {
                    // 시작
                    case Init:
                        myBaseTime = SystemClock.elapsedRealtime();
                        //System.out.println(myBaseTime);
                        //myTimer이라는 핸들러를 빈 메시지를 보내서 호출
                        myTimer.sendEmptyMessage(0);
                        btnStart.setText("Stop");
                        cur_Status = Run;
                        nirsitProvider.startMonitoring();
                        //inputDataTextView.setText("[InputData]\n");
                        //inputData.clear();
                        break;

                    // 종료
                    case Run:
                        Toast.makeText(getActivity(), txtTime.getText() + " 동안 학습하셨습니다.", Toast.LENGTH_SHORT).show();
                        myTimer.removeMessages(0);
                        myPauseTime = SystemClock.elapsedRealtime();
                        btnStart.setText("Start!");
                        cur_Status = Init;
                        if (nirsitProvider == null) {
                            return;
                        }
                        nirsitProvider.stopMonitoring();

                        // stop시 Mbll 초기화, count도 초기화
                        nirsitProvider.setMbll(false);
                        cnt = 0;

                        txtTime.setText("00:00:00");

                        Log.d("s3", OBJECT_KEY);
                        /*
                        String input = "";
                        for (int i = 0; i < inputData.size(); i++) {
                            input = input.concat(Arrays.toString(inputData.get(i))).concat(("\n"));
                        }
                        inputDataTextView.setText("[InputData]\n" + input);
                        */


                        Log.d(TAG, "COUNT:  " + cnt + "    Mbll:  " + nirsitProvider.isMbll());
                }
            }
        });

        // GAIN CAL BUTTON - Activity로 연결
        btnGainCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // wifi 무조건 변경하기
                // NIRSIT 네트워크에 연결되어있으면 서버와 통신 불가능.

                /**S3 upload**/
                credentialsProvider = new CognitoCachingCredentialsProvider(
                        getActivity().getApplicationContext(),
                        "ap-northeast-2:19fe23f6-7318-4188-b2ab-a0d4703ebfe1", // Identity pool ID
                        Regions.AP_NORTHEAST_2 // Region
                );

                //AmazonS3Client 객체 생성
                s3 = new AmazonS3Client(credentialsProvider);
                s3.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
                s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");
                transferUtility = new TransferUtility(s3, getActivity().getApplicationContext());

                //TransferObserver 객체 생성
                TransferObserver observer = transferUtility.upload(
                        MY_BUCKET, /* 업로드 할 버킷 이름 */
                        OBJECT_KEY, /* 버킷에 저장할 파일의 이름 */
                        mFile /* 버킷에 저장할 파일 */
                );
            }
        });
        return view;
    }

    String getTimeOut() {
        long now = SystemClock.elapsedRealtime(); //애플리케이션이 실행되고 나서 실제로 경과된 시간
        outTime = now - myBaseTime;
        String resultTime = String.format("%02d:%02d:%02d", (outTime / 1000) / 60, (outTime / 1000) % 60, (outTime % 1000) / 10);
        return resultTime;
    }

    /**
     * sdcard안에 저장되는 파일('sdcard\SMWU\DATA'에서 확인 가능)
     * 실시간으로 쓰여지기 때문에 중간에 어플이 중지되더라도 그 이전까지의 데이터는 파일에 남아있습니다.
     */
    public void saveData(String fileName, int cnt, double[] splittedHbO2HbR) {
        if (cnt == 1) {// 첫 데이터 - file 생성
            fileUtils.makeDirectory(PATH);
            mFile = new File(PATH, fileName + ".txt");
            try {
                mFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            mFile = new File(PATH, fileName + ".txt");
            FileWriter fw = new FileWriter(mFile, true);
            for (int i = 0; i < splittedHbO2HbR.length; i++) {
                fw.append(String.valueOf(splittedHbO2HbR[i]));
                fw.append(", ");
            }
            fw.append(System.getProperty("line.separator"));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * context 말고 sdcard에 올려야 파일 확인이 가능합니다.
     * context 로 저장하면 루팅된 디바이스 or android studio로 만 접근가능합니다 .
     * public void saveData(String data) {
     * String inputData = data;
     * FileOutputStream fos = null;
     * try {
     * fos = getContext().openFileOutput("mblldata.txt", Context.MODE_PRIVATE);
     * fos.write(inputData.getBytes());
     * fos.close();
     * <p>
     * } catch (FileNotFoundException e) {
     * e.printStackTrace();
     * } catch (IOException e) {
     * e.printStackTrace();
     * }
     * }
     */


}

