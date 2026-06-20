package com.calendar.ui;

import com.calendar.service.DataStore;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalDate;

public class CalendarApp extends JFrame {
    private final DataStore dataStore;
    private final CalendarPanel calendarPanel;
    private final DayDetailPanel detailPanel;

    public CalendarApp(DataStore dataStore) {
        this.dataStore = dataStore;
        setTitle("Checklist Calendar");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 640));
        setSize(1100, 720);
        setLocationRelativeTo(null);

        calendarPanel = new CalendarPanel(dataStore, this::onDateSelected);
        detailPanel = new DayDetailPanel(dataStore, this::onDataChanged);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(AppTheme.BG);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel main = new JPanel(new GridLayout(1, 2, 20, 0));
        main.setOpaque(false);
        main.add(wrapCard(calendarPanel));
        main.add(wrapCard(detailPanel));

        content.add(main, BorderLayout.CENTER);
        setContentPane(content);

        LocalDate today = LocalDate.now();
        calendarPanel.setSelectedDate(today);
        detailPanel.loadDate(today);
    }

    private JPanel wrapCard(JPanel panel) {
        JPanel card = AppTheme.roundedPanel(AppTheme.SURFACE, 16);
        card.setLayout(new BorderLayout());
        card.setBorder(new AppTheme.RoundedBorder(AppTheme.BORDER, 16, 1));
        card.add(panel, BorderLayout.CENTER);
        return card;
    }

    private void onDateSelected(LocalDate date) {
        detailPanel.saveCurrent();
        detailPanel.loadDate(date);
    }

    private void onDataChanged() {
        calendarPanel.refreshDays();
    }
}
