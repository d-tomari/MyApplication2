package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * リザルト画面　
 */
public class Result extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);

        Intent intent = getIntent();
        int deleteLineCount = intent.getIntExtra("DELETE_LINE", 0);

        ImageView imageView2 = findViewById(R.id.imageView1);
        imageView2.setImageResource(R.drawable.game_over);

        TextView textView = findViewById(R.id.textView);
        textView.setText("LINE: " + String.valueOf(deleteLineCount));
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
                Intent intent = new Intent(getApplication(), Title.class);
                //Intent intent = new Intent(getApplication(), TmpGame.class);
                startActivity(intent);

        }
        return false;
    }
}
