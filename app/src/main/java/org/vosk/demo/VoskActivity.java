package org.vosk.demo;
import org.vosk.demo.tetra.TetraController;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.vosk.Model;
import org.vosk.android.StorageService;

public class VoskActivity extends Activity {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private Model model;
    private AudioRecorder audioRecorder;
    private VoskRecognizer voskRecognizer;
    private IntentParser intentParser;
    
    private TextView resultView;
    private TextView intentView;
    private Button micButton;
    
    private Handler mainHandler;
    private boolean isModelLoaded = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.main);

        mainHandler = new Handler(Looper.getMainLooper());
        
        resultView = findViewById(R.id.result_text);
        resultView.setMovementMethod(new ScrollingMovementMethod());
        
        intentView = findViewById(R.id.intent_text);
        intentParser = new IntentParser();
        
        micButton = findViewById(R.id.recognize_mic);
        micButton.setEnabled(false);
        micButton.setOnClickListener(view -> toggleRecording());

        // Hide unused buttons
        findViewById(R.id.recognize_file).setEnabled(false);
        findViewById(R.id.pause).setEnabled(false);

        resultView.setText("Initializing...");

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            initModel();
        }
    }

    private void initModel() {
        resultView.setText("Loading model...");
        
        new Thread(() -> {
            StorageService.unpack(this, "model-en-us", "model",
                (model) -> {
                    this.model = model;
                    this.voskRecognizer = new VoskRecognizer(model);
                    this.audioRecorder = new AudioRecorder();
                    this.isModelLoaded = true;
                    
                    mainHandler.post(() -> {
                        resultView.setText("✅ Ready! Press button to start recording.");
                        intentView.setText("Status: Ready");
                        intentView.setBackgroundColor(0xFF4CAF50); // Green
                        micButton.setEnabled(true);
                        micButton.setText("START RECORDING");
                    });
                },
                (exception) -> {
                    mainHandler.post(() -> {
                        resultView.setText("❌ Model load failed: " + exception.getMessage());
                        intentView.setText("Status: Error");
                        intentView.setBackgroundColor(0xFFF44336); // Red
                    });
                });
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initModel();
            } else {
                resultView.setText("❌ Microphone permission denied");
                finish();
            }
        }
    }

    private void toggleRecording() {
        if (!isModelLoaded) return;
        
        if (audioRecorder.isRecording()) {
            stopAndRecognize();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        resultView.setText("🎤 Recording... Speak now!");
        intentView.setText("Status: Recording");
        intentView.setBackgroundColor(0xFFF44336); // Red (recording)
        micButton.setText("STOP & RECOGNIZE");
        
        audioRecorder.startRecording();
    }

    private void stopAndRecognize() {
        micButton.setEnabled(false);
        resultView.setText("⏳ Processing audio...");
        intentView.setText("Status: Processing");
        intentView.setBackgroundColor(0xFFFFC107); // Orange
        
        new Thread(() -> {
            // Stop recording and get complete audio buffer
            byte[] audioData = audioRecorder.stopRecording();
            
            // Recognize complete audio in one call
            RecognitionResult result = voskRecognizer.recognize(audioData);
            
            // Parse intent from final text
            ParsedIntent intent = intentParser.parse(result.getText());
            
            // Update UI on main thread
            mainHandler.post(() -> {
                displayResults(result, intent);
                micButton.setEnabled(true);
                micButton.setText("START RECORDING");
            });
        }).start();
    }

    private void displayResults(RecognitionResult result, ParsedIntent intent) {
        String text = result.getText();
        
        if (text.isEmpty()) {
            resultView.setText("❌ No speech detected. Try again.");
            intentView.setText("Status: No input");
            intentView.setBackgroundColor(0xFF9E9E9E);
            return;
        }
        
        resultView.setText("Recognized: \"" + text + "\"\n\nConfidence: " + 
            String.format("%.0f%%", result.getConfidence() * 100));
        
        if (intent != null && !intent.getAction().equals("unknown")) {
            String intentDisplay = String.format(
                "Action: %s | Target: %s | Param: %s | Confidence: %.0f%%",
                intent.getAction(),
                intent.getTarget() != null ? intent.getTarget() : "-",
                intent.getParameter() != null ? intent.getParameter() : "-",
                intent.getConfidence() * 100
            );
            intentView.setText(intentDisplay);
            
            if (intent.getConfidence() > 0.8f) {
                intentView.setBackgroundColor(0xFF4CAF50);
            } else if (intent.getConfidence() > 0.5f) {
                intentView.setBackgroundColor(0xFFFFC107);
            } else {
                intentView.setBackgroundColor(0xFFF44336);
            }
            
            executeCommand(intent);
        } else {
            intentView.setText("Status: No command recognized");
            intentView.setBackgroundColor(0xFF9E9E9E);
        }
    }
    
    private void executeCommand(ParsedIntent intent) {
        new Thread(() -> {
            org.vosk.demo.tetra.TetraController tetra = org.vosk.demo.tetra.TetraController.getInstance();
            org.vosk.demo.tetra.TetraResult result = tetra.execute(intent);
            
            mainHandler.post(() -> {
                if (result.isSuccess()) {
                    android.widget.Toast.makeText(this, result.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    android.widget.Toast.makeText(this, result.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioRecorder != null) {
            audioRecorder.release();
        }
        if (model != null) {
            model.close();
        }
    }
}
