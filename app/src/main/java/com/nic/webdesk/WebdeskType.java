package com.nic.webdesk;

//---------------------------------------------------------
// constructor WebdeskType(String type, int freq) used by type1/2Webdesk() in WebdeskDAO
public class WebdeskType {
    private String type1;
    private String type2;
    private String textColor;
    private String background;
    private int order1;          // order for Type1
    private int order2;          // order for Type2
    private int freq1;
    private int freq2;

    //------------------------------------------------- Constructor complete
    public WebdeskType(String type1, String type2, String textColor, String background, int order1, int order2) {
        this.type1 = type1;
        this.type2 = type2;
        this.textColor = textColor;
        this.background = background;
        this.order1 = order1;
        this.order2 = order2;
    }

    //------------------------------------------------- Constructor short
    /*
    new WebdeskType("type1value", null, 5, 0);  // solo freq1
    new WebdeskType("type1value", "type2value", 0, 7);  // solo freq2
    */
    public WebdeskType(String type1, String type2, int freq1, int freq2) {
        this.type1 = type1;
        this.type2 = type2;
        this.freq1 = freq1; // default
        this.freq2 = freq2; // default
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

    public int getFreq1() { return freq1; }
    public void setFreq1(int freq1) { this.freq1 = freq1; }

    public int getFreq2() { return freq2; }
    public void setFreq2(int freq2) { this.freq2 = freq2; }

}
