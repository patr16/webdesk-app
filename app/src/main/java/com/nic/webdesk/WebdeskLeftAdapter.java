package com.nic.webdesk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WebdeskLeftAdapter extends RecyclerView.Adapter<WebdeskLeftAdapter.LeftViewHolder> {

    private List<WebdeskItem> data;

    public WebdeskLeftAdapter(List<WebdeskItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public LeftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_left_name, parent, false);
        return new LeftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeftViewHolder holder, int position) {
        WebdeskItem item = data.get(position);
        holder.textNameLeft.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    public void setData(List<WebdeskItem> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    static class LeftViewHolder extends RecyclerView.ViewHolder {
        TextView textNameLeft;

        LeftViewHolder(View itemView) {
            super(itemView);
            textNameLeft = itemView.findViewById(R.id.textNameLeft);
        }
    }
}
