package com.nic.webdesk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
/*--------------------------------------------------------------------------------------
1 - DAO				        typeList = dao.type2Webdesk(type1);
2 - Adapter			        adapter = new TypeAdapter(typeList, this, typeLevel);
3 - RecyclerView		    recyclerView.setAdapter(adapter);
4 - Binding data into Card 	TypeAdapter - onBindViewHolder():
                            holder.textViewType.setText(item.getType2());
				            holder.textViewFreq.setText(String.valueOf(item.getFreq()));
5 - View cards
--------------------------------------------------------------------------------------*/
public class MainActivity extends AppCompatActivity implements OnTypeClickListener {
    private RecyclerView recyclerView;
    private TypeAdapter adapter;
    private List<WebdeskType> typeList;
    private WebdeskDAO dao;
    private int typeLevel = 1;
    //private int fontSizeSp = 16; // default value necessary also for RecyclerViewConfigurator class
    //----------------------------- persistence colums number
    private static final String PREFS_NAME = "webdesk_prefs";
    private static final String KEY_TYPE_COLUMN_COUNT = "column_count";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-------------------------------- recyclerView and title (textType1)
        recyclerView = findViewById(R.id.recyclerTypes);    // also for RecyclerViewConfigurator
        ImageButton btnColumns = findViewById(R.id.ButtonColumns);  // for RecyclerViewConfigurator

        //-------------------------------- DAO
        dao = new WebdeskDAO(this);


        //-------------------------------- adapter for recyclerview passandogli la lista
        /*   TypeAdapter receives:
           - typeList: list of cards with same Type1 or Type2 to view
           - this: contest or listner for the click
        */
        typeList = new ArrayList<>();
        adapter = new TypeAdapter(typeList, this, 1); // inizialmente livello 1,    // also for RecyclerViewConfigurator
        recyclerView.setAdapter(adapter);
        RecyclerView recyclerView = findViewById(R.id.recyclerTypes);    // also for RecyclerViewConfigurator

        //------------------------------------------------------------ recycler configurator
        // view column number and font configurator - parameters: colonne num default, font col 2, 3, 4
        RecyclerViewConfigurator.setupRecyclerColumns(this, recyclerView, btnColumns, adapter,
                2, 20,18,14,R.menu.menu_column_count);

        //------------------------------------------------------------ ToggleButton
        ToggleButton toggle = findViewById(R.id.toggleTypeLevel);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                typeLevel = 2;
            } else {
                typeLevel = 1;
                loadTypes(typeLevel);
            }
        });

        //-------------------------------- carica iniziale Type1
        loadTypes(typeLevel);
        System.out.println("@@@ MainActivity 84 - carica iniziale Type1: " + typeLevel);

        //-------------------------------- button new site
        findViewById(R.id.ButtonNewSite).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditSiteActivity.class);
            intent.putExtra("siteId", -1);  // Indica nuovo record
            startActivity(intent);
        });
    }

    //==============================================================================================

    //------------------------------------------------------------ loadTypes
    private void loadTypes(int level) {
        if (level == 1) {
            typeList = dao.type1Webdesk();
            adapter = new TypeAdapter(typeList, this, 1);
        } else {
            typeList = new ArrayList<>();
            adapter = new TypeAdapter(typeList, this, 2); // NON SERVE
        }
        recyclerView.setAdapter(adapter);
    }

    //------------------------------------------------------------ onClick on card Type1 or Type2
    @Override
    public void onTypeClicked(WebdeskType type) {
        if (typeLevel == 1) {
            System.out.println("@@@ MainActivity 146 - type1: " + type.getType1() + " type2: " + type.getType2() );
            Intent intent;
            intent = new Intent(MainActivity.this, SitesActivity.class);
            intent.putExtra("type1", type.getType1());
            intent.putExtra("type2", "");
            intent.putExtra("buttonType2Visible", false); // NON VIENE PIU' USATO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            startActivity(intent);
            finish();
        } else if (typeLevel == 2) {
            System.out.println("@@@ MainActivity 156 - type1: " + type.getType1() + " type2: " + type.getType2() );
            Intent intent;
            intent = new Intent(MainActivity.this, Type2Activity.class);
            intent.putExtra("type1", type.getType1());
            intent.putExtra("type2", type.getType2()); // NON SERVE!!!!!!!!!!!!!!!!!!!
            startActivity(intent);
            finish();
        }
    }

    //------------------------------------------------------------ reload data if toggle = 1
    // Reload data every time return to the MainActivity
    // necessary when creating a new site and assigning a new type1
    @Override
    protected void onResume() {
        super.onResume();
        loadTypes(typeLevel);
    }

    //------------------------------------------------------------ ButtonTable
    public void ButtonTable(View view) {
        Intent intent = new Intent(MainActivity.this, ActivityWebdeskTable.class);
        startActivity(intent);
    }

    //------------------------------------------------------------ ButtonTool
    public void ButtonTool(View view) {
        Intent intent = new Intent(MainActivity.this, ToolActivity.class);
        startActivity(intent);
    }
}
