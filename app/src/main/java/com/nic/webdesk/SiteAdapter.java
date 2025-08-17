package com.nic.webdesk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//-------------------------------------------------------------
// adapter for item_site.xml in activity_sites.xml RecyclerView
//-------------------------------------------------------------
public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.SiteViewHolder> {

    private final Context context;
    private final List<WebdeskSite> siteList;


    private final Map<String, Integer> iconSizeCache = new HashMap<>();
    public SiteAdapter(Context context, List<WebdeskSite> siteList) {
        this.context = context;
        this.siteList = siteList;
    }

    @NonNull
    @Override
    public SiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_site, parent, false);
        return new SiteViewHolder(view);
    }

     @Override
    public void onBindViewHolder(@NonNull SiteViewHolder holder, int position) {
        System.out.println("@@@ 1 - BINDING position: " + position + " / " + getItemCount());

        WebdeskSite site = siteList.get(position);

        // Site name
        holder.textSiteName.setText(site.getName());

        // Frequency
        int freq = site.getFrequency();
        holder.textFrequency.setText(String.valueOf(freq));

       //----------------------------------------- setting icon dimensions
        String iconUrl = site.getIcon();
        if (iconUrl != null && !iconUrl.isEmpty()) {
            //System.out.println("@@@ 3 - SiteAdapterColor: " + site.getTextColor() + ", Bg: " + site.getBackground());
            //---------------- Calcola la dimensione in pixel da dp (es. 128dp → pixel reali a seconda del display)
            int sizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 64, context.getResources().getDisplayMetrics()); // 128 64 56 48
            //---------------- Calcola la dimensione i
            Glide.with(context)
                    .load(iconUrl)
                    .override(sizeInPx, sizeInPx)           // dimensione reale in pixel
                    .fitCenter()                            // mantiene proporzioni ma cerca di riempire il contenitore
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(holder.iconSite);

        } else {
            holder.iconSite.setImageResource(R.drawable.ic_placeholder); // fallback
        }

        // Colori dinamici con utility ColorUtils (try catch into utility)
        int textColor = ColorUtils.resolveColor(site.getTextColor());
        holder.textSiteName.setTextColor(textColor);

        int backgroundColor = ColorUtils.resolveColor(site.getBackground());
        holder.cardContainer.setCardBackgroundColor(backgroundColor);
        
        // MODIFY !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // nella creazione di nuove card lo sfondo nasce white e anche il colore del testo
        // come soluzione temporanea si forza in tal caso il colore del testo a nero
        if (backgroundColor== 0xffffffff) {                 // color background: white
            holder.textSiteName.setTextColor(0xFF000000);   // color text: black
        }

        //------------------------------------------------ onClick
         // goto web site / edit card
        holder.itemView.setOnClickListener(v -> {
            System.out.println("@@@ - 95  url: " + site.getUrl() + " - id: " +  site.getId());
            // Azione click - apri browser con URL
            if (!SitesActivity.isEditMode) {
                String url = site.getUrl();
                if (url != null && !url.isEmpty()) {
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "http://" + url;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(intent);
                }
            } else {
                // Modalità modifica - apri activity di editing
                Intent intent = new Intent(context, EditSiteActivity.class);
                intent.putExtra("siteId", site.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return siteList.size();
    }

        public static class SiteViewHolder extends RecyclerView.ViewHolder {
        TextView textSiteName;
        TextView textFrequency;
        ImageView iconSite;
        CardView cardContainer;

        public SiteViewHolder(@NonNull View itemView) {
            super(itemView);
            textSiteName = itemView.findViewById(R.id.textSiteName);
            textFrequency = itemView.findViewById(R.id.textFrequency);
            iconSite = itemView.findViewById(R.id.iconSite);
            cardContainer = itemView.findViewById(R.id.cardContainer);
        }
    }
}
