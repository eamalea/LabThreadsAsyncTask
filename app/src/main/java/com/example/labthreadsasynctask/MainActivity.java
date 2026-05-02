package com.example.labthreadsasynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView statusLabel;
    private ProgressBar progressIndicator;
    private ImageView imageView;
    private Button threadLoadBtn, asyncTaskBtn, toastBtn, cancelTaskBtn;
    private Handler uiHandler;
    private HeavyComputationTask currentTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusLabel = findViewById(R.id.statusLabel);
        progressIndicator = findViewById(R.id.progressIndicator);
        imageView = findViewById(R.id.imageView);
        threadLoadBtn = findViewById(R.id.threadLoadBtn);
        asyncTaskBtn = findViewById(R.id.asyncTaskBtn);
        toastBtn = findViewById(R.id.toastBtn);
        cancelTaskBtn = findViewById(R.id.cancelTaskBtn);

        uiHandler = new Handler(Looper.getMainLooper());

        toastBtn.setOnClickListener(v ->
                Toast.makeText(this, "Interface fluide !", Toast.LENGTH_SHORT).show()
        );

        threadLoadBtn.setOnClickListener(v -> loadImageInBackground());

        asyncTaskBtn.setOnClickListener(v -> {
            if (currentTask != null && !currentTask.isCancelled()) {
                currentTask.cancel(true);
            }
            currentTask = new HeavyComputationTask();
            currentTask.execute();
            cancelTaskBtn.setVisibility(View.VISIBLE);
        });

        cancelTaskBtn.setOnClickListener(v -> {
            if (currentTask != null && !currentTask.isCancelled()) {
                currentTask.cancel(true);
                statusLabel.setText("Statut : calcul annulé");
                progressIndicator.setVisibility(View.INVISIBLE);
                cancelTaskBtn.setVisibility(View.GONE);
            }
        });
    }

    private void loadImageInBackground() {
        progressIndicator.setVisibility(View.VISIBLE);
        progressIndicator.setProgress(0);
        statusLabel.setText("Statut : chargement image (background)...");

        new Thread(() -> {
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Chargement de l'image personnalisée depuis drawable
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.my_image);
            uiHandler.post(() -> {
                imageView.setImageBitmap(bitmap);
                progressIndicator.setVisibility(View.INVISIBLE);
                statusLabel.setText("Statut : image chargée avec succès");
            });
        }).start();
    }

    private class HeavyComputationTask extends AsyncTask<Void, Integer, Long> {
        @Override
        protected void onPreExecute() {
            progressIndicator.setVisibility(View.VISIBLE);
            progressIndicator.setProgress(0);
            statusLabel.setText("Statut : calcul intensif en cours...");
        }

        @Override
        protected Long doInBackground(Void... voids) {
            long sum = 0;
            for (int step = 1; step <= 100; step++) {
                if (isCancelled()) break;
                for (int k = 0; k < 250000; k++) {
                    sum += (step * k) % 11;
                }
                publishProgress(step);
            }
            return sum;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressIndicator.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Long result) {
            progressIndicator.setVisibility(View.INVISIBLE);
            statusLabel.setText("Statut : calcul terminé, résultat = " + result);
            cancelTaskBtn.setVisibility(View.GONE);
            currentTask = null;
        }

        @Override
        protected void onCancelled(Long result) {
            progressIndicator.setVisibility(View.INVISIBLE);
            statusLabel.setText("Statut : opération annulée");
            cancelTaskBtn.setVisibility(View.GONE);
            currentTask = null;
        }
    }
}