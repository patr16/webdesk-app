package com.nic.webdesk;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
//---------------------------------------------------------
// model-bean for site
public class SitesActivity extends AppCompatActivity {
    private WebdeskDAO dao;
    private RecyclerView recyclerView;
    private SiteAdapter siteAdapter;
    private List<WebdeskSite> siteList = new ArrayList<>();
    private String type1;
    private String type2;
    private boolean buttonType2Visible = false;
    //----------------------------- persistence colums number
    //private static final String PREFS_NAME = "webdesk_prefs";
    //private static final String KEY_SITE_COLUMN_COUNT = "column_count";
    //private int columnCount = 2;  // default colums number
    //private GridLayoutManager gridLayoutManager;
    //private int fontSizeSp;

    ImageButton buttonWeb, buttonEditSite, buttonType2;
    public static boolean isEditMode = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sites);

        buttonWeb = findViewById(R.id.ButtonWeb);
        buttonEditSite = findViewById(R.id.ButtonEditSite);
        //buttonEditSite.setVisibility(View.GONE);
        //buttonWeb.setVisibility(View.VISIBLE);
        buttonWeb.setOnClickListener(v ->ButtonWeb());
        buttonEditSite.setOnClickListener(v -> ButtonEditSite());

        dao = new WebdeskDAO(this);

        recyclerView = findViewById(R.id.recyclerSites);            // also for RecyclerViewConfigurator
        ImageButton btnColumns = findViewById(R.id.ButtonColumns);  // for RecyclerViewConfigurator

        //------------------------------------------ read parameters from intent
        type1 = getIntent().getStringExtra("type1");
        type2 = getIntent().getStringExtra("type2"); // can is null
        //------------------------------------------ visibility button go activity_type2
        buttonType2 = findViewById(R.id.ButtonType2);
        buttonType2Visible = getIntent().getBooleanExtra("buttonType2Visible",false);   // from AcyivityMain=false,ActivityType2=true
        if(buttonType2Visible) {
            buttonType2.setVisibility(View.VISIBLE);
        }else{
            buttonType2.setVisibility(View.GONE);
            }
        //------------------------------------------ loads the data
        if (type1 == null || type1.isEmpty()) {
            Alert.alertDialog(this, "Gestione Errori", "Parametri mancanti", 20000);
        } else {
            loadSites();
        }

        //------------------------------------------ Title Type of cards
        TextView textType = findViewById(R.id.textType);
        if (type2 != null && !type2.isEmpty()) {
            textType.setText(type1 + "\n" + type2);
        } else {
            textType.setText(type1);
        }

        //------------------------------------------------------------ recycler configurator
        // view column number and font configurator - parameters: colonne num default, font col 2, 3, 4
        RecyclerViewConfigurator.setupRecyclerColumns(this, recyclerView, btnColumns, siteAdapter,
                2, 20,18,14,R.menu.menu_column_count);

    }

    //===================================================================================

    //--------------------------------------------------------- reload data
    // Reload data every time return to SitesActivity
    // necessary when modify name and icon

    @Override
    protected void onResume() {
        super.onResume();
        loadSites();
    }

    //--------------------------------------------------------- Load Sites
    private void loadSites() {
        if (type2 == null || type2.isEmpty()) {
            siteList = dao.sitesWebdesk(type1);         // only Type1
        } else {
            siteList = dao.sitesWebdesk(type1, type2);  // Type1 + Type2
        }

        if (siteList == null || siteList.isEmpty()) {
            Alert.alertDialog(this, "Attenzione", "Nessun sito disponibile", 10000);
            return;
        }

        siteAdapter = new SiteAdapter(this, siteList);  // also for RecyclerViewConfigurator
        recyclerView.setAdapter(siteAdapter);
    }

    //--------------------------------------------------------- ButtonWeb
    // active status edit card
    public void ButtonWeb() {
        buttonWeb.setVisibility(View.GONE);
        buttonEditSite.setVisibility(View.VISIBLE);
        isEditMode = false;      // SiteAdapter - holder.itemView.setOnClickListener

    }

    //--------------------------------------------------------- ButtonEditSite
    // active status go web site
    public void ButtonEditSite() {
        buttonWeb.setVisibility(View.VISIBLE);
        buttonEditSite.setVisibility(View.GONE);
        isEditMode = true;     // SiteAdapter - holder.itemView.setOnClickListener
    }

    //--------------------------------------------------------- Button Type1 (Home), Type2
    public void ButtonType1(View view) {
        Intent intent = new Intent(SitesActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    public void ButtonType2(View view) {
        Intent intent = new Intent(SitesActivity.this, Type2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}


