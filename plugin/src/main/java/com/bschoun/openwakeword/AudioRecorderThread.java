package com.bschoun.openwakeword;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

public class AudioRecorderThread extends Thread {
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord audioRecord;
    private boolean isRecording = false;

    ONNXModelRunner modelRunner;
    Model model;

    int chunkSize;

    // Reference to the plugin using this class
    OpenWakeWord plugin;

    AudioRecorderThread (OpenWakeWord plugin, ONNXModelRunner modelRunner, Model model, int chunkSize)
    {
        this.modelRunner=modelRunner;
        this.model=model;
        this.plugin=plugin;
        this.chunkSize = chunkSize;
    }
    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

        // Ensure the buffer size is at least as large as the chunk size needed
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        //int bufferSizeInShorts = 1280; // This is your 'chunk size' in terms of shorts
        int bufferSizeInShorts = chunkSize; // This is your 'chunk size' in terms of shorts
        Log.d("OpenWakeWord", "Min buffer size: " + String.valueOf(minBufferSize));
        Log.d("OpenWakeWord", "Buffer size in shorts: " + String.valueOf(bufferSizeInShorts));
        if (minBufferSize / 2 < bufferSizeInShorts) {
            minBufferSize = bufferSizeInShorts * 2; // Ensure buffer is large enough, adjusting if necessary
            Log.d("OpenWakeWord", "New min buffer size: " + String.valueOf(minBufferSize));
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            // Initialization error handling
            Log.d("OpenWakeWord","State is not AudioRecord.STATE_INITIALIZED, cannot begin recording.");
            return;
        }

        short[] audioBuffer = new short[bufferSizeInShorts]; // Allocate buffer for 'chunk size' shorts
        audioRecord.startRecording();
        isRecording = true;

        while (isRecording) {
            // Reading data from the microphone in chunks
            audioRecord.read(audioBuffer, 0, audioBuffer.length);
            float[] floatBuffer = new float[audioBuffer.length];

            // Convert each short to float
            for (int i = 0; i < audioBuffer.length; i++) {
                // Convert by dividing by the maximum value of short to normalize
                floatBuffer[i] = audioBuffer[i] / 32768.0f; // Normalize to range -1.0 to 1.0 if needed
            }
            String[] res = model.predict_WakeWord(floatBuffer);
            for (int j=0; j<res.length; j++) {
                if (Double.parseDouble(res[j]) > 0.05) {
                    Log.d("OpenWakeWord", "Word detected!");
                    plugin.onDetected(j);
                }
            }
        }

        releaseResources();
    }

    public boolean isRecording() { return isRecording; }

    public void stopRecording() {
        Log.d("OpenWakeWord", "Stopping AudioRecorderThread");
        isRecording = false;
    }

    private void releaseResources() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }
}