package com.nic.webdesk;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class RecyclerViewConfigurator {
/*
----------------------------------------------------
Verify that exist res/menu/menu_column_count.xml with id col_2, col_3, col_4.
1 – NELL’ADAPTER
1.1 - Declaration
	private int fontSizeSp = 16; // default value, non-essential
1.2 - Setter (immediately after the variables, before onBindViewHolder)
    //--------------------------------- set font size
    // necessary for setupRecyclerColumns, class RecyclerViewConfigurator
    public void setFontSizeSp(int fontSizeSp) {
        this.fontSizeSp = fontSizeSp;
        notifyDataSetChanged(); // update UI
	}
1.3 - Use in onBindViewHolder
	holder.textTypeName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);    // for RecyclerViewConfigurator
	or
	holder.textTypeName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);    // for RecyclerViewConfigurator
----------------------------------------------------
2 – NELL’ACTIVITY (onCreate)
2.1 - Declaration
	private TypeAdapter adapter;
    or
    private SiteAdapter siteAdapter;
2.2 - In onCreate
	RecyclerView recyclerView = findViewById(R.id.recyclerTypes);    // also for RecyclerViewConfigurator
	ImageButton btnColumns = findViewById(R.id.ButtonColumns);       // for RecyclerViewConfigurator

	// Adapter, create adapter and setting immediately after on recyclerview
	adapter = new TypeAdapter(typeList, this, 1);   // also for RecyclerViewConfigurator
    recyclerView.setAdapter(adapter); // optional: the configurator does it if it is missing

	// Configigurator (setting columns + font size
	RecyclerViewConfigurator.setupRecyclerColumns(
        	this,
        	recyclerView,
        	btnColumns,
        	adapter,
        	2,   // colonne di default
        	20,  // font con 2 colonne
        	18,  // font con 3 colonne
        	16   // font con 4 colonne
	);
	one in row:
	//------------------------------------------------------------ recycler configurator
    // view column number and font configurator - parameters: colonne num default, font col 2, 3, 4
    RecyclerViewConfigurator.setupRecyclerColumns(this, recyclerView, btnColumns, adapter,
                2, 20,18,14,R.menu.menu_column_count);
----------------------------------------------------
*/

    /**
     * Setup RecyclerView con gestione colonne e dimensione font.
     * @param activity Activity chiamante
     * @param recyclerView RecyclerView da configurare
     * @param btnColumns ImageButton per cambiare numero colonne
     * @param adapter Adapter del RecyclerView (deve avere metodo setFontSizeSp(int))
     * @param defaultColumns numero colonne di default (2,3,4)
     * @param font2Cols dimensione font per 2 colonne
     * @param font3Cols dimensione font per 3 colonne
     * @param font4Cols dimensione font per 4 colonne
     * @param menuResId resource id del menu (es. R.menu.menu_column_count)
     */
    public static void setupRecyclerColumns(
            Activity activity,
            RecyclerView recyclerView,
            ImageButton btnColumns,
            RecyclerView.Adapter adapter,
            int defaultColumns,
            int font2Cols,
            int font3Cols,
            int font4Cols,
            int menuResId
    ) {
        final String PREFS_NAME = "WebdeskPrefs";
        final String keyColumnCount = activity.getLocalClassName() + "_columns";
        final String keyFontSize = activity.getLocalClassName() + "_font";

        // Lambda per chiamare setFontSizeSp sull'adapter (riflessivo)
        Consumer<Integer> fontSetter = fontSizeSp -> {
            try {
                Method m = adapter.getClass().getMethod("setFontSizeSp", int.class);
                m.invoke(adapter, fontSizeSp);
            } catch (Exception e) {
                // se il metodo non esiste o errore, fai niente
                e.printStackTrace();
            }
        };

        // Associa l'adapter al RecyclerView se non già fatto
        if (recyclerView.getAdapter() == null) {
            recyclerView.setAdapter(adapter);
        }

        setupRecyclerView(
                activity,
                recyclerView,
                btnColumns,
                fontSetter,
                PREFS_NAME,
                keyColumnCount,
                keyFontSize,
                defaultColumns,
                font2Cols,
                font3Cols,
                font4Cols,
                menuResId
        );
    }

    // Metodo interno che fa il setup concreto
    private static void setupRecyclerView(
            Activity activity,
            RecyclerView recyclerView,
            ImageButton btnColumns,
            Consumer<Integer> fontSizeConsumer,
            String prefsName,
            String keyColumnCount,
            String keyFontSize,
            int defaultColumns,
            int font2Cols,
            int font3Cols,
            int font4Cols,
            int menuResId
    ) {
        SharedPreferences prefs = activity.getSharedPreferences(prefsName, Activity.MODE_PRIVATE);
        int savedColumnCount = prefs.getInt(keyColumnCount, defaultColumns);

        // Layout manager con colonne salvate o default
        GridLayoutManager glm = new GridLayoutManager(activity, savedColumnCount);
        recyclerView.setLayoutManager(glm);

        // Imposta font size iniziale da preferences o calcolato
        int fontSize = prefs.getInt(keyFontSize, getFontSize(savedColumnCount, font2Cols, font3Cols, font4Cols));
        fontSizeConsumer.accept(fontSize);

        // Click su bottone per cambiare colonne: popup menu
        btnColumns.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(activity, btnColumns);
            popup.getMenuInflater().inflate(menuResId, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int cols = colsFromMenuItemId(item.getItemId());
                if (cols <= 0) cols = defaultColumns;

                // Salva scelta colonne
                prefs.edit().putInt(keyColumnCount, cols).apply();

                // Cambia layout manager colonne
                recyclerView.setLayoutManager(new GridLayoutManager(activity, cols));

                // Calcola font size e salva
                int newFontSize = getFontSize(cols, font2Cols, font3Cols, font4Cols);
                prefs.edit().putInt(keyFontSize, newFontSize).apply();

                // Applica dimensione font
                fontSizeConsumer.accept(newFontSize);

                return true;
            });
            popup.show();
        });
    }

    // Mappa item id menu al numero di colonne
    private static int colsFromMenuItemId(int itemId) {
        if (itemId == R.id.col_2) return 2;
        else if (itemId == R.id.col_3) return 3;
        else if (itemId == R.id.col_4) return 4;
        else return -1;
    }

    // Calcola font size in base alle colonne
    private static int getFontSize(int cols, int font2Cols, int font3Cols, int font4Cols) {
        switch (cols) {
            case 2: return font2Cols;
            case 3: return font3Cols;
            case 4: return font4Cols;
            default: return font3Cols;
        }
    }
}
