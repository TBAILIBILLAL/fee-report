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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Admin window: add, view, edit and delete accountants. */
public class AdminFrame extends JFrame {

    private final AccountantDao dao = new AccountantDao();
    private final DefaultTableModel model =
            Ui.readOnlyModel("ID", "Name", "Username", "Email", "Contact No");
    private final JTable table = Ui.table(model, 50, 170, 130, 250, 150);
    private List<Accountant> accountants = new ArrayList<Accountant>();

    public AdminFrame(String adminName) {
        super("Fee Report - Admin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(Ui.banner("Admin Panel", "Signed in as " + adminName + "  |  Manage accountants"),
                BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(scroll, BorderLayout.CENTER);

        JButton addButton = new JButton("Add Accountant");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
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
                Accountant selected = selectedAccountant();
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
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 2, 10, 10));
        buttons.add(left, BorderLayout.WEST);
        buttons.add(logoutButton, BorderLayout.EAST);
        add(buttons, BorderLayout.SOUTH);

        setSize(760, 460);
        setLocationRelativeTo(null);
        reload();
    }

    /** Reads all accountants from the database again and refreshes the table. */
    private void reload() {
        try {
            accountants = dao.findAll();
        } catch (SQLException e) {
            Ui.error(this, "Database error: " + e.getMessage());
            return;
        }
        model.setRowCount(0);
        for (Accountant a : accountants) {
            model.addRow(new Object[] { a.getId(), a.getName(), a.getUsername(), a.getEmail(),
                    a.getContactNo() });
        }
    }

    private Accountant selectedAccountant() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Ui.info(this, "Please select an accountant in the table first.");
            return null;
        }
        // the table may be sorted, so the view row is converted to the list index
        return accountants.get(table.convertRowIndexToModel(row));
    }

    /** Opens the form to add a new accountant (null) or edit an existing one. */
    private void openDialog(Accountant accountant) {
        AccountantDialog dialog = new AccountantDialog(this, accountant);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reload();
        }
    }

    private void deleteSelected() {
        Accountant selected = selectedAccountant();
        if (selected == null
                || !Ui.confirm(this, "Delete accountant \"" + selected.getName() + "\"?")) {
            return;
        }
        try {
            dao.delete(selected.getId());
            reload();
        } catch (SQLException e) {
            Ui.error(this, "Database error: " + e.getMessage());
        }
    }
}
