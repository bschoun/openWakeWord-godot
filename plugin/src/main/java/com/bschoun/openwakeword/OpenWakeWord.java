package com.bschoun.openwakeword;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;
import org.godotengine.godot.plugin.SignalInfo;

import java.io.File;
import java.util.Set;
import java.util.HashSet;
public class OpenWakeWord extends GodotPlugin {

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

        // Signal we emit when the wakeword is detected, with the index of the detected word
        signals.add(new SignalInfo("wakeword_detected", Integer.class));
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
    public void startDetection(String[] models, int chunkSize) {
        final Activity activity = getActivity();
        activity.runOnUiThread((new Runnable() {
            @Override
            public void run() {
                Log.d(getPluginName(), "Starting detection");
                for (int i=0; i<models.length; i++) {
                    Log.d(getPluginName(), models[i]);
                }
                assetManager = activity.getApplicationContext().getAssets();
                try {
                    modelRunner = new ONNXModelRunner(assetManager, models);
                }
                catch (Exception e) {
                    Log.d(getPluginName(),e.getMessage());
                }
                // Start model. Make sure Godot asks for correct permissions (RECORD_AUDIO) first
                Model model = new Model(modelRunner, chunkSize);
                recorder = new AudioRecorderThread(OpenWakeWord.this, modelRunner, model, chunkSize);
                recorder.start(); // Start recording
            }
        }));
    }

    @UsedByGodot
    public void stopDetection() {
        Log.d(getPluginName(), "Stopping detection");
        recorder.stopRecording();
    }

    public void onDetected(int index) {
        emitSignal("wakeword_detected", index);
    }
 }
