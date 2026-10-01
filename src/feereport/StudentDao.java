package feereport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Database access for the student table. */
public class StudentDao {

    /**
     * Returns the students whose name, course or roll number matches the keyword.
     * An empty keyword matches everyone. With dueOnly, only students who still
     * owe part of the fee are returned.
     */
    public List<Student> find(String keyword, boolean dueOnly) throws SQLException {
        String sql = "SELECT * FROM student "
                + "WHERE (name LIKE ? OR course LIKE ? OR CAST(roll_no AS TEXT) = ?)";
        if (dueOnly) {
            sql += " AND fee > paid";
        }
        sql += " ORDER BY roll_no";

        List<Student> list = new ArrayList<Student>();
        try (Connection con = Database.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, keyword);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Student(rs.getInt("roll_no"), rs.getString("name"),
                            rs.getString("email"), rs.getString("course"), rs.getDouble("fee"),
                            rs.getDouble("paid"), rs.getString("address"), rs.getString("city"),
                            rs.getString("contact_no")));
                }
            }
        }
        return list;
    }

    public void insert(Student s, int accountantId) throws SQLException {
        String sql = "INSERT INTO student "
                + "(name, email, course, fee, paid, address, city, contact_no, added_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Database.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            fill(ps, s);
            ps.setInt(9, accountantId);
            ps.executeUpdate();
        }
    }

    public void update(Student s) throws SQLException {
        String sql = "UPDATE student SET name = ?, email = ?, course = ?, fee = ?, paid = ?, "
                + "address = ?, city = ?, contact_no = ? WHERE roll_no = ?";
        try (Connection con = Database.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            fill(ps, s);
            ps.setInt(9, s.getRollNo());
            ps.executeUpdate();
        }
    }

    public void delete(int rollNo) throws SQLException {
        try (Connection con = Database.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM student WHERE roll_no = ?")) {
            ps.setInt(1, rollNo);
            ps.executeUpdate();
        }
    }

    /** Sets parameters 1 to 8, which are the same for insert and update. */
    private void fill(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getName());
        ps.setString(2, s.getEmail());
        ps.setString(3, s.getCourse());
        ps.setDouble(4, s.getFee());
        ps.setDouble(5, s.getPaid());
        ps.setString(6, s.getAddress());
        ps.setString(7, s.getCity());
        ps.setString(8, s.getContactNo());
    }
}
