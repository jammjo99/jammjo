package com.calendar.ui;

import com.calendar.model.DayData;
import com.calendar.service.DataStore;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

public class CalendarPanel extends JPanel {
    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN);
    private static final String[] WEEKDAYS = {"일", "월", "화", "수", "목", "금", "토"};
    private static final Color NEUTRAL_CELL = new Color(241, 245, 249);

    private final DataStore dataStore;
    private final Consumer<LocalDate> onDateSelected;

    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private final JLabel monthLabel = new JLabel("", JLabel.CENTER);
    private final JPanel daysGrid = new JPanel(new GridLayout(0, 7, 8, 8));

    public CalendarPanel(DataStore dataStore, Consumer<LocalDate> onDateSelected) {
        this.dataStore = dataStore;
        this.onDateSelected = onDateSelected;
        this.currentMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildWeekdayHeader(), BorderLayout.CENTER);
        add(buildLegend(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("캘린더");
        title.setFont(AppTheme.TITLE);
        title.setForeground(AppTheme.TEXT);

        monthLabel.setFont(AppTheme.HEADING);
        monthLabel.setForeground(AppTheme.TEXT);

        JButton prev = navButton("◀");
        JButton next = navButton("▶");
        prev.addActionListener(e -> changeMonth(-1));
        next.addActionListener(e -> changeMonth(1));

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        nav.setOpaque(false);
        nav.add(prev);
        nav.add(monthLabel);
        nav.add(next);

        header.add(title, BorderLayout.NORTH);
        header.add(nav, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildWeekdayHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);

        JPanel weekdayRow = new JPanel(new GridLayout(1, 7, 8, 0));
        weekdayRow.setOpaque(false);
        weekdayRow.setPreferredSize(new Dimension(0, 28));

        for (int i = 0; i < WEEKDAYS.length; i++) {
            JLabel label = new JLabel(WEEKDAYS[i], JLabel.CENTER);
            label.setFont(AppTheme.SMALL);
            label.setForeground(i == 0 ? DayData.ACHIEVE_RED : i == 6 ? AppTheme.PRIMARY : AppTheme.TEXT_MUTED);
            weekdayRow.add(label);
        }

        daysGrid.setOpaque(false);
        wrapper.add(weekdayRow, BorderLayout.NORTH);
        wrapper.add(daysGrid, BorderLayout.CENTER);
        refreshDays();
        return wrapper;
    }

    private JButton navButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 16));
        btn.setPreferredSize(new Dimension(40, 40));
        AppTheme.styleGhostButton(btn);
        return btn;
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        this.currentMonth = YearMonth.from(date);
        refreshDays();
    }

    public void refreshDays() {
        monthLabel.setText(currentMonth.format(MONTH_FMT));
        daysGrid.removeAll();

        LocalDate first = currentMonth.atDay(1);
        int startOffset = first.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < startOffset; i++) {
            daysGrid.add(emptyCell());
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            daysGrid.add(createDayCell(date, today));
        }

        int totalCells = startOffset + daysInMonth;
        int remainder = (7 - (totalCells % 7)) % 7;
        for (int i = 0; i < remainder; i++) {
            daysGrid.add(emptyCell());
        }

        revalidate();
        repaint();
    }

    private JPanel emptyCell() {
        JPanel cell = new JPanel();
        cell.setOpaque(false);
        return cell;
    }

    private JPanel createDayCell(LocalDate date, LocalDate today) {
        boolean isSelected = date.equals(selectedDate);
        boolean isToday = date.equals(today);
        boolean isFuture = date.isAfter(today);
        DayData data = dataStore.get(date);
        double rate = data.getCompletionRate();
        Color achievement = DayData.achievementColor(rate);
        Color achievementBg = DayData.achievementBackground(rate);
        boolean hasDiary = data.getDiary() != null && !data.getDiary().isBlank();
        boolean hasChecklist = rate >= 0;

        JPanel cell = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int pad = 2;
                int r = 14;

                Color baseBg = achievementBg != null ? achievementBg : NEUTRAL_CELL;
                if (isFuture) {
                    baseBg = new Color(250, 250, 250);
                }
                g2.setColor(baseBg);
                g2.fillRoundRect(pad, pad, w - pad * 2, h - pad * 2, r, r);

                if (achievement != null && !isFuture) {
                    g2.setColor(new Color(achievement.getRed(), achievement.getGreen(), achievement.getBlue(), 60));
                    g2.fillRoundRect(pad, pad, w - pad * 2, h - pad * 2, r, r);
                    g2.setColor(achievement);
                    g2.setStroke(new java.awt.BasicStroke(2.5f));
                    g2.drawRoundRect(pad + 1, pad + 1, w - pad * 2 - 2, h - pad * 2 - 2, r, r);
                }

                if (isSelected) {
                    g2.setColor(AppTheme.PRIMARY);
                    g2.setStroke(new java.awt.BasicStroke(3f));
                    g2.drawRoundRect(pad, pad, w - pad * 2, h - pad * 2, r, r);
                } else if (isToday) {
                    g2.setColor(AppTheme.TODAY_RING);
                    g2.setStroke(new java.awt.BasicStroke(2.5f));
                    g2.drawRoundRect(pad, pad, w - pad * 2, h - pad * 2, r, r);
                }

                if (hasChecklist && achievement != null && !isFuture) {
                    int barH = 5;
                    int barY = h - pad - barH - 4;
                    int barW = w - pad * 2 - 8;
                    int barX = pad + 4;
                    g2.setColor(new Color(255, 255, 255, 180));
                    g2.fillRoundRect(barX, barY, barW, barH, 4, 4);
                    int fillW = (int) (barW * (rate / 100.0));
                    g2.setColor(achievement);
                    g2.fillRoundRect(barX, barY, Math.max(fillW, 4), barH, 4, 4);
                }

                g2.dispose();
            }
        };
        cell.setOpaque(false);
        cell.setPreferredSize(new Dimension(68, 72));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 6));

        JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        dayLabel.setForeground(date.getDayOfWeek() == DayOfWeek.SUNDAY
                ? DayData.ACHIEVE_RED
                : date.getDayOfWeek() == DayOfWeek.SATURDAY
                ? AppTheme.PRIMARY
                : AppTheme.TEXT);
        topRow.add(dayLabel, BorderLayout.WEST);

        if (hasDiary) {
            JLabel diaryBadge = new JLabel("일기") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(DayData.DIARY_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(DayData.DIARY_COLOR);
                    g2.setStroke(new java.awt.BasicStroke(1.2f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            diaryBadge.setFont(new Font("SansSerif", Font.BOLD, 9));
            diaryBadge.setForeground(DayData.DIARY_COLOR);
            diaryBadge.setOpaque(false);
            diaryBadge.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            diaryBadge.setHorizontalAlignment(JLabel.CENTER);
            topRow.add(diaryBadge, BorderLayout.EAST);
        }

        cell.add(topRow, BorderLayout.NORTH);

        if (hasChecklist && achievement != null && !isFuture) {
            JLabel rateLabel = new JLabel(String.format("%.0f%%", rate), JLabel.CENTER);
            rateLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            rateLabel.setForeground(achievement);
            rateLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            cell.add(rateLabel, BorderLayout.CENTER);
        }

        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedDate = date;
                refreshDays();
                onDateSelected.accept(date);
            }
        });

        return cell;
    }

    private void changeMonth(int delta) {
        currentMonth = currentMonth.plusMonths(delta);
        refreshDays();
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        legend.setOpaque(false);
        legend.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        legend.add(legendSwatch(DayData.ACHIEVE_BLUE, DayData.ACHIEVE_BLUE_BG, "100%"));
        legend.add(legendSwatch(DayData.ACHIEVE_GREEN, DayData.ACHIEVE_GREEN_BG, "50~99%"));
        legend.add(legendSwatch(DayData.ACHIEVE_RED, DayData.ACHIEVE_RED_BG, "0~49%"));
        legend.add(legendDiary());
        return legend;
    }

    private JPanel legendSwatch(Color border, Color fill, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(false);
        item.add(new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, 18, 18, 6, 6);
                g2.setColor(border);
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawRoundRect(1, 1, 16, 16, 6, 6);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(18, 18);
            }
        });
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.SMALL);
        label.setForeground(AppTheme.TEXT);
        item.add(label);
        return item;
    }

    private JPanel legendDiary() {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(false);
        JLabel badge = new JLabel("일기");
        badge.setFont(new Font("SansSerif", Font.BOLD, 9));
        badge.setForeground(DayData.DIARY_COLOR);
        badge.setOpaque(true);
        badge.setBackground(DayData.DIARY_BG);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DayData.DIARY_COLOR, 1, true),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        item.add(badge);
        JLabel label = new JLabel("일기 작성됨");
        label.setFont(AppTheme.SMALL);
        label.setForeground(AppTheme.TEXT);
        item.add(label);
        return item;
    }
}
