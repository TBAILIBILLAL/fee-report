package feereport;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Due fee report: lists every student who has not paid the full fee yet. */
public class DueFeeDialog extends JDialog {

    public DueFeeDialog(JFrame owner) {
        super(owner, "Due Fee Report", true);
        setLayout(new BorderLayout());

        DefaultTableModel model = Ui.readOnlyModel(
                "Roll No", "Name", "Course", "Contact No", "Fee", "Paid", "Due");
        JTable table = Ui.table(model, 65, 150, 180, 140, 100, 100, 100);

        double totalDue = 0;
        try {
            List<Student> students = new StudentDao().find("", true);
            for (Student s : students) {
                model.addRow(new Object[] { s.getRollNo(), s.getName(), s.getCourse(),
                        s.getContactNo(), s.getFee(), s.getPaid(), s.getDue() });
                totalDue += s.getDue();
            }
        } catch (SQLException e) {
            Ui.error(owner, "Database error: " + e.getMessage());
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(scroll, BorderLayout.CENTER);

        JLabel totalLabel = new JLabel(model.getRowCount() + " student(s) with due fee   |   "
                + "Total due: " + Ui.money(totalDue));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 10));
        bottom.add(totalLabel, BorderLayout.WEST);
        bottom.add(closeButton, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        setSize(760, 380);
        setLocationRelativeTo(owner);
    }
}
