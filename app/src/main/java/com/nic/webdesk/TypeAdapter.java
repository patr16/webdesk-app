package com.nic.webdesk;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.TypeViewHolder> {

    private List<WebdeskType> typeList;
    private OnTypeClickListener listener;
    private int level;
    private int fontSizeSp = 16; // default value necessary also for RecyclerViewConfigurator class
    private GridLayoutManager gridLayoutManager;

    public TypeAdapter(List<WebdeskType> typeList, OnTypeClickListener listener, int level) {
        this.typeList = typeList;
        this.listener = listener;
        this.level = level; // 1 o 2
        this.fontSizeSp = fontSizeSp;
    }

    @NonNull
    @Override
    public TypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                //.inflate(R.layout.item_type_card, parent, false); textTypeName
                .inflate(R.layout.item_type, parent, false);
        return new TypeViewHolder(view);

        //-------------------------------- recyclerView and title (textType1)
        //recyclerView = findViewById(R.id.recyclerTypes);
    }

    //--------------------------------- set font size
    // necessary for setupRecyclerColumns, class RecyclerViewConfigurator
    public void setFontSizeSp(int fontSizeSp) {
        this.fontSizeSp = fontSizeSp;
        notifyDataSetChanged(); // update UI
    }

    @Override
    public void onBindViewHolder(@NonNull TypeViewHolder holder, int position) {
        WebdeskType type = typeList.get(position);
        System.out.println("@@@ 37 TypeAdapter - onBindViewHolder pos=" + position + " level=" + level
                + " type1=" + type.getType1() + " type2=" + type.getType2()
                + " freq1=" + type.getFreq1() + " freq2=" + type.getFreq2());

        if (level == 1) {
            holder.textTypeName.setText(type.getType1() != null ? type.getType1() : ""); //NEW!!!!!
            //holder.textTypeName.setText(type.getType1());
            holder.textTypeFreq.setText(String.valueOf(type.getFreq1()));           // freq1
            holder.textTypeName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);    // for RecyclerViewConfigurator
            System.out.println("@@@ 40 TypeAdapter - onBindViewHolder type1: " + type.getType1() + " freq1: " + type.getFreq1());
        } else {
            holder.textTypeName.setText(type.getType2() != null ? type.getType2() : ""); // NEW!!!!!!!!!!!!!!!!!!
            //holder.textTypeName.setText(type.getType2());
            holder.textTypeFreq.setText(String.valueOf(type.getFreq2()));               // freq2
            holder.textTypeName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);    // for RecyclerViewConfigurator
            System.out.println("@@@ 44 TypeAdapter - onBindViewHolder type2: " + type.getType2() + " freq2: " + type.getFreq2());
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTypeClicked(type);
        });
    }

    @Override
    public int getItemCount() {
        return typeList != null ? typeList.size() : 0; //NEW!!!!!
        //return typeList.size();
    }

    public static class TypeViewHolder extends RecyclerView.ViewHolder {
        TextView textTypeName;
        TextView textTypeFreq;
        public TypeViewHolder(@NonNull View itemView) {
            super(itemView);
            textTypeName = itemView.findViewById(R.id.textTypeName);
            textTypeFreq = itemView.findViewById(R.id.textTypeFreq);
        }
    }

}
