package com.nic.webdesk;

import android.content.Context;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
//------------------------------ import from WebdeskTableActivity
import com.nic.webdesk.WebdeskTableActivity.ColumnConfig;
import com.nic.webdesk.WebdeskTableActivity.FieldType;

public class RightAdapter extends RecyclerView.Adapter<RightAdapter.ViewHolder> {

    private List<WebdeskItem> itemList = new ArrayList<>();
    private List<ColumnConfig> currentColumnConfigs = new ArrayList<>();
    private final WebdeskDAO webdeskDao;
    private final Context context; // Aggiunto per ViewHolder

    //----------------------------------- Listener specifici dell'adapter
    private OnFocusChangedListener focusChangedListener;
    private OnDataSavedListener dataSavedListener;
    private OnItemClickListener itemClickListener;
    public interface OnFocusChangedListener {
        void onFocusChanged(int id, String name);
    }

    public interface OnDataSavedListener {
             void onDataSaved(); // Per notificare l'activity che i dati sono cambiati e LeftAdapter necessita di refresh
    }

    public interface OnItemClickListener {
        void onItemClick(WebdeskItem item, int position);
        //itemView serve per mostrare il PopupMenu ancorato alla riga cliccata, serve per cambiare lo sfondo della riga
    }

    //=========================================================================== Constructor
    public RightAdapter(List<WebdeskItem> initialItems, WebdeskDAO dao,
                        OnFocusChangedListener focusListener, OnDataSavedListener savedListener, Context context) {

        //-------------------- Specifico per il button delete row
        if (initialItems != null) { // Aggiunto controllo e inizializzazione
            this.itemList.addAll(initialItems);
        }
        //--------------------
        this.webdeskDao = dao;
        this.focusChangedListener = focusListener;
        this.dataSavedListener = savedListener;
        this.context = context; // Salva il contesto passato
    }

    //-------------------- Set only for method Delete Row
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    //=========================================================================== Methods

    //--------------------------------------------------- Method Set Columns Configuration
    public void setColumnConfiguration(List<ColumnConfig> newConfig) {
        this.currentColumnConfigs = new ArrayList<>(newConfig); // Crea una copia
        // È fondamentale notificare che l'intera struttura è cambiata,
        // non solo i dati. Questo forzerà onCreateViewHolder per tutte le view visibili.
        notifyDataSetChanged();
    }

    //--------------------------------------------------- Method onCreateViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row_right, parent, false);
        return new ViewHolder(view, parent.getContext());
    }
    //--------------------------------------------------- Method onBindViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final WebdeskItem item = itemList.get(position); // L'item per questa riga
        holder.bind(item, currentColumnConfigs, webdeskDao,
                focusChangedListener, dataSavedListener, itemList);
    }
    //--------------------------------------------------- Method getItemCount
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    //--------------------------------------------------- Method Set Data con DiffUtil
    /*
    Metodo per aggiornare i dati dell'adapter usando DiffUtil
    È importante passare una nova lista a DiffUtil se la lista precedente è stata modificata
    direttamente (come accade qui quando aggiorniamo currentItem.setXXX()).
    Per una corretta implementazione di DiffUtil quando gli oggetti stessi nella lista
    vengono mutati, dovremmo passare una copia della lista prima delle modifiche
    e la lista dopo le modifiche.
    Tuttavia, in questo scenario, 'setData' è chiamato dall'Activity con una lista fresca dal DB.
    Quindi, la 'this.itemList' corrente è la "vecchia" lista rispetto a 'newData'.
   */
    public void setData(List<WebdeskItem> newData) {
        //if (newData != null && !newData.isEmpty()) {
        if (newData != null) {
            if (!newData.isEmpty()) {
                // Stampa il Type1 (o il campo per cui hai ordinato) e Name dei primi elementi ricevuti
                System.out.println("@@@ 121 ADAPTER_SET_DATA: Primo elemento newData: Type1=" + newData.get(0).getType1() + ", Name=" + newData.get(0).getName());
                if (newData.size() > 1) {
                    System.out.println("@@@ 123 ADAPTER_SET_DATA: Secondo elemento newData: Type1=" + newData.get(1).getType1() + ", Name=" + newData.get(1).getName());
                }
            }
        }

        if (this.itemList == null) {
            //--- Inizializzazione di sicurezza
            this.itemList = new ArrayList<>();
        }
        //--- Ccontrollo per newData null
        if (newData == null) {
            newData = new ArrayList<>();
        }
        // --- Punto critico: DiffUtil
        WebdeskItemDiffCallback diffCallback = new WebdeskItemDiffCallback(new ArrayList<>(this.itemList), newData); // Passa una copia della vecchia lista
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        //--- Aggiorna la lista interna dell'adapter
        this.itemList.clear();
        this.itemList.addAll(newData);

        /* CLEAN
        if (this.itemList != null && !this.itemList.isEmpty()) { // Controllo aggiunto per sicurezza
            System.out.println("@@@ 162 ADAPTER_SET_DATA: Primo elemento this.itemList (NUOVA): Type1=" + this.itemList.get(0).getType1() + ", Name=" + this.itemList.get(0).getName());
            if (this.itemList.size() > 1) {
                System.out.println("@@@ 164 ADAPTER_SET_DATA: Secondo elemento this.itemList (NUOVA): Type1=" + this.itemList.get(1).getType1() + ", Name=" + this.itemList.get(1).getName());
            }
        }
        */

        diffResult.dispatchUpdatesTo(this);
    }

    //===========================================================================  Class Internal ViewHolder
    // per il metodo delete row togliamo static! public static class ViewHolder extends RecyclerView.ViewHolder {
    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout dynamicColumnsContainer;
        Context context;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            dynamicColumnsContainer = itemView.findViewById(R.id.dynamic_columns_container);
            this.context = context;

            //----------------------------------------------------------------------------------
            // Specifico per method Delete Row
            // Imposta il click listener sull'intera riga (itemView)
            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                System.out.println("@@@ 212 ViewHolder - position: " + position);
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    // Passiamo l'item intero, così l'activity ha accesso a ID e altri campi se servono
                    WebdeskItem clickedItem = itemList.get(position);
                    itemClickListener.onItemClick(clickedItem, position);
                }
            });
            //----------------------------------------------------------------------------------

        }

        //--------------------------------------------------------------------------- Class ViewHolder - Method bind
        public void bind(final WebdeskItem item,
                         final List<ColumnConfig> columnConfigs,
                         final WebdeskDAO dao,
                         final OnFocusChangedListener focusListener,
                         final OnDataSavedListener savedListener,
                         final List<WebdeskItem> currentItemList // Passa la lista corrente per l'aggiornamento
        ) {
            // Rimuovi tutte le view precedenti dal contenitore prima di aggiungerne di nuove.
            // Questo è cruciale perché il ViewHolder viene riutilizzato.
            dynamicColumnsContainer.removeAllViews();

            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

            for (ColumnConfig config : columnConfigs) {
                View columnView = null;
                final String dbFieldName = config.dbFieldName;
                // --- Creazione della View per la colonna
                if (config.fieldType == FieldType.BOOLEAN) {
                    CheckBox checkBox = new CheckBox(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, config.columnWidthDp, displayMetrics),
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    checkBox.setLayoutParams(params);
                    checkBox.setGravity(Gravity.CENTER); // Centra la checkbox nel suo spazio

                    //--- Popolamento
                    Integer flagValue = (Integer) getItemValue(item, dbFieldName);
                    checkBox.setChecked(flagValue != null && flagValue == 1);

                    //--- Listener
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        setItemValue(item, dbFieldName, isChecked ? 1 : 0);
                        boolean success = dao.updateWebdeskItemField(item.getId(), dbFieldName, isChecked ? 1 : 0);
                        if (success) {
                            if (focusListener != null) { // Notifica il focus anche se non è un EditText
                                focusListener.onFocusChanged(item.getId(), item.getName());
                            }
                            // Opzionale: notifica il salvataggio se necessario per refresh immediato
                            // if (savedListener != null) savedListener.onDataSaved();

                            // Aggiorna l'item nella lista dell'adapter senza ricaricare tutto
                            int itemPosition = currentItemList.indexOf(item);
                            if (itemPosition != -1) {
                                // Questo è un hotfix. Idealmente, DiffUtil dovrebbe gestire questo
                                // se areContentsTheSame confrontasse tutti i campi.
                                // O l'activity dovrebbe ricaricare i dati e aggiornare.
                            }

                        } else {
                            // Gestisci errore di salvataggio (es. ripristina valore)
                            //System.out.println("@@@ 167 - RightAdapter Failed to save " + dbFieldName + " for item " + item.getId());
                            buttonView.setChecked(!isChecked); // Ripristina
                        }
                    });
                    columnView = checkBox;

                } else { // TEXT o INTEGER (entrambi usano EditText per ora)
                    EditText editText = new EditText(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, config.columnWidthDp, displayMetrics),
                            //ViewGroup.LayoutParams.WRAP_CONTENT
                            ViewGroup.LayoutParams.MATCH_PARENT // cambiare questo per occupare l'altezza della riga
                    );
                    editText.setLayoutParams(params);
                    editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    editText.setSingleLine(true); // Spesso non necessario se l'EditText è alto quanto una riga e il testo è breve
                    editText.setImeOptions(EditorInfo.IME_ACTION_DONE); // oppure IME_ACTION_NEXT per gestire la navigazione
                    editText.setBackgroundResource(R.drawable.cell_background_with_underline); // xlm per dare  il backgranud uguale alla left column
                    if (config.fieldType == FieldType.INTEGER) {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        // Si può anche aggiungere InputType.TYPE_NUMBER_FLAG_SIGNED o DECIMAL se necessario
                    } else { // FieldType.TEXT
                        editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    }

                    // Popolamento
                    Object value = getItemValue(item, dbFieldName);
                    editText.setText(value != null ? String.valueOf(value) : "");

                    // Listener per il focus e il salvataggio
                    editText.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) {

                            //----------------------------- SharePref id and Name for Delete Row e Go Site
                            // carica i dati per i button delete row e button goto site
                            SharedPrefs prefs = SharedPrefs.getInstance(context);
                            prefs.setSelectedItemId(item.getId());
                            prefs.setSelectedItemName(item.getName());
                            //-----------------------------

                            if (focusListener != null) {
                                focusListener.onFocusChanged(item.getId(), item.getName());
                            }
                        } else {
                            // Salva quando perde il focus
                            String newValueStr = ((EditText) v).getText().toString();
                            Object oldValue = getItemValue(item, dbFieldName);
                            String oldValueStr = oldValue != null ? String.valueOf(oldValue) : "";

                            // Salva solo se il valore è effettivamente cambiato
                            if (!Objects.equals(newValueStr, oldValueStr)) {
                                try {
                                    Object valueToSave = null;
                                    if (config.fieldType == FieldType.INTEGER) {
                                        if (newValueStr.isEmpty()) {
                                            valueToSave = null; // o 0, a seconda della tua logica di business
                                        } else {
                                            valueToSave = Integer.parseInt(newValueStr);
                                        }
                                    } else { // TEXT
                                        valueToSave = newValueStr;
                                    }

                                    setItemValue(item, dbFieldName, valueToSave);
                                    boolean success = dao.updateWebdeskItemField(item.getId(), dbFieldName, valueToSave);

                                    if (success) {
                                        if (savedListener != null) {
                                            // savedListener.onDataSaved(); // Questo ricarica tutto
                                        }
                                    } else {
                                        ((EditText) v).setText(oldValueStr); // Ripristina il valore precedente
                                    }
                                } catch (NumberFormatException e) {
                                    ((EditText) v).setText(oldValueStr); // Ripristina
                                    // Mostra un Toast all'utente se l'input non è valido
                                }
                            }
                        }
                    });

                    // Opzionale: Listener per IME_ACTION_DONE per salvare
                    editText.setOnEditorActionListener((v, actionId, event) -> {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            v.clearFocus(); // Questo triggererà il OnFocusChangeListener per salvare
                            // Nascondi la tastiera (opzionale, ma buona UX)
                            // InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            // imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            return true;
                        }
                        return false;
                    });
                    columnView = editText;
                }

                if (columnView != null) {
                    dynamicColumnsContainer.addView(columnView);
                }
            } // Fine del loop for (ColumnConfig config : columnConfigs)
        } // Fine del metodo bind

        //--------------------------------------------------------------------------- Class ViewHolder - Methods getItemValue, setItemValue
        // Helper methods per ottenere/impostare valori da WebdeskItem dinamicamente ---
        // Questi metodi usano uno switch sul dbFieldName per chiamare i getter/setter corretti.
        // In alternativa, si potrebbe usare la Reflection, ma è più lenta e complessa.
        private Object getItemValue(WebdeskItem item, String dbFieldName) {
            switch (dbFieldName) {
                case "UserCod": return item.getUserCod();
                case "Name": return item.getName();
                case "Url": return item.getUrl();
                case "Icon": return item.getIcon();
                case "Type1": return item.getType1();
                case "Type2": return item.getType2();
                case "Note": return item.getNote();
                case "Order1": return item.getOrder1();
                case "Order2": return item.getOrder2();
                case "DateCreate": return item.getDateCreate();
                case "DateVisit": return item.getDateVisit();
                case "Frequency": return item.getFrequency();
                case "TextColor": return item.getTextColor();
                case "Background": return item.getBackground();
                case "Flag1": return item.getFlag1();
                case "Flag2": return item.getFlag2();
                default:
                    System.out.println("@@@ 290 - RightAdapter getItemValue: Unknown dbFieldName '" + dbFieldName + "'");
                    return null;
            }
        }

        private void setItemValue(WebdeskItem item, String dbFieldName, Object value) {
            try {
                switch (dbFieldName) {
                    case "UserCod": item.setUserCod(value != null ? (Integer) value : null); break;
                    case "Name": item.setName(value != null ? (String) value : null); break;
                    case "Url": item.setUrl(value != null ? (String) value : null); break;
                    case "Icon": item.setIcon(value != null ? (String) value : null); break;
                    case "Type1": item.setType1(value != null ? (String) value : null); break;
                    case "Type2": item.setType2(value != null ? (String) value : null); break;
                    case "Note": item.setNote(value != null ? (String) value : null); break;
                    case "Order1": item.setOrder1(value != null ? (Integer) value : null); break;
                    case "Order2": item.setOrder2(value != null ? (Integer) value : null); break;
                    case "DateCreate": item.setDateCreate(value != null ? (String) value : null); break;
                    case "DateVisit": item.setDateVisit(value != null ? (String) value : null); break;
                    case "Frequency": item.setFrequency(value != null ? (Integer) value : null); break;
                    case "TextColor": item.setTextColor(value != null ? (String) value : null); break;
                    case "Background": item.setBackground(value != null ? (String) value : null); break;
                    case "Flag1": item.setFlag1(value != null ? (Integer) value : null); break;
                    case "Flag2": item.setFlag2(value != null ? (Integer) value : null); break;
                    default:
                        System.out.println("@@@ 315 - RightAdapter setItemValue: Unknown dbFieldName '" + dbFieldName + "'");
                }
            } catch (ClassCastException e) {
                System.out.println("@@@ 318 - RightAdapter setItemValue: Type mismatch for field " + dbFieldName + " with value " + value + " " + e);
                // si può lanciare un'eccezione o gestire l'errore in modo più robusto
            }
        }
    } // Fine della classe ViewHolder
} // Fine della classe RightAdapter
