package com.bschoun.openwakeword;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.util.Log;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.godotengine.godot.plugin.SignalInfo;

import java.util.Set;
import java.util.HashSet;
public class OpenWakeWord extends GodotPlugin {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 200;
    ONNXModelRunner modelRunner;
    AssetManager assetManager;

    private AudioRecorderThread recorder;
    public OpenWakeWord(Godot godot) {
        super(godot);
    }

    @Override
    public String getPluginName() {
        return "OpenWakeWord";
    }

    @Override
    public Set<SignalInfo> getPluginSignals() {
        // Define the signals this plugin will emit
        Set<SignalInfo> signals = new HashSet<>();

        // Signal we emit when the wakeword is detected
        signals.add(new SignalInfo("wakeword_detected"));
        return signals;
    }

    @UsedByGodot
    public boolean isDetecting() {
        if (recorder != null) {
            return recorder.isRecording();
        }
        return false;
    }

    @UsedByGodot
    public void startDetection(String model) {
        final Activity activity = getActivity();
        activity.runOnUiThread((new Runnable() {
            @Override
            public void run() {
                Log.d(getPluginName(), "Starting detection");
                Context context = activity.getApplicationContext();
                //Log.d("test", "TESTING 123");
                assetManager = activity.getApplicationContext().getAssets();
                try {
                    modelRunner = new ONNXModelRunner(assetManager, model);
                }
                catch (Exception e) {
                    Log.d(getPluginName(),e.getMessage());
                }
                // TODO: Handle this in Godot instead?
                //if (checkAndRequestPermissions(context)) {
                Model model = new Model(modelRunner);
                recorder = new AudioRecorderThread(OpenWakeWord.this, modelRunner, model);
                recorder.start(); // Start recording
                //}
            }
            /*private boolean checkAndRequestPermissions(Context context) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);
                    return false;
                }
                return true;
            }*/
        }));
    }

    @UsedByGodot
    public void stopDetection() {
        recorder.stopRecording();
    }

    public void onDetected() {
        emitSignal("wakeword_detected");
    }
 }
