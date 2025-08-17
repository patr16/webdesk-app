package com.nic.webdesk;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
Start dell'App
activity_login è la prima view allo start,
version riportata in basso della activity_login.xls
Ver.1.1-05.03.2024
*/
public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private boolean firstAccess = false;
    private WebdeskDAO dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //------------------------------------------------- app version
        TextView textViewVersion = findViewById(R.id.textViewVersion);
        textViewVersion.setText("Ver.1.0-26.07.2025");
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        dbHelper = new WebdeskDAO(this);

        // Check if user table has at least one record
        firstAccess = dbHelper.isUserTableEmpty();
        //------------------------------------------------- first access
        if (firstAccess) {
            Alert.alertDialog(this, "Login", "First access \ncreate credentials", 20000);
        }

        //------------------------------------------------- autofill
        // riempimento automatico dell'username e password
        // nella view.xml: android:autofillHints="username" "password"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
        }

        //------------------------------------------------- autoLog
        // setting to dump the login
        SharedPrefs prefs = SharedPrefs.getInstance(this);
        boolean autoLog = prefs.getAutoLog();
        if(autoLog){
            dbHelper.insertLog(1, "1", "AutoLogin OK");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    //================================================================== onLoginButton
    public void onLoginButton(View view) {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (firstAccess) {
            if (username.isEmpty() || password.isEmpty()) {
                Alert.alertDialog(this, "Login", "Inserisci username e password.", 5000);
                return;
            }

            // --- Encript username e password ---
            String encryptedUsername = LoginEncrypt.encrypt(username);
            String[] userParts = encryptedUsername.split(":");
            String saltUser = userParts[0];
            String hashUser = userParts[1];

            String encryptedPassword = LoginEncrypt.encrypt(password);
            String[] pwParts = encryptedPassword.split(":");
            String saltPw = pwParts[0];
            String hashPw = pwParts[1];

            // --- Inserisci nel DB ---
            dbHelper.insertUser(saltUser, hashUser, saltPw, hashPw);

            // Vai alla activity principale
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // --- Verifica credenziali ---
            Map<String, String> securityMap = dbHelper.readUser(1);
            if (securityMap == null) {
                Alert.alertDialog(this, "Login", "Errore di lettura credenziali.", 5000);
                return;
            }

            boolean userOk = LoginEncrypt.verify(username,
                    securityMap.get("saltUser"),
                    securityMap.get("hashUser"));
            boolean pwOk = LoginEncrypt.verify(password,
                    securityMap.get("saltPw"),
                    securityMap.get("hashPw"));

            if (userOk && pwOk) {
                // Login OK
                dbHelper.insertLog(1, "1", "Login OK");
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Alert.alertDialog(this, "Login", "Credenziali errate.", 5000);
            }
        }
    }

}

