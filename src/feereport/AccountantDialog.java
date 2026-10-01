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
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Form used by the admin to add a new accountant or edit an existing one. */
public class AccountantDialog extends JDialog {

    private final AccountantDao dao = new AccountantDao();
    private final Accountant existing;
    private boolean saved;

    private final JTextField nameField = new JTextField(22);
    private final JTextField usernameField = new JTextField(22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final JTextField emailField = new JTextField(22);
    private final JTextField contactField = new JTextField(22);

    /** @param existing the accountant to edit, or null to add a new one */
    public AccountantDialog(JFrame owner, Accountant existing) {
        super(owner, existing == null ? "Add Accountant" : "Edit Accountant", true);
        this.existing = existing;
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 6, 16));
        Ui.addFormRow(form, 0, "Name:", nameField);
        Ui.addFormRow(form, 1, "Username:", usernameField);
        Ui.addFormRow(form, 2, existing == null ? "Password:" : "New password:", passwordField);
        Ui.addFormRow(form, 3, "Email:", emailField);
        Ui.addFormRow(form, 4, "Contact No:", contactField);
        add(form, BorderLayout.CENTER);

        if (existing != null) {
            nameField.setText(existing.getName());
            usernameField.setText(existing.getUsername());
            emailField.setText(existing.getEmail());
            contactField.setText(existing.getContactNo());
            passwordField.setToolTipText("Leave empty to keep the current password");
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
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();
        String contact = contactField.getText().trim();

        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || contact.isEmpty()) {
            Ui.error(this, "Name, username, email and contact number are required.");
            return;
        }
        if (existing == null && password.isEmpty()) {
            Ui.error(this, "Please enter a password for the new accountant.");
            return;
        }
        if (!password.isEmpty() && password.length() < 6) {
            Ui.error(this, "The password must have at least 6 characters.");
            return;
        }
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            Ui.error(this, "Please enter a valid email address.");
            return;
        }
        if (!contact.matches("[0-9+\\- ]{6,20}")) {
            Ui.error(this, "The contact number may only contain digits, spaces, + and -.");
            return;
        }

        try {
            int id = existing == null ? 0 : existing.getId();
            if (dao.usernameTaken(username, id)) {
                Ui.error(this, "This username is already used by another accountant.");
                return;
            }
            Accountant accountant = new Accountant(id, name, username, email, contact);
            if (existing == null) {
                dao.insert(accountant, password);
            } else {
                dao.update(accountant, password.isEmpty() ? null : password);
            }
            saved = true;
            dispose();
        } catch (SQLException e) {
            Ui.error(this, "Database error: " + e.getMessage());
        }
    }
}
