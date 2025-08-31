package com.nic.webdesk;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;
import java.util.Objects; // Per Objects.equals per gestire i null in modo pulito

public class LeftAdapterDiffCallback extends DiffUtil.Callback {

    private final List<WebdeskItem> oldList;
    private final List<WebdeskItem> newList;

    public LeftAdapterDiffCallback(List<WebdeskItem> oldList, List<WebdeskItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Verifica se gli item rappresentano lo stesso oggetto logico
        // Solitamente si usa l'ID univoco
        if (oldList == null || newList == null) return false;
        // Controlla anche che gli indici siano validi prima di accedere
        if (oldItemPosition >= oldList.size() || newItemPosition >= newList.size() || oldItemPosition < 0 || newItemPosition < 0) {
            return false;
        }
        return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Verifica se il contenuto visualizzato dell'item è cambiato
        // Per LeftAdapter, ci interessa solo il 'name'
        if (oldList == null || newList == null) return false;
        // Controlla anche che gli indici siano validi prima di accedere
        if (oldItemPosition >= oldList.size() || newItemPosition >= newList.size() || oldItemPosition < 0 || newItemPosition < 0) {
            return false;
        }
        WebdeskItem oldItem = oldList.get(oldItemPosition);
        WebdeskItem newItem = newList.get(newItemPosition);

        // Confronta solo 'name', gestendo i null in modo sicuro
        return Objects.equals(oldItem.getName(), newItem.getName());
    }
}