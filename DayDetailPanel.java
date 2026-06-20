package com.calendar.ui;

import com.calendar.model.ChecklistItem;
import com.calendar.model.DayData;
import com.calendar.service.DataStore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class DayDetailPanel extends JPanel {
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일", Locale.KOREAN);

    private final DataStore dataStore;
    private final Runnable onDataChanged;

    private LocalDate currentDate = LocalDate.now();
    private DayData currentData = new DayData();
    private boolean suppressEvents;

    private final JLabel dateLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JLabel diaryStatusLabel = new JLabel();
    private final JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JTextArea diaryArea = new JTextArea();
    private final JPanel checklistContainer = new JPanel();
    private final JTabbedPane tabs = new JTabbedPane();

    public DayDetailPanel(DataStore dataStore, Runnable onDataChanged) {
        this.dataStore = dataStore;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);

        dateLabel.setFont(AppTheme.TITLE);
        dateLabel.setForeground(AppTheme.TEXT);

        JLabel subtitle = new JLabel("일기와 체크리스트를 관리하세요");
        subtitle.setFont(AppTheme.SMALL);
        subtitle.setForeground(AppTheme.TEXT_MUTED);

        progressLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        progressLabel.setOpaque(true);
        progressLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        diaryStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        diaryStatusLabel.setOpaque(true);
        diaryStatusLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        statusPanel.setOpaque(false);
        statusPanel.add(diaryStatusLabel);
        statusPanel.add(progressLabel);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(dateLabel, BorderLayout.NORTH);
        top.add(subtitle, BorderLayout.SOUTH);

        header.add(top, BorderLayout.WEST);
        header.add(statusPanel, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane buildTabs() {
        tabs.setFont(AppTheme.BODY);
        tabs.setBackground(AppTheme.SURFACE);
        tabs.setForeground(AppTheme.TEXT);

        diaryArea.setFont(AppTheme.BODY);
        diaryArea.setLineWrap(true);
        diaryArea.setWrapStyleWord(true);
        diaryArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        diaryArea.getDocument().addDocumentListener(simpleDocListener(this::saveDiary));

        JScrollPane diaryScroll = new JScrollPane(diaryArea);
        diaryScroll.setBorder(new AppTheme.RoundedBorder(AppTheme.BORDER, 12, 1));
        diaryScroll.getVerticalScrollBar().setUnitIncrement(16);

        checklistContainer.setLayout(new BoxLayout(checklistContainer, BoxLayout.Y_AXIS));
        checklistContainer.setOpaque(false);

        JScrollPane checklistScroll = new JScrollPane(checklistContainer);
        checklistScroll.setBorder(new AppTheme.RoundedBorder(AppTheme.BORDER, 12, 1));
        checklistScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel checklistPanel = new JPanel(new BorderLayout(0, 10));
        checklistPanel.setOpaque(false);
        checklistPanel.add(checklistScroll, BorderLayout.CENTER);
        checklistPanel.add(buildAddItemRow(), BorderLayout.SOUTH);

        tabs.addTab("  일기  ", diaryScroll);
        tabs.addTab("  체크리스트  ", checklistPanel);
        return tabs;
    }

    private JPanel buildAddItemRow() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JTextField input = new JTextField();
        input.setFont(AppTheme.BODY);
        input.setBorder(new AppTheme.RoundedBorder(AppTheme.BORDER, 10, 1));
        input.setPreferredSize(new Dimension(0, 40));

        JButton addBtn = new JButton("항목 추가");
        AppTheme.stylePrimaryButton(addBtn);
        addBtn.addActionListener(e -> {
            String text = input.getText().trim();
            if (!text.isEmpty()) {
                addChecklistItem(text);
                input.setText("");
            }
        });
        input.addActionListener(e -> addBtn.doClick());

        row.add(input, BorderLayout.CENTER);
        row.add(addBtn, BorderLayout.EAST);
        return row;
    }

    public void loadDate(LocalDate date) {
        currentDate = date;
        currentData = copy(dataStore.get(date));

        suppressEvents = true;
        dateLabel.setText(date.format(DATE_FMT) + " "
                + date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN));
        diaryArea.setText(currentData.getDiary());
        rebuildChecklist();
        updateStatusBadges();
        updateTabTitles();
        suppressEvents = false;
    }

    public void saveCurrent() {
        if (currentDate == null) {
            return;
        }
        dataStore.save(currentDate, copy(currentData));
    }

    private void saveDiary() {
        if (suppressEvents) {
            return;
        }
        currentData.setDiary(diaryArea.getText());
        persistAndRefresh();
    }

    private void addChecklistItem(String text) {
        currentData.getChecklist().add(new ChecklistItem(text, false));
        rebuildChecklist();
        persistAndRefresh();
    }

    private void rebuildChecklist() {
        checklistContainer.removeAll();
        var items = currentData.getChecklist();

        if (items.isEmpty()) {
            JLabel empty = new JLabel("체크리스트 항목을 추가해 보세요");
            empty.setFont(AppTheme.BODY);
            empty.setForeground(AppTheme.TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            checklistContainer.add(empty);
        } else {
            for (int i = 0; i < items.size(); i++) {
                checklistContainer.add(buildChecklistRow(items.get(i), i));
                checklistContainer.add(Box.createVerticalStrut(6));
            }
        }

        checklistContainer.revalidate();
        checklistContainer.repaint();
    }

    private JPanel buildChecklistRow(ChecklistItem item, int index) {
        JPanel row = AppTheme.roundedPanel(new Color(248, 250, 252), 10);
        row.setLayout(new BorderLayout(8, 0));
        row.setBorder(BorderFactory.createCompoundBorder(
                new AppTheme.RoundedBorder(AppTheme.BORDER, 10, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel label = new JLabel(item.getText());
        label.setFont(AppTheme.BODY);
        label.setForeground(item.isCompleted() ? AppTheme.TEXT_MUTED : AppTheme.TEXT);

        JCheckBox check = new JCheckBox();
        check.setSelected(item.isCompleted());
        check.setOpaque(false);
        check.addItemListener(e -> {
            if (suppressEvents) {
                return;
            }
            item.setCompleted(check.isSelected());
            label.setForeground(check.isSelected() ? AppTheme.TEXT_MUTED : AppTheme.TEXT);
            persistAndRefresh();
        });

        JButton delete = new JButton("×");
        delete.setFont(new Font("SansSerif", Font.PLAIN, 18));
        delete.setForeground(AppTheme.TEXT_MUTED);
        delete.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        delete.setContentAreaFilled(false);
        delete.setFocusPainted(false);
        delete.addActionListener(e -> {
            currentData.getChecklist().remove(index);
            rebuildChecklist();
            persistAndRefresh();
        });

        row.add(check, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        row.add(delete, BorderLayout.EAST);
        return row;
    }

    private void persistAndRefresh() {
        saveCurrent();
        updateStatusBadges();
        updateTabTitles();
        onDataChanged.run();
    }

    private void updateStatusBadges() {
        double rate = currentData.getCompletionRate();
        boolean hasDiary = currentData.getDiary() != null && !currentData.getDiary().isBlank();

        if (hasDiary) {
            diaryStatusLabel.setText("📝 일기 작성됨");
            diaryStatusLabel.setForeground(DayData.DIARY_COLOR);
            diaryStatusLabel.setBackground(DayData.DIARY_BG);
            diaryStatusLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DayData.DIARY_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        } else {
            diaryStatusLabel.setText("일기 없음");
            diaryStatusLabel.setForeground(AppTheme.TEXT_MUTED);
            diaryStatusLabel.setBackground(new Color(241, 245, 249));
            diaryStatusLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }

        if (rate < 0) {
            progressLabel.setText("체크리스트 없음");
            progressLabel.setForeground(AppTheme.TEXT_MUTED);
            progressLabel.setBackground(new Color(241, 245, 249));
            progressLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            return;
        }

        Color color = DayData.achievementColor(rate);
        Color bg = DayData.achievementBackground(rate);
        long done = currentData.getChecklist().stream().filter(ChecklistItem::isCompleted).count();
        int total = currentData.getChecklist().size();
        progressLabel.setText(String.format("성취 %d/%d · %.0f%%", done, total, rate));
        progressLabel.setForeground(color);
        progressLabel.setBackground(bg);
        progressLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
    }

    private void updateTabTitles() {
        boolean hasDiary = currentData.getDiary() != null && !currentData.getDiary().isBlank();
        double rate = currentData.getCompletionRate();
        String diaryTab = hasDiary ? "  📝 일기  " : "  일기  ";
        String checklistTab = rate >= 0
                ? String.format("  ✓ 체크리스트 (%.0f%%)  ", rate)
                : "  체크리스트  ";
        if (tabs.getTabCount() >= 2) {
            tabs.setTitleAt(0, diaryTab);
            tabs.setTitleAt(1, checklistTab);
        }
    }

    private DayData copy(DayData source) {
        DayData copy = new DayData();
        copy.setDiary(source.getDiary());
        var list = source.getChecklist().stream()
                .map(i -> new ChecklistItem(i.getText(), i.isCompleted()))
                .toList();
        copy.setChecklist(new java.util.ArrayList<>(list));
        return copy;
    }

    private DocumentListener simpleDocListener(Runnable onChange) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onChange.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onChange.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onChange.run();
            }
        };
    }
}
