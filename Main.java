package com.calendar;

import com.calendar.service.DataStore;
import com.calendar.service.TestDataSeeder;
import com.calendar.ui.CalendarApp;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            DataStore dataStore = new DataStore();
            TestDataSeeder.seedMonthToToday(dataStore);
            new CalendarApp(dataStore).setVisible(true);
        });
    }
}
