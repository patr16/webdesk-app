package com.nic.webdesk;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WebdeskSite {
    private int id;  // opzionale, utile per aggiornamento
    private String name;
    private String url;
    private String icon;
    private String type1;
    private String type2;
    private String note;
    private int order1;
    private int order2;
    private String dateCreate;
    private String dateVisit;
    private int frequency;
    private String textColor;
    private String background;
    private int flag1;
    private int flag2;

    //---------------------------------- Constructor void
    public WebdeskSite() {
        // serve per creare oggetto vuoto e poi usare i setter
    }

    //---------------------------------- Constructor for WebdeskDAO
    // usato in WebdeskDAO - public List<WebdeskSite> sitesWebdesk(String type1)
    public WebdeskSite(int id, String name, String url, String textColor, String background, String icon, int frequency) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.textColor = textColor;
        this.background = background;
        this.icon = icon;
        this.frequency = frequency;
    }

    //---------------------------------- Constructor for EditSiteActivity
    public WebdeskSite(int id, String name, String url, String icon, String type1, String type2,
                       String note, int order1, int order2, String dateCreate, String dateVisit,
                       int frequency, String textColor, String background, int flag1, int flag2) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.icon = icon;
        this.type1 = type1;
        this.type2 = type2;
        this.note = note;
        this.order1 = order1;
        this.order2 = order2;
        this.dateCreate = dateCreate;
        this.dateVisit = dateVisit;
        this.frequency = frequency;
        this.textColor = textColor;
        this.background = background;
        this.flag1 = flag1;
        this.flag2 = flag2;
    }
// --- Getter e Setter ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType1() {
        return type1;
    }

    public void setType1(String type1) {
        this.type1 = type1;
    }

    public String getType2() {
        return type2;
    }

    public void setType2(String type2) {
        this.type2 = type2;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getOrder1() {
        return order1;
    }

    public void setOrder1(int order1) {
        this.order1 = order1;
    }

    public int getOrder2() {
        return order2;
    }

    public void setOrder2(int order2) {
        this.order2 = order2;
    }

    public String getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(String dateCreate) {
        this.dateCreate = dateCreate;
    }

    public String getDateVisit() {
        return dateVisit;
    }

    public void setDateVisit(String dateVisit) {
        this.dateVisit = dateVisit;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }
    public int getFlag1() {
        return flag1;
    }

    public void setFlag1(int flag1) {
        this.flag1 = flag1;
    }

    public int getFlag2() {
        return flag2;
    }

    public void setFlag2(int flag2) {
        this.flag2 = flag2;
    }
}
