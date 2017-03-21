package com.jimin.recovoice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class SpottingService extends Service implements RecognitionListener {

    private static final String TAG = "SpottingService";

    private static final String KWS_SEARCH = "wakeup";
    private static final String KEYPHRASE = "hi android"; // Wakeup Keyword
    private SpeechRecognizer recognizer;
    private Timer timer;
    String text="";
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

        stopService(new Intent(getApplicationContext(), SttService.class));

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(getApplicationContext());
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        text = hypothesis.getHypstr();
        if (text.contains(KEYPHRASE)) { // Correspond Word
            Toast.makeText(getApplicationContext(), "Wakeup", Toast.LENGTH_SHORT).show();

            recognizer.stop();
            recognizer = null;

            timer = new Timer();
            timer.schedule(new MyTimer(), 500);
            MainActivity.waveView.speechStarted();
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        //switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        recognizer.startListening(searchName);
    }

    private void setupRecognizer(File assetsDir) {
        File modelsDir = new File(assetsDir, "models");
        recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                .setDictionary(new File(modelsDir, "dict/cmu07a.dic"))
                .setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
                .getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        switchSearch(KWS_SEARCH);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        if (recognizer != null) {
            recognizer.stop();
            recognizer = null;
        }

        stopSelf();
    }

    private class MyTimer extends TimerTask {
        @Override
        public void run() {
            BrokeLock(getApplicationContext());// Broke LockScreen
            startService(new Intent(getApplicationContext(), SttService.class));// Start Google SttService
            stopSelf();
        }
    }
    public static void BrokeLock(Context context)
    {
        if(!isScreenOn(context))
        {
            Intent intent = new Intent(context, BrokeLockActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        }
    }

    public static boolean isScreenOn(Context context) {
        return ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).isScreenOn();
    }
}