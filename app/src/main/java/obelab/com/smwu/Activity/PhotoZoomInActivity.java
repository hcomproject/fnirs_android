package obelab.com.smwu.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import obelab.com.smwu.R;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class PhotoZoomInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_zoom_in);
        LinearLayout btnClose = (LinearLayout) findViewById(R.id.btn_photo_zoom_close);


        Intent intent = getIntent();
        String imgaeURL = intent.getStringExtra("imageURL");

        PhotoView photoView = findViewById(R.id.pv_phto_zoom);
        Glide.with(getApplicationContext())
                    .load(imgaeURL)
                    .into(photoView);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
