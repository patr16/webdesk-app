
package com.nic.webdesk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
//--------------------------------------- import csv file
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;
import com.opencsv.exceptions.CsvValidationException;

/*-------------------------------------------------------------------
DAO - Data Access Object - query on tables

Se in futuro vuoi associare colori e ordine per ciascun Type1/Type2, ti conviene creare una tabella type_meta, tipo così:

sql
Copia
Modifica
CREATE TABLE type_meta (
    type TEXT PRIMARY KEY,
    order1 INTEGER,
    order2 INTEGER,
    textColor TEXT,
    background TEXT
);


-------------------------------------------------------------------*/
public class WebdeskDAO {
    private final WebdeskDbHelper dbHelper;
    private static final String TAG = "WebdeskDAO";
    public WebdeskDAO(Context context) {
        this.dbHelper = new WebdeskDbHelper(context);
    }

    //============================================================== 1 - User table

    //-------------------------------------------------------------- Controlla se la tabella user è vuota
    public boolean isUserTableEmpty() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM user", null);
        boolean isEmpty = true;
        if (cursor != null && cursor.moveToFirst()) {
            isEmpty = cursor.getInt(0) == 0;
        }
        cursor.close();
        db.close();
        return isEmpty;
    }

    //-------------------------------------------------------------- Insert User
    // insert user at first access
    public boolean insertUser(String saltUser, String hashUser, String saltPw, String hashPw) {
        SQLiteDatabase db = null;
        boolean success = false;

        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("id", 1);
            values.put("saltUser", saltUser);
            values.put("hashUser", hashUser);
            values.put("saltPw", saltPw);
            values.put("hashPw", hashPw);
            long result = db.insert("user", null, values);
            success = result != -1;
        } catch (Exception e) {
            Log.e("DB_INSERT_USER", "Errore inserimento user", e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
        return success;
    }

    //-------------------------------------------------------------- Read User

    public Map<String, String> readUser(int userId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Map<String, String> result = new HashMap<>();

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT saltUser, hashUser, saltPw, hashPw FROM user WHERE id = ?",
                    new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                result.put("saltUser", cursor.getString(cursor.getColumnIndexOrThrow("saltUser")));
                result.put("hashUser", cursor.getString(cursor.getColumnIndexOrThrow("hashUser")));
                result.put("saltPw", cursor.getString(cursor.getColumnIndexOrThrow("saltPw")));
                result.put("hashPw", cursor.getString(cursor.getColumnIndexOrThrow("hashPw")));
            }
        } catch (Exception e) {
            Log.e("DB_READ_USER", "Errore lettura user", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return result.isEmpty() ? null : result;
    }

    //-------------------------------------------------------------- Update User
    public boolean updateUser(Map<String, Object> values) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean success = false;

        try {
            db = dbHelper.getWritableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM user WHERE id = 1", null);
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    ContentValues content = new ContentValues();
                    content.put("saltUser", (byte[]) values.get("saltUser"));
                    content.put("hashUser", (String) values.get("hashUser"));
                    content.put("saltPw", (byte[]) values.get("saltPw"));
                    content.put("hashPw", (String) values.get("hashPw"));

                    int affected = db.update("user", content, "id = ?", new String[]{"1"});
                    success = affected > 0;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Errore durante l'aggiornamento utente ID 1", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return success;
    }

    //============================================================== 2 - Log table

    //-------------------------------------------------------------- Insert Log
        public void insertLog(int type, String status, String note) {
        SQLiteDatabase db = null;
        String date = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        date = sdf.format(new Date());

        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("date", date);
            values.put("type", type);
            values.put("status", status);
            values.put("note", note);
            db.insert("log", null, values);
        } catch (Exception e) {
            Log.e("DB_INSERT_LOG", "Errore inserimento log", e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }

    //-------------------------------------------------------------- Read all Log
    public Map<String, Object> readAllLog() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> logList = new ArrayList<>();

        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT * FROM log ORDER BY date DESC";
            cursor = db.rawQuery(query,null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        row.put(cursor.getColumnName(i), cursor.getString(i));
                    }
                    logList.add(row);
                } while (cursor.moveToNext());

                result.put("logs", logList);
            }
        } catch (Exception e) {
            Log.e("DB_USER_SECURITY", "Errore nel recupero dati sicurezza utente ID " + e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return result.isEmpty() ? null : result;
    }

    //-------------------------------------------------------------- Delete all Log
    public void deleteAllLog() {
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();
            db.delete("log", null, null);
            Log.d("DB_LOG", "All log are deleted.");
        } catch (Exception e) {
            Log.e("DB_LOG", "Error in delete log", e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }


    //============================================================== 3 - Webdesk table
    /*
    Copy tables from CSV format to SQLite.
    Required for transferring the data of webdesk table from desktop to mobile
    Separator: "semicolon" or "comma" is allowed.
    .withSeparator(';,') accepts them without making any changes to the code.
    There is no check for the correspondence between the field name in the SQLite database and the column name in the CSV file.
    Import method: public void updateTableWebdesk(SQLiteDatabase db, InputStream inputStream) {
    */
    public void updateTableWebdesk(InputStream inputStream) {
        SQLiteDatabase db = null;
        String message = "";
        String messageError = "";
        //------------------------------ delete all records webdesk table
        deleteAllWebdesk();
        //------------------------------ import from csv file
        try {
            db = dbHelper.getWritableDatabase();
            //Integer typeVar[] = {0, 0, 1, 0, 0, 1, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0};
            ArrayList<Object> fields = new ArrayList<>();
            //int[] numberField = new int[10];
            String[] stringField = new String[19];

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            CSVParserBuilder parserBuilder = new CSVParserBuilder().withSeparator(',').withQuoteChar('"');
            CSVReaderBuilder readerBuilder = new CSVReaderBuilder(inputStreamReader).withCSVParser(parserBuilder.build());
            CSVReader reader = readerBuilder.build();

            reader.readNext(); // Skip header
            int countOk = 0;
            int countNoOk = 0;
            int i = 0;
            ContentValues contentValues = new ContentValues();
            String[] line;
            while ((line = reader.readNext()) != null) {
                i = 0;
                fields.clear();
                for (String value : line) {
                    stringField[i] = value;
                    i++;
                }
                contentValues.clear();
                contentValues.put("Id", Integer.parseInt(stringField[0]));
                contentValues.put("UserCod", Integer.parseInt(stringField[1]));
                contentValues.put("Name", stringField[2]);
                contentValues.put("Url", stringField[3]);
                contentValues.put("Icon", stringField[4]);
                contentValues.put("Type1", stringField[5]);
                contentValues.put("Type2", stringField[6]);
                contentValues.put("Note", stringField[7]);
                contentValues.put("Order1", Integer.parseInt(stringField[8]));
                contentValues.put("Order2", Integer.parseInt(stringField[9]));
                contentValues.put("DateCreate", stringField[10]);
                contentValues.put("DateVisit", stringField[11]);
                contentValues.put("Frequency", Integer.parseInt(stringField[12]));
                contentValues.put("TextColor", stringField[13]);
                contentValues.put("Background", stringField[14]);
                contentValues.put("Flag1", Integer.parseInt(stringField[15]));
                contentValues.put("Flag2", Integer.parseInt(stringField[16]));

                long result;
                String errorMessage = null;

                try {
                    result = db.insertOrThrow("webdesk", null, contentValues);
                } catch (SQLException e) {
                    result = -1;
                    errorMessage = e.getMessage();
                }

                if (result == -1) {
                    System.out.println("@@@ Riga " + countNoOk++ + " errore inserimento riga: " + errorMessage);

                    if (countNoOk < 4) {
                        messageError = messageError + " - " +  countNoOk + " - " + errorMessage + "\n";
                    }
                    System.out.println("@@@@@@@ messageError row " + messageError);

                } else {
                    System.out.println("@@@ Riga " + countOk++ + " inserita con successo");
                }

            }
            reader.close();
            message = "Importazione completata. " +
                    "\n Record inseriti totali: " + countOk +
                    "\n Record non inseriti: " + countNoOk +
                    "\n Record totali: " + (countOk + countNoOk);
            if(messageError != null && ! messageError.isEmpty() ){
                System.out.println("@@@@@@@ messageError " + messageError);
                message = message + "\n -----------------------\n " + "Error\n" + messageError;
            }

        } catch (IOException | CsvValidationException e) {
            message = "Errore nell'inserimento dei dati " + e.getMessage();
            e.printStackTrace();
        }
        if (message.isEmpty()) {
            message = "Importazione completata senza dettagli.";
        }
         insertMessage(message);
    }

    //------------------------------------------------------------ Export Table Webdesk
    // Export in csv file
    public Uri exportTableWebdesk(Context context, boolean onlyNew) throws IOException {
        // Se onlyNew = true => export UserCode = 2 solo
        // Altrimenti esporta tutto (UserCode = 1, 2, null ecc)

        Cursor cursor = null;
        SQLiteDatabase db = null;
        File csvFile = null;
        Uri uri = null;

        // crea il nome file con timestamp
        String timeStamp = new SimpleDateFormat("dd_MM_yyyy-HH_mm", Locale.getDefault()).format(new Date());
        String fileName = "webdesk_export_" + timeStamp + ".csv";

        try {
            db = dbHelper.getReadableDatabase();

            String selection = null;
            String[] selectionArgs = null;

            if (onlyNew) {
                selection = "UserCode = ?";
                selectionArgs = new String[]{"2"};
            }

            cursor = db.query("webdesk", null, selection, selectionArgs, null, null, "Order1 ASC, Order2 ASC");

            if (cursor == null || !cursor.moveToFirst()) {
                throw new IOException("Nessun dato da esportare");
            }

            // Crea il file CSV nella cache
            csvFile = new File(context.getCacheDir(), fileName);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {

                // Scrivi intestazione colonna
                String[] columnNames = cursor.getColumnNames();
                bw.write(TextUtils.join(";", columnNames));
                bw.newLine();

                // Scrivi dati riga per riga
                do {
                    List<String> values = new ArrayList<>();
                    for (String colName : columnNames) {
                        String value = cursor.getString(cursor.getColumnIndex(colName));
                        if (value == null) value = "";
                        // Optional: escape valori con " se ci sono ; o " dentro (dipende da come vuoi gestire CSV)
                        values.add(value.replace("\"", "\"\"")); // raddoppia le " per sicurezza
                    }
                    bw.write("\"" + TextUtils.join("\";\"", values) + "\"");
                    bw.newLine();
                } while (cursor.moveToNext());
            }

            // Ottieni Uri per condivisione
            uri = FileProvider.getUriForFile(context, "com.nic.webdesk.fileprovider", csvFile);

        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return uri;
    }


    //------------------------------------------------------------ typesWebdesk -> type1Webdesk
    // WebdeskType is defined in TypeAdapter, for Type1
    // activity_main -> cards Type1
    // 1 - recyclerview in activity_main.xml
    // 2 - list for select type in activity_edit_site.xml
    public List<WebdeskType> type1Webdesk() {
        List<WebdeskType> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT Type1, COUNT(*) as freq1 " +
                "FROM webdesk " +
                "WHERE Type1 IS NOT NULL AND Type1 != '' " +
                "GROUP BY Type1  ORDER BY Type1";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String type = cursor.getString(0);
                int freq1 = cursor.getInt(1);
                list.add(new WebdeskType(type, "", freq1,0));
                System.out.println("@@@ 334 -  DAO - type1Webdesk type1 e freq1: " + type + " - " + freq1);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    //------------------------------------------------------------ Type2 subcategory Type1 -> type2Webdesk
    // types2ByType1 is defined in TypeAdapter, for Type2
    // activity_main -> Type1 -> select one card Type1 -> activity_main -> cards Type2 of selecterd Type1
    // 1 - recyclerview in activity_main.xml
    // 2 - list for select type in activity_edit_site.xml
    public List<WebdeskType> type2Webdesk(String type1) {
        System.out.println("@@@ 390 - DAO - type2Webdesk type1: " +  type1);

        List<WebdeskType> list = new ArrayList<>();
        if (type1 == null) {
            Log.e("WebdeskDAO", "types2ByType1 called with null type1");
            return list;  // lista vuota, niente query
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT  Type1, Type2, COUNT(*) as freq2 FROM webdesk " +
                        "WHERE Type1 = ? AND Type2 IS NOT NULL AND Type2 != '' " +
                        "GROUP BY Type2 ORDER BY Type2",
                new String[]{type1}
        );

        if (cursor.moveToFirst()) {
            do {
                type1 = cursor.getString(0);
                String type2 = cursor.getString(1);
                int freq2 = cursor.getInt(2);
                list.add(new WebdeskType(type1, type2, 0,freq2));
                System.out.println("@@@ 367 -  DAO - type2Webdesk type1: "  + type1 + " - type2: " + type2 + " - freq2:" + freq2);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    //------------------------------------------------------------ sitesWebdesk for SitesActivity Type1
    // getSitesByType
    public List<WebdeskSite> sitesWebdesk(String type1) {
        List<WebdeskSite> list = new ArrayList<>();

        if (type1 == null || type1.isEmpty()) {
            return list;
        }

        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // Query SQL esplicita con WHERE e ORDER BY
            String sql = "SELECT Id, Name, Url, TextColor, Background, Icon, Frequency " +
                    "FROM webdesk WHERE Type1 = ? ORDER BY Order2 ASC";

            cursor = db.rawQuery(sql, new String[]{ type1 });

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.isNull(0) ? 0 : cursor.getInt(0);
                    String name = cursor.getString(1);
                    String url = cursor.getString(2);
                    String textColor = cursor.getString(3);
                    String background = cursor.getString(4);
                    String icon = cursor.getString(5);
                    int frequency = cursor.isNull(6) ? 0 : cursor.getInt(6);

                    WebdeskSite site = new WebdeskSite(id, name, url, textColor, background, icon, frequency);
                    list.add(site);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            System.out.println("@@@ ERRORE getSitesByType(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }

    //------------------------------------------------------------ sitesWebdesk for SitesActivity Type1 and Type2
    // getSitesByType1 and Type2
    public List<WebdeskSite> sitesWebdesk(String type1, String type2) {
        List<WebdeskSite> list = new ArrayList<>();

        if (type1 == null || type1.isEmpty() || type2 == null || type2.isEmpty()) {
            return list;
        }

        Cursor cursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String sql = "SELECT Id, Name, Url, TextColor, Background, Icon, Frequency " +
                    "FROM webdesk WHERE Type1 = ? AND Type2 = ? ORDER BY Order2 ASC";

            cursor = db.rawQuery(sql, new String[]{ type1, type2 });

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.isNull(0) ? 0 : cursor.getInt(0);
                    String name = cursor.getString(1);
                    String url = cursor.getString(2);
                    String textColor = cursor.getString(3);
                    String background = cursor.getString(4);
                    String icon = cursor.getString(5);
                    int frequency = cursor.isNull(6) ? 0 : cursor.getInt(6);

                    WebdeskSite site = new WebdeskSite(id, name, url, textColor, background, icon, frequency);
                    list.add(site);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            System.out.println("@@@ ERRORE getSitesByType(): " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }

        return list;
    }


    //------------------------------------------------------------ Insert record Webdesk
    public long insertWebdesk(WebdeskSite site) {
        long result = -1;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("Name", site.getName());
            values.put("Url", site.getUrl());
            values.put("Icon", site.getIcon());
            values.put("Type1", site.getType1());
            values.put("Type2", site.getType2());
            values.put("Note", site.getNote());
            values.put("Order1", site.getOrder1());
            values.put("Order2", site.getOrder2());
            values.put("DateCreate", site.getDateCreate());

            // current date in format dd/MM/yyyy
            String dateNow = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
            values.put("DateCreate", dateNow);
            values.put("DateVisit", site.getDateVisit());
            values.put("Frequency", site.getFrequency());
            values.put("TextColor", site.getTextColor());
            values.put("Background", site.getBackground());
            values.put("Flag1", 1);
            values.put("Flaf2", 0);

            result = db.insert("webdesk", null, values);
        } catch (Exception e) {
            Log.e("DB_INSERT", "Errore durante l'inserimento", e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
        return result;
    }

    //------------------------------------------------------------ Read all record Webdesk
    public Cursor readAllWebdesk() {
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("webdesk", null, null, null, null, null, "Order1 ASC, Order2 ASC");
        } catch (Exception e) {
            Log.e("DB_READ_ALL", "Errore durante la lettura dei record", e);
        }
        return cursor;
    }

    //------------------------------------------------------------ Read a record Webdesk
    public WebdeskSite readIdWebdesk(int id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        WebdeskSite site = null;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query("webdesk", null, "Id = ?", new String[]{String.valueOf(id)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow("Id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("Name"));
                String url = cursor.getString(cursor.getColumnIndexOrThrow("Url"));
                String icon = cursor.getString(cursor.getColumnIndexOrThrow("Icon"));
                String type1 = cursor.getString(cursor.getColumnIndexOrThrow("Type1"));
                String type2 = cursor.getString(cursor.getColumnIndexOrThrow("Type2"));
                String note = cursor.getString(cursor.getColumnIndexOrThrow("Note"));
                int order1 = cursor.getInt(cursor.getColumnIndexOrThrow("Order1"));
                int order2 = cursor.getInt(cursor.getColumnIndexOrThrow("Order2"));
                String dateCreate = cursor.getString(cursor.getColumnIndexOrThrow("DateCreate"));
                String dateVisit = cursor.getString(cursor.getColumnIndexOrThrow("DateVisit"));
                int frequency = cursor.getInt(cursor.getColumnIndexOrThrow("Frequency"));
                String textColor = cursor.getString(cursor.getColumnIndexOrThrow("TextColor"));
                String background = cursor.getString(cursor.getColumnIndexOrThrow("Background"));
                int flag1 = cursor.getInt(cursor.getColumnIndexOrThrow("Flag1"));
                int flag2 = cursor.getInt(cursor.getColumnIndexOrThrow("Flag2"));

                site = new WebdeskSite(
                        id, name, url, icon, type1, type2, note,
                        order1, order2, dateCreate, dateVisit,
                        frequency, textColor, background, flag1, flag2
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return site;
    }


    //------------------------------------------------------------ Update a record Webdesk
    public int updateWebdesk(WebdeskSite site) {
        int rowsAffected = 0;
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("Name", site.getName());
            values.put("Url", site.getUrl());
            values.put("Icon", site.getIcon());
            values.put("Type1", site.getType1());
            values.put("Type2", site.getType2());
            values.put("Note", site.getNote());
            values.put("Order1", site.getOrder1());
            values.put("Order2", site.getOrder2());
            values.put("DateCreate", site.getDateCreate());
            values.put("DateVisit", site.getDateVisit());
            values.put("Frequency", site.getFrequency());
            values.put("TextColor", site.getTextColor());
            values.put("Background", site.getBackground());
            values.put("Flag1", site.getFlag1());
            values.put("Flag2", site.getFlag2());

            rowsAffected = db.update("webdesk", values, "Id = ?", new String[]{String.valueOf(site.getId())});
        } catch (Exception e) {
            Log.e("DB_UPDATE", "Errore nell'aggiornamento del record ID " + site.getId(), e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }

        return rowsAffected;
    }


    // Aggiornamento record tramite WebdeskItem Update used ib ActivityWebdeskTable
    public void updateWebdesk(WebdeskItem item) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("Name", item.getName());
            values.put("Url", item.getUrl());
            values.put("Icon", item.getIcon());
            values.put("Type1", item.getType1());
            values.put("Type2", item.getType2());
            values.put("Note", item.getNote());
            if(item.getOrder1() != null)
                values.put("Order1", item.getOrder1());
            if(item.getOrder2() != null)
                values.put("Order2", item.getOrder2());
            values.put("DateCreate", item.getDateCreate());
            values.put("DateVisit", item.getDateVisit());
            if(item.getFrequency() != null)
                values.put("Frequency", item.getFrequency());
            values.put("TextColor", item.getTextColor());
            values.put("Background", item.getBackground());

            if(item.getFlag1() != null)
                values.put("Flag1", item.getFlag1());
            if(item.getFlag2() != null)
                values.put("Flag2", item.getFlag2());

            // WHERE Id = ?
            db.update("webdesk", values, "Id = ?", new String[]{String.valueOf(item.getId())});

        } catch (Exception e) {
            Log.e("DB_UPDATE", "Errore update", e);
        }
    }


    //------------------------------------------------------------ Delete a record Webdesk
    public int deleteWebdesk(int id) {
        int rowsDeleted = 0;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            rowsDeleted = db.delete("webdesk", "Id = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Log.e("DB_DELETE", "Errore durante l'eliminazione del record ID " + id, e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
        return rowsDeleted;
    }

    //------------------------------------------------------------ Delete all records Webdesk
    public void deleteAllWebdesk() {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.delete("webdesk", null, null);
            Log.d("DB_LOG", "All records in webdesk table are deleted.");
        } catch (Exception e) {
            Log.e("DB_LOG", "Error in delete webdesk table", e);
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }

        public List<WebdeskItem> readAllWebdeskList() {
        Cursor cursor = readAllWebdesk();  // usa già l'ordinamento Order1 ASC, Order2 ASC
        List<WebdeskItem> list = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                WebdeskItem item = new WebdeskItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow("Id")));
                item.setUserCod(cursor.isNull(cursor.getColumnIndexOrThrow("UserCod")) ? null :
                        cursor.getInt(cursor.getColumnIndexOrThrow("UserCod")));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow("Name")));
                item.setUrl(cursor.getString(cursor.getColumnIndexOrThrow("Url")));
                item.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("Icon")));
                item.setType1(cursor.getString(cursor.getColumnIndexOrThrow("Type1")));
                item.setType2(cursor.getString(cursor.getColumnIndexOrThrow("Type2")));
                item.setNote(cursor.getString(cursor.getColumnIndexOrThrow("Note")));
                item.setOrder1(cursor.isNull(cursor.getColumnIndexOrThrow("Order1")) ? null :
                        cursor.getInt(cursor.getColumnIndexOrThrow("Order1")));
                item.setOrder2(cursor.isNull(cursor.getColumnIndexOrThrow("Order2")) ? null :
                        cursor.getInt(cursor.getColumnIndexOrThrow("Order2")));
                item.setDateCreate(cursor.getString(cursor.getColumnIndexOrThrow("DateCreate")));
                item.setDateVisit(cursor.getString(cursor.getColumnIndexOrThrow("DateVisit")));
                item.setFrequency(cursor.isNull(cursor.getColumnIndexOrThrow("Frequency")) ? null :
                        cursor.getInt(cursor.getColumnIndexOrThrow("Frequency")));
                item.setTextColor(cursor.getString(cursor.getColumnIndexOrThrow("TextColor")));
                item.setBackground(cursor.getString(cursor.getColumnIndexOrThrow("Background")));
                item.setFlag1(cursor.isNull(cursor.getColumnIndexOrThrow("Flag1")) ? null :
                        cursor.getInt(cursor.getColumnIndexOrThrow("Flag1")));
                item.setFlag2(cursor.isNull(cursor.getColumnIndexOrThrow("Flag2")) ? null :
                        cursor.getInt(cursor.getColumnIndexOrThrow("Flag2")));

                list.add(item);
            }
            cursor.close();
        }
        return list;
    }



    //============================================================== Message table

    //-------------------------------------------------------------- Update Message
    public void updateMessage(String message) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getWritableDatabase();
            // Verifica se esiste almeno un record nella tabella message
            cursor = db.rawQuery("SELECT id FROM message LIMIT 1", null);

            ContentValues contentValues = new ContentValues();
            contentValues.put("message", message);

            if (cursor != null && cursor.moveToFirst()) {
                // Esiste un record -> aggiorna il primo record trovato
                int existingId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int rowsAffected = db.update("message", contentValues, "id = ?", new String[]{String.valueOf(existingId)});
                Log.d("DB_UPDATE", "Messaggio aggiornato, righe modificate: " + rowsAffected);
            } else {
                // Nessun record -> inserisce un nuovo record
                long newId = db.insert("message", null, contentValues);
                Log.d("DB_UPDATE", "Messaggio inserito, nuovo ID: " + newId);
            }
        } catch (Exception e) {
            Log.e("DB_UPDATE", "Errore nella gestione del messaggio", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
    }

    //-------------------------------------------------------------- Insert Message
    public void insertMessage(String message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", 1); // ID fisso per sovrascrivere sempre la stessa riga
        contentValues.put("message", message);
        db.insertWithOnConflict("message", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    //-------------------------------------------------------------- Read Message
    public String readMessage() {
        String message = "";
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT message FROM message WHERE id = 1", null);

            if (cursor != null && cursor.moveToFirst()) {
                message = cursor.getString(0);
            } else {
                message = "Messaggio non trovato.";
            }
        } catch (Exception e) {
            Log.e(TAG, "Errore nella lettura del messaggio", e);
            message = "Errore nella lettura del messaggio.";
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return message;
    }


}
