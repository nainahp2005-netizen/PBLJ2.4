import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StudentManagementApp {

    static class Student {
        private int studentID;
        private String name;
        private String department;
        private int marks;

        public Student() {}

        public Student(int studentID, String name, String department, int marks) {
            this.studentID = studentID;
            this.name = name;
            this.department = department;
            this.marks = marks;
        }

        public int getStudentID() { return studentID; }
        public void setStudentID(int studentID) { this.studentID = studentID; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public int getMarks() { return marks; }
        public void setMarks(int marks) { this.marks = marks; }
    }

    static class StudentDAO {
        private static final String URL = "jdbc:mysql://localhost:3306/your_database_name";
        private static final String USER = "your_username";
        private static final String PASSWORD = "your_password";

        private Connection conn;

        public StudentDAO() throws SQLException, ClassNotFoundException {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        }

        public void addStudent(Student s) throws SQLException {
            String sql = "INSERT INTO Student (StudentID, Name, Department, Marks) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, s.getStudentID());
                pstmt.setString(2, s.getName());
                pstmt.setString(3, s.getDepartment());
                pstmt.setInt(4, s.getMarks());
                pstmt.executeUpdate();
            }
        }

        public List<Student> getAllStudents() throws SQLException {
            List<Student> students = new ArrayList<>();
            String sql = "SELECT * FROM Student";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Student s = new Student();
                    s.setStudentID(rs.getInt("StudentID"));
                    s.setName(rs.getString("Name"));
                    s.setDepartment(rs.getString("Department"));
                    s.setMarks(rs.getInt("Marks"));
                    students.add(s);
                }
            }
            return students;
        }

        public boolean updateStudent(Student s) throws SQLException {
            String sql = "UPDATE Student SET Name = ?, Department = ?, Marks = ? WHERE StudentID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, s.getName());
                pstmt.setString(2, s.getDepartment());
                pstmt.setInt(3, s.getMarks());
                pstmt.setInt(4, s.getStudentID());

                int rows = pstmt.executeUpdate();
                return rows > 0;
            }
        }

        public boolean deleteStudent(int studentID) throws SQLException {
            String sql = "DELETE FROM Student WHERE StudentID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentID);
                int rows = pstmt.executeUpdate();
                return rows > 0;
            }
        }

        public void close() throws SQLException {
            if (conn != null) conn.close();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StudentDAO dao;

        try {
            dao = new StudentDAO();
        } catch (Exception e) {
            System.out.println("Failed to connect to database. Exiting.");
            e.printStackTrace();
            scanner.close();
            return;
        }

        int choice = -1;

        do {
            System.out.println("\n--- Student Management ---");
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Update Student");
            System.out.println("4. Delete Student");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, please enter a number.");
                continue;
            }

            try {
                switch (choice) {
                    case 1 -> {
                        System.out.print("Enter StudentID: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter Department: ");
                        String dept = scanner.nextLine();
                        System.out.print("Enter Marks: ");
                        int marks = Integer.parseInt(scanner.nextLine());

                        Student s = new Student(id, name, dept, marks);
                        dao.addStudent(s);
                        System.out.println("Student added successfully.");
                    }
                    case 2 -> {
                        List<Student> students = dao.getAllStudents();
                        System.out.println("ID\tName\tDepartment\tMarks");
                        for (Student s : students) {
                            System.out.printf("%d\t%s\t%s\t%d\n",
                                    s.getStudentID(), s.getName(), s.getDepartment(), s.getMarks());
                        }
                    }
                    case 3 -> {
                        System.out.print("Enter StudentID to update: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter new Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter new Department: ");
                        String dept = scanner.nextLine();
                        System.out.print("Enter new Marks: ");
                        int marks = Integer.parseInt(scanner.nextLine());

                        Student s = new Student(id, name, dept, marks);
                        boolean updated = dao.updateStudent(s);
                        if (updated) {
                            System.out.println("Student updated successfully.");
                        } else {
                            System.out.println("StudentID not found.");
                        }
                    }
                    case 4 -> {
                        System.out.print("Enter StudentID to delete: ");
                        int id = Integer.parseInt(scanner.nextLine());

                        boolean deleted = dao.deleteStudent(id);
                        if (deleted) {
                            System.out.println("Student deleted successfully.");
                        } else {
                            System.out.println("StudentID not found.");
                        }
                    }
                    case 5 -> System.out.println("Exiting...");
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error occurred:");
                e.printStackTrace();
            } catch (NumberFormatException e) {
                System.out.println("Invalid number input.");
            }

        } while (choice != 5);

        try {
            dao.close();
        } catch (SQLException e) {
            System.out.println("Error closing database connection.");
            e.printStackTrace();
        }
        scanner.close();
    }
}
