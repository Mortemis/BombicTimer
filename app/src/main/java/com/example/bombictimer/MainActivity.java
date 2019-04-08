package com.example.bombictimer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Timer timer = new Timer();
    private final int secondsStep = 10;
    private int minutes = 0;
    private int seconds = 0;
    private boolean isActive = false;

    private final int TONE = ToneGenerator.TONE_PROP_BEEP2;

    String time = "00:00";

    private MediaPlayer plantedPlayer;
    private MediaPlayer explosionPlayer;

    ToneGenerator beeper;

    @SuppressLint("HandlerLeak") //Static handler doesn't work here because of vibrator.
            Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Toast.makeText(MainActivity.this.getApplicationContext(), "Boom!", Toast.LENGTH_SHORT).show();
                long[] pattern = {0, 800};
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(pattern, -1);
                }
            } else if (msg.what == 1) {
                TextView timerView = findViewById(R.id.timerView);
                timerView.setText(time);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        plantedPlayer = MediaPlayer.create(this, R.raw.planted);
        explosionPlayer = MediaPlayer.create(this, R.raw.boom);
        beeper = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
    }

    public void startTimer(View view) {
        if (!isActive) {
            plantedPlayer.start();
            if (minutes != 0 || seconds != 0) {
                timer.schedule(new TimerTask() {
                    int estimatedTime = minutes * 60 + seconds;

                    @Override
                    public void run() {
                        beep();
                        estimatedTime--;
                        seconds = estimatedTime % 60;
                        minutes = estimatedTime / 60;
                        updateTimerView();
                        if (estimatedTime == 0) {
                            onTimerAlarm();
                            this.cancel();
                        }
                    }
                }, 0, 1000);
                isActive = true;
            }
        }
    }

    private void onTimerAlarm() {

        handler.sendEmptyMessage(0);
        explosionPlayer.start();
        isActive = false;
    }

    public void updateTimerView() {
        String min;
        if (minutes / 10 == 0) {
            min = "0" + minutes;
        } else {
            min = "" + minutes;
        }
        String sec;
        if (seconds / 10 == 0) {
            sec = "0" + seconds;
        } else {
            sec = "" + seconds;
        }
        time = min + ":" + sec;
        handler.sendEmptyMessage(1);
    }


    public void incSeconds(View view) {
        if (!isActive) {
            beep();
            seconds += secondsStep;
            if (seconds >= 60) {
                seconds = 0;
                if (minutes < 60) {
                    minutes++;
                }
            }
            updateTimerView();
        }
    }

    public void decSeconds(View view) {
        if (!isActive) {
            beep();
            seconds -= secondsStep;
            if (seconds < 0) {
                seconds = 60 - secondsStep;
                if (minutes > 0) {
                    minutes--;
                } else {
                    seconds = 0;
                }
            }
            updateTimerView();
        }
    }

    public void incMinutes(View view) {
        if (!isActive) {
            beep();
            if (minutes <= 60) {
                minutes++;
            }
            updateTimerView();
        }
    }

    public void decMinutes(View view) {
        if (!isActive) {
            beep();
            if (minutes > 0) {
                minutes--;
            }
            updateTimerView();
        }
    }


    private void beep() {
        beeper.startTone(TONE, 80);
    }
}
