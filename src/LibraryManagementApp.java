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

    @Override
    public boolean rent(String userName) {
        if (!available) return false;
        available = false;
        rentedBy = userName;
        rentDate = LocalDate.now();
        return true;
    }

    @Override
    public boolean returnItem() {
        if (available) return false;
        available = true;
        rentedBy = null;
        rentDate = null;
        return true;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public boolean matches(String query) {
        String q = query.toLowerCase();
        return id.toLowerCase().contains(q) || title.toLowerCase().contains(q);
    }

    public abstract String getType();

    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getYear() { return year; }
    public String getRentedBy() { return rentedBy; }
}

class Book extends LibraryItem {
    private String author;
    private int pages;

    public Book(String id, String title, int year, String author, int pages) {
        super(id, title, year);
        this.author = author;
        this.pages = pages;
    }

    @Override
    public String getType() {
        return "Книга";
    }

    @Override
    public String[] toTableRow() {
        return new String[]{
                id,
                getType(),
                title,
                String.valueOf(year),
                "Автор: " + author + ", Сторінок: " + pages,
                available ? "Доступна" : "Орендована",
                rentedBy != null ? rentedBy : "-"
        };
    }

    @Override
    public boolean matches(String query) {
        return super.matches(query) || author.toLowerCase().contains(query.toLowerCase());
    }
}

class Magazine extends LibraryItem {
    private int issueNumber;
    private String publisher;

    public Magazine(String id, String title, int year, int issueNumber, String publisher) {
        super(id, title, year);
        this.issueNumber = issueNumber;
        this.publisher = publisher;
    }

    @Override
    public String getType() {
        return "Журнал";
    }

    @Override
    public String[] toTableRow() {
        return new String[]{
                id,
                getType(),
                title,
                String.valueOf(year),
                "Випуск: " + issueNumber + ", Видавець: " + publisher,
                available ? "Доступний" : "Орендований",
                rentedBy != null ? rentedBy : "-"
        };
    }

    @Override
    public boolean matches(String query) {
        return super.matches(query) || publisher.toLowerCase().contains(query.toLowerCase());
    }
}

class DVD extends LibraryItem {
    private String director;
    private int duration;

    public DVD(String id, String title, int year, String director, int duration) {
        super(id, title, year);
        this.director = director;
        this.duration = duration;
    }

    @Override
    public String getType() {
        return "DVD";
    }

    @Override
    public String[] toTableRow() {
        return new String[]{
                id,
                getType(),
                title,
                String.valueOf(year),
                "Режисер: " + director + ", Тривалість: " + duration + " хв",
                available ? "Доступний" : "Орендований",
                rentedBy != null ? rentedBy : "-"
        };
    }

    @Override
    public boolean matches(String query) {
        return super.matches(query) || director.toLowerCase().contains(query.toLowerCase());
    }
}

class Repository<T extends LibraryItem> {
    private List<T> items;

    public Repository() {
        items = new ArrayList<>();
    }

    public void add(T item) {
        items.add(item);
    }

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

    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    public List<T> search(String query) {
        List<T> results = new ArrayList<>();
        for (T item : items) {
            if (item.matches(query)) results.add(item);
        }
        return results;
    }
}

class LibraryManager {
    private Repository<Book> books;
    private Repository<Magazine> magazines;
    private Repository<DVD> dvds;

    public LibraryManager() {
        books = new Repository<>();
        magazines = new Repository<>();
        dvds = new Repository<>();
    }

    public void addItem(LibraryItem item) {
        if (item instanceof Book) books.add((Book) item);
        else if (item instanceof Magazine) magazines.add((Magazine) item);
        else if (item instanceof DVD) dvds.add((DVD) item);
    }

    public List<LibraryItem> getAllItems() {
        List<LibraryItem> all = new ArrayList<>();
        all.addAll(books.getAll());
        all.addAll(magazines.getAll());
        all.addAll(dvds.getAll());
        return all;
    }

    public List<LibraryItem> searchAll(String query) {
        List<LibraryItem> all = new ArrayList<>();
        all.addAll(books.search(query));
        all.addAll(magazines.search(query));
        all.addAll(dvds.search(query));
        return all;
    }

    public LibraryItem findById(String id) {
        LibraryItem item = books.findById(id);
        if (item != null) return item;
        item = magazines.findById(id);
        if (item != null) return item;
        return dvds.findById(id);
    }
}

public class LibraryManagementApp extends JFrame {
    private LibraryManager manager;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<String> filterCombo;

    public LibraryManagementApp() {
        manager = new LibraryManager();
        initData();
        initUI();
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

    private void initUI() {
        setTitle("Система управління бібліотекою");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.add(new JLabel("Пошук:"));
        searchField = new JTextField(20);
        top.add(searchField);

        JButton searchBtn = new JButton("Шукати");
        JButton clearBtn = new JButton("Очистити");
        JButton rentBtn = new JButton("Орендувати");
        JButton returnBtn = new JButton("Повернути");
        JButton addBtn = new JButton("Додати елемент");
        JButton refreshBtn = new JButton("Оновити");
        JButton statsBtn = new JButton("Статистика");

        top.add(searchBtn);
        top.add(clearBtn);
        top.add(new JLabel("Фільтр:"));
        filterCombo = new JComboBox<>(new String[]{"Усі", "Книги", "Журнали", "DVD", "Доступні", "Орендовані"});
        top.add(filterCombo);
        add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Тип", "Назва", "Рік", "Додатково", "Статус", "Орендовано"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.add(rentBtn);
        bottom.add(returnBtn);
        bottom.add(addBtn);
        bottom.add(refreshBtn);
        bottom.add(statsBtn);
        add(bottom, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) refresh();
            else show(manager.searchAll(q));
        });

        clearBtn.addActionListener(e -> {
            searchField.setText("");
            filterCombo.setSelectedIndex(0);
            refresh();
        });

        rentBtn.addActionListener(e -> rentItem());
        returnBtn.addActionListener(e -> returnItem());
        addBtn.addActionListener(e -> showAddDialog());
        refreshBtn.addActionListener(e -> refresh());
        statsBtn.addActionListener(e -> showStats());
        searchField.addActionListener(e -> searchBtn.doClick());

        filterCombo.addActionListener(e -> {
            String selected = (String) filterCombo.getSelectedItem();
            List<LibraryItem> all = manager.getAllItems();
            List<LibraryItem> res = new ArrayList<>();
            for (LibraryItem i : all) {
                switch (selected) {
                    case "Книги" -> { if (i instanceof Book) res.add(i); }
                    case "Журнали" -> { if (i instanceof Magazine) res.add(i); }
                    case "DVD" -> { if (i instanceof DVD) res.add(i); }
                    case "Доступні" -> { if (i.isAvailable()) res.add(i); }
                    case "Орендовані" -> { if (!i.isAvailable()) res.add(i); }
                    default -> res = all;
                }
            }
            show(res);
        });

        refresh();
    }

    private void refresh() {
        show(manager.getAllItems());
    }

    private void show(List<LibraryItem> items) {
        model.setRowCount(0);
        for (LibraryItem i : items) model.addRow(i.toTableRow());
    }

    private void rentItem() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Виберіть елемент для оренди");
            return;
        }
        String id = (String) model.getValueAt(row, 0);
        LibraryItem i = manager.findById(id);
        if (i == null) return;
        if (!i.isAvailable()) {
            JOptionPane.showMessageDialog(this, "Уже орендовано: " + i.getRentedBy());
            return;
        }
        String user = JOptionPane.showInputDialog(this, "Введіть ім'я користувача:");
        if (user != null && !user.trim().isEmpty()) {
            i.rent(user.trim());
            refresh();
        }
    }

    private void returnItem() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Виберіть елемент для повернення");
            return;
        }
        String id = (String) model.getValueAt(row, 0);
        LibraryItem i = manager.findById(id);
        if (i == null || i.isAvailable()) {
            JOptionPane.showMessageDialog(this, "Цей елемент не орендовано");
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this, "Повернути елемент, орендований " + i.getRentedBy() + "?");
        if (conf == JOptionPane.YES_OPTION) {
            i.returnItem();
            refresh();
        }
    }

    private void showStats() {
        List<LibraryItem> all = manager.getAllItems();
        int total = all.size();
        int books = 0, mags = 0, dvds = 0, rented = 0;
        Map<Integer, Integer> yearCount = new HashMap<>();

        for (LibraryItem i : all) {
            if (i instanceof Book) books++;
            else if (i instanceof Magazine) mags++;
            else if (i instanceof DVD) dvds++;
            if (!i.isAvailable()) rented++;
            yearCount.put(i.getYear(), yearCount.getOrDefault(i.getYear(), 0) + 1);
        }

        int popularYear = 0, max = 0;
        for (var e : yearCount.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                popularYear = e.getKey();
            }
        }

        String stats = """
                === Статистика бібліотеки ===
                Всього елементів: %d
                Книги: %d
                Журнали: %d
                DVD: %d
                Орендовано: %d
                Доступно: %d
                Найпопулярніший рік: %d (%d елементів)
                """.formatted(total, books, mags, dvds, rented, total - rented, popularYear, max);

        JTextArea area = new JTextArea(stats);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Статистика", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAddDialog() {
        String[] types = {"Книга", "Журнал", "DVD"};
        String type = (String) JOptionPane.showInputDialog(this, "Тип елемента:", "Додавання", JOptionPane.QUESTION_MESSAGE, null, types, types[0]);
        if (type == null) return;

        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField idF = new JTextField();
        JTextField titleF = new JTextField();
        JTextField yearF = new JTextField();
        JTextField f1 = new JTextField();
        JTextField f2 = new JTextField();

        p.add(new JLabel("ID:")); p.add(idF);
        p.add(new JLabel("Назва:")); p.add(titleF);
        p.add(new JLabel("Рік:")); p.add(yearF);

        if (type.equals("Книга")) {
            p.add(new JLabel("Автор:")); p.add(f1);
            p.add(new JLabel("Сторінок:")); p.add(f2);
        } else if (type.equals("Журнал")) {
            p.add(new JLabel("Номер випуску:")); p.add(f1);
            p.add(new JLabel("Видавець:")); p.add(f2);
        } else {
            p.add(new JLabel("Режисер:")); p.add(f1);
            p.add(new JLabel("Тривалість:")); p.add(f2);
        }

        int r = JOptionPane.showConfirmDialog(this, p, "Новий елемент", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            try {
                String id = idF.getText().trim();
                String title = titleF.getText().trim();
                int year = Integer.parseInt(yearF.getText().trim());
                if (id.isEmpty() || title.isEmpty()) throw new Exception("ID і назва обов'язкові");

                LibraryItem item = switch (type) {
                    case "Книга" -> new Book(id, title, year, f1.getText().trim(), Integer.parseInt(f2.getText().trim()));
                    case "Журнал" -> new Magazine(id, title, year, Integer.parseInt(f1.getText().trim()), f2.getText().trim());
                    default -> new DVD(id, title, year, f1.getText().trim(), Integer.parseInt(f2.getText().trim()));
                };

                manager.addItem(item);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Помилка: " + ex.getMessage());
            }
        }
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
