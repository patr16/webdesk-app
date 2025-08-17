package com.nic.webdesk;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;

public class IconUrlUtils {

    public static String fixIconUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. Clearbit con URL codificato (es: https://logo.clearbit.com/http%3A%2F%2Fwww.site.it)
            if (rawUrl.startsWith("https://logo.clearbit.com/http")) {
                URI uri = new URI(rawUrl);
                String encodedPath = uri.getPath().substring(1); // rimuovi primo slash
                String decodedUrl = URLDecoder.decode(encodedPath, "UTF-8"); // http://www.site.it
                URL realUrl = new URL(decodedUrl);
                String host = realUrl.getHost(); // www.site.it
                return "https://logo.clearbit.com/" + host;
            }

            // 2. Google favicon con URL interno (accettiamo ma potremmo fare fallback)
            if (rawUrl.contains("t0.gstatic.com/faviconV2") && rawUrl.contains("url=")) {
                // potresti aggiungere logica di fallback qui se vuoi
                return rawUrl;
            }

            // 3. Se è già un URL diretto accettabile, lascialo stare
            return rawUrl;

        } catch (Exception e) {
            e.printStackTrace();
            return null; // fallback se c'è errore
        }
    }
}
