import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;

class Person {
    protected String name;
    public Person(String name) { this.name = name; }
}

interface ResultCalculator {
    void calculateResult();
}

class InvalidMarksException extends Exception {
    public InvalidMarksException(String message) { super(message); }
}

class Student extends Person implements ResultCalculator {
    private int id, javaMarks, phpMarks, dsMarks, total;
    private double percentage;
    private String grade, status;

    public Student(int id, String name, int javaMarks, int phpMarks, int dsMarks)
            throws InvalidMarksException {
        super(name);
        if (javaMarks < 0 || javaMarks > 100 ||
            phpMarks < 0 || phpMarks > 100 ||
            dsMarks < 0 || dsMarks > 100) {
            throw new InvalidMarksException("Marks must be between 0 and 100.");
        }
        this.id = id;
        this.javaMarks = javaMarks;
        this.phpMarks = phpMarks;
        this.dsMarks = dsMarks;
        calculateResult();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getJavaMarks() { return javaMarks; }
    public int getPhpMarks() { return phpMarks; }
    public int getDsMarks() { return dsMarks; }
    public int getTotal() { return total; }
    public double getPercentage() { return percentage; }
    public String getGrade() { return grade; }
    public String getStatus() { return status; }

    public void calculateResult() {
        total = javaMarks + phpMarks + dsMarks;
        percentage = total / 3.0;
        if (javaMarks < 35 || phpMarks < 35 || dsMarks < 35) {
            grade = "F"; status = "Fail";
        } else if (percentage >= 80) {
            grade = "A"; status = "Pass";
        } else if (percentage >= 60) {
            grade = "B"; status = "Pass";
        } else {
            grade = "C"; status = "Pass";
        }
    }
}

public class StudentResultApp extends JFrame {
    private JTextField idField, nameField, javaField, phpField, dsField;
    private JComboBox<String> sortBox;
    private DefaultTableModel model;
    private JTable table;
    private ArrayList<Student> students = new ArrayList<Student>();
    private HashMap<Integer, String> results = new HashMap<Integer, String>();

    public StudentResultApp() {
        setTitle("Student Examination and Result Management System");
        setSize(950, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel input = new JPanel(new GridLayout(3, 4, 8, 8));
        input.setBorder(BorderFactory.createTitledBorder("Student Details"));
        idField = new JTextField();
        nameField = new JTextField();
        javaField = new JTextField();
        phpField = new JTextField();
        dsField = new JTextField();

        input.add(new JLabel("Student ID:")); input.add(idField);
        input.add(new JLabel("Student Name:")); input.add(nameField);
        input.add(new JLabel("Java Marks:")); input.add(javaField);
        input.add(new JLabel("PHP Marks:")); input.add(phpField);
        input.add(new JLabel("DS Marks:")); input.add(dsField);

        JButton addButton = new JButton("Add Student");
        JButton clearButton = new JButton("Clear");
        input.add(addButton); input.add(clearButton);

        String[] columns = {"ID", "Name", "Java", "PHP", "DS", "Total", "Percentage", "Grade", "Status"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);

        JPanel bottom = new JPanel();
        sortBox = new JComboBox<String>(new String[]{"Sort by Percentage", "Sort by Name"});
        JButton sortButton = new JButton("Sort");
        JButton resultButton = new JButton("Show Result");
        bottom.add(new JLabel("Sort:")); bottom.add(sortBox);
        bottom.add(sortButton); bottom.add(resultButton);

        add(input, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addStudent());
        clearButton.addActionListener(e -> clearFields());
        sortButton.addActionListener(e -> sortStudents());
        resultButton.addActionListener(e -> showResult());
    }

    private void addStudent() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter student name.");
                return;
            }
            for (Student s : students) {
                if (s.getId() == id) {
                    JOptionPane.showMessageDialog(this, "Student ID already exists.");
                    return;
                }
            }
            int java = Integer.parseInt(javaField.getText().trim());
            int php = Integer.parseInt(phpField.getText().trim());
            int ds = Integer.parseInt(dsField.getText().trim());
            Student s = new Student(id, name, java, php, ds);
            students.add(s);
            results.put(id, "Percentage: " + String.format("%.2f", s.getPercentage())
                    + "%, Grade: " + s.getGrade() + ", Status: " + s.getStatus());
            addRow(s);
            JOptionPane.showMessageDialog(this, "Student result added successfully.");
            clearFields();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID and marks must be numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidMarksException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid Marks", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addRow(Student s) {
        model.addRow(new Object[]{s.getId(), s.getName(), s.getJavaMarks(), s.getPhpMarks(),
            s.getDsMarks(), s.getTotal(), String.format("%.2f%%", s.getPercentage()), s.getGrade(), s.getStatus()});
    }

    private void clearFields() {
        idField.setText(""); nameField.setText(""); javaField.setText("");
        phpField.setText(""); dsField.setText("");
    }

    private void sortStudents() {
        if (sortBox.getSelectedIndex() == 0) {
            Collections.sort(students, Comparator.comparingDouble(Student::getPercentage).reversed());
        } else {
            Collections.sort(students, Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));
        }
        model.setRowCount(0);
        for (Student s : students) addRow(s);
    }

    private void showResult() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a student from the table.");
            return;
        }
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        JOptionPane.showMessageDialog(this, "Student ID: " + id + "\n" + results.get(id),
                "Result Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentResultApp().setVisible(true));
    }
}
