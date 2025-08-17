package com.nic.webdesk;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WebdeskRightAdapter extends RecyclerView.Adapter<WebdeskRightAdapter.ViewHolder> {

    public interface OnFocusChangedListener {
        void onFocusChanged(int id, String name);
    }

    private List<WebdeskItem> itemList;
    private WebdeskDAO webdeskDao;
    private OnFocusChangedListener focusChangedListener;

    public WebdeskRightAdapter(List<WebdeskItem> itemList, WebdeskDAO dao, OnFocusChangedListener listener) {
        this.itemList = itemList;
        this.webdeskDao = dao;
        this.focusChangedListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.webdesk_right_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WebdeskItem item = itemList.get(position);

        holder.editType1.setText(item.getType1());
        holder.editType2.setText(item.getType2());

        holder.editOrder1.setText(item.getOrder1() != null ? String.valueOf(item.getOrder1()) : "");
        holder.editOrder2.setText(item.getOrder2() != null ? String.valueOf(item.getOrder2()) : "");

        holder.checkFlag1.setChecked(item.getFlag1() != null && item.getFlag1() == 1);
        holder.checkFlag2.setChecked(item.getFlag2() != null && item.getFlag2() == 1);

        // salva focus per delete
        holder.editType1.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                focusChangedListener.onFocusChanged(item.getId(), item.getName());
            }
        });

        // Type1 focus change update
        holder.editType1.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String newValue = holder.editType1.getText().toString();
                if (!newValue.equals(item.getType1())) {
                    item.setType1(newValue);
                    webdeskDao.updateWebdesk(item); // used specific metod Dao updateWebdesk(WebdeskItem item)
                }
            } else {
                focusChangedListener.onFocusChanged(item.getId(), item.getName());
            }
        });

        // Type2
        holder.editType2.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String newValue = holder.editType2.getText().toString();
                if (!newValue.equals(item.getType2())) {
                    item.setType2(newValue);
                    webdeskDao.updateWebdesk(item);
                }
            } else {
                focusChangedListener.onFocusChanged(item.getId(), item.getName());
            }
        });

        // Order1
        holder.editOrder1.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String val = holder.editOrder1.getText().toString();
                Integer newVal = val.isEmpty() ? null : Integer.valueOf(val);
                if (item.getOrder1() == null || !item.getOrder1().equals(newVal)) {
                    item.setOrder1(newVal);
                    webdeskDao.updateWebdesk(item);
                }
            } else {
                focusChangedListener.onFocusChanged(item.getId(), item.getName());
            }
        });

        // Order2
        holder.editOrder2.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String val = holder.editOrder2.getText().toString();
                Integer newVal = val.isEmpty() ? null : Integer.valueOf(val);
                if (item.getOrder2() == null || !item.getOrder2().equals(newVal)) {
                    item.setOrder2(newVal);
                    webdeskDao.updateWebdesk(item);
                }
            } else {
                focusChangedListener.onFocusChanged(item.getId(), item.getName());
            }
        });

        // Flag1
        holder.checkFlag1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setFlag1(isChecked ? 1 : 0);
            webdeskDao.updateWebdesk(item);
        });

        // Flag2
        holder.checkFlag2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setFlag2(isChecked ? 1 : 0);
            webdeskDao.updateWebdesk(item);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void setData(List<WebdeskItem> newData) {
        this.itemList = newData;
        notifyDataSetChanged();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText editType1, editType2, editOrder1, editOrder2;
        CheckBox checkFlag1, checkFlag2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editType1   = itemView.findViewById(R.id.editType1);
            editType2   = itemView.findViewById(R.id.editType2);
            editOrder1  = itemView.findViewById(R.id.editOrder1);
            editOrder2  = itemView.findViewById(R.id.editOrder2);
            checkFlag1  = itemView.findViewById(R.id.checkFlag1);
            checkFlag2  = itemView.findViewById(R.id.checkFlag2);
        }
    }
}
