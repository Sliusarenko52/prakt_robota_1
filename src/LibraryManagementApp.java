import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

interface Searchable {
    boolean matches(String query);
}
interface Rentable {
    boolean rent(String userName);
    boolean returnItem();
    boolean isAvailable();
}
interface Displayable {
    String[] toTableRow();
}

abstract class LibraryItem implements Searchable, Rentable, Displayable {
    protected String id;
    protected String title;
    protected int year;
    protected boolean available;
    protected String rentedBy;
    protected LocalDate rentDate;

    public LibraryItem(String id, String title, int year) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.available = true;
    }

    public boolean rent(String userName) {
        if (!available) return false;
        available = false;
        rentedBy = userName;
        rentDate = LocalDate.now();
        return true;
    }

    public boolean returnItem() {
        if (available) return false;
        available = true;
        rentedBy = null;
        rentDate = null;
        return true;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean matches(String query) {
        String q = query.toLowerCase();
        return id.toLowerCase().contains(q) || title.toLowerCase().contains(q);
    }

    public abstract String getType();

    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getYear() { return year; }
    public String getRentedBy() { return rentedBy; }

    public void setTitle(String title) { this.title = title; }
    public void setYear(int year) { this.year = year; }
}

class Book extends LibraryItem {
    private String author;
    private int pages;

    public Book(String id, String title, int year, String author, int pages) {
        super(id, title, year);
        this.author = author;
        this.pages = pages;
    }
    public String getType() { return "Книга"; }
    public String[] toTableRow() {
        return new String[]{ id, getType(), title, String.valueOf(year),
                "Автор: " + author + ", Сторінок: " + pages,
                available ? "Доступна" : "Орендована",
                rentedBy != null ? rentedBy : "-" };
    }
    public boolean matches(String query) {
        return super.matches(query) || author.toLowerCase().contains(query.toLowerCase());
    }
    public String getAuthor() { return author; }
    public int getPages() { return pages; }
    public void setAuthor(String author) { this.author = author; }
    public void setPages(int pages) { this.pages = pages; }
}

class Magazine extends LibraryItem {
    private int issueNumber;
    private String publisher;

    public Magazine(String id, String title, int year, int issueNumber, String publisher) {
        super(id, title, year);
        this.issueNumber = issueNumber;
        this.publisher = publisher;
    }
    public String getType() { return "Журнал"; }
    public String[] toTableRow() {
        return new String[]{ id, getType(), title, String.valueOf(year),
                "Випуск: " + issueNumber + ", Видавець: " + publisher,
                available ? "Доступний" : "Орендований",
                rentedBy != null ? rentedBy : "-" };
    }
    public boolean matches(String query) {
        return super.matches(query) || publisher.toLowerCase().contains(query.toLowerCase());
    }
    public int getIssueNumber() { return issueNumber; }
    public String getPublisher() { return publisher; }
    public void setIssueNumber(int issueNumber) { this.issueNumber = issueNumber; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
}

class DVD extends LibraryItem {
    private String director;
    private int duration;

    public DVD(String id, String title, int year, String director, int duration) {
        super(id, title, year);
        this.director = director;
        this.duration = duration;
    }
    public String getType() { return "DVD"; }
    public String[] toTableRow() {
        return new String[]{ id, getType(), title, String.valueOf(year),
                "Режисер: " + director + ", Тривалість: " + duration + " хв",
                available ? "Доступний" : "Орендований",
                rentedBy != null ? rentedBy : "-" };
    }
    public boolean matches(String query) {
        return super.matches(query) || director.toLowerCase().contains(query.toLowerCase());
    }
    public String getDirector() { return director; }
    public int getDuration() { return duration; }
    public void setDirector(String director) { this.director = director; }
    public void setDuration(int duration) { this.duration = duration; }
}

class Repository<T extends LibraryItem> {
    private List<T> items;
    public Repository() { items = new ArrayList<>(); }
    public void add(T item) { items.add(item); }
    public boolean remove(String id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(id)) {
                items.remove(i);
                return true;
            }
        }
        return false;
    }
    public T findById(String id) {
        for (T item : items) {
            if (item.getId().equals(id)) return item;
        }
        return null;
    }
    public List<T> getAll() { return new ArrayList<>(items); }
    public List<T> search(String query) {
        List<T> results = new ArrayList<>();
        for (T item : items) {
            if (item.matches(query)) results.add(item);
        }
        return results;
    }
}

class LibraryManager {
    private Repository<Book> bookRepository;
    private Repository<Magazine> magazineRepository;
    private Repository<DVD> dvdRepository;

    public LibraryManager() {
        bookRepository = new Repository<>();
        magazineRepository = new Repository<>();
        dvdRepository = new Repository<>();
    }

    public void addItem(LibraryItem item) {
        if (item instanceof Book) bookRepository.add((Book) item);
        else if (item instanceof Magazine) magazineRepository.add((Magazine) item);
        else if (item instanceof DVD) dvdRepository.add((DVD) item);
    }

    public boolean removeItem(String id) {
        if (bookRepository.remove(id)) return true;
        if (magazineRepository.remove(id)) return true;
        return dvdRepository.remove(id);
    }

    public List<LibraryItem> getAllItems() {
        List<LibraryItem> all = new ArrayList<>();
        all.addAll(bookRepository.getAll());
        all.addAll(magazineRepository.getAll());
        all.addAll(dvdRepository.getAll());
        return all;
    }
    public int getBookCount() { return bookRepository.getAll().size(); }
    public int getMagazineCount() { return magazineRepository.getAll().size(); }
    public int getDvdCount() { return dvdRepository.getAll().size(); }
    public List<LibraryItem> searchAll(String query) {
        List<LibraryItem> all = new ArrayList<>();
        all.addAll(bookRepository.search(query));
        all.addAll(magazineRepository.search(query));
        all.addAll(dvdRepository.search(query));
        return all;
    }
    public LibraryItem findById(String id) {
        LibraryItem item = bookRepository.findById(id);
        if (item != null) return item;
        item = magazineRepository.findById(id);
        if (item != null) return item;
        return dvdRepository.findById(id);
    }
}

public class LibraryManagementApp extends JFrame {
    private LibraryManager manager;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> filterCombo;

    public LibraryManagementApp() {
        manager = new LibraryManager();
        initData();
        setupUI();
    }

    private void initData() {
        manager.addItem(new Book("B001", "Дім у волошковому морі", 2023, "Т. Дж. Клюн", 400));
        manager.addItem(new Book("B002", "Крёстный отец»", 1969, "Марио Пьюзо", 608));
        manager.addItem(new Book("B003", "Гаррі Поттер", 1997, "Дж. Роулінг", 350));
        manager.addItem(new Magazine("M001", "National Geographic", 2023, 145, "NG Society"));
        manager.addItem(new Magazine("M002", "Forbes Україна", 2024, 88, "Forbes Media"));
        manager.addItem(new DVD("D001", "Матриця", 1999, "Вачовскі", 136));
        manager.addItem(new DVD("D002", "Інтерстеллар", 2014, "Крістофер Нолан", 169));
    }

    private void setupUI() {
        setTitle("Система управління бібліотекою");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.add(new JLabel("Пошук:"));
        searchField = new JTextField(20);
        topPanel.add(searchField);
        JButton searchBtn = new JButton("Шукати");
        topPanel.add(searchBtn);
        JButton clearSearchBtn = new JButton("Очистити");
        topPanel.add(clearSearchBtn);
        topPanel.add(new JLabel("Фільтр:"));
        filterCombo = new JComboBox<>(new String[]{"Усі", "Книги", "Журнали", "DVD", "Доступні", "Орендовані"});
        topPanel.add(filterCombo);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Тип", "Назва", "Рік", "Додатково", "Статус", "Орендовано"};
        tableModel = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton rentBtn = new JButton("Орендувати");
        JButton returnBtn = new JButton("Повернути");
        JButton addBtn = new JButton("Додати елемент");
        JButton editBtn = new JButton("Редагувати");
        JButton removeBtn = new JButton("Видалити");
        JButton refreshBtn = new JButton("Оновити");
        JButton statsBtn = new JButton("Статистика");

        bottomPanel.add(rentBtn);
        bottomPanel.add(returnBtn);
        bottomPanel.add(addBtn);
        bottomPanel.add(editBtn);
        bottomPanel.add(removeBtn);
        bottomPanel.add(refreshBtn);
        bottomPanel.add(statsBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) refreshTable();
            else filterTable(manager.searchAll(query));
        });
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            filterCombo.setSelectedIndex(0);
            refreshTable();
        });
        rentBtn.addActionListener(e -> rentSelectedItem());
        returnBtn.addActionListener(e -> returnSelectedItem());
        refreshBtn.addActionListener(e -> { searchField.setText(""); filterCombo.setSelectedIndex(0); refreshTable(); });

        addBtn.addActionListener(e -> showItemDialog(null));
        editBtn.addActionListener(e -> {
            LibraryItem selected = getSelectedItem();
            if (selected != null) showItemDialog(selected);
        });
        removeBtn.addActionListener(e -> removeSelectedItem());

        statsBtn.addActionListener(e -> showStatistics());
        searchField.addActionListener(e -> searchBtn.doClick());
        filterCombo.addActionListener(e -> {
            String selected = (String) filterCombo.getSelectedItem();
            List<LibraryItem> filtered = manager.getAllItems();
            List<LibraryItem> result = new ArrayList<>();
            switch (selected) {
                case "Книги" -> { for (LibraryItem i : filtered) if (i instanceof Book) result.add(i); }
                case "Журнали" -> { for (LibraryItem i : filtered) if (i instanceof Magazine) result.add(i); }
                case "DVD" -> { for (LibraryItem i : filtered) if (i instanceof DVD) result.add(i); }
                case "Доступні" -> { for (LibraryItem i : filtered) if (i.isAvailable()) result.add(i); }
                case "Орендовані" -> { for (LibraryItem i : filtered) if (!i.isAvailable()) result.add(i); }
                default -> result = filtered;
            }
            filterTable(result);
        });
        refreshTable();
    }

    private void refreshTable() {
        filterTable(manager.getAllItems());
    }

    private void filterTable(List<LibraryItem> items) {
        tableModel.setRowCount(0);
        for (LibraryItem i : items) tableModel.addRow(i.toTableRow());
    }

    private LibraryItem getSelectedItem() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Будь ласка, спочатку виберіть елемент у таблиці", "Помилка", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String id = (String) tableModel.getValueAt(row, 0);
        return manager.findById(id);
    }

    private void rentSelectedItem() {
        LibraryItem item = getSelectedItem();
        if (item == null) return;

        if (!item.isAvailable()) {
            JOptionPane.showMessageDialog(this, "Цей елемент вже орендовано користувачем: " + item.getRentedBy());
            return;
        }
        String user = JOptionPane.showInputDialog(this, "Введіть ім'я користувача:");
        if (user != null && !user.trim().isEmpty()) {
            item.rent(user.trim());
            JOptionPane.showMessageDialog(this, "Елемент успішно орендовано!");
            refreshTable();
        }
    }

    private void returnSelectedItem() {
        LibraryItem item = getSelectedItem();
        if (item == null) return;

        if (item.isAvailable()) {
            JOptionPane.showMessageDialog(this, "Цей елемент не орендовано", "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this, "Повернути елемент, орендований користувачем: " + item.getRentedBy() + "?", "Підтвердження", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            item.returnItem();
            JOptionPane.showMessageDialog(this, "Елемент повернено!");
            refreshTable();
        }
    }

    private void removeSelectedItem() {
        LibraryItem item = getSelectedItem();
        if (item == null) return;

        if (!item.isAvailable()) {
            JOptionPane.showMessageDialog(this, "Неможливо видалити орендований елемент. Спочатку поверніть його.", "Помилка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int conf = JOptionPane.showConfirmDialog(this,
                "Ви впевнені, що хочете видалити:\n" + item.getTitle() + " (ID: " + item.getId() + ")?",
                "Підтвердження видалення",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (conf == JOptionPane.YES_OPTION) {
            manager.removeItem(item.getId());
            refreshTable();
            JOptionPane.showMessageDialog(this, "Елемент видалено.", "Успіх", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showItemDialog(LibraryItem itemToEdit) {
        boolean isEditMode = itemToEdit != null;
        String dialogTitle = isEditMode ? "Редагувати елемент" : "Додати новий елемент";

        String type;
        if (isEditMode) {
            type = itemToEdit.getType();
        } else {
            String[] types = {"Книга", "Журнал", "DVD"};
            type = (String) JOptionPane.showInputDialog(
                    this, "Виберіть тип елемента:", dialogTitle,
                    JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
            if (type == null) return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField idField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField yearField = new JTextField();

        panel.add(new JLabel("ID (напр. B004):"));
        panel.add(idField);
        panel.add(new JLabel("Назва:"));
        panel.add(titleField);
        panel.add(new JLabel("Рік:"));
        panel.add(yearField);

        if (isEditMode) {
            idField.setText(itemToEdit.getId());
            idField.setEditable(false);
            titleField.setText(itemToEdit.getTitle());
            yearField.setText(String.valueOf(itemToEdit.getYear()));
        }

        JTextField field1 = new JTextField();
        JTextField field2 = new JTextField();

        switch (type) {
            case "Книга" -> {
                panel.add(new JLabel("Автор:"));
                panel.add(field1);
                panel.add(new JLabel("Сторінки:"));
                panel.add(field2);
                if (isEditMode) {
                    Book book = (Book) itemToEdit;
                    field1.setText(book.getAuthor());
                    field2.setText(String.valueOf(book.getPages()));
                }
            }
            case "Журнал" -> {
                panel.add(new JLabel("Номер випуску:"));
                panel.add(field1);
                panel.add(new JLabel("Видавець:"));
                panel.add(field2);
                if (isEditMode) {
                    Magazine mag = (Magazine) itemToEdit;
                    field1.setText(String.valueOf(mag.getIssueNumber()));
                    field2.setText(mag.getPublisher());
                }
            }
            case "DVD" -> {
                panel.add(new JLabel("Режисер:"));
                panel.add(field1);
                panel.add(new JLabel("Тривалість (хв):"));
                panel.add(field2);
                if (isEditMode) {
                    DVD dvd = (DVD) itemToEdit;
                    field1.setText(dvd.getDirector());
                    field2.setText(String.valueOf(dvd.getDuration()));
                }
            }
        }

        int result = JOptionPane.showConfirmDialog(this, panel, dialogTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String id = idField.getText().trim();
                String title = titleField.getText().trim();

                if (id.isEmpty() || title.isEmpty() || yearField.getText().trim().isEmpty() ||
                        field1.getText().trim().isEmpty() || field2.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Всі поля мають бути заповнені");
                }

                int year = Integer.parseInt(yearField.getText().trim());

                if (isEditMode) {
                    itemToEdit.setTitle(title);
                    itemToEdit.setYear(year);
                    switch (type) {
                        case "Книга" -> {
                            ((Book) itemToEdit).setAuthor(field1.getText().trim());
                            ((Book) itemToEdit).setPages(Integer.parseInt(field2.getText().trim()));
                        }
                        case "Журнал" -> {
                            ((Magazine) itemToEdit).setIssueNumber(Integer.parseInt(field1.getText().trim()));
                            ((Magazine) itemToEdit).setPublisher(field2.getText().trim());
                        }
                        case "DVD" -> {
                            ((DVD) itemToEdit).setDirector(field1.getText().trim());
                            ((DVD) itemToEdit).setDuration(Integer.parseInt(field2.getText().trim()));
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Елемент успішно оновлено!", "Успіх", JOptionPane.INFORMATION_MESSAGE);

                } else {
                    if (manager.findById(id) != null) {
                        throw new IllegalArgumentException("Елемент з таким ID вже існує");
                    }
                    LibraryItem newItem = null;
                    switch (type) {
                        case "Книга" -> {
                            int pages = Integer.parseInt(field2.getText().trim());
                            newItem = new Book(id, title, year, field1.getText().trim(), pages);
                        }
                        case "Журнал" -> {
                            int issue = Integer.parseInt(field1.getText().trim());
                            newItem = new Magazine(id, title, year, issue, field2.getText().trim());
                        }
                        case "DVD" -> {
                            int duration = Integer.parseInt(field2.getText().trim());
                            newItem = new DVD(id, title, year, field1.getText().trim(), duration);
                        }
                    }
                    if (newItem != null) {
                        manager.addItem(newItem);
                        JOptionPane.showMessageDialog(this, "Елемент успішно додано!", "Успіх", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                refreshTable();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Помилка: 'Рік', 'Сторінки', 'Номер випуску' або 'Тривалість' мають бути числами.", "Помилка вводу", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Помилка: " + ex.getMessage(), "Помилка вводу", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showStatistics() {
        List<LibraryItem> allItems = manager.getAllItems();
        int total = allItems.size();
        int rented = 0;
        for (LibraryItem item : allItems) {
            if (!item.isAvailable()) {
                rented++;
            }
        }
        int available = total - rented;
        int books = manager.getBookCount();
        int magazines = manager.getMagazineCount();
        int dvds = manager.getDvdCount();

        String message = String.format(
                "--- Загальна статистика бібліотеки ---\n\n" +
                        "Всього елементів: %d\n" +
                        "   - Книги: %d\n" +
                        "   - Журнали: %d\n" +
                        "   - DVD: %d\n\n" +
                        "--- Статус --- \n" +
                        "Доступно: %d\n" +
                        "Орендовано: %d\n",
                total, books, magazines, dvds, available, rented
        );
        JOptionPane.showMessageDialog(this, message, "Статистика", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            LibraryManagementApp app = new LibraryManagementApp();
            app.setLocationRelativeTo(null);
            app.setVisible(true);
        });
    }
}