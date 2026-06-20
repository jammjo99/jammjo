package com.calendar.model;

import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DayData {
    private String diary = "";
    private List<ChecklistItem> checklist = new ArrayList<>();

    public String getDiary() {
        return diary;
    }

    public void setDiary(String diary) {
        this.diary = diary != null ? diary : "";
    }

    public List<ChecklistItem> getChecklist() {
        return checklist;
    }

    public void setChecklist(List<ChecklistItem> checklist) {
        this.checklist = checklist != null ? checklist : new ArrayList<>();
    }

    public double getCompletionRate() {
        if (checklist == null || checklist.isEmpty()) {
            return -1;
        }
        long done = checklist.stream().filter(ChecklistItem::isCompleted).count();
        return (double) done / checklist.size() * 100;
    }

    public static final Color ACHIEVE_BLUE = new Color(37, 99, 235);
    public static final Color ACHIEVE_GREEN = new Color(22, 163, 74);
    public static final Color ACHIEVE_RED = new Color(220, 38, 38);
    public static final Color ACHIEVE_BLUE_BG = new Color(191, 219, 254);
    public static final Color ACHIEVE_GREEN_BG = new Color(187, 247, 208);
    public static final Color ACHIEVE_RED_BG = new Color(254, 202, 202);
    public static final Color DIARY_COLOR = new Color(147, 51, 234);
    public static final Color DIARY_BG = new Color(233, 213, 255);

    public static Color achievementColor(double rate) {
        if (rate < 0) {
            return null;
        }
        if (rate >= 100) {
            return ACHIEVE_BLUE;
        }
        if (rate >= 50) {
            return ACHIEVE_GREEN;
        }
        return ACHIEVE_RED;
    }

    public static Color achievementBackground(double rate) {
        if (rate < 0) {
            return null;
        }
        if (rate >= 100) {
            return ACHIEVE_BLUE_BG;
        }
        if (rate >= 50) {
            return ACHIEVE_GREEN_BG;
        }
        return ACHIEVE_RED_BG;
    }

    public static String dateKey(LocalDate date) {
        return date.toString();
    }
}
