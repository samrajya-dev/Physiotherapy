package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class ModeActivity extends AppCompatActivity {


    private EditText mEditTextInput;
    private TextView mTextViewCountdown;

    private Button mButtonSet;
    private Button mButtonStartPause;
    private Button mButtonReset;

    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;

     private long mTimeLeftsInMillis;
    private long mStartTimeInMillis;
    private long mEndTime;

    //@SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);

        mEditTextInput = findViewById(R.id.timerSet);
        mTextViewCountdown = findViewById(R.id.timerDisplay);


        mButtonSet = findViewById(R.id.button_set);
        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonReset = findViewById(R.id.button_reset);

        mButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = mEditTextInput.getText().toString();
                if(input.length()==0){
                    Toast.makeText(ModeActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                long millisInput = Long.parseLong(input) * 60000;
                if (millisInput == 0){
                    Toast.makeText(ModeActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }

                setTime(millisInput);
            }
        });

        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(mTimerRunning){
                   pauseTimer();
               }else {
                   startTimer();
               }
            }
        });


        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });

        updateCountDownText();

    }

    private void startTimer(){
        mCountDownTimer = new CountDownTimer(mTimeLeftsInMillis, 1000) {
            @Override
            public void onTick(long l) {
                mTimeLeftsInMillis = l;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                mButtonStartPause.setText("START");
                mButtonStartPause.setVisibility(View.INVISIBLE);
                mButtonReset.setVisibility(View.VISIBLE);
            }
        }.start();

        mTimerRunning = true;
        mButtonStartPause.setText("PAUSE");
        mButtonReset.setVisibility(View.INVISIBLE);
    }

    private void pauseTimer(){
        mCountDownTimer.cancel();
        mTimerRunning = false;
        mButtonStartPause.setText("START");
        mButtonReset.setVisibility(View.VISIBLE);
    }

    private void resetTimer(){

        mTimeLeftsInMillis = mStartTimeInMillis;
        updateCountDownText();
        mButtonReset.setVisibility(View.INVISIBLE);
        mButtonStartPause.setVisibility(View.VISIBLE);
    }

    private void updateCountDownText(){
        int minutes = (int) (mTimeLeftsInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftsInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d",minutes,seconds);
        mTextViewCountdown.setText(timeLeftFormatted);
    }

    private void setTime(long miliseconds){
        mStartTimeInMillis = miliseconds;
        resetTimer();
        closeKeyboard();
    }

    private void closeKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }

    }

    protected void onStop(){
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis",mStartTimeInMillis);
        editor.putLong("millisLeft", mTimeLeftsInMillis);
        editor.putBoolean("timerRunning",mTimerRunning);
        editor.putLong("endTime",mEndTime);

        editor.apply();

        if(mCountDownTimer != null){
            mCountDownTimer.cancel();
        }

    }

    protected void onStart(){
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        mStartTimeInMillis = prefs.getLong("startTimeInMillis",600000);
        mTimeLeftsInMillis = prefs.getLong("millisLeft",mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean("timerRunning",false);

        updateCountDownText();
        updateWatchInterface();

        if(mTimerRunning){
            mEndTime = prefs.getLong("endTime",0);
            mTimeLeftsInMillis = mEndTime - System.currentTimeMillis();

            if(mTimeLeftsInMillis < 0){
                mTimeLeftsInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            }else {
                startTimer();
            }
        }

    }

   private void updateWatchInterface(){
        if(mTimerRunning){
            mEditTextInput.setVisibility(View.INVISIBLE);
            mButtonSet.setVisibility(View.INVISIBLE);
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText("Pause");
        }else {
            mEditTextInput.setVisibility(View.VISIBLE);
            mButtonSet.setVisibility(View.VISIBLE);
            mButtonStartPause.setText("Start");

            if(mTimeLeftsInMillis < 1000){
                mButtonStartPause.setVisibility(View.INVISIBLE);
            }else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

            if(mTimeLeftsInMillis < mStartTimeInMillis){
                mButtonReset.setVisibility(View.VISIBLE);
            }else {
                mButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }


}