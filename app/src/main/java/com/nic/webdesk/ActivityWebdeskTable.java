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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActivityWebdeskTable extends AppCompatActivity {

    private RecyclerView recyclerViewLeft;
    private RecyclerView recyclerViewRight;
    private ImageButton buttonDeleteRow;
    private ImageButton buttonHome;

    private WebdeskDAO webdeskDao;
    //private List<Webdesk> webdeskList;
    private List<WebdeskItem> webdeskList;

    private WebdeskLeftAdapter leftAdapter;
    private WebdeskRightAdapter rightAdapter;

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

    // lista prioritaria
    private List<SortEntry> sortPriority = new ArrayList<>();

    TextView hType1, hType2, hOrder1, hOrder2, hFlag1, hFlag2;

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

        // Adattatori
        leftAdapter = new WebdeskLeftAdapter(webdeskList);
        rightAdapter = new WebdeskRightAdapter(webdeskList, webdeskDao, new WebdeskRightAdapter.OnFocusChangedListener() {
            @Override
            public void onFocusChanged(int id, String name) {
                currentFocusedId = id;
                currentFocusedName = name;
            }
        });

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

        //--------------------------------------------- Update headerClickListener
        View.OnClickListener headerClickListener = view -> {
            String column = "";

            int id = view.getId();
            if (id == R.id.headerType1) {
                column = "Type1";
            } else if (id == R.id.headerType2) {
                column = "Type2";
            } else if (id == R.id.headerOrder1) {
                column = "Order1";
            } else if (id == R.id.headerOrder2) {
                column = "Order2";
            } else if (id == R.id.headerFlag1) {
                column = "Flag1";
            } else if (id == R.id.headerFlag2) {
                column = "Flag2";
            }

            // Trova se la colonna è già in lista
            SortEntry existing = null;
            for (SortEntry entry : sortPriority) {
                if (entry.columnName.equals(column)) {
                    existing = entry;
                    break;
                }
            }

            if (existing == null) {
                // colonna nuova -> aggiungiamo come ASC
                sortPriority.add(new SortEntry(column, SortState.ASC));
            } else {
                // colonna già presente -> cambia stato
                if (existing.state == SortState.ASC) {
                    existing.state = SortState.DESC;
                } else {
                    // era DESC -> cicla a NONE = la togliamo dalla lista
                    sortPriority.remove(existing);
                }
            }

            applySorting();
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
            //multiSortList.clear();

            // Resetta le frecce e numeri sugli header
            resetHeaderSortIndicators();

            // Aggiorna RecyclerView
            //adapterRight.notifyDataSetChanged();
            //adapterLeft.notifyDataSetChanged();
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
        /*
        new AlertDialog.Builder(this)
                .setTitle("Conferma eliminazione")
                .setMessage("Vuoi eliminare il sito: " + name + "?")
                .setPositiveButton("Elimina", (dialog, which) -> {
                    webdeskDao.deleteWebdesk(id);
                    reloadData();
                })
                .setNegativeButton("Annulla", null)
                .show();
         */

    }

    //----------------------------------------------------------------------- Reload Data
    private void reloadData() {
        webdeskList = webdeskDao.readAllWebdeskList();
        leftAdapter.setData(webdeskList);
        rightAdapter.setData(webdeskList);
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
                    comp = Comparator.comparing(
                            item -> item.getOrder1() != null ? item.getOrder1() : Integer.MAX_VALUE
                    );
                    break;
                case "Order2":
                    comp = Comparator.comparing(
                            item -> item.getOrder2() != null ? item.getOrder2() : Integer.MAX_VALUE
                    );
                    break;
                case "Flag1":
                    comp = Comparator.comparing(
                            item -> item.getFlag1() != null ? item.getFlag1() : 0
                    );
                    break;
                case "Flag2":
                    comp = Comparator.comparing(
                            item -> item.getFlag2() != null ? item.getFlag2() : 0
                    );
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
        // Reset base
        hType1.setText("Type1");
        hType2.setText("Type2");
        hOrder1.setText("Order1");
        hOrder2.setText("Order2");
        hFlag1.setText("Flag1");
        hFlag2.setText("Flag2");

        // Aggiungiamo per ogni entry la freccia con posizione
        for (int i = 0; i < sortPriority.size(); i++) {
            SortEntry entry = sortPriority.get(i);
            String arrow = entry.state == SortState.ASC ? "↑" : "↓";
            String label = (i + 1) + arrow; // ex: "1↑", "2↓"

            switch (entry.columnName) {
                case "Type1":
                    hType1.setText("Type1 " + label);
                    break;
                case "Type2":
                    hType2.setText("Type2 " + label);
                    break;
                case "Order1":
                    hOrder1.setText("Order1 " + label);
                    break;
                case "Order2":
                    hOrder2.setText("Order2 " + label);
                    break;
                case "Flag1":
                    hFlag1.setText("Flag1 " + label);
                    break;
                case "Flag2":
                    hFlag2.setText("Flag2 " + label);
                    break;
            }
        }
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

}
