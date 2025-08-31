package com.nic.webdesk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LeftAdapter extends RecyclerView.Adapter<LeftAdapter.LeftViewHolder> {

    private List<WebdeskItem> items;

    public LeftAdapter(List<WebdeskItem> initialItems) {
        // Inizializza con una nuova lista vuota
        this.items = (initialItems != null) ? new ArrayList<>(initialItems) : new ArrayList<>();
    }

    // Costruttore vuoto, se preferisci inizializzare e popolare solo tramite setData
    public LeftAdapter() {
        this.items = new ArrayList<>();
    }

    // Metodo per aggiornare i dati usando DiffUtil
    public void setData(List<WebdeskItem> newWebdeskItems) {
        // È buona pratica lavorare con una nuova lista per evitare problemi di concorrenza
        // o modifiche inaspettate durante il calcolo delle differenze.
        final List<WebdeskItem> newItems = (newWebdeskItems != null) ? new ArrayList<>(newWebdeskItems) : new ArrayList<>();

        // Crea il Callback
        final LeftAdapterDiffCallback diffCallback = new LeftAdapterDiffCallback(this.items, newItems);
        // Calcola le differenze
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        // Aggiorna la lista interna dell'adapter (importante fare prima di dispatchUpdates)
        this.items.clear();
        this.items.addAll(newItems);

        // Applica le modifiche al RecyclerView
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemCount() {
        // this.items è ora sempre inizializzato (vuoto o pieno)
        return items.size();
    }

    @NonNull
    @Override
    public LeftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row_left, parent, false);
        return new LeftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeftViewHolder holder, int position) {
        if (position >= 0 && position < items.size()) {
            WebdeskItem item = items.get(position);
            holder.textNameLeft.setText(item.getName());
        }
    }

    static class LeftViewHolder extends RecyclerView.ViewHolder {
        TextView textNameLeft;

        LeftViewHolder(@NonNull View itemView) {
            super(itemView);
            textNameLeft = itemView.findViewById(R.id.textNameLeft);
            if (textNameLeft == null) {
                // log o un'eccezione se l'ID non viene trovato
            }
        }
    }
}
