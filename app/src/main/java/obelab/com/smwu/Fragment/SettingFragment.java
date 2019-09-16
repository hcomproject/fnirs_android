package obelab.com.smwu.Fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

import obelab.com.nirsitsdk.NirsitProvider;
import obelab.com.smwu.Activity.OpenActivity;
import obelab.com.smwu.R;
import obelab.com.smwu.databinding.ActivitySettingBinding;

public class SettingFragment extends Fragment {
    private String TAG = "TAG_" + this.getClass().getSimpleName();
    ActivitySettingBinding binding;

    public static final String IP = "192.168.0.1";

    private NirsitProvider mNirsitProvider;
    private CAL_STATUS mCalStatus = CAL_STATUS.NONE;
    private int mProgress = 0;
    private int channels = 0;

    enum CAL_STATUS {
        NONE,
        START,
        STOP,
        END,
    }
    OpenActivity activity;
    public SettingFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_setting, container, false);
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_setting, container, false);
        PermissionListener permissionDataListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // findViewById 대신 databinding 이용
                initNirsit();
                initLayout();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(getActivity().getApplicationContext(), "권한  거부", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(getActivity().getApplicationContext())
                .setPermissionListener(permissionDataListener)
                .setRationaleMessage("데이터 저장을 위해 파일 쓰기/읽기 권한이 필요합니다.")
                .setDeniedMessage("왜 거부하셨어요...\n하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
        return view;
    }
    private void initLayout() {
        binding.buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.channels.setText("00/48");
                refreshStatusValue();
                refreshChannelLayout();
                refreshButtonLayout();
            }
        });

        binding.buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCalStatus == CAL_STATUS.END) {
                    channels = 40;
                    if(channels >= 30){ // 통과한 채널 개수가 40 이상일 때
                        activity = (OpenActivity) getActivity();
                        activity.onFragmentChange(0);
                        //finish();
                    }else {
                        AlertDialog.Builder aa = new AlertDialog.Builder(getActivity().getApplicationContext());
                        aa.setTitle("Error");
                        aa.setMessage("장비가 올바르게 착용되지 않았어요. 다시 시작해주세요.");
                        aa.setCancelable(true);
                        aa.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        aa.create();
                        aa.show();
                    }
                } else {
                    refreshStatusValue();
                    refreshChannelLayout();
                    refreshButtonLayout();
                }
            }
        });

        refreshChannelLayout();
        refreshButtonLayout();
    }

    private void refreshStatusValue() {
        switch (mCalStatus) {
            case NONE:
                mCalStatus = CAL_STATUS.START;
                break;
            case STOP:
                mCalStatus = CAL_STATUS.START;
                break;
            case START:
                mCalStatus = CAL_STATUS.STOP;
                break;
            case END:
                mCalStatus = CAL_STATUS.START;
                break;
            default:
                break;
        }
    }

    private void refreshButtonLayout() {

        switch (mCalStatus) {
            case NONE:
                binding.buttonLeft.setVisibility(View.GONE);
                binding.buttonRight.setText("CALIBRATION");     //CALIBRATION
                break;
            case STOP:
                binding.buttonLeft.setVisibility(View.GONE);
                binding.buttonRight.setText("START");           //START
                mNirsitProvider.stopCalibration();
                break;
            case START:
                binding.buttonLeft.setVisibility(View.GONE);
                binding.buttonRight.setText("STOP");                //STOP
                mNirsitProvider.startCalibration(binding.snrPicker.getValue());
                break;
            case END:
                binding.buttonLeft.setVisibility(View.VISIBLE);
                binding.buttonRight.setText("START TASK");          //START TASK
                mNirsitProvider.stopCalibration();
                break;
            default:
                break;
        }
    }

    private void refreshChannelLayout() {
        switch (mCalStatus) {
            case NONE:
            case STOP:
            case START:
                mProgress = 0;
                binding.progressBar.setProgress(mProgress);
                break;
            case END:
                mProgress = binding.progressBar.getMax();
                binding.progressBar.setProgress(mProgress);
                break;
            default:
                break;
        }
    }

    private  void initNirsit() {
        mNirsitProvider = new NirsitProvider(getActivity().getApplicationContext(), IP);
        mNirsitProvider.setCalibListener(new NirsitProvider.NirsitCalibListener() {
            @Override
            public void onReceiveData(String string) {
                Log.d(TAG, "onReceiveData :" + string);
                mProgress++;
                if(mProgress <= binding.progressBar.getMax()) {
                    binding.progressBar.setProgress(mProgress);
                }
            }

            @Override
            public void onLaserData(String ld) { // gain cal 시작 전 배열
                Log.d(TAG, "onLaserData :" + ld);
                String[] ldPower = ld.split(" ");
                mProgress++;
                if(mProgress <= binding.progressBar.getMax()) {
                    binding.progressBar.setProgress(mProgress);
                }
            }

            @Override
            public void onDisconnected() {
                //END
                mCalStatus = CAL_STATUS.END;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * calibration 하는 법:
                         * snr780[i]과 snr850[i]가 모두 기준치(snr picker value)보다 높으면 통과(reject false), 아니면 탈락(reject true)
                         * 이는 NirsitProvider의 메소드 getRejectChannel에서 수행되는 내용으로, return 받은 boolean 배열의 false 개수를 세면 통과한 채널의 개수를 알 수 있습니다.
                         */
                        channels = 0;
                        boolean[] reject = mNirsitProvider.getRejectChannel(binding.snrPicker.getValue());
                        for(boolean b : reject) {
                            if(!b)
                                channels++;
                        }

                        binding.channels.setText(channels +"/48");
                        refreshChannelLayout();
                        refreshButtonLayout();
                    }
                });
            }

            /**
             * NirsitProvider의 메소드 snrCalculation()으로부터 받아오는 데이터
             */
            @Override
            public void onSNRdata(final int[] snr780, final int[] snr850) {
                Log.d(TAG, "onSNRdata snr780: " + snr780.length + " snr850: " + snr850.length);
            }

        });
    }

//    public void onBackPressed() {
//        super.getActivity().onBackPressed();
//        if(mNirsitProvider != null){
//            mNirsitProvider.stopCalibration();
//        }
//    }


    @Override
    public void onPause() {
        super.onPause();
        if (mNirsitProvider != null) {
            mNirsitProvider.stopCalibration();
        }
    }
//
//    public void onBackPressed() {
//        super.getActivity().onBackPressed();
//        if(mNirsitProvider != null){
//            mNirsitProvider.stopCalibration();
//        }
//    }

}