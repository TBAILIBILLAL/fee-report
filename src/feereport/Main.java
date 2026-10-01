package feereport;

import java.sql.SQLException;

import javax.swing.SwingUtilities;

/** Entry point: prepares the database and opens the login window. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Ui.installLookAndFeel();
                try {
                    Database.initialize();
                } catch (SQLException e) {
                    Ui.error(null, "The database could not be opened:\n" + e.getMessage());
                    return;
                }
                new LoginFrame().setVisible(true);
            }
        });
    }
}
