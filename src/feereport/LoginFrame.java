package feereport;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** First window: the user chooses a role and logs in. */
public class LoginFrame extends JFrame {

    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_ACCOUNTANT = "Accountant";

    private final JComboBox<String> roleBox =
            new JComboBox<String>(new String[] { ROLE_ADMIN, ROLE_ACCOUNTANT });
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public LoginFrame() {
        super("Fee Report - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(Ui.banner("Fee Report", "Student Management System"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 22, 6, 22));
        Ui.addFormRow(form, 0, "Login as:", roleBox);
        Ui.addFormRow(form, 1, "Username:", usernameField);
        Ui.addFormRow(form, 2, "Password:", passwordField);
        add(form, BorderLayout.CENTER);

        JButton loginButton = new JButton("Login");
        JButton exitButton = new JButton("Exit");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBorder(BorderFactory.createEmptyBorder(6, 22, 16, 22));
        buttons.add(loginButton);
        buttons.add(exitButton);
        add(buttons, BorderLayout.SOUTH);

        // pressing Enter in any field is the same as clicking Login
        getRootPane().setDefaultButton(loginButton);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            Ui.error(this, "Please enter your username and password.");
            return;
        }

        try {
            if (ROLE_ADMIN.equals(roleBox.getSelectedItem())) {
                String fullName = new AdminDao().login(username, password);
                if (fullName != null) {
                    new AdminFrame(fullName).setVisible(true);
                    dispose();
                    return;
                }
            } else {
                Accountant accountant = new AccountantDao().login(username, password);
                if (accountant != null) {
                    new AccountantFrame(accountant).setVisible(true);
                    dispose();
                    return;
                }
            }
            Ui.error(this, "Wrong username or password.");
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        } catch (SQLException e) {
            Ui.error(this, "Database error: " + e.getMessage());
        }
    }
}
