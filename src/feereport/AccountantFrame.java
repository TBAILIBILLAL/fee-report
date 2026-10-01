package feereport;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/** Accountant window: add, view, edit and delete students and check due fees. */
public class AccountantFrame extends JFrame {

    private final StudentDao dao = new StudentDao();
    private final Accountant accountant;
    private final DefaultTableModel model = Ui.readOnlyModel(
            "Roll No", "Name", "Course", "Fee", "Paid", "Due", "Contact No", "City");
    private final JTable table = Ui.table(model, 65, 150, 180, 100, 100, 100, 140, 100);
    private final JTextField searchField = new JTextField(20);
    private final JLabel summaryLabel = new JLabel(" ");
    private List<Student> students = new ArrayList<Student>();

    public AccountantFrame(Accountant accountant) {
        super("Fee Report - Accountant");
        this.accountant = accountant;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(Ui.banner("Accountant Panel",
                "Signed in as " + accountant.getName() + "  |  Manage students and fees"),
                BorderLayout.NORTH);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setBorder(BorderFactory.createEmptyBorder(10, 2, 8, 10));
        searchBar.add(new JLabel("Search (name, course or roll no):"));
        searchBar.add(searchField);
        // the table is filtered again every time the search text changes
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reload();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reload();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reload();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel center = new JPanel(new BorderLayout());
        center.add(searchBar, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 10));
        center.add(summaryLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        JButton addButton = new JButton("Add Student");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton dueButton = new JButton("Check Due Fee");
        JButton logoutButton = new JButton("Logout");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDialog(null);
            }
        });
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Student selected = selectedStudent();
                if (selected != null) {
                    openDialog(selected);
                }
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected();
            }
        });
        dueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DueFeeDialog(AccountantFrame.this).setVisible(true);
            }
        });
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.add(addButton);
        left.add(editButton);
        left.add(deleteButton);
        left.add(dueButton);
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 2, 10, 10));
        buttons.add(left, BorderLayout.WEST);
        buttons.add(logoutButton, BorderLayout.EAST);
        add(buttons, BorderLayout.SOUTH);

        setSize(980, 560);
        setLocationRelativeTo(null);
        reload();
    }

    /** Reads the students matching the search text and refreshes the table and totals. */
    private void reload() {
        try {
            students = dao.find(searchField.getText().trim(), false);
        } catch (SQLException e) {
            Ui.error(this, "Database error: " + e.getMessage());
            return;
        }
        model.setRowCount(0);
        double totalFee = 0;
        double totalPaid = 0;
        for (Student s : students) {
            model.addRow(new Object[] { s.getRollNo(), s.getName(), s.getCourse(), s.getFee(),
                    s.getPaid(), s.getDue(), s.getContactNo(), s.getCity() });
            totalFee += s.getFee();
            totalPaid += s.getPaid();
        }
        summaryLabel.setText(students.size() + " student(s)   |   Total fee: " + Ui.money(totalFee)
                + "   |   Paid: " + Ui.money(totalPaid)
                + "   |   Due: " + Ui.money(totalFee - totalPaid));
    }

    private Student selectedStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Ui.info(this, "Please select a student in the table first.");
            return null;
        }
        // the table may be sorted, so the view row is converted to the list index
        return students.get(table.convertRowIndexToModel(row));
    }

    /** Opens the form to add a new student (null) or edit an existing one. */
    private void openDialog(Student student) {
        StudentDialog dialog = new StudentDialog(this, student, accountant.getId());
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reload();
        }
    }

    private void deleteSelected() {
        Student selected = selectedStudent();
        if (selected == null
                || !Ui.confirm(this, "Delete student \"" + selected.getName() + "\"?")) {
            return;
        }
        try {
            dao.delete(selected.getRollNo());
            reload();
        } catch (SQLException e) {
            Ui.error(this, "Database error: " + e.getMessage());
        }
    }
}
