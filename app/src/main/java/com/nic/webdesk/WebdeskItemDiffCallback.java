package com.nic.webdesk;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;
import java.util.Objects;

/*
 WebdeskItemDiffCallback:
areItemsTheSame(): Questo metodo è cruciale. Dice a DiffUtil se due item in posizioni diverse nelle due liste sono in realtà lo stesso item logico.
Di solito, si usa un ID univoco. Se non hai un ID univoco stabile, DiffUtil sarà meno efficace.
areContentsTheSame(): Se areItemsTheSame() restituisce true, DiffUtil chiama questo metodo per vedere se il contenuto visibile dell'item è cambiato.
Se è cambiato, l'item verrà ridisegnato. Assicurati di confrontare tutti i campi che influenzano la UI.
Objects.equals(): È una buona pratica usare `Objects.equals(a,

 Verifica i dati in RightAdapter - setData
 */
class WebdeskItemDiffCallback extends DiffUtil.Callback {
    private final List<WebdeskItem> oldList;
    private final List<WebdeskItem> newList;

    public WebdeskItemDiffCallback(List<WebdeskItem> oldList, List<WebdeskItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    // Chiamato per decidere se due oggetti rappresentano lo stesso Item.
    // Solitamente si usa un ID univoco.
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
    }

    // Chiamato solo se areItemsTheSame() restituisce true.
    // Controlla se il contenuto dell'item è cambiato.
    // Dovresti confrontare tutti i campi che, se cambiati, richiedono un ridisegno dell'item.
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        WebdeskItem oldItem = oldList.get(oldItemPosition);
        WebdeskItem newItem = newList.get(newItemPosition);

        // Confronta tutti i campi rilevanti.
        // Assicurati di gestire correttamente i valori null.
        // Per gli oggetti, usa .equals(). Per i primitivi, usa ==.
        return Objects.equals(oldItem.getType1(), newItem.getType1()) &&
                Objects.equals(oldItem.getType2(), newItem.getType2()) &&
                Objects.equals(oldItem.getOrder1(), newItem.getOrder1()) &&
                Objects.equals(oldItem.getOrder2(), newItem.getOrder2()) &&
                Objects.equals(oldItem.getFlag1(), newItem.getFlag1()) &&
                Objects.equals(oldItem.getFlag2(), newItem.getFlag2()) &&
                Objects.equals(oldItem.getName(), newItem.getName()); // Se anche il nome può cambiare e influisce sulla UI
    }
}