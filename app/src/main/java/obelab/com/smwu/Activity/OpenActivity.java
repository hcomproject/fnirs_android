package obelab.com.smwu.Activity;

import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import obelab.com.smwu.Fragment.HomeFragment;
import obelab.com.smwu.Fragment.ResultFragment;
import obelab.com.smwu.Fragment.SettingFragment;
import obelab.com.smwu.R;

public class OpenActivity  extends AppCompatActivity {
    ViewPager vp;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        vp = (ViewPager)findViewById(R.id.vp_main_product);
        Button btn_first = (Button)findViewById(R.id.btn_result);
        Button btn_second = (Button)findViewById(R.id.btn_home);
        Button btn_third = (Button)findViewById(R.id.btn_setting);

        vp.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        vp.setCurrentItem(1);

        btn_first.setOnClickListener(movePageListener);
        btn_first.setTag(0);
        btn_second.setOnClickListener(movePageListener);
        btn_second.setTag(1);
        btn_third.setOnClickListener(movePageListener);
        btn_third.setTag(2);
    }

    View.OnClickListener movePageListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            int tag = (int) v.getTag();
            vp.setCurrentItem(tag);
        }
    };

    private class pagerAdapter extends FragmentStatePagerAdapter
    {
        public pagerAdapter(android.support.v4.app.FragmentManager fm)
        {
            super(fm);
        }
        @Override
        public android.support.v4.app.Fragment getItem(int position)
        {
            switch(position)
            {
                case 0:
                    return new ResultFragment();
                case 1:
                    return new HomeFragment();
                case 2:
                    return new SettingFragment();
                default:
                    return null;
            }
        }
        @Override
        public int getCount()
        {
            return 3;
        }
    }
    public void onFragmentChange(int index){
        if(index == 0 ){
            getSupportFragmentManager().beginTransaction().replace(R.id.vp_main_product, new HomeFragment());
        }
    }
}