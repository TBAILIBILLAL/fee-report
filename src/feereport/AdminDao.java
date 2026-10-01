package feereport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Database access for the admin table. */
public class AdminDao {

    /** Returns the admin's full name if the login is correct, otherwise null. */
    public String login(String username, String password) throws SQLException {
        String sql = "SELECT password, full_name FROM admin WHERE username = ?";
        try (Connection con = Database.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verify(password, rs.getString("password"))) {
                    return rs.getString("full_name");
                }
                return null;
            }
        }
    }
}
