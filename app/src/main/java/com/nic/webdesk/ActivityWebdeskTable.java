package com.nic.webdesk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActivityWebdeskTable extends AppCompatActivity {

    private RecyclerView recyclerViewLeft;
    private RecyclerView recyclerViewRight;
    private ImageButton buttonDeleteRow;
    private ImageButton buttonHome;
    private WebdeskDAO webdeskDao;
    private List<WebdeskItem> webdeskList;
    private WebdeskLeftAdapter leftAdapter;
    private WebdeskDynamicAdapter rightAdapter;
    private int currentFocusedId = -1;
    private String currentFocusedName = "";
    //private enum SortState { NONE, ASC, DESC }
    private enum SortState { ASC, DESC }

    private static class SortEntry {
        String columnName;
        SortState state;

        SortEntry(String name, SortState s) {
            this.columnName = name;
            this.state = s;
        }
    }

    private List<SortEntry> sortPriority = new ArrayList<>();

    TextView hType1, hType2, hOrder1, hOrder2, hFlag1, hFlag2;

    private String[] allColumns = new String[] {
            "Url", "Icon", "Type1", "Type2", "Note",
            "Order1", "Order2", "DateCreate", "DateVisit",
            "Frequency", "TextColor", "Background", "Flag1", "Flag2"
    };

    //---------------------- colonne visibili correnti (max 6)
    private List<String> visibleColumns = new ArrayList<>();

    //============================================================================ onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webdesk_table);

        // Colleghiamo le view
        recyclerViewLeft = findViewById(R.id.recyclerViewLeft);
        recyclerViewRight = findViewById(R.id.recyclerViewRight);
        buttonDeleteRow = findViewById(R.id.buttonDeleteRow);
        buttonHome = findViewById(R.id.buttonHome);

        // Inizializzazione DAO
        webdeskDao = new WebdeskDAO(this);

        // Carica dati in lista
        webdeskList = webdeskDao.readAllWebdeskList();

        // Layout verticale
        LinearLayoutManager leftLayoutManager = new LinearLayoutManager(this);
        LinearLayoutManager rightLayoutManager = new LinearLayoutManager(this);

        recyclerViewLeft.setLayoutManager(leftLayoutManager);
        recyclerViewRight.setLayoutManager(rightLayoutManager);

        WebdeskDynamicAdapter.OnFocusChangedListener listener = new WebdeskDynamicAdapter.OnFocusChangedListener() {
            @Override
            public void onFocusChanged(int id, String name) {
                currentFocusedId = id;
                currentFocusedName = name;
            }
        };

        // Adattatori
        leftAdapter = new WebdeskLeftAdapter(webdeskList);
        rightAdapter = new WebdeskDynamicAdapter(this, webdeskList, visibleColumns, webdeskDao, listener);
        recyclerViewRight.setAdapter(rightAdapter);

/*
        rightAdapter = new WebdeskRightAdapter(webdeskList, webdeskDao, new WebdeskRightAdapter.OnFocusChangedListener() {
            @Override
            public void onFocusChanged(int id, String name) {
                currentFocusedId = id;
                currentFocusedName = name;
            }
        });
*/
        recyclerViewLeft.setAdapter(leftAdapter);
        recyclerViewRight.setAdapter(rightAdapter);

        //---------------------------------------------------------------------- Synchronizer vertical and horizontal
        // Sincronizza scroll verticale tra i due RecyclerView
        setupScrollSync();

        // sincronizza scroll orizzontale dell'header destro
        HorizontalScrollView headerScroll = findViewById(R.id.headerScrollView);
        HorizontalScrollView bodyScroll  = findViewById(R.id.horizontalScrollView);

        headerScroll.setOnScrollChangeListener((v, x, y, oldX, oldY) -> {
            bodyScroll.scrollTo(x, y);
        });

        bodyScroll.setOnScrollChangeListener((v, x, y, oldX, oldY) -> {
            headerScroll.scrollTo(x, y);
        });

        //---------------------------------------------------------------------- Column Sort
        hType1 = findViewById(R.id.headerType1);
        hType2 = findViewById(R.id.headerType2);
        hOrder1 = findViewById(R.id.headerOrder1);
        hOrder2 = findViewById(R.id.headerOrder2);
        hFlag1 = findViewById(R.id.headerFlag1);
        hFlag2 = findViewById(R.id.headerFlag2);

        //---------------------------------------------------------------------- Column Select
        visibleColumns.clear();
        visibleColumns.addAll(Arrays.asList("Type1", "Type2", "Order1", "Order2", "Flag1", "Flag2"));

        ImageButton buttonChooseColumns = findViewById(R.id.buttonChooseColumns);

        buttonChooseColumns.setOnClickListener(v -> {
            openColumnPicker();
        });

        //--------------------------------------------- Update headerClickListener
        // nuovo gestione clic header dinamico
        View.OnClickListener headerClickListener = view -> {
            TextView textView = (TextView) view;

            // Ottieni il testo attuale del titolo (es: "Url 1↑" o "Icon 2↓")
            String fullText = textView.getText().toString().trim();

            // Elimina eventuali numeri o frecce alla fine, ottenendo il nome originale della colonna
            // Es: "Url 1↑" -> "Url"
            String colName = fullText.replaceAll("\\d+\\s*[↑↓]?", "").trim();

            // Trova se è già nella lista sortPriority
            SortEntry existing = null;
            for (SortEntry entry : sortPriority) {
                if (entry.columnName.equals(colName)) {
                    existing = entry;
                    break;
                }
            }

            if (existing == null) {
                // non era presente → lo aggiungiamo alla fine come ASC
                sortPriority.add(new SortEntry(colName, SortState.ASC));
            } else {
                // già presente → cicla ASC -> DESC -> rimuovi
                if (existing.state == SortState.ASC) {
                    existing.state = SortState.DESC;
                } else {
                    sortPriority.remove(existing);
                }
            }

            applySorting();  // riordina e aggiorna frecce (refreshHeaderTitles)
        };

        hType1.setOnClickListener(headerClickListener);
        hType2.setOnClickListener(headerClickListener);
        hOrder1.setOnClickListener(headerClickListener);
        hOrder2.setOnClickListener(headerClickListener);
        hFlag1.setOnClickListener(headerClickListener);
        hFlag2.setOnClickListener(headerClickListener);

        //---------------------------------------------------------------------- Button Reset Sort Columns
        ImageButton buttonResetSort = findViewById(R.id.buttonResetSort);
        buttonResetSort.setOnClickListener(v -> {
            // Svuota la lista di ordinamento multiplo
            sortPriority.clear();

            // Resetta le frecce e numeri sugli header
            resetHeaderSortIndicators();

            // Aggiorna RecyclerView
            leftAdapter.notifyDataSetChanged();
            rightAdapter.notifyDataSetChanged();
        });

        //---------------------------------------------------------------------- Button Delete Row
        buttonDeleteRow.setOnClickListener(view -> {
            if (currentFocusedId != -1) {
                showDeleteConfirmation(currentFocusedId, currentFocusedName);
            } else {
                Alert.alertDialog(this,"Webdwsk Tabella","Nessuna riga selezionata",20000);
            }
        });

        //----------------------------------------------------------------------- Button Home
        buttonHome.setOnClickListener(view -> {
            Intent intent = new Intent(ActivityWebdeskTable.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    //============================================================================ Methods
    // questo deve essere dichiarato come variabile membro della classe Activity:
    private RecyclerView.OnScrollListener rightScrollListener;

    //----------------------------------------------------------------------- Show delete confirmation
    private void showDeleteConfirmation(int id, String name) {
        //import YesNoCallback
        Alert.alertYesNoDialog(this, "Conferma eliminazione", "Vuoi eliminare il sito: " + name + "?", new Alert.YesNoCallback() {
            @Override
            public void onResult(boolean yes) {
                if (yes) {
                    webdeskDao.deleteWebdesk(id);
                    finish();
                    reloadData();
                }
            }
        });
    }

    //----------------------------------------------------------------------- Reload Data
    private void reloadData() {
        webdeskList = webdeskDao.readAllWebdeskList();
        leftAdapter.setData(webdeskList);
        rightAdapter.setData(webdeskList);
    }

    //----------------------------------------------------------------------- Reset Sort Indicators
    private void resetHeaderSortIndicators() {
        hType1.setText("Type1");
        hType2.setText("Type2");
        hOrder1.setText("Order1");
        hOrder2.setText("Order2");
        hFlag1.setText("Flag1");
        hFlag2.setText("Flag2");
    }

    private void recreateRightAdapter() {
        // Listener focus da riutilizzare
        WebdeskDynamicAdapter.OnFocusChangedListener listener = (id, name) -> {
            currentFocusedId = id;
            currentFocusedName = name;
        };

        rightAdapter = new WebdeskDynamicAdapter(this, webdeskList, visibleColumns, webdeskDao, listener);
        recyclerViewRight.setAdapter(rightAdapter);

        // se hai header che scorrono orizzontalmente, possiamo risincronizzarli:
        setupScrollSync();

        refreshHeaderTitles();  // (opzionale, se vuoi resettare frecce quando cambi colonne)
        refreshHeaderNames();
    }

    //----------------------------------------------------------------------- Open Columns Picker
    private void openColumnPicker() {
        boolean[] checked = new boolean[allColumns.length];
        for (int i = 0; i < allColumns.length; i++) {
            checked[i] = visibleColumns.contains(allColumns[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scegli colonne (max 6)");

        builder.setMultiChoiceItems(allColumns, checked, (dialog, which, isChecked) -> {
            // quando clicchi un checkbox aggiorni i boolean
            checked[which] = isChecked;
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            List<String> selected = new ArrayList<>();
            for (int i = 0; i < allColumns.length; i++) {
                if (checked[i]) {
                    selected.add(allColumns[i]);
                }
            }

            // se superiamo 6, ne prendiamo solo le prime 6
            if (selected.size() > 6) {
                Alert.alertDialog(this,"Columns select","You can select up to 6 columns!",20000 );
                selected = selected.subList(0, 6);
            }

            visibleColumns.clear();
            visibleColumns.addAll(selected);
            refreshHeaderNames();           // refresh header name
            recreateRightAdapter();         // recreate right adapter based on visibleColumns
        });

        builder.setNegativeButton("Annulla", null);
        builder.show();
    }

    //----------------------------------------------------------------------- Column Sorting
    private void applySorting() {
        if (sortPriority.isEmpty()) {
            return;
        }

        Comparator<WebdeskItem> finalComp = null;

        for (SortEntry entry : sortPriority) {
            Comparator<WebdeskItem> comp = null;

            switch (entry.columnName) {
                case "Type1":
                    comp = Comparator.comparing(
                            WebdeskItem::getType1,
                            Comparator.nullsLast(String::compareToIgnoreCase)
                    );
                    break;
                case "Type2":
                    comp = Comparator.comparing(
                            WebdeskItem::getType2,
                            Comparator.nullsLast(String::compareToIgnoreCase)
                    );
                    break;
                case "Order1":
                    comp = Comparator.comparing(item -> item.getOrder1() != null ? item.getOrder1() : Integer.MAX_VALUE
                    );
                    break;
                case "Order2":
                    comp = Comparator.comparing(item -> item.getOrder2() != null ? item.getOrder2() : Integer.MAX_VALUE
                    );
                    break;
                case "Flag1":
                    comp = Comparator.comparing(item -> item.getFlag1() != null ? item.getFlag1() : 0
                    );
                    break;
                case "Flag2":
                    comp = Comparator.comparing(item -> item.getFlag2() != null ? item.getFlag2() : 0
                    );
                    break;
                case "Url":
                    comp = Comparator.comparing(WebdeskItem::getUrl, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "Icon":
                    comp = Comparator.comparing(WebdeskItem::getIcon, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "Note":
                    comp = Comparator.comparing(WebdeskItem::getNote, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "DateCreate":
                    comp = Comparator.comparing(WebdeskItem::getDateCreate, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "DateVisit":
                    comp = Comparator.comparing(WebdeskItem::getDateVisit, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "Frequency":
                    comp = Comparator.comparing(item -> item.getFrequency() != null ? item.getFrequency() : Integer.MAX_VALUE);
                    break;
                case "TextColor":
                    comp = Comparator.comparing(WebdeskItem::getTextColor, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
                case "Background":
                    comp = Comparator.comparing(WebdeskItem::getBackground, Comparator.nullsLast(String::compareToIgnoreCase));
                    break;
            }

            if (comp != null) {
                if (entry.state == SortState.DESC) {
                    comp = comp.reversed();
                }
                if (finalComp == null) {
                    finalComp = comp;
                } else {
                    finalComp = finalComp.thenComparing(comp);
                }
            }
        }

        if (finalComp != null) {
            Collections.sort(webdeskList, finalComp);
            leftAdapter.notifyDataSetChanged();
            rightAdapter.notifyDataSetChanged();
        }

        refreshHeaderTitles();
    }

    //----------------------------------------------------------------------- Column Sorting
    // Necessario per gestire le fecce che indicano la direzione dell'ordinamento in the header of columns
    private void refreshHeaderTitles() {
        // 1. Prima resettiamo i testi originali dinamici
        refreshHeaderNames();   // questo rimette "Url", "Icon", ecc. senza frecce

        // 2. Array di TextView in posizione di slot (header sinistro già a parte)
        TextView[] headerViews = new TextView[] { hType1, hType2, hOrder1, hOrder2, hFlag1, hFlag2 };

        // 3. Applichiamo frecce e numeri solo sui campi presenti nella lista sortPriority
        for (int i = 0; i < sortPriority.size(); i++) {
            SortEntry entry = sortPriority.get(i);
            String arrow = entry.state == SortState.ASC ? " ↑" : " ↓";
            String labelNum = (i + 1) + arrow;   // es: "1↑"

            // Cerchiamo quale header contiene il testo di quella colonna
            for (TextView tv : headerViews) {
                if (tv == null) continue;
                String currentText = tv.getText().toString().trim();

                // puliamo eventuali vecchie frecce/numeri
                String pureText = currentText.replaceAll("\\d+\\s*[↑↓]?", "").trim();

                if (pureText.equals(entry.columnName)) {
                    // Applichiamo l'etichetta con numero
                    tv.setText(pureText + " " + labelNum);
                    break;
                }
            }
        }
    }

    //---------------------------------------------------------------------------- Synchronize vertical scroll
    // Sincronizzazione dello scroll verticale tra i due RecyclerView (versione sicura)
    private void setupScrollSync() {

        // Listener che sincronizza left -> right
        RecyclerView.OnScrollListener leftScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                recyclerViewRight.removeOnScrollListener(rightScrollListener);
                recyclerViewRight.scrollBy(dx, dy);
                recyclerViewRight.addOnScrollListener(rightScrollListener);
            }
        };

        // Listener che sincronizza right -> left
        rightScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                recyclerViewLeft.removeOnScrollListener(leftScrollListener);
                recyclerViewLeft.scrollBy(dx, dy);
                recyclerViewLeft.addOnScrollListener(leftScrollListener);
            }
        };

        recyclerViewLeft.addOnScrollListener(leftScrollListener);
        recyclerViewRight.addOnScrollListener(rightScrollListener);
    }

    private void refreshHeaderNames() {
        // Qui supponiamo che visibleColumns abbia max 6 elementi già selezionati
        // Reset testo di tutti
        hType1.setText("");
        hType2.setText("");
        hOrder1.setText("");
        hOrder2.setText("");
        hFlag1.setText("");
        hFlag2.setText("");

        // Array temporaneo di quelli sopra in ordine, così è facile assegnare:
        TextView[] headerViews = new TextView[] {
                hType1, hType2, hOrder1, hOrder2, hFlag1, hFlag2
        };

        for (int i = 0; i < visibleColumns.size() && i < headerViews.length; i++) {
            String colName = visibleColumns.get(i);
            headerViews[i].setText(colName);   // es. "Url", "Icon", etc.
        }
    }

}
