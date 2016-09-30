package com.yy.nc.demo;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by huangzhilong on 2016/9/20.
 */
public class MyListener implements MyCallBack.Test {
    private MyCallBack.SpeedTest speedTest;

    public MyListener(MyCallBack.SpeedTest speedTest){
        this.speedTest = speedTest;
    }

    @Override
    public void success(Message message) {
        MainActivity.indexs ++;
        if( MainActivity.indexs == MainActivity.availabCount ) {
            long costTime = (System.currentTimeMillis() - message.getTime());
            System.out.println("notification costTime : " + costTime);
            speedTest.costTime("notification", costTime);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Message message) {
        MainActivity.indexs ++;
        if( MainActivity.indexs == MainActivity.availabCount) {
            long costTime = (System.currentTimeMillis() - message.getTime());
            System.out.println("eventBus costTime : " + costTime);
            speedTest.costTime("eventBus", costTime);
        }
    }
}
