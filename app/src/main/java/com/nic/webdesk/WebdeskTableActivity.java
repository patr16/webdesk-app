package com.nic.webdesk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class WebdeskTableActivity extends AppCompatActivity {
    private int currentFocusedId = -1;
    private String currentFocusedName = "";
    private ImageButton buttonHome, buttonChooseColumns, buttonResetSort, buttonDeleteRow, buttonHideKeyboard, buttonWeb;
    private RecyclerView recyclerLeft, recyclerRight;
    private LeftAdapter leftAdapter;
    private RightAdapter rightAdapter;
    private HorizontalScrollView headerScrollView, horizontalScrollView;
    private boolean isSyncingScroll = false;  // <<< flag condiviso
    private List<WebdeskItem> webdeskList;
    private List<WebdeskSite> siteList;
    private WebdeskDAO webdeskDao;
    private String filterType1FromIntent = null; // Per conservare il valore del filtro

    private boolean isSyncingHorizontalScroll = false; // Flag specifico per lo scroll orizzontale

    //--------------------------------------- Variables for dinamic colums
    private LinearLayout rightTableHeaderLayout; // Il LinearLayout dentro headerScrollView
    private String[] allAvailableDisplayNames;
    private String[] allAvailableDbFieldNames;
    private boolean[] checkedFields;       // Stato delle checkbox nel dialogo di selezione
    private final List<ColumnConfig> activeColumnConfigs = new ArrayList<>(); // Colonne attualmente attive
    private static final int MAX_SELECTED_COLUMNS = 6;

    //--------------------------------------- Variables for delete method
    private Integer targetItemIdForDeletion = null;
    private String targeItemNameForDeletion = null;
    private int targetItemPositionForDeletion = -1; // non in uso

    //--------------------------------------- Field Definitions
    // Field del DB che si vuole rendere selezionabili
    // e il loro nome visualizzato di default (puo essere cambiato)
    // L'ordine qui determina l'ordine nell'AlertDialog, quindi cambiando qui l'ordine, questo verrà recepito nelle colonne delatable
    private static final String[][] DYNAMIC_FIELD_DEFINITIONSold = {
            // {DB_FIELD_NAME, DISPLAY_NAME}
            {"UserCod", "User Cod"},
            {"Name", "Name"}, // 'Name' è già mostrato a sinistra, ma potrebbe essere utile averlo anche a destra per modifiche
            {"Url", "URL"},
            {"Icon", "Icon"},
            {"Type1", "Type1"},
            {"Type2", "Type2"},
            {"Note", "Note"},
            {"Order1", "Order1"},
            {"Order2", "Order2"},
            {"DateCreate", "Created"},
            {"DateVisit", "Visited"},
            {"Frequency", "Freq."},
            {"TextColor", "Txt Color"},
            {"Background", "BG Color"}
    };

    private static final String[][] DYNAMIC_FIELD_DEFINITIONS = {
            // {DB_FIELD_NAME, DISPLAY_NAME}
            {"UserCod", "User Cod"},
            {"Name", "Name"}, // 'Name' è già mostrato a sinistra, ma potrebbe essere utile averlo anche a destra per modifiche
            {"Type1", "Type1"},
            {"Type2", "Type2"},
            {"Order1", "Order1"},
            {"Order2", "Order2"},
            {"Url", "URL"},
            {"Icon", "Icon"},
            {"DateCreate", "Created"},
            {"DateVisit", "Visited"},
            {"Frequency", "Freq."},
            {"TextColor", "Txt Color"},
            {"Background", "BG Color"},
            {"Note", "Note"}
    };

    static enum FieldType {
        TEXT, INTEGER, BOOLEAN
    }

    //--------------------------------------- Internal Class ColumnConfig
    static class ColumnConfig {
        public final String dbFieldName;
        public final String displayName;
        public final FieldType fieldType;
        public final int columnWidthDp; // Larghezza suggerita in DP

        public ColumnConfig(String dbFieldName, String displayName, WebdeskTableActivity.FieldType fieldType, int columnWidthDp) {
            this.dbFieldName = dbFieldName;
            this.displayName = displayName;
            this.fieldType = fieldType;
            this.columnWidthDp = columnWidthDp;
        }

        // È buona pratica sovrascrivere equals e hashCode se usi questa classe in Set o come chiavi in Map
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ColumnConfig that = (ColumnConfig) o;
            return dbFieldName.equals(that.dbFieldName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dbFieldName);
        }
    }

    //--------------------------------------- Sort Columns
    private List<SortCriterion> activeSortCriteria = new ArrayList<>();
    private static final int MAX_SORT_LEVELS = 3;

    //========================================================================= on Create
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webdesk_table);

        //------------------------------------------- 0.1 Inizializzazioni
        initializeColumnSelection(); // Nuovo metodo per organizzare
        //--- Button
        buttonHome = findViewById(R.id.buttonHome);
        buttonChooseColumns = findViewById(R.id.buttonChooseColumns);
        buttonResetSort = findViewById(R.id.buttonResetSort);
        buttonDeleteRow = findViewById(R.id.buttonDeleteRow);
        buttonHideKeyboard = findViewById(R.id.buttonHideKeyboard);
        buttonWeb = findViewById(R.id.buttonWeb);
        //--- RecyclerViews e ScrollViews
        recyclerLeft = findViewById(R.id.recyclerViewLeft);
        recyclerRight = findViewById(R.id.recyclerViewRight);
        headerScrollView = findViewById(R.id.headerScrollView);
        horizontalScrollView = findViewById(R.id.horizontalScrollView);
        //--- Nuovi findViewById
        rightTableHeaderLayout = findViewById(R.id.rightTableHeaderLayout);
        //--- Recupero dati dal DB
        webdeskDao = new WebdeskDAO(this);

        //------------------------------------------- 0.2 - Seleziona Dati DAO con/senza filtro
        // button MainActivity -> tutti i dati
        // button Type2Activity -> filtra per Type1
        // webdeskList the original of data

        //--- Controlla se è stato passato un filtro dall'intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("FILTER_TYPE1")) {
            filterType1FromIntent = intent.getStringExtra("FILTER_TYPE1");
            //Log.d("WebdeskTableActivity", "Ricevuto filtro Type1: " + filterType1FromIntent);
        } else {
            //Log.d("WebdeskTableActivity", "Nessun filtro Type1 ricevuto, si caricheranno tutti i dati.");
        }

        // Carica i dati chiamando il metodo DAO modificato,
        // passando il valore del filtro (che sarà null se non presente)
        webdeskList = webdeskDao.readAllWebdeskList(filterType1FromIntent);
/*
        if (webdeskList.isEmpty() && filterType1FromIntent != null) {
            Log.w("WebdeskTableActivity", "Nessun dato trovato per il filtro Type1: " + filterType1FromIntent);
            // Potresti voler mostrare un messaggio all'utente qui
        } else if (webdeskList.isEmpty()) {
            Log.w("WebdeskTableActivity", "Nessun dato trovato nel database.");
            // Potresti voler mostrare un messaggio all'utente qui
        }

        */



        //------------------------------------------- 1.1 Setup RecyclerView Left
        recyclerLeft.setLayoutManager(new LinearLayoutManager(this));
        leftAdapter = new LeftAdapter(); // costruttore vuoto
        recyclerLeft.setAdapter(leftAdapter);
        if (webdeskList != null && !webdeskList.isEmpty()) {
            leftAdapter.setData(webdeskList);
        }

        //------------------------------------------- 1.2 Setup RecyclerView Right
        recyclerRight.setLayoutManager(new LinearLayoutManager(this));
        RightAdapter.OnFocusChangedListener focusListener = (id, name) -> {
            currentFocusedId = id;
            currentFocusedName = name;
        };

        // Definisci il listener per il salvataggio dei dati.
        // Questo listener viene chiamato dall'adapter quando un dato è stato modificato e salvato nel DB,
        // permettendo all'Activity di reagire (es. ricaricando i dati anche per il LeftAdapter).
        RightAdapter.OnDataSavedListener dataSavedListener = () -> {
            refreshDataAndUpdateAdapters(); // Chiama il tuo metodo per aggiornare entrambi gli adapter
        };

        // Inizializza l'adapter
        rightAdapter = new RightAdapter(new ArrayList<>(), webdeskDao, focusListener, dataSavedListener, this);
        recyclerRight.setAdapter(rightAdapter); // Assegna l'adapter AL RecyclerView, necessaria per il delete row

        //------------------------------------------- 2 - Imposta i Listener sugli Adapter
        //--------------- 2.1 - Delete Row - 1 target item for deletion
        rightAdapter.setOnItemClickListener((item, position) -> {
            targetItemIdForDeletion = item.getId();
            targeItemNameForDeletion=item.getName();
            targetItemPositionForDeletion = position; // non in uso, per selezionare  la posizione attuale dopo un delete row
        });

        //--------------- 2.2 - Imposta l'adapter e popola i dati
        recyclerRight.setAdapter(rightAdapter);
        // rightAdapter.setData(webdeskList); // Rimandiamo questa chiamata

        //---------------- if che potrebbe essere escluso
        if (webdeskList != null && !webdeskList.isEmpty()) {
        } else if (webdeskList == null) {
            webdeskList = new ArrayList<>(); // Evita NPE se fosse null
        }

        // --- Configurazione iniziale Colonne Destra e Dati ---
        setupInitialRightTable();

        //------------------------------------------- 3. Setup Button e Altri Listener UI ---
        // --- Button Home
        buttonHome.setOnClickListener(view -> {
            Intent intent1 = new Intent(WebdeskTableActivity.this, MainActivity.class);
            startActivity(intent1);
            finish();
        });

        // --- Listener per Configurazione Colonne ---
        buttonChooseColumns.setOnClickListener(v -> showSelectColumnsDialog());
        // Popola l'adapter con i dati effettivi
        rightAdapter.setData(webdeskList); // Questo userà DiffUtil per aggiornare la vista

        // --- Button Reset Sort
        buttonResetSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleResetSort();
            }
        });

        // --- Button Delete Row
        // Per sicurezza, pulisci una possibile selezione precedente all'avvio dell'activity:
        SharedPrefs.getInstance(this).clearSelectedItem();
        buttonDeleteRow.setOnClickListener(v -> {
            handleDeleteRow();
        });

        // --- Button Hide Keyboard
        buttonHideKeyboard.setOnClickListener(v -> {
            hideKeyboard();
        });

        // --- Button Web
        buttonWeb.setOnClickListener(v -> {
            gotoWeb();
        });
        //------------------------------------------- 4 - Caricamento e Visualizzazione Dati Iniziali
        //loadInitialData();
        //loadAndDisplayInitialData();

        //------------------------------------------- 5. Sincronizzazione scroll verticale e orizzontale
        syncVerticalScroll(recyclerRight, recyclerLeft);
        syncVerticalScroll(recyclerLeft, recyclerRight);
        syncHorizontalScroll();
    }

    // =========================================================================================
    // Metodi per la gestione delle colonne dinamiche
    // =========================================================================================

    // ------------------------------------------------------------------------ Method initializeColumnSelection
    private void initializeColumnSelection() {
        // --- PASSO 1: CARICAMENTO DELLE PREFERENZE ---

        //--------------------------------------------- Load saved preferences
        // Carica i nomi dei campi DB salvati. Restituisce null se la chiave non esiste.
        SharedPrefs prefs = SharedPrefs.getInstance(getApplicationContext());
        Set<String> savedSelectedDbFieldNames = prefs.getSelectedTableColumns();

        int numFields = DYNAMIC_FIELD_DEFINITIONS.length;
        allAvailableDisplayNames = new String[numFields];
        allAvailableDbFieldNames = new String[numFields];
        checkedFields = new boolean[numFields];

        for (int i = 0; i < numFields; i++) {
            allAvailableDbFieldNames[i] = DYNAMIC_FIELD_DEFINITIONS[i][0];  // Nome del campo DB
            allAvailableDisplayNames[i] = DYNAMIC_FIELD_DEFINITIONS[i][1];  // Nome visualizzato
        }

        // Imposta i primi MAX_SELECTED_COLUMNS (o meno se ce ne sono meno disponibili) come selezionati di default
        activeColumnConfigs.clear();

        // --- PASSO 3: DECIDERE SE USARE LE PREFERENZE O I DEFAULT ---
        // Controlla se sono state trovate delle preferenze salvate e se il set non è vuoto.
        if (savedSelectedDbFieldNames != null && !savedSelectedDbFieldNames.isEmpty()) {
            // --- CASO A: PREFERENZE TROVATE E NON VUOTE ---
            //Log.d("WebdeskTableActivity", "Caricamento colonne da SharedPreferences: " + savedSelectedDbFieldNames);
            int currentSelectedCount = 0; // Contatore per non superare MAX_SELECTED_COLUMNS

            // Itera su TUTTE le colonne definite in DYNAMIC_FIELD_DEFINITIONS.
            // Questo è importante per popolare correttamente l'array `checkedFields`
            // per il dialogo di selezione e per mantenere un ordine consistente se possibile.
            for (int i = 0; i < numFields; i++) {
                String currentDbFieldName = allAvailableDbFieldNames[i];

                // Verifica se il nome del campo DB corrente è presente nelle preferenze salvate
                // E se non abbiamo ancora raggiunto il numero massimo di colonne selezionabili.
                if (savedSelectedDbFieldNames.contains(currentDbFieldName) && currentSelectedCount < MAX_SELECTED_COLUMNS) {
                    checkedFields[i] = true; // Questa colonna era selezionata e lo sarà ancora.
                    activeColumnConfigs.add(new ColumnConfig(
                            currentDbFieldName,
                            allAvailableDisplayNames[i], // Usa il nome visualizzato corrispondente
                            getFieldType(currentDbFieldName),       // Metodo helper per ottenere il tipo
                            getDefaultColumnWidthDp(currentDbFieldName) // Metodo helper per la larghezza
                    ));
                    currentSelectedCount++; // Incrementa il contatore delle colonne selezionate
                } else {
                    // Se la colonna non era nelle preferenze o abbiamo superato il limite,
                    // assicurati che sia marcata come non selezionata.
                    checkedFields[i] = false;
                }
            }

        } else {
            // --- CASO B: NESSUNA PREFERENZA TROVATA (o set vuoto, o savedSelectedDbFieldNames è null) ---
            // In questo caso, applichiamo la selezione di colonne di default.
            //Log.d("WebdeskTableActivity", "Nessuna preferenza per le colonne trovata o set vuoto. Applico i valori di default.");
            applyDefaultColumnSelection(numFields); // Chiamata al metodo helper
        }

    } // Fine del metodo initializeColumnSelection (per ora, manca il metodo helper)

    // ------------------------------------------------------------------------ Method DefaultColumnSelection
    /**
     * Metodo helper per applicare la selezione di colonne di default.
     * Questo metodo viene chiamato quando non ci sono preferenze utente salvate
     * per le colonne da visualizzare, oppure se le preferenze salvate sono invalide/vuote.
     * Popola `activeColumnConfigs` con le colonne di default e aggiorna `checkedFields`.
     *
     * @param numTotalFields Il numero totale di campi/colonne disponibili
     *                       definiti in DYNAMIC_FIELD_DEFINITIONS.
     */
    private void applyDefaultColumnSelection(int numTotalFields) {
        //Log.d("WebdeskTableActivity", "applyDefaultColumnSelection chiamato.");

        // activeColumnConfigs dovrebbe essere già stata pulita da initializeColumnSelection
        // prima di chiamare questo metodo.

        // Determina quante colonne selezionare di default,
        // senza superare MAX_SELECTED_COLUMNS o il numero totale di campi disponibili.
        int defaultSelectedCount = Math.min(MAX_SELECTED_COLUMNS, numTotalFields);

        // Itera su tutti i campi disponibili.
        for (int i = 0; i < numTotalFields; i++) {
            // Se l'indice corrente è minore del numero di colonne da selezionare di default,
            // allora questa colonna sarà una di quelle di default.
            if (i < defaultSelectedCount) {
                checkedFields[i] = true; // Marca questa colonna come selezionata per il dialogo.

                // Recupera il nome del campo del database e il nome visualizzato.
                String dbFieldName = allAvailableDbFieldNames[i];
                String displayName = allAvailableDisplayNames[i];

                // Aggiungi la configurazione della colonna alla lista delle colonne attive.
                activeColumnConfigs.add(new ColumnConfig(
                        dbFieldName,
                        displayName,
                        getFieldType(dbFieldName),       // Metodo helper per ottenere il tipo
                        getDefaultColumnWidthDp(dbFieldName) // Metodo helper per la larghezza
                ));
            } else {
                // Se la colonna non è tra quelle di default,
                // assicurati che sia marcata come non selezionata.
                checkedFields[i] = false;
            }
        }
    } // Fine del metodo applyDefaultColumnSelection

    // ------------------------------------------------------------------------ Method setupInitialRightTable
    private void setupInitialRightTable() {
        // 1. Aggiorna l'header della tabella di destra
        updateRightTableHeader();
        //updateRightTableHeader(activeColumnConfigs);

        // 2. Passa la configurazione delle colonne all'adapter di destra
        // (Creeremo questo metodo in RightAdapter più tardi)
        if (rightAdapter != null) {
            rightAdapter.setColumnConfiguration(activeColumnConfigs); // NUOVO METODO DA IMPLEMENTARE IN RightAdapter
        }

        // 3. Popola l'adapter con i dati effettivi
        if (rightAdapter != null && webdeskList != null) {
            rightAdapter.setData(webdeskList);
        } else if (webdeskList == null) {
            webdeskList = new ArrayList<>(); // Evita NPE
            if (rightAdapter != null) {
                rightAdapter.setData(webdeskList); // Passa lista vuota
            }
        }
    }

    // ------------------------------------------------------------------------ Method getFieldType
    private FieldType getFieldType(String dbFieldName) {
        // Questa è una mappatura basata sui tipi che hai in WebdeskItem
        // Dovrai adattarla esattamente ai tuoi campi
        switch (dbFieldName) {
            case "UserCod":
            case "Order1":
            case "Order2":
            case "Frequency":
                return FieldType.INTEGER;
            case "Flag1":
            case "Flag2":
                return FieldType.BOOLEAN; // Tratteremo Integer 0/1 come Boolean
            case "Name":
            case "Url":
            case "Icon":
            case "Type1":
            case "Type2":
            case "Note":
            case "DateCreate":
            case "DateVisit":
            case "TextColor":
            case "Background":
                return FieldType.TEXT;
            default:
                return FieldType.TEXT; // Default o lancia un'eccezione se il campo non è riconosciuto
        }
    }

    // ------------------------------------------------------------------------ Method getDefaultColumnWidthDp
    private int getDefaultColumnWidthDp(String dbFieldName) {
        // Fornisci larghezze di default in DP per ogni campo.
        // Queste sono solo stime, puoi aggiustarle.
        switch (dbFieldName) {
            case "UserCod":
                return 80;
            case "Name":
                return 150;
            case "Url":
                return 100;
            case "Icon":
                return 100;
            case "Type1":
                return 80;
            case "Type2":
                return 60;
            case "Note":
                return 100;
            case "Order1":
            case "Order2":
                return 60;
            case "DateCreate":
            case "DateVisit":
                return 100;
            case "Frequency":
                return 70;
            case "TextColor":
            case "Background":
                return 100;
            case "Flag1":
            case "Flag2":
                return 60; // Larghezza per CheckBox
            default:
                return 120; // Larghezza di default generica
        }
    }

    // ------------------------------------------------------------------------ Method showSelectColumnsDialog
    private void showSelectColumnsDialog() {
        // Copia lo stato corrente delle checkbox per permettere l'annullamento
        final boolean[] tempCheckedFields = Arrays.copyOf(checkedFields, checkedFields.length);
        int currentSelectedCount = 0;
        for (boolean checked : tempCheckedFields) {
            if (checked) {
                currentSelectedCount++;
            }
        }
        final int initialSelectedCount = currentSelectedCount;


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleziona Colonne (Max " + MAX_SELECTED_COLUMNS + ")");
        builder.setMultiChoiceItems(allAvailableDisplayNames, tempCheckedFields, (dialog, which, isChecked) -> {
            // Conta quante sono selezionate
            int count = 0;
            for (int i = 0; i < tempCheckedFields.length; i++) {
                if (((AlertDialog) dialog).getListView().isItemChecked(i)) {
                    count++;
                }
            }

            if (count > MAX_SELECTED_COLUMNS) {
                // Se si supera il massimo, deseleziona l'ultimo elemento cliccato
                Toast.makeText(WebdeskTableActivity.this, "Massimo " + MAX_SELECTED_COLUMNS + " colonne selezionabili.", Toast.LENGTH_SHORT).show();
                ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                // Aggiorna tempCheckedFields[which] perché il listener non lo fa automaticamente in questo caso
                tempCheckedFields[which] = false;
            } else {
                tempCheckedFields[which] = isChecked;
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Applica le modifiche da tempCheckedFields a checkedFields
            System.arraycopy(tempCheckedFields, 0, checkedFields, 0, tempCheckedFields.length);
            Set<String> selectedDbFieldNamesToSave = new HashSet<>();

            activeColumnConfigs.clear();
            for (int i = 0; i < allAvailableDisplayNames.length; i++) {
                if (checkedFields[i]) {
                    String dbFieldName = allAvailableDbFieldNames[i];
                    String displayName = allAvailableDisplayNames[i];
                    activeColumnConfigs.add(new ColumnConfig(
                            dbFieldName,
                            displayName,
                            getFieldType(dbFieldName),
                            getDefaultColumnWidthDp(dbFieldName)
                    ));
                    selectedDbFieldNamesToSave.add(dbFieldName);
                }
            }

            //-------------------------------------------- Salva le preferenze
            SharedPrefs.getInstance(getApplicationContext()).setSelectedTableColumns(selectedDbFieldNamesToSave);
            // Oppure SharedPrefs.getInstance(this).setSelectedTableColumns(selectedDbFieldNamesToSave);

            // Aggiorna l'adapter della tabella di destra con la nuova configurazione
            if (rightAdapter != null) {
                // È buona pratica passare una nuova lista o una copia per triggerare correttamente
                // gli aggiornamenti se l'adapter si basa sull'identità della lista.
                rightAdapter.setColumnConfiguration(new ArrayList<>(activeColumnConfigs)); // Passa una copia
            }


            // Aggiorna l'header e l'adapter di destra
            // updateRightTableHeader(activeColumnConfigs);
            updateRightTableHeader();
            if (rightAdapter != null) {
                rightAdapter.setColumnConfiguration(activeColumnConfigs);
                // Non è necessario chiamare setData qui se la lista degli item non è cambiata,
                // l'adapter dovrebbe aggiornare solo la sua struttura di colonne.
                // Se setData è necessario per forzare un rebind completo con la nuova struttura:
                // rightAdapter.setData(webdeskList);
            }
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> {
            // Nessuna modifica, tempCheckedFields non viene usato per aggiornare checkedFields
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // ------------------------------------------------------------------------ Method updateRightTableHeader
    private void updateRightTableHeader() {
        if (rightTableHeaderLayout == null || activeColumnConfigs == null || activeColumnConfigs.isEmpty()) {
            if (rightTableHeaderLayout != null) {
                rightTableHeaderLayout.removeAllViews(); // Pulisci comunque se possibile
            }
            return;
        }
        rightTableHeaderLayout.removeAllViews();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        //----------------------------------------------------------- header destro - parte 1
        /* gli header a sinistra e a destra devono avere altezza di due righe,
        poichè nelle colonne di destra nell'ordinamento può esserci il triangolo di ordinamento e il numero di priorità
        che fanno slittare il testo in seconda riga.
        Nella colonna di sinistar si definisce direttamente l'altezza dell'header nel file xml
        */
        int headerRowHeightDp = 50;
        int headerRowHeightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                headerRowHeightDp,
                displayMetrics
        );
        ViewGroup.LayoutParams layoutParams = rightTableHeaderLayout.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, headerRowHeightPx);
        } else {
            layoutParams.height = headerRowHeightPx;
        }
        rightTableHeaderLayout.setLayoutParams(layoutParams);
        //-----------------------------------------------------------

        for (int i = 0; i < activeColumnConfigs.size(); i++) {
            ColumnConfig config = activeColumnConfigs.get(i);
            TextView headerTextView = new TextView(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, config.columnWidthDp, displayMetrics),
                    LinearLayout.LayoutParams.MATCH_PARENT
            );

            if (i < activeColumnConfigs.size() -1) { // Aggiungi margine se non è l'ultimo
                params.setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, displayMetrics));
            }

            headerTextView.setLayoutParams(params);
            int paddingInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, displayMetrics); // Se 8 è DP
            headerTextView.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);

            //----------------------------------------------------------- header destro - parte 2
            // Stile della cella TextView dell'header
            headerTextView.setGravity(Gravity.CENTER);              // Centra orizzontalmente E verticalmente
            headerTextView.setMaxLines(2);                          // Permetti fino a 2 righe
            headerTextView.setEllipsize(TextUtils.TruncateAt.END);  // Aggiungi ellissi se il testo è più lungo di 2 righe
            //-----------------------------------------------------------

            headerTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.bluBackgroundColor)); // Esempio table_header_background
            headerTextView.setTextColor(ContextCompat.getColor(this, R.color.black));       // Esempio table_header_text
            headerTextView.setTypeface(null, Typeface.BOLD);

            //Imposta testo header con indicatori di ordinamento
            String headerText = config.displayName;
            SortCriterion existingCriterion = findSortCriterion(config.dbFieldName);
            if (existingCriterion != null) {
                String arrow = existingCriterion.isAscending() ? " \u25B2" : " \u25BC"; // Triangolo su/giù
                headerText += arrow + existingCriterion.getPriority();
            }
            headerTextView.setText(headerText);

            final String dbFieldNameForSort = config.dbFieldName; // Per usarlo nella lambda
            headerTextView.setOnClickListener(v -> {
                handleHeaderClick(dbFieldNameForSort);
            });

            rightTableHeaderLayout.addView(headerTextView);
        }
    }

    // ------------------------------------------------------------------------ Method SortCriterion
    // metodo helper: trova un criterio di ordinamento esistente
    private SortCriterion findSortCriterion(String dbFieldName) {
        for (SortCriterion criterion : activeSortCriteria) {
            if (criterion.getDbFieldName().equals(dbFieldName)) {
                return criterion;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------ Method syncVerticalScroll
    private void syncVerticalScroll(final RecyclerView source, final RecyclerView target) {
        source.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                if (!isSyncingScroll && dy != 0) {
                    isSyncingScroll = true;
                    target.scrollBy(0, dy);
                    isSyncingScroll = false;
                }
            }
        });
    }

    // ------------------------------------------------------------------------ Method syncHorizontalScroll
    // Scroll orizzontale header <-> dati
    private void syncHorizontalScroll() {
        // Assicurati che horizontalScrollView e headerScrollView siano inizializzati in onCreate
        if (horizontalScrollView == null || headerScrollView == null) {
            return;
        }

        horizontalScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!isSyncingHorizontalScroll) { // isSyncingHorizontalScroll è il flag booleano membro della classe
                isSyncingHorizontalScroll = true;
                headerScrollView.scrollTo(scrollX, 0);
                isSyncingHorizontalScroll = false;
            }
        });

        headerScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!isSyncingHorizontalScroll) {
                isSyncingHorizontalScroll = true;
                horizontalScrollView.scrollTo(scrollX, 0);
                isSyncingHorizontalScroll = false;
            }
        });
    }

    // ------------------------------------------------------------------------ Method Aggiornamento dati
    private void refreshDataAndUpdateAdapters() {
        webdeskList = webdeskDao.readAllWebdeskList(filterType1FromIntent);
        if (leftAdapter != null) {
            leftAdapter.setData(webdeskList);
        }
        if (rightAdapter != null) {
            // Ora l'adapter di destra ha già la sua configurazione di colonne.
            // Il metodo setData dovrebbe solo aggiornare gli item.
            rightAdapter.setData(webdeskList);
        }
    }


    // ------------------------------------------------------------------------ Method Handle Header Click
    // Click sull'header della colonna per impostare l'ordinamento
    private void handleHeaderClick(String clickedDbFieldName) {
        SortCriterion existingCriterion = findSortCriterion(clickedDbFieldName);

        if (existingCriterion != null) {
            //--- CASO 1: La colonna è già un criterio di ordinamento ---
            if (existingCriterion.getPriority() == 1) {
                // Se è il primo criterio (priorità 1), inverti solo la direzione
                existingCriterion.setAscending(!existingCriterion.isAscending());
            } else {
                // Se non è il primo, promuovilo a priorità 1.
                // Tutti i criteri con priorità inferiore a quella vecchia del criterio cliccato,
                // e che non sono il criterio cliccato stesso, scalano la loro priorità (+1).
                int oldPriorityOfClicked = existingCriterion.getPriority();
                for (SortCriterion criterion : activeSortCriteria) {
                    if (criterion != existingCriterion && criterion.getPriority() < oldPriorityOfClicked) {
                        criterion.setPriority(criterion.getPriority() + 1);
                    }
                }
                existingCriterion.setPriority(1);
                // Generalmente quando si promuove un criterio a P1, si resetta la direzione ad ascendente
                existingCriterion.setAscending(true);
            }
        } else {
            //--- CASO 2: La colonna non è ancora un criterio di ordinamento
            // La colonna cliccata diventa il nuovo criterio di Priorità 1.
            // Gli altri criteri esistenti scalano la loro priorità (P1->P2, P2->P3).
            // Se si raggiunge MAX_SORT_LEVELS, il criterio che era P3 viene rimosso.

            //--- Rimuovi il criterio che attualmente ha priorità MAX_SORT_LEVELS, se esiste e siamo al limite
            if (activeSortCriteria.size() == MAX_SORT_LEVELS) {
                activeSortCriteria.removeIf(criterion -> criterion.getPriority() == MAX_SORT_LEVELS);
            }

            //--- Scala la priorità di tutti i criteri esistenti
            for (SortCriterion criterion : activeSortCriteria) {
                criterion.setPriority(criterion.getPriority() + 1);
            }

            //--- Aggiungi il nuovo criterio come Priorità 1, ascendente di default
            activeSortCriteria.add(new SortCriterion(clickedDbFieldName, true, 1));
            //Log.d("SortLogic", "Nuovo criterio aggiunto come P1: " + clickedDbFieldName);
        }

        //---Rinormalizza le priorità per assicurare che siano 1, 2, 3...
        normalizeSortPriorities();

        //--- Stampa lo stato attuale per debug
        for(SortCriterion sc : activeSortCriteria) {
            //Log.d("SortLogic", "Post-HandleClick: " + sc.getDbFieldName() + " P" + sc.getPriority() + (sc.isAscending() ? " ASC" : " DESC"));
        }

        //--- Aggiornamenti post modifica criteri
        updateRightTableHeader(); // Aggiorna la visualizzazione degli header
        System.out.println("@@@ 713 - prima di sortData()");
        sortData();               // Riordina i dati nella tabella e aggiorna l'adapter
        System.out.println("@@@ 713 - dopo sortData()");
    }

    // ------------------------------------------------------------------------ Method helper - Nnormalize Sort Priorities
    /* Normalizza le Priorità
    Assicura che le priorità siano 1, 2, ...N senza buchi e nell'ordine corretto
    basato sull'ordine in cui appaiono nella lista dopo le manipolazioni.
    Questa versione è più semplice: riordina la lista per priorità e poi riassegna 1, 2, 3...*/
    private void normalizeSortPriorities() {
        if (activeSortCriteria.isEmpty()) {
            return;
        }

        // Ordina i criteri in base alla loro priorità attuale (ASC)
        // Questo è importante perché le manipolazioni in handleHeaderClick potrebbero averle
        // temporaneamente messe fuori ordine rispetto alla numerazione desiderata 1, 2, 3.
        Collections.sort(activeSortCriteria, new Comparator<SortCriterion>() {
            @Override
            public int compare(SortCriterion sc1, SortCriterion sc2) {
                return Integer.compare(sc1.getPriority(), sc2.getPriority());
            }
        });

        // Riassegna le priorità in modo che siano strettamente 1, 2, 3...
        for (int i = 0; i < activeSortCriteria.size(); i++) {
            activeSortCriteria.get(i).setPriority(i + 1); // Le priorità diventano 1-based
        }

        // Log per debug (opzionale)

        System.out.println("@@@ 745 - SortLogic Priorità Normalizzate:");
        for(SortCriterion sc : activeSortCriteria) {
            System.out.println("@@@ 747 - SortLogic " + sc.getDbFieldName() + " P" + sc.getPriority() + (sc.isAscending() ? " ASC" : " DESC"));
        }

    }

    // ------------------------------------------------------------------------ Method Sort Data
    private void sortData() {
        if ( webdeskList == null ||  webdeskList.isEmpty()) {
            // Se la lista è vuota, potresti volerlo comunicare all'adapter
            if (rightAdapter != null) {
                rightAdapter.setData(new ArrayList<>()); // Passa una lista vuota
            }
            if (leftAdapter != null) {
                // Assumendo che leftAdapter abbia un metodo setData simile o che
                // semplicemente notifichi se condivide implicitamente i dati o la loro assenza.
                // leftAdapter.setData(new ArrayList<>());
                leftAdapter.notifyDataSetChanged(); // o il suo metodo di aggiornamento
            }
            return; // Non c'è nulla da ordinare
        }

        if (activeSortCriteria.isEmpty()) {
            // Quando non ci sono criteri, vuoi l'ordinamento di default del DAO
            webdeskList = webdeskDao.readAllWebdeskList(filterType1FromIntent); // Ricarica per avere l'ordinamento originale
            if (rightAdapter != null) {
                // Se l'adapter ha un metodo per aggiornare i dati:
                // rightTableAdapter.updateData(webdeskList);
                //rightAdapter.notifyDataSetChanged(); // Assicurati che l'adapter rifletta eventuali modifiche precedenti
                rightAdapter.setData(webdeskList); // <<< CHIAVE QUI
            }
            if (leftAdapter != null) {
                // leftTableAdapter.updateData(webdeskList);
                leftAdapter.notifyDataSetChanged();
            }
            return;
        }

        // Ordina i criteri di ordinamento per priorità (se non già garantito da normalizeSortPriorities)
        // normalizeSortPriorities() dovrebbe già averlo fatto, ma una doppia verifica non fa male
        // o se chiami sortData() da altri punti.
        Collections.sort(activeSortCriteria, Comparator.comparingInt(SortCriterion::getPriority));

        Collections.sort(webdeskList, new Comparator<WebdeskItem>(){
            @Override
            public int compare(WebdeskItem row1, WebdeskItem row2) {
                for (SortCriterion criterion : activeSortCriteria) {
                    String fieldName = criterion.getDbFieldName();
                    Object val1 = getFieldValueByName(row1, fieldName);
                    Object val2 = getFieldValueByName(row2, fieldName);

                    int comparisonResult = 0;

                    // Gestisci i valori null (i null possono essere considerati minori o maggiori)
                    if (val1 == null && val2 == null) {
                        comparisonResult = 0;
                    } else if (val1 == null) {
                        comparisonResult = -1; // nulls first (o 1 per nulls last)
                    } else if (val2 == null) {
                        comparisonResult = 1;  // nulls first (o -1 per nulls last)
                    } else {
                        // Confronta i valori in base al loro tipo
                        // Questo è un punto CRUCIALE e deve essere robusto
                        if (val1 instanceof Comparable && val2 instanceof Comparable) {
                            // Tentativo di confronto generico se i tipi sono uguali e Comparable
                            if (val1.getClass().equals(val2.getClass())) {
                                try {
                                    comparisonResult = ((Comparable) val1).compareTo(val2);
                                } catch (ClassCastException e) {
                                    // Se il cast diretto fallisce o i tipi non sono confrontabili direttamente
                                    // Converti a stringa come fallback (non ideale per numeri/date)
                                    comparisonResult = val1.toString().compareToIgnoreCase(val2.toString());
                                }
                            } else {
                                // Tipi diversi, fallback a confronto di stringhe
                                comparisonResult = val1.toString().compareToIgnoreCase(val2.toString());
                            }
                        } else {
                            // Se non sono Comparable o il tentativo precedente non è andato a buon fine,
                            // confronta come stringhe (case-insensitive come fallback).
                            // Questo è un fallback; per tipi specifici come numeri o date,
                            comparisonResult = val1.toString().compareToIgnoreCase(val2.toString());
                        }
                    } // Fine gestione valori non null

                    // Se la direzione non è ascendente, inverti il risultato del confronto
                    if (!criterion.isAscending()) {
                        comparisonResult = -comparisonResult;
                    }

                    // Se i valori sono diversi per questo criterio, abbiamo trovato l'ordine.
                    // Non c'è bisogno di controllare i criteri di ordinamento successivi (di priorità inferiore).
                    if (comparisonResult != 0) {
                        return comparisonResult;
                    }
                }
                // Se tutti i criteri di ordinamento danno come risultato 0 (cioè, le righe sono uguali
                // secondo tutti i criteri di ordinamento), allora considerale uguali.
                return 0;
            }
        });

        //---------- Print verifica che l'ordinamento funziona
        /*
        System.out.println("@@@ 868 SORTDATA: Fine Collections.sort(webdeskList).");
        System.out.println("@@@ 869 SortCheck --- webdeskList IN WebdeskTableActivity DOPO sort ---");
        for (int i = 0; i < Math.min(webdeskList.size(), 50); i++) {
            WebdeskItem currentItem = webdeskList.get(i);
            System.out.println("@@@ 872 SortCheck Item " + i + ": Type1 =" + currentItem.getType1() + ", Name=" + currentItem.getName()); // Scegli campi rilevanti
        }
        System.out.println("@@@ 874 SortCheck --- Fine verifica webdeskList ---");
        */
        //---------- Fine print verifica

        // Dopo aver ordinato allTableData, notifica all'adapter che i dati sono cambiati.
        if (rightAdapter != null) {
            rightAdapter.setData(this.webdeskList);
        }
        if (leftAdapter != null) { // leftTableAdapter è il nome del tuo adapter per la tabella sinistra
            leftAdapter.setData(this.webdeskList);
        }
        // Dopo l'ordinamento, scrolla entrambe le tabelle in cima
        if (recyclerRight != null) { // rightRecyclerView è il nome del tuo RecyclerView destro
            recyclerRight.scrollToPosition(0);
        }
        if (recyclerLeft != null) { // leftRecyclerView è il nome del tuo RecyclerView sinistro
            recyclerLeft.scrollToPosition(0);
        }
    }

    // ------------------------------------------------------------------------ Method Resert Sort
    private Object getFieldValueByName(WebdeskItem item, String fieldName) {
        if (item == null || fieldName == null) {
            return null;
        }
        // Mappare il fieldName (usato in ColumnConfig.dbFieldName)
        // al getter corretto dell'oggetto WebdeskItem.
        switch (fieldName) {
            case "Id": // Se hai una colonna "Id" e vuoi ordinarla
                return item.getId();
            case "UserCod":
                return item.getUserCod(); // Restituisce Integer, gestisce null
            case "Name":
                return item.getName();    // Restituisce String
            case "Url":
                return item.getUrl();     // Restituisce String
            case "Icon":
                return item.getIcon();    // Restituisce String
            case "Type1":
                return item.getType1();   // Restituisce String
            case "Type2":
                return item.getType2();   // Restituisce String
            case "Note":
                return item.getNote();    // Restituisce String
            case "Order1":
                return item.getOrder1();  // Restituisce Integer, gestisce null
            case "Order2":
                return item.getOrder2();  // Restituisce Integer, gestisce null
            case "DateCreate":
                return item.getDateCreate(); // Restituisce String (considera di convertirlo in Date/Long per un ordinamento migliore)
            case "DateVisit":
                return item.getDateVisit();  // Restituisce String (considera di convertirlo in Date/Long per un ordinamento migliore)
            case "Frequency":
                return item.getFrequency(); // Restituisce Integer, gestisce null
            case "TextColor":
                return item.getTextColor(); // Restituisce String
            case "Background":
                return item.getBackground(); // Restituisce String
            case "Flag1":
                return item.getFlag1();   // Restituisce Integer, gestisce null
            case "Flag2":
                return item.getFlag2();   // Restituisce Integer, gestisce null
            default:
                return null; // O lancia un'eccezione se preferisci
        }
    }

    // ------------------------------------------------------------------------ Method Resert Sort
    private void handleResetSort() {

        // 1. Svuota i criteri di ordinamento attivi
        activeSortCriteria.clear();

        // 2. Ricarica i dati nell'ordine originale dal DAO
        // Questo è il modo più semplice per tornare all'ordine "naturale"
        if (webdeskDao != null) {
            webdeskList = webdeskDao.readAllWebdeskList(filterType1FromIntent); // Assumendo che questo dia l'ordine di default
        } else {
            return;
        }

        // 3. Aggiorna gli header della tabella (rimuovi frecce/numeri)
        updateRightTableHeader(); // Questo metodo dovrebbe già gestire il caso di activeSortCriteria vuota

        // 4. Aggiorna i dati negli adapter con la lista (ora originale/non ordinata)
        if (rightAdapter != null && webdeskList != null) {
            rightAdapter.setData(webdeskList);
        }
        if (leftAdapter != null && webdeskList != null) {
            // Assumendo che leftAdapter abbia setData
            leftAdapter.setData(webdeskList);
        }

        // 5. Scrolla all'inizio
        if (recyclerRight != null) {
            recyclerRight.scrollToPosition(0);
        }
        if (recyclerLeft != null) { // Se necessario
            recyclerLeft.scrollToPosition(0);
        }
    }

    //-------------------------------------------------------- Delete Row
    private void handleDeleteRow() {
        SharedPrefs prefs = SharedPrefs.getInstance(this);   // Ottieni istanza SharedPreferences
        final int itemIdToDelete = prefs.getSelectedItemId();       // Recupera l'ID salvato
        final String itemName = prefs.getSelectedItemName();        // Recupera il Nome salvato

        if (itemIdToDelete != -1 && itemName != null) {             // Controlla se un item valido è stato selezionato
            // (-1 è il default per ID se non trovato)
            //-------------------------------------------------- Dialog box to confirm delete row
            // Item valido trovato, mostra il dialogo di conferma
            new AlertDialog.Builder(this)
                    .setTitle(itemName)
                    .setMessage("Cancellazione scheda\nOperazione irreversibile")
                    .setPositiveButton("Sì", (dialog, which) -> {
                        int success = webdeskDao.deleteWebdesk(itemIdToDelete); // Chiamata al DAO per cancellare
                        if (success > 0) {
                            Toast.makeText(WebdeskTableActivity.this, "\"" + itemName + "\" cancellato con successo.", Toast.LENGTH_SHORT).show();
                            prefs.clearSelectedItem(); // Pulisci l'ID e il Nome dalle SharedPreferences dopo la cancellazione
                            refreshDataAndUpdateAdapters(); // Ricarica i dati e aggiorna la UI
                        } else {
                            Toast.makeText(WebdeskTableActivity.this, "Errore durante la cancellazione di \"" + itemName + "\".", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Utente ha cliccato "No", chiudi il dialogo
                        dialog.dismiss();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert) // Icona standard per alert
                    .show(); // Mostra il dialogo
        } else {
            // Nessun item valido trovato nelle SharedPreferences (ID = -1 o Nome = null)
            Toast.makeText(this, "Nessuna riga selezionata per la cancellazione.", Toast.LENGTH_SHORT).show();
        }
    }

    //-------------------------------------------------------- Hide Keyboard
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Trova la view che ha attualmente il focus.
        View view = getCurrentFocus();
        // Se nessuna view ha il focus, crea una nuova view fittizia (non visibile) per ottenere un token della finestra.
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //-------------------------------------------------------- GoTo Web Site
    private void gotoWeb() {
        SharedPrefs prefs = SharedPrefs.getInstance(this);   // Ottieni istanza SharedPreferences
        final int siteId = prefs.getSelectedItemId();               // Recupera l'ID salvato
        if(siteId == -1) { return; }

        WebdeskSite site = webdeskDao.readIdWebdesk(siteId);
        if (site != null) {
            String url = site.getUrl();
            if (url != null && !url.isEmpty()) {
                String SUrl = url.trim();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        }
    }
}
