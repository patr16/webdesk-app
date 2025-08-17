package com.nic.webdesk;

public class WebdeskItem {

    private int id;
    private Integer userCod;
    private String name;
    private String url;
    private String icon;
    private String type1;
    private String type2;
    private String note;
    private Integer order1;
    private Integer order2;
    private String dateCreate;
    private String dateVisit;
    private Integer frequency;
    private String textColor;
    private String background;
    private Integer flag1;
    private Integer flag2;

    // Costruttore vuoto
    public WebdeskItem() { }

    // Getter e Setter
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public Integer getUserCod() {
        return userCod;
    }
    public void setUserCod(Integer userCod) {
        this.userCod = userCod;
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

    public Integer getOrder1() {
        return order1;
    }
    public void setOrder1(Integer order1) {
        this.order1 = order1;
    }

    public Integer getOrder2() {
        return order2;
    }
    public void setOrder2(Integer order2) {
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

    public Integer getFrequency() {
        return frequency;
    }
    public void setFrequency(Integer frequency) {
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

    public Integer getFlag1() {
        return flag1;
    }
    public void setFlag1(Integer flag1) {
        this.flag1 = flag1;
    }

    public Integer getFlag2() {
        return flag2;
    }
    public void setFlag2(Integer flag2) {
        this.flag2 = flag2;
    }
}
