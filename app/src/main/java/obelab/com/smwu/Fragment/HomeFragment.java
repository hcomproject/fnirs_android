package obelab.com.smwu.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import obelab.com.nirsitsdk.NirsitData;
import obelab.com.nirsitsdk.NirsitProvider;
import obelab.com.smwu.Activity.SettingActivity;
import obelab.com.smwu.R;
import obelab.com.smwu.network.NetworkService;
import obelab.com.smwu.utils.fileUtils;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.WIFI_SERVICE;


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
    //Button btnGainCal;
    LinearLayout btnUpload;
    //timer
    long outTime;
    Handler myTimer = new Handler() {
        public void handleMessage(Message msg) {
            txtTime.setText(getTimeOut());
            myTimer.sendEmptyMessage(0);    // sendEmptyMessage는 비어있는 메시지를 Handler에게 전송.
        }
    };

    private int cnt = 0;
    private Timestamp ts;
    File mFile;

    //S3 upload 변수
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;
    final String MY_BUCKET = "hcom-fnirs";
    String OBJECT_KEY = "";

    // 네트워크
    ScanResult scanResult;
    WifiManager wm;
    WifiConfiguration wifiConfig = new WifiConfiguration(); // 와이파이 연결하기

    List<ScanResult> apList = new ArrayList<ScanResult>();
    ArrayAdapter<String> adapter;

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

        // gaincal로 넘어가기 위해 추가한 버튼
        //btnGainCal = (Button) view.findViewById(R.id.btnGainCal);
        btnUpload = (LinearLayout) view.findViewById(R.id.btnUpload);

        // 네트워크 변경 권한 얻기
        getPermission();

        //WiFi Scan List 불러오기
        wm = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);

        if (!wm.isWifiEnabled()) {
            wm.setWifiEnabled(true); // wifi 가 켜져있지 않을 경우 자동으로 wifi를 켜줍니다.
        }

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
                    String timestamp = ts.toString();
                    String date = timestamp.split("\\s")[0];
                    String time = timestamp.split("\\s")[1];
                    String filename = date + "_" +time.split(":")[0]+time.split(":")[1]+time.split(":")[2];

                // 파일 이름: "mbll_start를 누른 시각의 timestamp"
                saveData("mbll_" + filename, cnt, splittedHbO2HbR);
                OBJECT_KEY = "mbll_" + filename + ".txt";

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
                        String minute = txtTime.getText().toString().split(":")[0];
                        Integer int_minute = Integer.parseInt(minute);
                        Log.d("현주", int_minute.toString());

                        if (int_minute >= 1) {
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

                            Log.d(TAG, "COUNT:  " + cnt + "    Mbll:  " + nirsitProvider.isMbll());
                        } else {
                            Toast.makeText(getActivity(), "최소 1분은 학습하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        }
                }
            }
        });

        // GAIN CAL BUTTON - Activity로 연결
//        btnGainCal.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                wifiConfig.SSID = String.format("\"%s\"", "NIRSIT4");
//                wifiConfig.preSharedKey = String.format("\"%s\"", "12345678");
//
//                int netId = wm.addNetwork(wifiConfig);
//                wm.disconnect();
//                wm.enableNetwork(netId, true);
//                wm.reconnect();
//                startActivity(new Intent(getActivity(), SettingActivity.class));
//            }
//        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // wifi 무조건 변경하기
                // NIRSIT 네트워크에 연결되어있으면 서버와 통신 불가능.
                connection_check("NIRSIT4");

                try{
                    uploadToS3(MY_BUCKET, OBJECT_KEY, mFile);
                }catch (Exception e) {
                    Toast.makeText(getActivity().getApplicationContext(), "업로드할 파일이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }


    /**
     *  upload the processed data to S3
     *  @param  bucket_name : 업로드 할 버킷 이름,
     *                      file_name : 버킷에 저장할 파일의 이름,
     *                      file : 버킷에 저장할 파일
     */
    private void uploadToS3(String bucket_name, String file_name, File file){
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
                bucket_name, /* 업로드 할 버킷 이름 */
                file_name, /* 버킷에 저장할 파일의 이름 */
                file /* 버킷에 저장할 파일  */
        );
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
     * 실행에 필요한 권한 얻기
     */
    public void getPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //Toast.makeText(getActivity().getApplicationContext(), "권한 허가", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                //Toast.makeText(getActivity().getApplicationContext(), "권한 거부", Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.with(getActivity().getApplicationContext())
                .setPermissionListener(permissionListener)
                .setRationaleMessage("네트워크 통신을 위해서 다음 권한을 허용해야 합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있어요.")
                .setPermissions(Manifest.permission.ACCESS_WIFI_STATE)
                .setPermissions(Manifest.permission.CHANGE_WIFI_STATE)
                .setPermissions(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)
                .setPermissions(Manifest.permission.CHANGE_NETWORK_STATE)
                .setPermissions(Manifest.permission.ACCESS_NETWORK_STATE)
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }

    /**
     *  연결 가능한 네트워크 찾기
     */
    public void searchWifi() {
        wm.startScan();
        apList = wm.getScanResults();       //WiFi Scan 결과 - Return List
        if (wm.getScanResults() != null) {
            adapter = new ArrayAdapter<>(getContext(), android.R.layout.select_dialog_item);

            int size = apList.size();
            if (size == 0)
                Toast.makeText(getContext(), "GPS를 켜주세요.", Toast.LENGTH_SHORT).show();
            for (int i = 0; i < size; i++) {
                scanResult = (ScanResult) apList.get(i);
                adapter.add(apList.get(i).SSID);
                Log.d("현주", apList.get(i).SSID);
            }
            adapter.notifyDataSetChanged();
        }
        CreateListDialog();
    }

    /**
     *  현재 접속된 WIFI 이름(SSID) 가져와서 해제하기
     * @param  wifi_name
     */
    public void connection_check(String wifi_name) {
        // 현재 접속된 WIFI 이름(SSID) 가져오기
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        // 특정 와이파이와 연결 해제
        if (ssid != null && ssid.contains(wifi_name)) {
            wm.disconnect();
            searchWifi();
        }
    }

    /**
     *  연결 가능한 네트워크 알려주는 다이얼로그 생성
     */
    public void CreateListDialog() {
        AlertDialog.Builder wifi_list = new AlertDialog.Builder(getContext());
        wifi_list.setTitle("와이파이 변경");
        wifi_list.setIcon(R.drawable.brain);

        wifi_list.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String menu = adapter.getItem(which);

                wifiConfig.SSID = String.format("\"%s\"", menu);
                if (menu.contains("HCILAB")){
                    wifiConfig.preSharedKey = String.format("\"%s\"", "hcilab@417");
                }
                if (menu.contains("NIRSIT4")){
                    wifiConfig.preSharedKey = String.format("\"%s\"", "12345678");
                }
                int netId = wm.addNetwork(wifiConfig);
                wm.disconnect();
                wm.enableNetwork(netId, true);
                wm.reconnect();
                //Toast.makeText(getActivity().getApplicationContext(), menu, Toast.LENGTH_SHORT).show();
            }
        });
        wifi_list.show();
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

