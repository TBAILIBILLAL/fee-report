package feereport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Opens connections to the SQLite database file and creates the tables
 * the first time the program runs.
 */
public final class Database {

    /** The database is a single file stored next to the program. */
    private static final String URL =
            "jdbc:sqlite:" + System.getProperty("feereport.db", "feereport.db");

    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(URL);
        try (Statement st = con.createStatement()) {
            // SQLite only enforces foreign keys when this is switched on
            st.execute("PRAGMA foreign_keys = ON");
        }
        return con;
    }

    /** Creates the tables if they do not exist and adds the default admin. */
    public static void initialize() throws SQLException {
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS admin ("
                    + "admin_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT NOT NULL UNIQUE, "
                    + "password TEXT NOT NULL, "
                    + "full_name TEXT NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS accountant ("
                    + "accountant_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, "
                    + "username TEXT NOT NULL UNIQUE, "
                    + "password TEXT NOT NULL, "
                    + "email TEXT NOT NULL, "
                    + "contact_no TEXT NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS student ("
                    + "roll_no INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, "
                    + "email TEXT, "
                    + "course TEXT NOT NULL, "
                    + "fee REAL NOT NULL, "
                    + "paid REAL NOT NULL DEFAULT 0, "
                    + "address TEXT, "
                    + "city TEXT, "
                    + "contact_no TEXT NOT NULL, "
                    + "added_by INTEGER REFERENCES accountant(accountant_id) ON DELETE SET NULL)");

            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM admin")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    insertDefaultAdmin(con);
                }
            }
        }
    }

    private static void insertDefaultAdmin(Connection con) throws SQLException {
        String sql = "INSERT INTO admin (username, password, full_name) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, DEFAULT_ADMIN_USERNAME);
            ps.setString(2, PasswordUtil.hash(DEFAULT_ADMIN_PASSWORD));
            ps.setString(3, "Administrator");
            ps.executeUpdate();
        }
    }
}
