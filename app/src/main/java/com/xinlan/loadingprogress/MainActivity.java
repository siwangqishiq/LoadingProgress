package com.xinlan.loadingprogress;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private View btn1, btn2;

    private LoadingView mLoadView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = findViewById(R.id.test1);
        btn2 = findViewById(R.id.test2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        mLoadView  = (LoadingView) findViewById(R.id.loading_view);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.test1:
                btn1Click();
                break;
            case R.id.test2:
                btn2Click();
                break;
        }//end switch
    }

    private void btn1Click() {
        mLoadView.restart();
    }

    private void btn2Click() {
        mLoadView.startLoading();
    }
}//end class
