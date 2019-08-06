package obelab.com.smwu.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.regex.Pattern;

import obelab.com.nirsitsdk.NirsitData;
import obelab.com.nirsitsdk.NirsitProvider;
import obelab.com.smwu.R;


public class MainActivity extends AppCompatActivity {
    private final String  TAG = "[" + MainActivity.class.getSimpleName() + "]";
    TextView data780TextView;
    TextView data850TextView;
    TextView data850mTextView;
    Button startButton;
    Button stopButton;
    Button resetButton;
    NirsitProvider nirsitProvider;

    Switch lpfSwitch;
    Switch mbllSwitch;
    Switch heartbeatSwitch;

    // Default device ip
    String ip = "192.168.0.1";
    final int port = 50007;
    final int TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data780TextView = (TextView)findViewById(R.id.data780TextView);
        data850TextView = (TextView)findViewById(R.id.data850TextView);
        data850mTextView = (TextView)findViewById(R.id.dataRawTextView);
        startButton = (Button)findViewById(R.id.startButton);
        stopButton = (Button)findViewById(R.id.stopButton);
        lpfSwitch = (Switch)findViewById(R.id.lpfSwitch);
        mbllSwitch = (Switch)findViewById(R.id.mbllSwitch);
        heartbeatSwitch = (Switch)findViewById(R.id.heartbeatSwitch);
        resetButton = (Button)findViewById(R.id.resetButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nirsitProvider.startMonitoring();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nirsitProvider == null) { return; }
                nirsitProvider.stopMonitoring();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nirsitProvider == null) { return; }
                nirsitProvider.resetHemo();
            }
        });

        lpfSwitch.setChecked(true);
        mbllSwitch.setChecked(true);
        heartbeatSwitch.setChecked(true);
        lpfSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(nirsitProvider == null) { return; }
                nirsitProvider.setLpf(b);
            }
        });

        mbllSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(nirsitProvider == null) { return; }
                nirsitProvider.setMbll(b);
            }
        });
        heartbeatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(nirsitProvider == null) { return; }
                nirsitProvider.setHeartbeat(b);
            }
        });

        nirsitProvider = new NirsitProvider(this, ip);
        nirsitProvider.setDataListener(new NirsitProvider.NirsitDataListener() {
            @Override
            public void onReceiveData(NirsitData data) {
                data780TextView.setText("[d780]\n" + Arrays.toString(data.getHbO2()));
                data850TextView.setText("[d850]\n" + Arrays.toString(data.getHbR()));
                data850mTextView.setText("[d780]\n" + data.getRaw());
                Log.d(TAG , "raw:" + data.getRaw() );
            }

            @Override
            public void onDisconnected() {
                Toast.makeText(getApplicationContext(), "onDisconnected()", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onReceiveDemodData(NirsitData data) {
                Log.d(TAG , "raw:" + data.getRaw() );
            }
        });

    }

    public void onClick(View view){
        Intent intent = new Intent(this, OpenActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_ip:
                showChangeAddressDialog();
                break;
            case R.id.menu_clear:
                data780TextView.setText("[d780]\n");
                data850TextView.setText("[d850]\n");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChangeAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Nirsit IP");
        final EditText input = new EditText(this);
        input.setText(ip);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newIp = input.getText().toString();
                String validIp = "^([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}$";
                if (!Pattern.matches(validIp, newIp )) {
                    Toast.makeText(getApplicationContext(), "Invalid IP address", Toast.LENGTH_SHORT).show();
                }
                else {
                    ip = input.getText().toString();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
