package com.jimin.recovoice;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SttService extends Service {

    private static final String TAG = "SttService";
    SpeechRecognizer mRecognizer;
    Intent intent_voice;
    int sttrecount=0;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");

        stopService(new Intent(getApplicationContext(), SpottingService.class));

        intent_voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent_voice.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent_voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);
        mRecognizer.startListening(intent_voice);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();

        if (mRecognizer != null) {
            mRecognizer.destroy();
            mRecognizer = null;
        }
        //startService(new Intent(getApplicationContext(), SpottingService.class)); // Start CMUSphinx by Service

        stopSelf();
    }

    private RecognitionListener listener = new RecognitionListener() {

        @Override
        public void onRmsChanged(float rmsdB) {
            // TODO Auto-generated method stub
            Log.i("sequence", "onRmsChanged");
        }

        @Override
        public void onResults(Bundle results) {
            // TODO Auto-generated method stub
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);

            if (rs[0].contains("Bye")) {
                Toast.makeText(getApplicationContext(), "Bye Kim!", Toast.LENGTH_SHORT).show();
                if (mRecognizer != null) {
                    mRecognizer.destroy();
                    mRecognizer = null;
                }

                stopSelf();
            }
            else // Wrong word
            {
                sttrecount++;
                if (sttrecount < 4) {
                    stopSelf();
                    startService(new Intent(getApplicationContext(), SttService.class));
                } else {
                    startService(new Intent(getApplicationContext(), SpottingService.class));
                    stopSelf();

                    sttrecount = 0;
                }
            }
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            // TODO Auto-generated method stub
            Log.i("Sequence", "onReadyForSpeech");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // TODO Auto-generated method stub
            Log.i("Sequence", "onPartialResults");
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // TODO Auto-generated method stub
            Log.i("Sequence", "onEvent");
        }

        @Override
        public void onError(int error) {
            // TODO Auto-generated method stub
            Log.i("Sequence", "onError");

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    Log.i("Error", "Audio Error");
                    startService(new Intent(getApplicationContext(), SpottingService.class));
                    stopSelf();
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    Log.i("Error", "Device Error");
                    startService(new Intent(getApplicationContext(), SpottingService.class));
                    stopSelf();
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    Log.i("Error", "Not Admin");
                    startService(new Intent(getApplicationContext(), SpottingService.class));
                    stopSelf();
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    Log.i("Error", "Not Matching Word");
                    if (mRecognizer != null) {
                        mRecognizer.destroy();
                        mRecognizer = null;
                    }
                    intent_voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent_voice.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                    intent_voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                    mRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                    mRecognizer.setRecognitionListener(listener);
                    mRecognizer.startListening(intent_voice);
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    Log.i("Error", "Overload");
                    startService(new Intent(getApplicationContext(), SpottingService.class));
                    stopSelf();
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    Log.i("Error", "Server Error");
                    startService(new Intent(getApplicationContext(), SpottingService.class));
                    stopSelf();
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    Log.i("Error", "None Input");
                    startService(new Intent(getApplicationContext(), SpottingService.class));
                    stopSelf();
                    break;
            }
        }

        @Override
        public void onEndOfSpeech() {
            // TODO Auto-generated method stub
            Log.i("Sequence", "onEndOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // TODO Auto-generated method stub
            Log.i("Sequence", "onBufferReceived");
        }

        @Override
        public void onBeginningOfSpeech() {
            // TODO Auto-generated method stub
            Log.i("Sequence", "onBeginningOfSpeech");
        }
    };
}