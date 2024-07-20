package com.example.myapplication;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import io.gitctrlx.service.SSML;
import io.gitctrlx.service.TTSService;
import io.gitctrlx.constant.OutputFormat;
import io.gitctrlx.constant.VoiceEnum;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private TTSService ttsService;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        Button convertButton = findViewById(R.id.convertButton);

        ttsService = new TTSService();
        mediaPlayer = new MediaPlayer();

        convertButton.setOnClickListener(v -> {
            String text = editText.getText().toString();
            convertTextToSpeech(text);
        });
    }

    private void convertTextToSpeech(String text) {
        String outputFileName = "tts_output";
        File outputDir = getExternalFilesDir(null);
        if (outputDir != null) {
            String outputPath = new File(outputDir, outputFileName).getAbsolutePath();

            SSML ssml = SSML.builder()
                    .outputFormat(OutputFormat.audio_24khz_48kbitrate_mono_mp3)
                    .synthesisText(text)
                    .outputFile(outputPath)
                    .voice(VoiceEnum.zh_CN_XiaoxiaoNeural)
                    .build();

            new Thread(() -> {
                ttsService.sendText(ssml, new TTSService.TTSCallback() {
                    @Override
                    public void onSuccess(String filePath) {
                        runOnUiThread(() -> playAudio(filePath));
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("TTS", "Error generating audio", e);
                        runOnUiThread(() -> editText.append("\n[Error]: " + e.getMessage()));
                    }
                });
            }).start();
        } else {
            Log.e("TTS", "External files directory not available");
        }
    }

    private void playAudio(String audioFilePath) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e("TTS", "Error playing audio", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsService.close();
        mediaPlayer.release();
    }
}
