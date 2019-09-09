package com.example.myapplication;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

public class Game extends Activity {
    private class FieldView extends SurfaceView {

        Random mRand = new Random(System.currentTimeMillis());

        //各ブロックの形状を保持している配列
        int[][][] blocks = {
                {
                        {1,1},
                        {0,1},
                        {0,1}
                },
                {
                        {1,1},
                        {1,0},
                        {1,0}
                },
                {
                        {1,1},
                        {1,1}
                },
                {
                        {1,0},
                        {1,1},
                        {1,0}
                },
                {
                        {1,0},
                        {1,1},
                        {0,1}
                },
                {
                        {0,1},
                        {1,1},
                        {1,0}
                },
                {
                        {1},
                        {1},
                        {1},
                        {1}
                }
        };

        int[][] block = blocks[mRand.nextInt(blocks.length)];
        int posx, posy; //ブロックの現在位置を保持?
        int mapWidth  = 10; //マップの横幅
        int mapHeight = 20; //マップの縦幅
        // NEW 消したやつ記憶
        int deleteCount = 0;

        int[][] map = new int[mapHeight][]; //マップのサイズ? mapHeightだけ入れているのはinitGameで使用するため?

        //FieldViewクラスのコンストラクタ
        public FieldView(Context context) {
            super(context);

            setBackgroundColor(0xFFFFFFFF);
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        }

        //マップのサイズ設定　縦:20 横:10
        public void initGame() {
            for (int y = 0; y < mapHeight; y++) {
                map[y] = new int[mapWidth];
                for (int x = 0; x < mapWidth; x++) {
                    map[y][x] = 0;
                }
            }
        }

        // ブロックやマップの色付け
        private void paintMatrix(Canvas canvas, int[][] matrix, int offsetx, int offsety, int color) {
            ShapeDrawable rect = new ShapeDrawable(new RectShape());
            rect.getPaint().setColor(color);
            int h = matrix.length;
            int w = matrix[0].length;

            for (int y = 0; y < h; y ++) {
                for (int x = 0; x < w; x ++) {
                    if (matrix[y][x] != 0) {
                        int px = (x + offsetx) * 20;
                        int py = (y + offsety) * 20;
                        rect.setBounds(px, py, px + 20, py + 20);
                        rect.draw(canvas);
                    }
                }
            }
        }

        //キー入力時(ブロック移動時)のチェック　ブロックの移動先がマップ内であればtrue マップ外ならfalseを返す
        boolean check(int[][] block, int offsetx, int offsety) {
            if (offsetx < 0 || offsety < 0 ||
                    mapHeight < offsety + block.length ||
                    mapWidth < offsetx + block[0].length) {
                return false;
            }
            for (int y = 0; y < block.length; y ++) {
                for (int x = 0; x < block[y].length; x ++) {
                    if (block[y][x] != 0 && map[y + offsety][x + offsetx] != 0) {
                        return false;
                    }
                }
            }
            return true;
        }

        // ブロックが落ちた時に固定する
        void mergeMatrix(int[][] block, int offsetx, int offsety) {
            for (int y = 0; y < block.length; y ++) {
                for (int x = 0; x < block[0].length; x ++) {
                    if (block[y][x] != 0) {
                        map[offsety + y][offsetx + x] = block[y][x];
                    }
                }
            }
        }

        void clearRows() {
            // 埋めた行を消す(null)
            for (int y = 0; y < mapHeight; y ++) {
                boolean full = true;
                for (int x = 0; x < mapWidth; x ++) {
                    if (map[y][x] == 0) {
                        full = false;
                        break;
                    }
                }

                if (full) map[y] = null;
            }

            // 新しいmapにnull以外の行を詰めてコピーする
            //ここのforで
            int[][] newMap = new int[mapHeight][];
            int y2 = mapHeight - 1;
            for (int y = mapHeight - 1; y >= 0; y--) {
                if (map[y] == null) {
                    //消した列数をカウント
                    deleteCount += 1;
                    continue;
                } else {
                    newMap[y2--] = map[y];
                }
            }

            // 消えた行数分新しい行を追加する
            for (int i = 0; i <= y2; i++) {
                int[] newRow = new int[mapWidth];
                for (int j = 0; j < mapWidth; j ++) {
                    newRow[j] = 0;
                }
                newMap[i] = newRow;
            }
            map = newMap;
        }

        // 画面に表示するものの色等の設定
        /**
         * Draws the 2D layer.
         */
        @Override
        protected void onDraw(Canvas canvas) {
            ShapeDrawable rect = new ShapeDrawable(new RectShape());
            rect.setBounds(0, 0, 210, 410);
            rect.getPaint().setColor(0xFF000000);
            rect.draw(canvas);
            canvas.translate(5, 5);
            rect.setBounds(0, 0, 200, 400);
            rect.getPaint().setColor(0xFFFFFFFF);
            rect.draw(canvas);

            paintMatrix(canvas, block, posx, posy, 0xFFFF0000);
            paintMatrix(canvas, map, 0, 0, 0xFF808080);
        }

        //ブロックの回転
        int[][] rotate(final int[][] block) {
            int[][] rotated = new int[block[0].length][];
            for (int x = 0; x < block[0].length; x ++) {
                rotated[x] = new int[block.length];
                for (int y = 0; y < block.length; y ++) {
                    rotated[x][block.length - y - 1] = block[y][x];
                }
            }
            return rotated;
        }


        //======================================================================================
        // フリック時の処理
        //======================================================================================
        // 最後にタッチされた座標
        private float startTouchX;
        private float startTouchY;

        // 現在タッチ中の座標
        private float nowTouchedX;
        private float nowTouchedY;

        // フリックの遊び部分（最低限移動しないといけない距離）TODO もっと値が小さい方がいいかも
        private float adjust = 120;

        @Override
        public boolean onTouchEvent(MotionEvent event_){
            switch( event_.getAction() ){

                // タッチ　タッチされた座標を記録
                case MotionEvent.ACTION_DOWN:
                    Log.v( "motionEvent", "--ACTION_DOWN" );
                    startTouchX = event_.getX();
                    startTouchY = event_.getY();
                    break;

                // タッチが離れた　タッチされた時の座標と離れたときの座標を元にフリック方向を求める
                case MotionEvent.ACTION_UP:
                    Log.v( "motionEvent", "--ACTION_UP" );
                    nowTouchedX = event_.getX();
                    nowTouchedY = event_.getY();

                    if( startTouchY > nowTouchedY ){
                        if( startTouchX > nowTouchedX ){
                            if( ( startTouchY - nowTouchedY ) > ( startTouchX - nowTouchedX ) ){
                                if( startTouchY > nowTouchedY + adjust ){
                                    // 上フリック時の処理を記述する
                                }
                            }
                            else if( ( startTouchY - nowTouchedY ) < ( startTouchX - nowTouchedX ) ){
                                if( startTouchX > nowTouchedX + adjust ){
                                    // 左フリック時の処理を記述する
                                    if (check(block, posx - 1, posy)) {
                                        posx = posx - 1;
                                    }
                                    break;
                                }
                            }
                        }
                        else if( startTouchX < nowTouchedX ){
                            if( ( startTouchY - nowTouchedY ) > ( nowTouchedX - startTouchX ) ){
                                if( startTouchY > nowTouchedY + adjust ){
                                    // 上フリック時の処理を記述する
                                }
                            }
                            else if( ( startTouchY - nowTouchedY ) < ( nowTouchedX - startTouchX ) ){
                                if( startTouchX < nowTouchedX + adjust ){
                                    // 右フリック時の処理を記述する
                                    if (check(block, posx + 1, posy)) {
                                        posx = posx + 1;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    else if( startTouchY < nowTouchedY ){
                        if( startTouchX > nowTouchedX ){
                            if( ( nowTouchedY - startTouchY ) > ( startTouchX - nowTouchedX ) ){
                                if( startTouchY < nowTouchedY + adjust ){
                                    // 下フリック時の処理を記述する
                                    int y = posy;
                                    while (check(block, posx, y)) { y++; }
                                    if (y > 0) posy = y - 1;

                                    break;
                                }
                            }
                            else if( ( nowTouchedY - startTouchY ) < ( startTouchX - nowTouchedX ) ){
                                if( startTouchX > nowTouchedX + adjust ){
                                    Log.v( "Flick", "--左" );
                                    // 左フリック時の処理を記述する
                                    if (check(block, posx - 1, posy)) {
                                        posx = posx - 1;
                                    }
                                    break;
                                }
                            }
                        }
                        else if( startTouchX < nowTouchedX ){
                            if( ( nowTouchedY - startTouchY ) > (  nowTouchedX - startTouchX  ) ){
                                if( startTouchY < nowTouchedY + adjust ){
                                    // 下フリック時の処理を記述する
                                    int y = posy;
                                    while (check(block, posx, y)) { y++; }
                                    if (y > 0) posy = y - 1;

                                    break;
                                }
                            }
                            else if( ( nowTouchedY - startTouchY ) < ( nowTouchedX - startTouchX ) ){
                                if( startTouchX < nowTouchedX + adjust ){
                                    // 右フリック時の処理を記述する
                                    if (check(block, posx + 1, posy)) {
                                        posx = posx + 1;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    break;
            }
            return( true );
        }
        //======================================================================================

        //キー押下時の動作　※実機(スマホ)で動かす時は使用しないコード
        //センターキー :ブロックの回転?
        //右キー : 右に移動　x軸に+1
        //左キー : 左に移動　x軸に-1
        //上キー and 下キー : 一番下まで移動　Y軸が0になるまで移動
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    int[][] newBlock = rotate(block);
                    if (check(newBlock, posx, posy)) {
                        block = newBlock;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (check(block, posx + 1, posy)) {
                        posx = posx + 1;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (check(block, posx - 1, posy)) {
                        posx = posx - 1;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    int y = posy;
                    while (check(block, posx, y)) { y++; }
                    if (y > 0) posy = y - 1;

                    break;
            }
            mHandler.sendEmptyMessage(INVALIDATE);
            return true;
        }

        //アニメーションのスタートand再開? sendEmptyMessageの昨日までは未調査
        public void startAnime() {
            mHandler.sendEmptyMessage(INVALIDATE);
            mHandler.sendEmptyMessage(DROPBLOCK);
        }

        //アニメーションのストップ? removeMessages　の機能までは未調査
        public void stopAnime() {
            mHandler.removeMessages(INVALIDATE);
            mHandler.removeMessages(DROPBLOCK);
        }

        private static final int INVALIDATE = 1;
        private static final int DROPBLOCK = 2;
        /**
         * Controls the animation using the message queue. Every time we receive an
         * INVALIDATE message, we redraw and place another message in the queue.
         */

        private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case INVALIDATE:
                        invalidate();
                        break;
                    case DROPBLOCK:
                        if (check(block, posx, posy + 1)) {
                            posy++;
                        } else {
                            mergeMatrix(block, posx, posy);
                            clearRows();
                            //消した列数が10以上でリザルト画面に遷移　
                            //その時消した列数の数を渡す
                            if(deleteCount >= 10){
                                Intent intentGoResult = new Intent(getApplication(), Result.class);
                                intentGoResult.putExtra("DELETE_LINE" ,deleteCount);
                                startActivity(intentGoResult);
                                //onStop();
                            }

                            posx = 0; posy = 0;
                            block = blocks[mRand.nextInt(blocks.length)];
                        }

                        invalidate();
                        Message massage = new Message();
                        massage.what = DROPBLOCK;
                        sendMessageDelayed(massage, 500);
                        break;
                }
            }
        };
    }


    //--------------------------------------------------------------------------　
    // フリックされた方向を算出する
    //--------------------------------------------------------------------------
    private class FlickTouchListener implements View.OnTouchListener
    {
        // 最後にタッチされた座標
        private float startTouchX;
        private float startTouchY;

        // 現在タッチ中の座標
        private float nowTouchedX;
        private float nowTouchedY;

        // フリックの遊び部分（最低限移動しないといけない距離）
        private float adjust = 120;

        @Override
        public boolean onTouch( View v_, MotionEvent event_ )
        {
            // タッチされている指の本数
            Log.v( "motionEvent", "--touch_count = "+event_.getPointerCount() );

            // タッチされている座標
            Log.v( "Y", ""+event_.getY() );
            Log.v( "X", ""+event_.getX() );

            switch( event_.getAction() ){

                // タッチ
                case MotionEvent.ACTION_DOWN:
                    Log.v( "motionEvent", "--ACTION_DOWN" );
                    startTouchX = event_.getX();
                    startTouchY = event_.getY();
                    break;

                // タッチ中に追加でタッチした場合
                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.v( "motionEvent", "--ACTION_POINTER_DOWN" );
                    break;

                // スライド
                case MotionEvent.ACTION_MOVE:
                    Log.v( "motionEvent", "--ACTION_MOVE" );
                    break;

                // タッチが離れた
                case MotionEvent.ACTION_UP:
                    Log.v( "motionEvent", "--ACTION_UP" );
                    nowTouchedX = event_.getX();
                    nowTouchedY = event_.getY();

                    if( startTouchY > nowTouchedY ){
                        if( startTouchX > nowTouchedX ){
                            if( ( startTouchY - nowTouchedY ) > ( startTouchX - nowTouchedX ) ){
                                if( startTouchY > nowTouchedY + adjust ){
                                    Log.v( "Flick", "--上" );
                                    // 上フリック時の処理を記述する
                                }
                            }
                            else if( ( startTouchY - nowTouchedY ) < ( startTouchX - nowTouchedX ) ){
                                if( startTouchX > nowTouchedX + adjust ){
                                    Log.v( "Flick", "--左" );
                                    // 左フリック時の処理を記述する
                                }
                            }
                        }
                        else if( startTouchX < nowTouchedX ){
                            if( ( startTouchY - nowTouchedY ) > ( nowTouchedX - startTouchX ) ){
                                if( startTouchY > nowTouchedY + adjust ){
                                    Log.v( "Flick", "--上" );
                                    // 上フリック時の処理を記述する
                                }
                            }
                            else if( ( startTouchY - nowTouchedY ) < ( nowTouchedX - startTouchX ) ){
                                if( startTouchX < nowTouchedX + adjust ){
                                    Log.v( "Flick", "--右" );
                                    // 右フリック時の処理を記述する
                                }
                            }
                        }
                    }
                    else if( startTouchY < nowTouchedY ){
                        if( startTouchX > nowTouchedX ){
                            if( ( nowTouchedY - startTouchY ) > ( startTouchX - nowTouchedX ) ){
                                if( startTouchY < nowTouchedY + adjust ){
                                    Log.v( "Flick", "--下" );
                                    // 下フリック時の処理を記述する
                                }
                            }
                            else if( ( nowTouchedY - startTouchY ) < ( startTouchX - nowTouchedX ) ){
                                if( startTouchX > nowTouchedX + adjust ){
                                    Log.v( "Flick", "--左" );
                                    // 左フリック時の処理を記述する
                                }
                            }
                        }
                        else if( startTouchX < nowTouchedX ){
                            if( ( nowTouchedY - startTouchY ) > (  nowTouchedX - startTouchX  ) ){
                                if( startTouchY < nowTouchedY + adjust ){
                                    Log.v( "Flick", "--下" );
                                    // 下フリック時の処理を記述する
                                }
                            }
                            else if( ( nowTouchedY - startTouchY ) < ( nowTouchedX - startTouchX ) ){
                                if( startTouchX < nowTouchedX + adjust ){
                                    Log.v( "Flick", "--右" );
                                    // 右フリック時の処理を記述する
                                }
                            }
                        }
                    }
                    break;

                // アップ後にほかの指がタッチ中の場合
                case MotionEvent.ACTION_POINTER_UP:
                    Log.v( "motionEvent", "--ACTION_POINTER_UP" );
                    break;

                // UP+DOWNの同時発生(タッチのキャンセル）
                case MotionEvent.ACTION_CANCEL:
                    Log.v( "motionEvent", "--ACTION_CANCEL" );

                    // ターゲットとするUIの範囲外を押下
                case MotionEvent.ACTION_OUTSIDE:
                    Log.v( "motionEvent", "--ACTION_OUTSIDE" );
                    break;
            }
            return( true );
        }
    }


    FieldView mFieldView;

    //setFieldViewがnullの場合インスタンスの生成
    private void setFieldView() {
        if (mFieldView == null) {
            mFieldView = new FieldView(getApplication());
            setContentView(mFieldView);
        }
    }

    //画面生成
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View touch_view = new View( this );
        touch_view.setOnTouchListener( new FlickTouchListener() );
    }

    //画面の再表示
    @Override
    protected void onResume() {
        super.onResume();
        setFieldView();
        mFieldView.initGame();
        mFieldView.startAnime();
        Looper.myQueue().addIdleHandler(new ActivityIdle());
    }

    //現在のActivityがバックグラウンドに移動する時に行われる処理
    @Override
    protected void onPause() {
        super.onPause();
        mFieldView.stopAnime();
    }

    //現在のActivityが見えなくなる時に行われる処理
    @Override
    protected void onStop() {
        super.onStop();
        mFieldView.stopAnime();
    }

    //画面が再表示された時の処理　処理自体の詳細は不明
    class ActivityIdle implements MessageQueue.IdleHandler {
        public ActivityIdle() {
            super();
        }
        public final boolean queueIdle() {
            return false;
        }
    }

}
