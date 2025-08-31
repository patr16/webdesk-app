// File: SortCriterion.java
package com.nic.webdesk; // o il tuo package

public class SortCriterion {
    private String dbFieldName;
    private boolean ascending;
    private int priority; // 1, 2, o 3

    public SortCriterion(String dbFieldName, boolean ascending, int priority) {
        this.dbFieldName = dbFieldName;
        this.ascending = ascending;
        this.priority = priority;
    }

    public String getDbFieldName() {
        return dbFieldName;
    }

    public boolean isAscending() {
        return ascending;
    }

    public int getPriority() {
        return priority;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
    // Potresti aggiungere equals e hashCode se necessario, ma per ora non è indispensabile
}
