package com.xinlan.loadingprogress;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private View btn1, btn2;

    private LoadingView mLoadView;

    private CustomLoadingView mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = findViewById(R.id.test1);
        btn2 = findViewById(R.id.test2);

        mLoadingView = (CustomLoadingView) findViewById(R.id.loading_view2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        mLoadView = (LoadingView) findViewById(R.id.loading_view);
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
        mLoadingView.reset();
    }

    private void btn2Click() {
        mLoadingView.startLoading();
    }

    private float init_y;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //System.out.println("action_down");
                init_y = event.getY();
                ret = true;
                break;
            case MotionEvent.ACTION_MOVE:
                mLoadingView.setDistanceRatio(0.01f * (Math.abs(init_y - event.getY())));
                //System.out.println("action_move");
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //System.out.println("action_up");
                //mLoadingView.hide(true);
                mLoadingView.recover(true);
                break;
        }//end switch
        return ret;
    }

}//end class
