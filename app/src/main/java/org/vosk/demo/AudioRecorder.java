package org.vosk.demo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AudioRecorder {
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;
    private ByteArrayOutputStream audioBuffer;

    public AudioRecorder() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING);
        audioRecord = new AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL,
            ENCODING,
            bufferSize
        );
        audioBuffer = new ByteArrayOutputStream();
    }

    public void startRecording() {
        if (isRecording) return;
        
        audioBuffer.reset();
        isRecording = true;
        
        audioRecord.startRecording();
        
        recordingThread = new Thread(() -> {
            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING);
            byte[] buffer = new byte[bufferSize];
            
            while (isRecording) {
                int bytesRead = audioRecord.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    audioBuffer.write(buffer, 0, bytesRead);
                }
            }
        });
        
        recordingThread.start();
    }

    public byte[] stopRecording() {
        if (!isRecording) return new byte[0];
        
        isRecording = false;
        
        try {
            if (recordingThread != null) {
                recordingThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        audioRecord.stop();
        
        return audioBuffer.toByteArray();
    }

    public void release() {
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }
}
