package feereport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Database access for the accountant table. */
public class AccountantDao {

    public List<Accountant> findAll() throws SQLException {
        String sql = "SELECT accountant_id, name, username, email, contact_no "
                + "FROM accountant ORDER BY accountant_id";
        List<Accountant> list = new ArrayList<Accountant>();
        try (Connection con = Database.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(read(rs));
            }
        }
        return list;
    }

    /** Returns the accountant if the login is correct, otherwise null. */
    public Accountant login(String username, String password) throws SQLException {
        String sql = "SELECT accountant_id, name, username, email, contact_no, password "
                + "FROM accountant WHERE username = ?";
        try (Connection con = Database.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verify(password, rs.getString("password"))) {
                    return read(rs);
                }
                return null;
            }
        }
    }

    /** True if another accountant (not the one with excludeId) already uses this username. */
    public boolean usernameTaken(String username, int excludeId) throws SQLException {
        String sql = "SELECT 1 FROM accountant WHERE username = ? AND accountant_id <> ?";
        try (Connection con = Database.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(Accountant a, String password) throws SQLException {
        String sql = "INSERT INTO accountant (name, username, password, email, contact_no) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Database.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, a.getName());
            ps.setString(2, a.getUsername());
            ps.setString(3, PasswordUtil.hash(password));
            ps.setString(4, a.getEmail());
            ps.setString(5, a.getContactNo());
            ps.executeUpdate();
        }
    }

    /** Updates the details; the password only changes when newPassword is not null. */
    public void update(Accountant a, String newPassword) throws SQLException {
        try (Connection con = Database.getConnection()) {
            String sql = "UPDATE accountant SET name = ?, username = ?, email = ?, contact_no = ? "
                    + "WHERE accountant_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, a.getName());
                ps.setString(2, a.getUsername());
                ps.setString(3, a.getEmail());
                ps.setString(4, a.getContactNo());
                ps.setInt(5, a.getId());
                ps.executeUpdate();
            }
            if (newPassword != null) {
                String passwordSql = "UPDATE accountant SET password = ? WHERE accountant_id = ?";
                try (PreparedStatement ps = con.prepareStatement(passwordSql)) {
                    ps.setString(1, PasswordUtil.hash(newPassword));
                    ps.setInt(2, a.getId());
                    ps.executeUpdate();
                }
            }
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = Database.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM accountant WHERE accountant_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Accountant read(ResultSet rs) throws SQLException {
        return new Accountant(rs.getInt("accountant_id"), rs.getString("name"),
                rs.getString("username"), rs.getString("email"), rs.getString("contact_no"));
    }
}
