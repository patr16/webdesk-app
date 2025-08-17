package com.nic.webdesk;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
//import org.w3c.dom.Document;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Executors;

public class EditSiteActivity extends AppCompatActivity {

    private AutoCompleteTextView editType1;
    private AutoCompleteTextView editType2;
    private ImageButton buttonSelectType1;
    private ImageButton buttonSelectType2;
    private EditText editName, editUrl, editIcon, editNote;
    private EditText editOrder1, editOrder2, editDateCreate, editDateVisit, editFrequency;
    private EditText editTextColor, editBackground, editFlag1, editFlag2;
    private ImageView imageViewIcon;


    private int siteId;
    private WebdeskDAO webdeskDao;

    private List<WebdeskType> typeList;

    //========================================================================= onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_site);

        //-------------------------------------------------------------------- 1 - activity launched by sharing link (action_send) or clipboard (clipboard_service)
        editUrl = findViewById(R.id.editUrl);
        editName = findViewById(R.id.editName);
        imageViewIcon = findViewById(R.id.imageViewIcon);

        Intent intent = getIntent();
        String urlFromShare = null;

        // 1. Check if launched via "Share" from the browser
        if (Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())) {
            CharSequence sharedText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
            if (sharedText != null && Patterns.WEB_URL.matcher(sharedText).find()) {
                urlFromShare = sharedText.toString().trim();
            }
        }

        // 2. If nothing arrives via sharing, check the clipboard
        if (urlFromShare == null || urlFromShare.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    CharSequence clipText = clip.getItemAt(0).getText();
                    if (clipText != null && Patterns.WEB_URL.matcher(clipText).find()) {
                        urlFromShare = clipText.toString().trim();
                    }
                }
            }
        }

        // 3. If we have obtained a valid URL, we put it in the URL field
/*
        if (urlFromShare != null && !urlFromShare.isEmpty()) {
            editUrl.setText(urlFromShare);
            System.out.println("@@@ 74 EditSiteActivity - url: " + urlFromShare);
            String url = urlFromShare.trim();
            String currentName = editName.getText().toString().trim();
            System.out.println("@@@ 77 EditSiteActivity - name void!:..." + currentName + "...");
            if (currentName.isEmpty()) {
                String suggestedName = suggestNameFromUrl(url);         //<--- suggested name from url
                System.out.println("@@@ 80 EditSiteActivity - suggestedName: " + suggestedName);
                editName.setText(suggestedName);
                fetchSiteIcon(url, imageViewIcon);                      //<--- fetch site icon
                System.out.println("@@@ 83 EditSiteActivity - imageViewIcon: " + imageViewIcon);
            }
        }


        if (urlFromShare != null && !urlFromShare.isEmpty()) {
            System.out.println("@@@ 74 EditSiteActivity - url: " + urlFromShare);
            handleUrlInsertion(urlFromShare.trim());
        }
*/



        // Recupera ID del record da intent
        siteId = getIntent().getIntExtra("siteId", -1);
        webdeskDao = new WebdeskDAO(this);

        // Inizializza i campi
        editName = findViewById(R.id.editName);
        editUrl = findViewById(R.id.editUrl);
        editIcon = findViewById(R.id.editIcon);
        editType1 = findViewById(R.id.editType1);
        editType2 = findViewById(R.id.editType2);
        editNote = findViewById(R.id.editNote);
        editOrder1 = findViewById(R.id.editOrder1);
        editOrder2 = findViewById(R.id.editOrder2);
        editDateCreate = findViewById(R.id.editDateCreate);
        editDateVisit = findViewById(R.id.editDateVisit);
        editFrequency = findViewById(R.id.editFrequency);
        editTextColor = findViewById(R.id.editTextColor);
        editBackground = findViewById(R.id.editBackground);
        editFlag1 = findViewById(R.id.editFlag1);
        editFlag2 = findViewById(R.id.editFlag2);



        //-------------------------------------------------------------------- 2 - Load data
        // At open view load data of selected site in edit mode
        if (siteId != -1) {
            WebdeskSite site = webdeskDao.readIdWebdesk(siteId);
            if (site != null) {
                // Populate the fields of website
                populateFields(site);

                // Show existing icon if present
                String iconUrl = editIcon.getText().toString().trim();
                if (!iconUrl.isEmpty()) {
                    Glide.with(this)
                            .load(iconUrl)
                            .placeholder(R.drawable.placeholder_icon)
                            .error(R.drawable.error_icon)
                            .into(imageViewIcon);
                }
            }
        }

        //-------------------------------------------------------------------- 3 - Button Save Site
        findViewById(R.id.ButtonSaveSite).setOnClickListener(v -> {
            WebdeskSite updated = collectFields();
            if (siteId == -1) {
                webdeskDao.insertWebdesk(updated); // new record
            } else {
                updated.setId(siteId);
                webdeskDao.updateWebdesk(updated); // record exist, update record
            }
            finish();
        });

        //-------------------------------------------------------------------- 4 - Button Delete Site
        findViewById(R.id.ButtonDeleteSite).setOnClickListener(v -> {
            Alert.alertYesNoDialog(this, "Delete site", "Do you really want to delete this site?", new Alert.YesNoCallback() {
                @Override
                public void onResult(boolean yes) {
                    if (yes) {
                        webdeskDao.deleteWebdesk(siteId);
                        finish();
                    }
                }
            });
        });

        //-------------------------------------------------------------------- 5 - Button PasteUrl
        /*
        findViewById(R.id.ButtonPasteUrl).setOnClickListener(v -> {
            EditText editUrl = findViewById(R.id.editUrl);
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    CharSequence clipText = clip.getItemAt(0).getText();
                    if (clipText != null && Patterns.WEB_URL.matcher(clipText).find()) {
                        editUrl.setText(clipText.toString().trim());
                        Alert.alertDialog(this,"New site - url from browser", "URL pasted from clipboard", 30000);
                    } else {
                        Alert.alertDialog(this,"New site - url from browser", "No valid URL in clipboard", 30000);
                    }
                }
            } else {
                Alert.alertDialog(this,"New site - url from browser", "clipboard void", 30000);
            }
        });
        */
        // modify record
        findViewById(R.id.ButtonPasteUrl).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    CharSequence clipText = clip.getItemAt(0).getText();
                    if (clipText != null && Patterns.WEB_URL.matcher(clipText).find()) {
                        String url = clipText.toString().trim();
                        handleUrlInsertion(url);
                        Alert.alertDialog(this, "New site - url from browser", "URL pasted from clipboard", 30000);
                    } else {
                        Alert.alertDialog(this, "New site - url from browser", "No valid URL in clipboard", 30000);
                    }
                }
            } else {
                Alert.alertDialog(this, "New site - url from browser", "clipboard void", 30000);
            }
        });

        // -------------------------------------------------------------------- 6 - Button RefreshNameIcon
        findViewById(R.id.ButtonRefreshNameIcon).setOnClickListener(v -> {
            String url = editUrl.getText().toString().trim();
            String currentName = editName.getText().toString().trim();

            if (!url.isEmpty()) {
                String suggestedName = suggestNameFromUrl(url);
                editName.setText(suggestedName);

                System.out.println("@@@ ButtonRefreshNameIcon - URL: " + url);
                System.out.println("@@@ ButtonRefreshNameIcon - suggestedName: " + suggestedName);

                fetchSiteIcon(url, imageViewIcon);

                Alert.alertDialog(this, "Dati aggiornati", "Nome e icona aggiornati dal sito", 2000);
            } else {
                Alert.alertDialog(this, "URL mancante", "Inserisci prima un URL valido", 3000);
            }
        });

        //-------------------------------------------------------------------- 7 - Button Select Type1 or Type2
        WebdeskDAO dao = new WebdeskDAO(this);
        //List<WebdeskType> typeList = dao.typesWebdesk(typeField);

        TypeDropdownAdapter adapter = new TypeDropdownAdapter(this, typeList);
        //editType1.setAdapter(adapter);

        // Mostra il menu a tendina al click del pulsante
        //buttonSelectType1 = findViewById(R.id.buttonSelectType1);List<WebdeskType> typeList = dao.typesWebdesk(typeField);

        //buttonSelectType1.setOnClickListener(v -> editType1.showDropDown());

        editType1 = findViewById(R.id.editType1);
        buttonSelectType1 = findViewById(R.id.buttonSelectType1);
        setupTypeDropdown(editType1, buttonSelectType1, "Type1");

        editType2 = findViewById(R.id.editType2);
        buttonSelectType2 = findViewById(R.id.buttonSelectType2);
        setupTypeDropdown(editType2, buttonSelectType2, "Type2");


        // Quando selezioni un item dal menu, aggiorna il testo (anche se convertToString già lo fa)
        editType1.setOnItemClickListener((parent, view, position, id) -> {
            WebdeskType selected = (WebdeskType) parent.getItemAtPosition(position);
            editType1.setText(selected.getType1()); // già fatto implicitamente, ma se vuoi fare qualcosa in più, qui lo puoi fare
        });


        //-------------------------------------------------------------------- 7 - Button Home
        findViewById(R.id.ButtonHome).setOnClickListener(v -> finish());

    }

    //------------------------------- adapter for Type1 or Type 2
    private void setupTypeDropdown(AutoCompleteTextView editText, ImageButton button, String typeField) {
        WebdeskDAO dao = new WebdeskDAO(this);
        //typeField = "Type1";
        //List<WebdeskType> typeList = dao.typesWebdesk(typeField);
        List<WebdeskType> typeList = dao.type1Webdesk();

        TypeDropdownAdapter adapter = new TypeDropdownAdapter(this, typeList);
        editText.setAdapter(adapter);

        button.setOnClickListener(v -> editText.showDropDown());

        editText.setOnItemClickListener((parent, view, position, id) -> {
            WebdeskType selected = (WebdeskType) parent.getItemAtPosition(position);
            editText.setText(selected.getType1());
        });
    }

    //========================================================================= Inner Class - TypeDropdownAdapter
    // used by button select type1
    private class TypeDropdownAdapter extends ArrayAdapter<WebdeskType> {
        private final LayoutInflater inflater;
        private final List<WebdeskType> types;

        public TypeDropdownAdapter(Context context, List<WebdeskType> types) {
            super(context, 0, types);
            inflater = LayoutInflater.from(context);
            this.types = types;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Vista per la EditText (quella che mostra il valore selezionato)
            return createItemView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Vista per l’elenco popup dropdown
            return createItemView(position, convertView, parent);
        }

        private View createItemView(int position, View convertView, ViewGroup parent) {
            //View view = LayoutInflater.from(getContext()).inflate(R.layout.dropdown_item_type, parent, false);
            View view = inflater.inflate(R.layout.dropdown_item_type, parent, false);
            TextView typeText = view.findViewById(R.id.typeText);
            TextView freqText = view.findViewById(R.id.freqText);

            WebdeskType item = getItem(position);
            if (item != null) {
                String type = item.getType1();
                //typeText.setText(type1);
                typeText.setText(item.getType1());
                freqText.setText("(" + item.getFreq1() + ")");

                // Evidenzia il selezionato
                if (type != null && type.equalsIgnoreCase(editType1.getText().toString().trim())) {
                    typeText.setTypeface(null, Typeface.BOLD);
                } else {
                    typeText.setTypeface(null, Typeface.NORMAL);
                }
            }

            return view;
        }

        // serve per mostrare solo il nome nel campo editType1
        public CharSequence convertToString(Object item) {
            if (item instanceof WebdeskType) {
                return ((WebdeskType) item).getType1(); // mostra solo il nome del tipo
            }
            return ""; // fallback sicuro
        }
    }

    //EditText editType1 = findViewById(R.id.editType1);

    // Supponendo che tu abbia già la lista aggiornata di WebdeskType con frequenze:
    //List<WebdeskType> typesList = dao.typesWebdesk(); // o come la carichi tu
/*
    TypeDropdownAdapter adapter = new TypeDropdownAdapter(this, typesList);

    // Usa AutoCompleteTextView invece di EditText per poter avere dropdown con suggerimenti:
    AutoCompleteTextView autoCompleteType1 = (AutoCompleteTextView) editType1;

    // Imposta l'adapter sul campo
    autoCompleteType1.setAdapter(adapter);

    // Opzionale: comportamento quando selezioni un valore dalla lista
    autoCompleteType1.setOnItemClickListener((parent, view, position, id) -> {
            WebdeskType selected = adapter.getItem(position);
            if (selected != null) {
                autoCompleteType1.setText(selected.getType1());
                // Puoi aggiungere logica extra se serve
            }
        });
*/
    //========================================================================= Functions

    //------------------------------------------------------------------------- populateFields
    private void populateFields(WebdeskSite site) {
        editName.setText(site.getName());
        editUrl.setText(site.getUrl());
        editIcon.setText(site.getIcon());
        editType1.setText(site.getType1());
        editType2.setText(site.getType2());
        editNote.setText(site.getNote());
        editOrder1.setText(String.valueOf(site.getOrder1()));
        editOrder2.setText(String.valueOf(site.getOrder2()));
        editDateCreate.setText(site.getDateCreate());
        editDateVisit.setText(site.getDateVisit());
        editFrequency.setText(String.valueOf(site.getFrequency()));
        editTextColor.setText(site.getTextColor());
        editBackground.setText(site.getBackground());
        editFlag1.setText(String.valueOf(site.getFlag1()));
        editFlag2.setText(String.valueOf(site.getFlag2()));
    }

    /*---------------------------------------------------------------------- collectFields
    Collects all the data visible and editable in the view.
    It doesn't include the id, because it isn't modifiable,
    and this collection is also used for the new record.
     */
    private WebdeskSite collectFields() {
        WebdeskSite site = new WebdeskSite();
        site.setName(editName.getText().toString());
        site.setUrl(editUrl.getText().toString());
        site.setIcon(editIcon.getText().toString());
        site.setType1(editType1.getText().toString());
        site.setType2(editType2.getText().toString());
        site.setNote(editNote.getText().toString());
        site.setOrder1(parseInt(editOrder1));
        site.setOrder2(parseInt(editOrder2));
        site.setDateCreate(editDateCreate.getText().toString());
        site.setDateVisit(editDateVisit.getText().toString());
        site.setFrequency(parseInt(editFrequency));
        site.setTextColor(editTextColor.getText().toString());
        site.setBackground(editBackground.getText().toString());
        site.setFlag1(parseInt(editFlag1));
        site.setFlag2(parseInt(editFlag2));
        return site;
    }

    private int parseInt(EditText e) {
        try {
            return Integer.parseInt(e.getText().toString());
        } catch (Exception ex) {
            return 0;
        }
    }

    //------------------------------------------------------------------------- suggestNameFromUrl
    // insert new site, from url string suggest the name of site
    private String suggestNameFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            String path = uri.getPath();

            if (host == null) return "";

            // Rimuove www.
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            // Estrai pezzo utile del path
            String[] pathParts = path.split("/");
            String extra = pathParts.length > 1 ? pathParts[1].replace("-", " ") : "";

            // Costruisci suggerimento
            if (!extra.isEmpty()) {
                return capitalizeWords(extra + " (" + host + ")");
            } else {
                return capitalizeWords(host);
            }

        } catch (Exception e) {
            return "";
        }
    }

    private String capitalizeWords(String input) {
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.length() > 0) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                        .append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    //------------------------------------------------------------------------- fetchSiteIcon
    // insert new site, from url string search a icon file o logo file and capture the uri of this file
    private void fetchSiteIcon(String url, ImageView imageView) {
        System.out.println("@@@ 259 - fetchSiteIcon: " +  url);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 1. Ricava dominio base (es: https://www.example.com)
                URI uri = new URI(url);
                String baseUrl = uri.getScheme() + "://" + uri.getHost();

                // 2. Prova favicon standard
                String faviconUrl = baseUrl + "/favicon.ico";
                System.out.println("@@@ 268 - fetchSiteIcon: " +  faviconUrl);
                // 3. Prova a scaricare HTML e cercare <link rel="icon">
                Document doc = Jsoup.connect(baseUrl).get();
                Elements icons = doc.select("link[rel~=(?i)^(shortcut )?icon$]");
                System.out.println("@@@ 268 - fetchSiteIcon: " +  icons);
                if (!icons.isEmpty()) {
                    String href = icons.first().attr("href");
                    if (!href.startsWith("http")) {
                        // Favicon relativa → converti in assoluta
                        if (href.startsWith("/")) {
                            faviconUrl = baseUrl + href;
                        } else {
                            faviconUrl = baseUrl + "/" + href;
                        }
                    } else {
                        faviconUrl = href;
                    }
                }

                String finalFaviconUrl = faviconUrl;

                new Handler(Looper.getMainLooper()).post(() -> {
                    // 4. Mostra l’icona sull’ImageView usando Glide (sulla UI thread)
                    Glide.with(imageView.getContext())
                            .load(finalFaviconUrl)
                            .placeholder(R.drawable.placeholder_icon) // immagine predefinita
                            .error(R.drawable.error_icon) // fallback se fallisce
                            .into(imageView);
                    // Mostra URL anche nell'EditText
                    if (editIcon != null) {
                        editIcon.setText(finalFaviconUrl);
                    }
                });

            } catch (Exception e) {
                System.out.println("@@@ 297 - exception: " + e);
                e.printStackTrace();
            }
        });
    }

    //------------------------------------------------------------------------- handleUrlInsertion
    // method that active all
    private void handleUrlInsertion(String url) {
        editUrl.setText(url);
        String currentName = editName.getText().toString().trim();

        if (currentName.isEmpty()) {
            String suggestedName = suggestNameFromUrl(url);
            editName.setText(suggestedName);
            fetchSiteIcon(url, imageViewIcon);
            System.out.println("@@@ handleUrlInsertion - suggestedName: " + suggestedName);
        }
    }


}
