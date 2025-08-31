package com.nic.webdesk;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Set;

/*
----------------------------------------------------------------
Shared Preferences
----------------------------------------------------------------
Get instance
SharedPrefs prefs = SharedPrefs.getInstance(this);

Save data
prefs.setUsername("pat");
prefs.setFlag1(true);

Read data
String user = prefs.getUsername();
boolean f1 = prefs.getFlag1();

Clean alla data
prefs.clearAll();
----------------------------------------------------------------
 */
public class SharedPrefs {

    private static final String PREFS_NAME = "webdesk_prefs";
    private static SharedPrefs instance;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    //private Context context;
    //-------------------------------- Keys
    private static final String KEY_AUTOLOG = "autoLog";                                 // autoLog
    private static final String KEY_SELECTED_TABLE_COLUMNS = "selected_table_columns";   // column select for table
    private static final String KEY_SELECTED_ITEM_ID = "selected_item_id";              // selected item id for table delete row
    private static final String KEY_SELECTED_ITEM_NAME = "selected_item_name";          // selected item name for table delete row


    //-------------------------------- Private constructor for singleton
    private SharedPrefs(Context context) {
        // È meglio usare getApplicationContext() per evitare memory leak legati al contesto di un'Activity
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit(); // Puoi ottenere l'editor quando serve, invece di tenerlo come membro
    }

    //-------------------------------- Get instance singleton
    public static synchronized SharedPrefs getInstance(Context context) { // Aggiunto synchronized per thread-safety
        if (instance == null) {
            instance = new SharedPrefs(context.getApplicationContext()); // Usa getApplicationContext() anche qui
        }
        return instance;
    }


    //============================================= autoLog
    public void setAutoLog(boolean autoLog) {
        // Ottieni l'editor qui se non lo tieni come membro
        // SharedPreferences.Editor localEditor = preferences.edit();
        // localEditor.putBoolean(KEY_AUTOLOG, autoLog).apply();
        editor.putBoolean(KEY_AUTOLOG, autoLog);
        editor.apply(); // o editor.commit() se hai bisogno di sapere subito il risultato
    }

    public boolean getAutoLog() {
        return preferences.getBoolean(KEY_AUTOLOG, false); // false è il valore di default
    }

    //============================================= selectedTableColumns
    /* Salva l'insieme dei nomi dei campi (dbFieldName) delle colonne selezionate.
       Params: selectedColumns, un Set di String contenente i dbFieldName delle colonne selezionate.
    */
    public void setSelectedTableColumns(Set<String> selectedColumns) {
        editor.putStringSet(KEY_SELECTED_TABLE_COLUMNS, selectedColumns);
        editor.apply();
    }

    /* Recupera l'insieme dei nomi dei campi (dbFieldName) delle colonne selezionate.
       Return: un Set di String con i dbFieldName. Ritorna null se nessuna preferenza è stata salvata
       (o un Set vuoto se si preferisce un default diverso, es. new HashSet<>()).
   */
    public Set<String> getSelectedTableColumns() {
        // Se non viene trovato nulla, restituisce null.
        // Cambiare il secondo parametro in 'new HashSet<>()' se si vuole che restituisca
        // un set vuoto invece di null quando la preferenza non esiste.
        return preferences.getStringSet(KEY_SELECTED_TABLE_COLUMNS, null);
    }

    //============================================= Table Id and Name
    /* metodi per catturare dall'RightAdapter public void bind() Id e Name selezionati nelle EditText
       necessario per passare i parametri al medoto delete row nella WebdeskTableActivity
    */
    // Metodo per salvare l'ID dell'item selezionato
    public void setSelectedItemId(int itemId) {
        editor.putInt(KEY_SELECTED_ITEM_ID, itemId);
        editor.apply(); // Usa apply() per operazioni asincrone in background
    }

    // Metodo per recuperare l'ID dell'item selezionato
    // Restituisce un valore di default (es. -1) se non trovato
    public int getSelectedItemId() {
        return preferences.getInt(KEY_SELECTED_ITEM_ID, -1); // -1 indica nessun ID valido salvato
    }

    // Metodo per salvare il Nome dell'item selezionato
    public void setSelectedItemName(String itemName) {
        editor.putString(KEY_SELECTED_ITEM_NAME, itemName);
        editor.apply();
    }

    // Metodo per recuperare il Nome dell'item selezionato
    // Restituisce null o una stringa vuota se non trovato
    public String getSelectedItemName() {
        return preferences.getString(KEY_SELECTED_ITEM_NAME, null);
    }

    // Metodo opzionale per pulire i valori dopo l'uso
    public void clearSelectedItem() {
        editor.remove(KEY_SELECTED_ITEM_ID);
        editor.remove(KEY_SELECTED_ITEM_NAME);
        editor.apply();
    }

    // -------------------------------------------- Clear all
    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}
