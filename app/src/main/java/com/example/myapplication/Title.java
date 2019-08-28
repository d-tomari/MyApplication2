package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

/**
 * ゲーム　タイトル
 */
public class Title extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.title);
    }

    /**
     *　画面タッチの判定　
     *
     *  画面から指を離した時ゲーム画面に遷移します
     * @param event
     * @return true,false
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                Intent intent = new Intent(getApplication(), Game.class);
                //Intent intent = new Intent(getApplication(), TmpGame.class);
                startActivity(intent);

        }
        return false;
    }

}