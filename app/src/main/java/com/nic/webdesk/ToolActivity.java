package com.nic.webdesk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

public class ToolActivity extends AppCompatActivity {

    private WebdeskDAO dao;
    private ActivityResultLauncher<Intent> pickCsvFileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool);

        dao = new WebdeskDAO(this);

        //---------------------------------------------------------------------- registration picker file
        pickCsvFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            importCsvFromUri(uri);
                        } else {
                            Alert.alertDialog(this, "Errore", "File selezionato non valido.", 10000);
                        }
                    }
                }
        );

        //========================================================= ButtonAutoLog
        SharedPrefs prefs = SharedPrefs.getInstance(this);

        ToggleButton toggle = findViewById(R.id.ButtonAutoLog);
        // Imposta valore iniziale del Toggle in base al salvataggio precedente
        boolean savedState = prefs.getAutoLog();
        toggle.setChecked(savedState);
        // Listener
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Stai in ON
                toggle.setText("Auto Log ON");
                prefs.setAutoLog(true);
            } else {
                // Stai in OFF
                toggle.setText("Auto Log OFF");
                prefs.setAutoLog(false);
            }
        });
    }

    //========================================================= Button View Log
    // Bottone per visualizzare il log
    public void onLogViewButton(View view) {
        Intent intent = new Intent(ToolActivity.this, LogActivity.class);
        startActivity(intent);
        finish();
    }

    //========================================================= Button Import CSV
    // Button to import table webdesk from extern CSV
    public void onTableImportButton(View view) {
        try {
            // Avvia il file picker per selezionare file CSV dalla memoria del dispositivo
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");
            String[] mimeTypes = {
                    "text/csv",
                    "text/comma-separated-values",
                    "application/csv",
                    "text/plain"
            };
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

            pickCsvFileLauncher.launch(intent);

        } catch (Exception e) {
            Alert.alertDialog(this, "Errore", "Errore durante l'apertura del file picker: " + e.getMessage(), 15000);
        }
    }

    //-------------------------------------- importCsvFromUri
    // Metodo di supporto per importare dati da un URI CSV
    private void importCsvFromUri(@NonNull Uri uri) {
        String message = "";
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                Alert.alertDialog(this, "Errore", "Impossibile aprire il file selezionato.", 15000);
                return;
            }

            dao.updateTableWebdesk(inputStream); // importa i dati
            message = dao.readMessage();

            Alert.alertDialog(this,
                    "Importazione completata",
                    message + "\n\nRicorda di cancellare manualmente il file CSV.",
                    20000);

        } catch (IOException e) {
            Alert.alertDialog(this, "Errore di lettura", "Impossibile leggere il file: " + e.getMessage(), 15000);
        } catch (Exception e) {
            Alert.alertDialog(this, "Errore", "Errore durante l'importazione: " + e.getMessage(), 15000);
        }
    }

    //========================================================= Button Export CSV - all data
    // Botton to export sqlite table webdesk  in CSV file, all records
    public void onAllTableExportButton(View view) {
        try {
            // true = solo UserCode=2, false = tutto
            Uri fileUri = dao.exportTableWebdesk(this, false);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Condividi file CSV"));

        } catch (IOException e) {
            Alert.alertDialog(this, "Errore", "Errore durante esportazione: " + e.getMessage(), 15000);
        }
    }

    //========================================================= Button Export CSV - new data
    // Botton to export sqlite table webdesk  in CSV file, only new records with flag1 = 1
    public void onNewTableExportButton(View view) {
        try {
            // true = solo UserCode=2, false = tutto
            Uri fileUri = dao.exportTableWebdesk(this, true);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Condividi file CSV"));

        } catch (IOException e) {
            Alert.alertDialog(this, "Errore", "Errore durante esportazione: " + e.getMessage(), 15000);
        }
    }

    public void onTableButton(View view) {
        Intent intent = new Intent(ToolActivity.this, ActivityWebdeskTable.class);
        startActivity(intent);
        finish();
    }


    //========================================================= ButtonHome
    public void onHomeButton(View view) {
        Intent intent = new Intent(ToolActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
