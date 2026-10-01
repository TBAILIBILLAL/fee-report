package feereport;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Form used by the accountant to add a new student or edit an existing one. */
public class StudentDialog extends JDialog {

    private final StudentDao dao = new StudentDao();
    private final Student existing;
    private final int accountantId;
    private boolean saved;

    private final JTextField nameField = new JTextField(22);
    private final JTextField emailField = new JTextField(22);
    private final JTextField courseField = new JTextField(22);
    private final JTextField feeField = new JTextField(22);
    private final JTextField paidField = new JTextField(22);
    private final JTextField addressField = new JTextField(22);
    private final JTextField cityField = new JTextField(22);
    private final JTextField contactField = new JTextField(22);

    /** @param existing the student to edit, or null to add a new one */
    public StudentDialog(JFrame owner, Student existing, int accountantId) {
        super(owner, existing == null ? "Add Student" : "Edit Student", true);
        this.existing = existing;
        this.accountantId = accountantId;
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 6, 16));
        int row = 0;
        if (existing != null) {
            JTextField rollNoField = new JTextField(String.valueOf(existing.getRollNo()));
            rollNoField.setEditable(false);
            Ui.addFormRow(form, row++, "Roll No:", rollNoField);
        }
        Ui.addFormRow(form, row++, "Name:", nameField);
        Ui.addFormRow(form, row++, "Email:", emailField);
        Ui.addFormRow(form, row++, "Course:", courseField);
        Ui.addFormRow(form, row++, "Fee:", feeField);
        Ui.addFormRow(form, row++, "Paid:", paidField);
        Ui.addFormRow(form, row++, "Address:", addressField);
        Ui.addFormRow(form, row++, "City:", cityField);
        Ui.addFormRow(form, row++, "Contact No:", contactField);
        add(form, BorderLayout.CENTER);

        if (existing != null) {
            nameField.setText(existing.getName());
            emailField.setText(existing.getEmail());
            courseField.setText(existing.getCourse());
            feeField.setText(String.format("%.2f", existing.getFee()));
            paidField.setText(String.format("%.2f", existing.getPaid()));
            addressField.setText(existing.getAddress());
            cityField.setText(existing.getCity());
            contactField.setText(existing.getContactNo());
        }

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBorder(BorderFactory.createEmptyBorder(6, 16, 14, 16));
        buttons.add(saveButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveButton);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    private void save() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String course = courseField.getText().trim();
        String contact = contactField.getText().trim();

        if (name.isEmpty() || course.isEmpty() || contact.isEmpty()) {
            Ui.error(this, "Name, course and contact number are required.");
            return;
        }
        if (!email.isEmpty() && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            Ui.error(this, "Please enter a valid email address or leave it empty.");
            return;
        }
        if (!contact.matches("[0-9+\\- ]{6,20}")) {
            Ui.error(this, "The contact number may only contain digits, spaces, + and -.");
            return;
        }

        double fee;
        double paid;
        try {
            fee = Double.parseDouble(feeField.getText().trim());
            // an empty "Paid" field means nothing has been paid yet
            String paidText = paidField.getText().trim();
            paid = paidText.isEmpty() ? 0 : Double.parseDouble(paidText);
        } catch (NumberFormatException e) {
            Ui.error(this, "Fee and paid must be numbers, for example 12000 or 4500.50.");
            return;
        }
        if (Double.isNaN(fee) || Double.isInfinite(fee) || fee <= 0) {
            Ui.error(this, "The fee must be greater than zero.");
            return;
        }
        if (Double.isNaN(paid) || paid < 0 || paid > fee) {
            Ui.error(this, "The paid amount must be between 0 and the fee.");
            return;
        }

        int rollNo = existing == null ? 0 : existing.getRollNo();
        Student student = new Student(rollNo, name, email, course, fee, paid,
                addressField.getText().trim(), cityField.getText().trim(), contact);
        try {
            if (existing == null) {
                dao.insert(student, accountantId);
            } else {
                dao.update(student);
            }
            saved = true;
            dispose();
        } catch (SQLException e) {
            Ui.error(this, "Database error: " + e.getMessage());
        }
    }
}
