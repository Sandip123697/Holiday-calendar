package com.example.calendar.demo;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple calendar application that displays a calendar view and allows users to navigate through different months and years.
 * It also highlights government holidays and special Saturdays.
 */
public class SimpleCalendarApp {

    private JFrame frame;
    private JTable calendarTable;
    private DefaultTableModel calendarModel;
    private JLabel monthLabel;
    private JButton prevButton, nextButton;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<CountryOption> countryComboBox;
    private JToggleButton viewToggleButton;

    private JToggleButton switchToVacationMode;
    private JPanel calendarPanel;
    private LocalDate currentDate;
    private Map<LocalDate, String> governmentHolidays;
    private final Color LIGHT_GREEN = new Color(200, 255, 200);
    private final Color DARK_GREEN = new Color(100, 200, 100);
    private HolidayService holidayService;
    private String currentCountryCode = "IN";
    private boolean isThreeMonthView = false;

    private boolean isVacationMode = false;

    public SimpleCalendarApp() {
        holidayService = new HolidayService();
        governmentHolidays = new HashMap<>();
        currentDate = LocalDate.now();

        // Create the main frame
        frame = new JFrame("Holiday Calendar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        initializeUI();

        // Load initial holidays for default country (India)
        loadHolidays();
        updateCalendar();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Loads government holidays for the current country and year.
     */
    private void loadHolidays() {
        int year = currentDate.getYear();
        governmentHolidays = holidayService.getHolidays(currentCountryCode, year);
        updateCalendar();
    }

    /**
     * Initializes the UI components and layout.
     */
    private void initializeUI() {

        JPanel controlPanel = new JPanel(new BorderLayout());

        JPanel navPanel = new JPanel();
        prevButton = new JButton("<<");
        nextButton = new JButton(">>");
        monthLabel = new JLabel("", JLabel.CENTER);
        viewToggleButton = new JToggleButton("Three Month View");
        switchToVacationMode = new JToggleButton("Vacation Mode");
        viewToggleButton.addActionListener(e -> {
            isThreeMonthView = viewToggleButton.isSelected();
            viewToggleButton.setText(isThreeMonthView ? "Single Month View" : "Three Month View");
            updateCalendar();
        });

        switchToVacationMode.addActionListener(e -> {
            isVacationMode = switchToVacationMode.isSelected();
            switchToVacationMode.setText(isVacationMode ? "Normal Mode" : "Vacation Mode");
            updateCalendar();
        });

        navPanel.add(prevButton);
        navPanel.add(monthLabel);
        navPanel.add(nextButton);
        navPanel.add(Box.createHorizontalStrut(20));
        navPanel.add(viewToggleButton);
        navPanel.add(switchToVacationMode);

        // Year and month selection panel
        JPanel selectionPanel = new JPanel();
        yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 5; year <= currentYear + 5; year++) {
            yearComboBox.addItem(year);
        }
        yearComboBox.setSelectedItem(currentDate.getYear());

        monthComboBox = new JComboBox<>(new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        });
        monthComboBox.setSelectedIndex(currentDate.getMonthValue() - 1);

        countryComboBox = new JComboBox<>(getCountryOptions());
        countryComboBox.setSelectedItem(getCountryByCode(currentCountryCode));

        selectionPanel.add(new JLabel("Year:"));
        selectionPanel.add(yearComboBox);
        selectionPanel.add(new JLabel("Month:"));
        selectionPanel.add(monthComboBox);
        selectionPanel.add(new JLabel("Country:"));
        selectionPanel.add(countryComboBox);

        controlPanel.add(navPanel, BorderLayout.NORTH);
        controlPanel.add(selectionPanel, BorderLayout.SOUTH);

        calendarPanel = new JPanel(new BorderLayout());

        setupSingleMonthView();
        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(calendarPanel, BorderLayout.CENTER);

        prevButton.addActionListener(e -> {
            if (isThreeMonthView) {
                currentDate = currentDate.minusMonths(3);
            } else {
                currentDate = currentDate.minusMonths(1);
            }
            updateCalendar();
        });

        nextButton.addActionListener(e -> {
            if (isThreeMonthView) {
                currentDate = currentDate.plusMonths(3);
            } else {
                currentDate = currentDate.plusMonths(1);
            }
            updateCalendar();
        });

        yearComboBox.addActionListener(e -> {
            int year = (Integer) yearComboBox.getSelectedItem();
            if (year != currentDate.getYear()) {
                currentDate = currentDate.withYear(year);
                loadHolidays();
            } else {
                updateCalendar();
            }
        });

        monthComboBox.addActionListener(e -> {
            int month = monthComboBox.getSelectedIndex() + 1;
            currentDate = currentDate.withMonth(month);
            updateCalendar();
        });

        countryComboBox.addActionListener(e -> {
            CountryOption selectedCountry = (CountryOption) countryComboBox.getSelectedItem();
            if (selectedCountry != null && !selectedCountry.getCode().equals(currentCountryCode)) {
                currentCountryCode = selectedCountry.getCode();
                loadHolidays(); // Reload holidays for new country
            }
        });

        viewToggleButton.addActionListener(e -> {
            isThreeMonthView = viewToggleButton.isSelected();
            updateCalendar();
        });

        switchToVacationMode.addActionListener(e -> {
            isVacationMode = switchToVacationMode.isSelected();
            updateCalendar();
        });
    }

    /**
     * Sets up the single month view.
     */
    private void setupSingleMonthView() {
        calendarModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        calendarModel.setColumnIdentifiers(new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"});
        calendarTable = new JTable(calendarModel);
        calendarTable.setRowHeight(100);
        calendarTable.setCellSelectionEnabled(true);
        calendarTable.setDefaultRenderer(Object.class, new CalendarCellRenderer());

        calendarPanel.removeAll();
        calendarPanel.add(new JScrollPane(calendarTable), BorderLayout.CENTER);
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private void setupThreeMonthView() {
        calendarPanel.removeAll();
        calendarPanel.setLayout(new GridLayout(1, 3, 10, 0));

        for (int i = -1; i <= 1; i++) {
            LocalDate monthDate = currentDate.plusMonths(i);
            JPanel monthPanel = createMonthPanel(monthDate);
            calendarPanel.add(monthPanel);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private void setupVacationModeView() {
        if (isThreeMonthView) {
            // Handle three-month view in vacation mode
            setupVacationThreeMonthView();
        } else {
            // Handle single-month view in vacation mode
            setupVacationSingleMonthView();
        }
    }

    private void setupVacationSingleMonthView() {
        calendarModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        calendarModel.setColumnIdentifiers(new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"});
        calendarTable = new JTable(calendarModel);
        calendarTable.setRowHeight(100);
        calendarTable.setCellSelectionEnabled(true);
        calendarTable.setDefaultRenderer(Object.class, new CalendarCellRenderer());

        calendarPanel.removeAll();
        calendarPanel.add(new JScrollPane(calendarTable), BorderLayout.CENTER);
        calendarPanel.revalidate();
        calendarPanel.repaint();

        // Now fill the table with vacation weeks only
        fillVacationCalendarTable();
    }

    private void setupVacationThreeMonthView() {
        calendarPanel.removeAll();
        calendarPanel.setLayout(new GridLayout(1, 3, 10, 0));

        for (int i = -1; i <= 1; i++) {
            LocalDate monthDate = currentDate.plusMonths(i);
            JPanel monthPanel = createVacationMonthPanel(monthDate);
            calendarPanel.add(monthPanel);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private JPanel createMonthPanel(LocalDate date) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel(date.format(DateTimeFormatter.ofPattern("MMMM yyyy")), JLabel.CENTER);
        label.setFont(new Font(label.getFont().getName(), Font.BOLD, 14));
        panel.add(label, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{"S", "M", "T", "W", "T", "F", "S"});

        JTable table = new JTable(model);
        table.setRowHeight(60); // Reduced from 80 to 60
        table.setCellSelectionEnabled(true);
        table.setDefaultRenderer(Object.class, new CalendarCellRenderer());

        fillCalendarTable(table, model, date);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createVacationMonthPanel(LocalDate date) {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel(date.format(DateTimeFormatter.ofPattern("MMMM yyyy")), JLabel.CENTER);
        label.setFont(new Font(label.getFont().getName(), Font.BOLD, 14));
        panel.add(label, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(new String[]{"S", "M", "T", "W", "T", "F", "S"});

        JTable table = new JTable(model);
        table.setRowHeight(60);
        table.setCellSelectionEnabled(true);
        table.setDefaultRenderer(Object.class, new CalendarCellRenderer());

        fillVacationCalendarTable(table, model, date);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void fillCalendarTable(JTable table, DefaultTableModel model, LocalDate date) {
        model.setRowCount(0);

        YearMonth yearMonth = YearMonth.from(date);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        int rows = (int) Math.ceil((daysInMonth + dayOfWeek) / 7.0);
        for (int i = 0; i < rows; i++) {
            Object[] row = new Object[7];
            for (int j = 0; j < 7; j++) {
                int dayNum = (i * 7) + j + 1 - dayOfWeek;
                if (dayNum > 0 && dayNum <= daysInMonth) {
                    LocalDate cellDate = LocalDate.of(date.getYear(), date.getMonth(), dayNum);

                    StringBuilder cellText = new StringBuilder();
                    cellText.append(dayNum);

                    if (governmentHolidays.containsKey(cellDate)) {
                        cellText.append("\n").append(governmentHolidays.get(cellDate));
                    }

                    if (isSecondSaturday(cellDate)) {
                        cellText.append("\n2nd Sat");
                    } else if (isFourthSaturday(cellDate)) {
                        cellText.append("\n4th Sat");
                    }

                    row[j] = cellText.toString();
                } else {
                    row[j] = "";
                }
            }
            model.addRow(row);
        }
    }

    private void fillVacationCalendarTable() {
        calendarModel.setRowCount(0);
        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        int rows = (int) Math.ceil((daysInMonth + dayOfWeek) / 7.0);
        for (int i = 0; i < rows; i++) {
            Object[] row = new Object[7];
            boolean isVacationWeek = false;

            // First check if this week has any vacation days
            for (int j = 0; j < 7; j++) {
                int dayNum = (i * 7) + j + 1 - dayOfWeek;
                if (dayNum > 0 && dayNum <= daysInMonth) {
                    LocalDate cellDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), dayNum);

                    int holidaysInWeek = countHolidaysInWeek(cellDate);
                    if (holidaysInWeek > 0) {
                        isVacationWeek = true;
                        break;
                    }
                }
            }

            // If it's a vacation week, add the data
            if (isVacationWeek) {
                for (int j = 0; j < 7; j++) {
                    int dayNum = (i * 7) + j + 1 - dayOfWeek;
                    if (dayNum > 0 && dayNum <= daysInMonth) {
                        LocalDate cellDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), dayNum);

                        StringBuilder cellText = new StringBuilder();
                        cellText.append(dayNum);

                        if (governmentHolidays.containsKey(cellDate)) {
                            cellText.append("\n").append(governmentHolidays.get(cellDate));
                        }

                        if (isSecondSaturday(cellDate)) {
                            cellText.append("\n2nd Saturday");
                        } else if (isFourthSaturday(cellDate)) {
                            cellText.append("\n4th Saturday");
                        }

                        row[j] = cellText.toString();
                    } else {
                        row[j] = "";
                    }
                }
                calendarModel.addRow(row);
            }
        }

        calendarTable.repaint();
    }

    private void fillVacationCalendarTable(JTable table, DefaultTableModel model, LocalDate date) {
        model.setRowCount(0);

        YearMonth yearMonth = YearMonth.from(date);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        int rows = (int) Math.ceil((daysInMonth + dayOfWeek) / 7.0);
        for (int i = 0; i < rows; i++) {
            Object[] row = new Object[7];
            boolean isVacationWeek = false;

            // First check if this week has any vacation days
            for (int j = 0; j < 7; j++) {
                int dayNum = (i * 7) + j + 1 - dayOfWeek;
                if (dayNum > 0 && dayNum <= daysInMonth) {
                    LocalDate cellDate = LocalDate.of(date.getYear(), date.getMonth(), dayNum);

                    // Check if this week has any holidays (which makes it a vacation week)
                    int holidaysInWeek = countHolidaysInWeek(cellDate);
                    if (holidaysInWeek > 0) {
                        isVacationWeek = true;
                        break;
                    }
                }
            }

            // If it's a vacation week, add the data
            if (isVacationWeek) {
                for (int j = 0; j < 7; j++) {
                    int dayNum = (i * 7) + j + 1 - dayOfWeek;
                    if (dayNum > 0 && dayNum <= daysInMonth) {
                        LocalDate cellDate = LocalDate.of(date.getYear(), date.getMonth(), dayNum);

                        StringBuilder cellText = new StringBuilder();
                        cellText.append(dayNum);

                        if (governmentHolidays.containsKey(cellDate)) {
                            cellText.append("\n").append(governmentHolidays.get(cellDate));
                        }

                        if (isSecondSaturday(cellDate)) {
                            cellText.append("\n2nd Sat");
                        } else if (isFourthSaturday(cellDate)) {
                            cellText.append("\n4th Sat");
                        }

                        row[j] = cellText.toString();
                    } else {
                        row[j] = "";
                    }
                }
                model.addRow(row);
            }
        }
    }

    /**
     * Updates the calendar view based on the current date and view toggle state.
     */
    private void updateCalendar() {
        // Update month and year display
        monthLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        yearComboBox.setSelectedItem(currentDate.getYear());
        monthComboBox.setSelectedIndex(currentDate.getMonthValue() - 1);

        // Update calendar view based on toggle states
        if (isVacationMode) {
            setupVacationModeView();
            // Adjust frame size
            frame.setSize(1200, isThreeMonthView ? 600 : 800);
        } else if (isThreeMonthView) {
            setupThreeMonthView();
            // Adjust frame size for three-month view
            frame.setSize(1200, 600);
        } else {
            setupSingleMonthView();
            // Reset to original size for single-month view
            frame.setSize(1200, 800);

            // Clear existing calendar data
            calendarModel.setRowCount(0);

            // Get the first day of the month and total days in month
            YearMonth yearMonth = YearMonth.from(currentDate);
            LocalDate firstOfMonth = yearMonth.atDay(1);
            int daysInMonth = yearMonth.lengthOfMonth();

            // Get the day of week (0 = Sunday, 6 = Saturday)
            int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

            // Create calendar grid
            int rows = (int) Math.ceil((daysInMonth + dayOfWeek) / 7.0);
            for (int i = 0; i < rows; i++) {
                Object[] row = new Object[7];
                for (int j = 0; j < 7; j++) {
                    int dayNum = (i * 7) + j + 1 - dayOfWeek;
                    if (dayNum > 0 && dayNum <= daysInMonth) {
                        LocalDate cellDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), dayNum);

                        StringBuilder cellText = new StringBuilder();
                        cellText.append(dayNum);

                        // Add holiday name if it's a government holiday
                        if (governmentHolidays.containsKey(cellDate)) {
                            cellText.append("\n").append(governmentHolidays.get(cellDate));
                        }

                        // Add special Saturday indicator
                        if (isSecondSaturday(cellDate)) {
                            cellText.append("\n2nd Saturday");
                        } else if (isFourthSaturday(cellDate)) {
                            cellText.append("\n4th Saturday");
                        }

                        row[j] = cellText.toString();
                    } else {
                        row[j] = "";
                    }
                }
                calendarModel.addRow(row);
            }

            calendarTable.repaint();
        }

        // Pack the frame to fit the content
        frame.pack();

        // Ensure minimum size
        Dimension minSize = new Dimension(1000, isThreeMonthView ? 550 : 700);
        if (frame.getSize().width < minSize.width || frame.getSize().height < minSize.height) {
            frame.setSize(
                Math.max(frame.getSize().width, minSize.width),
                Math.max(frame.getSize().height, minSize.height)
            );
        }
    }

    private boolean isVacationDay(LocalDate cellDate) {
        DayOfWeek dayOfWeek = cellDate.getDayOfWeek();

        // A day is a vacation day if it's a government holiday that's not on a weekend
        if (governmentHolidays.containsKey(cellDate) && dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a date is the second Saturday of the month.
     *
     * @param date the date to check
     * @return true if the date is the second Saturday, false otherwise
     */
    private boolean isSecondSaturday(LocalDate date) {
        if (date.getDayOfWeek() != DayOfWeek.SATURDAY) {
            return false;
        }

        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate secondSaturday = firstDayOfMonth.with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.SATURDAY));
        return date.equals(secondSaturday);
    }

    /**
     * Checks if a date is the fourth Saturday of the month.
     *
     * @param date the date to check
     * @return true if the date is the fourth Saturday, false otherwise
     */
    private boolean isFourthSaturday(LocalDate date) {
        if (date.getDayOfWeek() != DayOfWeek.SATURDAY) {
            return false;
        }

        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        LocalDate fourthSaturday = firstDayOfMonth.with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.SATURDAY));
        return date.equals(fourthSaturday);
    }

    /**
     * Counts the number of holidays in a week.
     *
     * @param date the date to start from
     * @return the number of holidays in the week
     */
    private int countHolidaysInWeek(LocalDate date) {
        // Get the start of the week (Sunday)
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        int count = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = startOfWeek.plusDays(i);
            // Only count government holidays that don't fall on weekends
            if (governmentHolidays.containsKey(currentDay) &&
                currentDay.getDayOfWeek() != DayOfWeek.SATURDAY &&
                currentDay.getDayOfWeek() != DayOfWeek.SUNDAY) {
                count++;
            }
        }

        return count;
    }

    /**
     * A custom cell renderer for the calendar table.
     */
    private class CalendarCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            Component cell = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

            cell.setBackground(Color.WHITE);
            setHorizontalAlignment(JLabel.CENTER);
            setVerticalAlignment(JLabel.TOP); // Align text to top for multi-line display

            // If the cell is empty, return as is
            if (value == null || value.toString().isEmpty()) {
                return cell;
            }

            // Extract the day number from the cell value
            String cellText = value.toString();
            String[] lines = cellText.split("\n");
            int day = Integer.parseInt(lines[0]);

            // Determine which month we're looking at
            LocalDate cellDate;
            if (table == calendarTable) {
                // Single month view
                cellDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), day);
            } else {
                // In three-month view, we need to determine which month panel this table belongs to
                int panelIndex = -1;
                for (int i = 0; i < calendarPanel.getComponentCount(); i++) {
                    if (calendarPanel.getComponent(i) instanceof JPanel) {
                        JPanel monthPanel = (JPanel) calendarPanel.getComponent(i);
                        if (monthPanel.getComponentCount() > 1 &&
                            monthPanel.getComponent(1) instanceof JScrollPane) {
                            JScrollPane scrollPane = (JScrollPane) monthPanel.getComponent(1);
                            if (scrollPane.getViewport().getView() == table) {
                                panelIndex = i;
                                break;
                            }
                        }
                    }
                }

                // Calculate the month based on panel index
                LocalDate monthDate = currentDate.plusMonths(panelIndex - 1);
                cellDate = LocalDate.of(monthDate.getYear(), monthDate.getMonth(), day);
            }

            // Check if the date is a holiday
            boolean isHoliday = governmentHolidays.containsKey(cellDate);
            boolean isSpecialSaturday = isSecondSaturday(cellDate) || isFourthSaturday(cellDate);

            // Set text color for holidays and special Saturdays
            if (isHoliday) {
                setForeground(Color.RED);
            } else if (isSpecialSaturday) {
                setForeground(Color.BLUE);
            } else if (column == 0) { // Sunday
                setForeground(Color.RED);
            } else if (column == 6) { // Saturday
                setForeground(new Color(0, 0, 128)); // Dark blue
            } else {
                setForeground(Color.BLACK);
            }

            // Color the cell based on holiday count in the week
            int holidaysInWeek = countHolidaysInWeek(cellDate);
            if (holidaysInWeek == 1) {
                cell.setBackground(LIGHT_GREEN);
            } else if (holidaysInWeek > 1) {
                cell.setBackground(DARK_GREEN);
            }

            // Add a border to today's date
            if (cellDate.equals(LocalDate.now())) {
                setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            } else {
                setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }

            return cell;
        }
    }

    /**
     * A simple class to represent a country option.
     */
    private static class CountryOption {
        private final String code;
        private final String name;

        public CountryOption(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Returns an array of country options.
     *
     * @return the country options
     */
    private CountryOption[] getCountryOptions() {
        return new CountryOption[]{
            new CountryOption("IN", "India"),
            new CountryOption("US", "United States"),
            new CountryOption("GB", "United Kingdom"),
            new CountryOption("CA", "Canada"),
            new CountryOption("AU", "Australia"),
            new CountryOption("DE", "Germany"),
            new CountryOption("FR", "France"),
            new CountryOption("JP", "Japan"),
            new CountryOption("SG", "Singapore"),
            new CountryOption("AE", "UAE")
        };
    }

    /**
     * Returns the country option for a given code.
     *
     * @param code the country code
     * @return the country option
     */
    private CountryOption getCountryByCode(String code) {
        for (CountryOption option : getCountryOptions()) {
            if (option.getCode().equals(code)) {
                return option;
            }
        }
        return getCountryOptions()[0]; // Default to first country
    }

    public static void main(String[] args) {
        // Set system properties to avoid HeadlessException
        System.setProperty("java.awt.headless", "false");

        // Use SwingUtilities to ensure thread safety
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SimpleCalendarApp();
        });
    }
}
