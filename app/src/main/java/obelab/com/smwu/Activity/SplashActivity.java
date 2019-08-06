package obelab.com.smwu.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        try{
            Thread.sleep(2000);
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
        startActivity(new Intent(this, OpenActivity.class));
        // 첫 액티비티로 넘어간 후 로딩화면은 끝내주세요.
        SplashActivity.this.finish();
    }
}
