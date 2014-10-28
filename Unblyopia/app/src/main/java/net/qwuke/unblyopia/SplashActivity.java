package net.qwuke.unblyopia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;


public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_splash);
        Thread logoTimer = new Thread() {
            public void run(){
                try{
                    sleep(1200);
                }

                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                finally{
                    startActivity(new Intent("android.intent.action.GAME"));
                }
            }
        };

        logoTimer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish(); //closes the splash screen
    }

}
