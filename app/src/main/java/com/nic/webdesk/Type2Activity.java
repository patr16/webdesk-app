package com.nic.webdesk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Type2Activity extends AppCompatActivity implements OnTypeClickListener {

    private RecyclerView recyclerView;
    private TypeAdapter adapter;
    private List<WebdeskType> type2List;
    private WebdeskDAO dao;
    private int typeLevel; // = 1;
    private String selectedType1;
    private TextView textType;

    //----------------------------- persistence colums number
    //private static final String PREFS_NAME = "webdesk_prefs";
    //private static final String KEY_TYPE_COLUMN_COUNT = "column_count";
    //private int columnCount = 2;  // default colums number


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type2);

        recyclerView = findViewById(R.id.recyclerTypes);            // also for RecyclerViewConfigurator
        ImageButton btnColumns = findViewById(R.id.ButtonColumns);  // for RecyclerViewConfigurator
        textType = findViewById(R.id.textType);

        dao = new WebdeskDAO(this);

        // Prendo il type1 dall'intent
        selectedType1 = getIntent().getStringExtra("type1");
        if (selectedType1 == null) {
            // Se non arriva nulla, chiudo activity o fai fallback
            Alert.alertDialog(this,"Gestione Errori", "Errore: nessun type1 specificato", 20000);
            //finish(); CI VUOLE??????????????????????????
            return;
        }

        textType.setText(selectedType1);
        textType.setVisibility(View.VISIBLE);

        loadTypesLevel2(selectedType1);

        //------------------------------------------------------------ recycler configurator
        // view column number and font configurator - parameters: colonne num default, font col 2, 3, 4
        RecyclerViewConfigurator.setupRecyclerColumns(this, recyclerView, btnColumns, adapter,
                2, 20,18,14,R.menu.menu_column_count);
    }

    private void loadTypesLevel2(String type1) {
        type2List = dao.type2Webdesk(type1);
        System.out.println("@@@ 67 Type2Activity - Lista type2 size: " + (type2List != null ? type2List.size() : "null"));

        adapter = new TypeAdapter(type2List, this, 2); // livello 2 per type2, also for RecyclerViewConfigurator
        System.out.println("@@@ 69 Type2Activity - Toggle set to Type2 (click on Type1 will load Type2)");
        //recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 colonne di default, also for RecyclerViewConfigurator
        recyclerView.setAdapter(adapter);
        if (recyclerView != null && adapter != null) {
            System.out.println("@@@ 74 Type2Activity - RecyclerView e Adapter settati correttamente");
        } else {
            System.out.println("@@@ 76 Type2Activity - ATTENZIONE: RecyclerView o Adapter sono null");
        }

    }

    @Override
    public void onTypeClicked(WebdeskType type) {
        // Qui cosa vuoi fare cliccando un type2?
        // Di default, apro MainActivity con i dati passati (come fai in MainActivity)
        System.out.println("@@@ 76 Type2Activity - loadSites con filtro type1: " + type.getType1() + ", type2: " + type.getType2());
        Intent intent = new Intent(this, SitesActivity.class);
        intent.putExtra("type1", type.getType1());
        intent.putExtra("type2", type.getType2());
        intent.putExtra("typeLevel", 2);
        intent.putExtra("buttonType2Visible", true);
        startActivity(intent);
    }

    //========================================================= ButtonHome
    public void ButtonType1(View view) {
        Intent intent = new Intent(Type2Activity.this, MainActivity.class);
        intent.putExtra("type1", selectedType1);
        intent.putExtra("typeLevel", 1);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
