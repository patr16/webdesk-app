package com.nic.webdesk;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WebdeskDynamicAdapter extends RecyclerView.Adapter<WebdeskDynamicAdapter.DynamicViewHolder> {
    private Context context;

    public interface OnFocusChangedListener {
        void onFocusChanged(int id, String name);
    }

    private List<WebdeskItem> itemList;
    private List<String> visibleColumns;
    private WebdeskDAO webdeskDao;
    private OnFocusChangedListener focusChangedListener;

    public WebdeskDynamicAdapter(Context context, List<WebdeskItem> itemList, List<String> visibleColumns,
                                 WebdeskDAO dao, OnFocusChangedListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.visibleColumns = visibleColumns;
        this.webdeskDao = dao;
        this.focusChangedListener = listener;
    }

    @NonNull
    @Override
    public DynamicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // creiamo un LinearLayout orizzontale vuoto
        LinearLayout rowLayout = new LinearLayout(context);
        //LinearLayout rowLayout = new LinearLayout(parent.getContext()); // SAREBBE DA BUTATRE !!!!!!!!!!!!!!!!
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        ));
        return new DynamicViewHolder(rowLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull DynamicViewHolder holder, int position) {
        WebdeskItem item = itemList.get(position);

        // puliamo la riga e la ricostruiamo
        holder.rowLayout.removeAllViews();
        holder.fields.clear();

        for (String col : visibleColumns) {
            View cellView;

            if (col.equals("Flag1") || col.equals("Flag2")) {
                // Checkbox
                CheckBox cb = new CheckBox(holder.rowLayout.getContext());
                cb.setLayoutParams(defaultCheckBoxLayoutParams());

                // Imposta valore
                if (col.equals("Flag1")) {
                    cb.setChecked(item.getFlag1() != null && item.getFlag1() == 1);
                } else {
                    cb.setChecked(item.getFlag2() != null && item.getFlag2() == 1);
                }

                cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (col.equals("Flag1")) {
                        item.setFlag1(isChecked ? 1 : 0);
                    } else {
                        item.setFlag2(isChecked ? 1 : 0);
                    }
                    webdeskDao.updateWebdesk(item);
                });

                cellView = cb;
            } else {
                EditText ed = new EditText(holder.rowLayout.getContext());
                ed.setLayoutParams(defaultEditTextLayoutParams(col));
                ed.setSingleLine(true);

                // imposta il testo iniziale
                switch (col) {
                    case "Url": ed.setText(item.getUrl()); break;
                    case "Icon": ed.setText(item.getIcon()); break;
                    case "Type1": ed.setText(item.getType1()); break;
                    case "Type2": ed.setText(item.getType2()); break;
                    case "Note": ed.setText(item.getNote()); break;
                    case "Order1":
                        ed.setText(item.getOrder1() != null ? String.valueOf(item.getOrder1()) : "");
                        ed.setInputType(InputType.TYPE_CLASS_NUMBER);
                        break;
                    case "Order2":
                        ed.setText(item.getOrder2() != null ? String.valueOf(item.getOrder2()) : "");
                        ed.setInputType(InputType.TYPE_CLASS_NUMBER);
                        break;
                    case "DateCreate": ed.setText(item.getDateCreate()); break;
                    case "DateVisit": ed.setText(item.getDateVisit()); break;
                    case "Frequency":
                        ed.setText(item.getFrequency() != null ? String.valueOf(item.getFrequency()) : "");
                        ed.setInputType(InputType.TYPE_CLASS_NUMBER);
                        break;
                    case "TextColor": ed.setText(item.getTextColor()); break;
                    case "Background": ed.setText(item.getBackground()); break;
                }

                ed.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        String val = ed.getText().toString();

                        // salva nei campi dell'item
                        switch (col) {
                            case "Url": item.setUrl(val); break;
                            case "Icon": item.setIcon(val); break;
                            case "Type1": item.setType1(val); break;
                            case "Type2": item.setType2(val); break;
                            case "Note": item.setNote(val); break;
                            case "Order1":
                                item.setOrder1(val.isEmpty() ? null : Integer.valueOf(val));
                                break;
                            case "Order2":
                                item.setOrder2(val.isEmpty() ? null : Integer.valueOf(val));
                                break;
                            case "DateCreate": item.setDateCreate(val); break;
                            case "DateVisit": item.setDateVisit(val); break;
                            case "Frequency":
                                item.setFrequency(val.isEmpty() ? null : Integer.valueOf(val));
                                break;
                            case "TextColor": item.setTextColor(val); break;
                            case "Background": item.setBackground(val); break;
                        }

                        webdeskDao.updateWebdesk(item);
                    } else {
                        // focus gained: usiamo per delete
                        focusChangedListener.onFocusChanged(item.getId(), item.getName());
                    }
                });

                cellView = ed;
            }

            holder.rowLayout.addView(cellView);
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public void setVisibleColumns(List<String> newColumns) {
        this.visibleColumns = newColumns;
        notifyDataSetChanged();
    }

    public void setData(List<WebdeskItem> newData) {
        this.itemList = newData;
        notifyDataSetChanged();
    }


    static class DynamicViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rowLayout;
        List<View> fields;

        public DynamicViewHolder(@NonNull View itemView) {
            super(itemView);
            rowLayout = (LinearLayout) itemView;
            fields = new java.util.ArrayList<>();
        }
    }

    // LayoutParams per EditText
    private LinearLayout.LayoutParams defaultEditTextLayoutParams(String colName) {
        int width = 120; // default
        if (colName.equals("Order1") || colName.equals("Order2") || colName.equals("Frequency")) {
            width = 80;
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                dpToPx(width),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        return lp;
    }

    // LayoutParams per CheckBox
    private LinearLayout.LayoutParams defaultCheckBoxLayoutParams() {
        return new LinearLayout.LayoutParams(
                dpToPx(60),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

}
