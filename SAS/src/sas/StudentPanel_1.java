
package sas;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.io.ByteArrayOutputStream;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.util.Base64;
import java.util.Date;

/**
 *
 * @author Rayogitra
 */

public class StudentPanel_1 extends javax.swing.JFrame {
 private Connection connection;
    private PreparedStatement pstmt;
    private final String DB_URL = "jdbc:mysql://localhost:3306/finals_db";
    private final String DB_USER = "root";
    private final String DB_PASS = "";
    private int studentId;
    private String studentProgram; 
    
    public StudentPanel_1(int studentId, String tableName) {  // Add String tableName parameter
     this.studentId = studentId;
    this.studentProgram = tableName;
    connectToDatabase();
    initComponents();
    
    // Initialize and load data
    initializeTables();
    loadStudentData();
    loadGradeData();
    loadAttendanceData();
    
    // Set window closing behavior
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    });
}


private void initializeTables() {
    // Initialize Grade Table (Tgrade)
    DefaultTableModel gradeModel = new DefaultTableModel(
        new Object[][]{},
        new String[]{
            "STUDENT ID", "SUBJECT ID", "SUBJECT NAME", "MIDTERM", "FINALTERM", "TOTAL", "UNITS"
        }
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Make all cells non-editable
        }
    };
    Tgrade.setModel(gradeModel);
    
    // Initialize Attendance Table (TAttendance)
    DefaultTableModel attendanceModel = new DefaultTableModel(
        new Object[][]{},
        new String[]{
            "STUDENT ID", "PATHFIT", "FINALS PATHFIT", "COMPPROG", "FINALS COMPPROG", 
            "FREEHAND", "FINALS FREEHAND", "MATH", "FINALS MATH", "NSTP", 
            "FINALS NSTP", "BAS", "FINALS BAS", "GAMEDEV", "FINALS GAMEDEV", "TOTAL ABSENCES"
        }
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Make all cells non-editable
        }
    };
    TAttendance.setModel(attendanceModel);
    
    // Center align text in tables
    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    Tgrade.setDefaultRenderer(Object.class, centerRenderer);
    TAttendance.setDefaultRenderer(Object.class, centerRenderer);
    
    // Set column widths
    Tgrade.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    TAttendance.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    // Set specific column widths for Tgrade
    Tgrade.getColumnModel().getColumn(0).setPreferredWidth(100); // STUDENT ID
    Tgrade.getColumnModel().getColumn(1).setPreferredWidth(100); // SUBJECT ID
    Tgrade.getColumnModel().getColumn(2).setPreferredWidth(250); // SUBJECT NAME
    Tgrade.getColumnModel().getColumn(3).setPreferredWidth(80);  // MIDTERM
    Tgrade.getColumnModel().getColumn(4).setPreferredWidth(80);  // FINALTERM
    Tgrade.getColumnModel().getColumn(5).setPreferredWidth(80);  // TOTAL
    Tgrade.getColumnModel().getColumn(6).setPreferredWidth(60);  // UNITS
    
    // Set specific column widths for TAttendance
    for (int i = 0; i < TAttendance.getColumnCount(); i++) {
        TAttendance.getColumnModel().getColumn(i).setPreferredWidth(100);
    }
    TAttendance.getColumnModel().getColumn(15).setPreferredWidth(120); // TOTAL ABSENCES
}

private void loadGradeData() {
    try {
        String query = "SELECT * FROM grades_gwa WHERE student_id = ?";
        pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, studentId);
        
        ResultSet rs = pstmt.executeQuery();
        DefaultTableModel model = (DefaultTableModel) Tgrade.getModel();
        model.setRowCount(0);
        
        double totalGrade = 0;
        int totalUnits = 0;
        
        while (rs.next()) {
            String subjectId = rs.getString("subject_id");
            String subjectName = rs.getString("subject_name");
            String midterm = rs.getString("midterm");
            String finals = rs.getString("finals");
            String total = rs.getString("total");
            String units = rs.getString("units");
            
            model.addRow(new Object[]{
                studentId, 
                subjectId, 
                subjectName, 
                midterm != null ? midterm : "",
                finals != null ? finals : "",
                total != null ? total : "",
                units != null ? units : ""
                    
            });
            
            if (total != null && !total.isEmpty() && units != null && !units.isEmpty()) {
                try {
                    double gradeValue = Double.parseDouble(total);
                    int unitValue = Integer.parseInt(units);
                    totalGrade += gradeValue * unitValue;
                    totalUnits += unitValue;
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
        }
        
        // Calculate and display GWA
        if (totalUnits > 0) {
            double gwa = totalGrade / totalUnits;
            DecimalFormat df = new DecimalFormat("#.##");
            Lgwa.setText(df.format(gwa));
        } else {
            Lgwa.setText("N/A");
        }
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Error loading grade data: " + e.getMessage(), 
            "Database Error", 
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
private String formatGrade(String grade) {
    if (grade == null || grade.trim().isEmpty()) {
        return "";
    }
    try {
        double value = Double.parseDouble(grade);
        return String.format("%.2f", value);
    } catch (NumberFormatException e) {
        return grade; // return as-is if not a number
    }
}
private void loadAttendanceData() {
    try {
        String query = "SELECT * FROM grades_gwa WHERE student_id = ?";
        pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, studentId);
        
        ResultSet rs = pstmt.executeQuery();
        DefaultTableModel model = (DefaultTableModel) Tgrade.getModel();
        model.setRowCount(0); // Clear existing data
        
        double totalGrade = 0;
        int totalUnits = 0;
        boolean hasGrades = false;
        
        while (rs.next()) {
            hasGrades = true;
            String subjectId = rs.getString("subject_id");
            String subjectName = rs.getString("subject_name");
            String midterm = formatGrade(rs.getString("midterm"));
            String finals = formatGrade(rs.getString("finals"));
            String total = formatGrade(rs.getString("total"));
            String units = rs.getString("units");
            
            model.addRow(new Object[]{
                studentId, 
                subjectId, 
                subjectName, 
                midterm,
                finals,
                total,
                units
            });
            
            // Calculate GWA if grades are available
            if (total != null && !total.isEmpty() && units != null && !units.isEmpty()) {
                try {
                    double gradeValue = Double.parseDouble(total);
                    int unitValue = Integer.parseInt(units);
                    totalGrade += gradeValue * unitValue;
                    totalUnits += unitValue;
                } catch (NumberFormatException e) {
                    // Log or handle parsing errors
                    System.err.println("Error parsing grade/units for subject: " + subjectName);
                }
            }
        }
        
        if (!hasGrades) {
            JOptionPane.showMessageDialog(this, 
                "No grade records found for this student.", 
                "Information", 
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        // Calculate and display GWA
        if (totalUnits > 0) {
            double gwa = totalGrade / totalUnits;
            DecimalFormat df = new DecimalFormat("#.##");
            Lgwa.setText(df.format(gwa));
        } else {
            Lgwa.setText("N/A");
        }
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Error loading grade data: " + e.getMessage(), 
            "Database Error", 
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
  private void connectToDatabase() {
         try {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        // Test the connection
        connection.createStatement().executeQuery("SELECT 1");
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Database connection failed: " + e.getMessage(), 
            "Connection Error", 
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
        // Close the window if connection fails
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
}


 private void loadStudentData() {
 try{
        String tableName = studentProgram.toUpperCase();
        // Use the correct ID column name here too
       
         
        // Use the stored table name directly
        String query = "SELECT * FROM " + studentProgram + " WHERE id = ?";
        pstmt = connection.prepareStatement(query);
        pstmt.setInt(1, studentId);
        
        ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Set all the student information from the result set
                jLabel17.setText(rs.getString("last_name"));
                jLabel5.setText(rs.getString("last_name"));
                jLabel16.setText(rs.getString("id"));
                jLabel9.setText(rs.getString("student_id"));
                lfname.setText(rs.getString("first_name"));
                lmname.setText(rs.getString("middle_name"));
                llname.setText(rs.getString("last_name"));
                dontmind.setText(rs.getString("grade"));
                lEmail.setText(rs.getString("email"));
                if(studentProgram == "students"){
                lCourse.setText("BSEMC");
                        }
                 if(studentProgram == "bsit_students"){
                lCourse.setText("BSIT");
                        } 
                if(studentProgram == "bscs_students"){
                lCourse.setText("BSCS");
                        }
                if(studentProgram == "bsis_students"){
                lCourse.setText("BSIS");
                        }
                lCampus.setText(rs.getString("campus"));
                lYearLevel.setText(rs.getString("school_year"));
                lPlaceofBirth.setText(rs.getString("birth_place"));
                lSection.setText(rs.getString("section"));
                lAddress.setText(rs.getString("home_address"));
                lSex.setText(rs.getString("sex"));
                lCivil.setText(rs.getString("civil_status"));
                lDateofBirth.setText(rs.getString("birth_date"));
                lZip.setText(rs.getString("zip_code"));
                lNationality.setText(rs.getString("nationality"));
                lReligion.setText(rs.getString("religion"));
                lHeight.setText(rs.getString("height"));
                lWeight.setText(rs.getString("weight"));
                lCity.setText(rs.getString("city"));
                lScheme.setText(rs.getString("scheme"));
                lBarangay.setText(rs.getString("barangay"));
                lDistrict.setText(rs.getString("district"));
                lProvince.setText(rs.getString("province"));
                lContactNo.setText(rs.getString("contact_no"));
                lMobileNo.setText(rs.getString("mobile_no"));
                 lfathername.setText(rs.getString("father_name"));
                lfatheraddress.setText(rs.getString("father_address"));
                loccupationfather.setText(rs.getString("father_occupation"));
                lnumberfather.setText(rs.getString("father_contact"));

                lmothername.setText(rs.getString("mother_name"));
                lmotheraddress.setText(rs.getString("mother_address"));
                loccupationmother.setText(rs.getString("mother_occupation"));
                lmothernumber.setText(rs.getString("mother_contact"));

                lguardianname.setText(rs.getString("guardian_name"));
                lguardianrelationship.setText(rs.getString("guardian_relationship"));
                lguardiannumber.setText(rs.getString("guardian_contact"));
                byte[] photoBytes = rs.getBytes("photo");
                if (photoBytes != null && photoBytes.length > 0) {
                    ImageIcon photo = new ImageIcon(photoBytes);
                    Image scaledPhoto = photo.getImage().getScaledInstance(
                        photoPreview.getWidth(), photoPreview.getHeight(), Image.SCALE_SMOOTH);
                    photoPreview.setIcon(new ImageIcon(scaledPhoto));
                }
            }
        } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage(), 
            "Database Error", JOptionPane.ERROR_MESSAGE); 
        }
   }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jLayeredPane4 = new javax.swing.JLayeredPane();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Tgrade = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        Lgwa = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        TAttendance = new javax.swing.JTable();
        jLayeredPane2 = new javax.swing.JLayeredPane();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        photoPreview = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        lCampus = new javax.swing.JLabel();
        lCourse = new javax.swing.JLabel();
        lYearLevel = new javax.swing.JLabel();
        lSection = new javax.swing.JLabel();
        lfname = new javax.swing.JLabel();
        lmname = new javax.swing.JLabel();
        llname = new javax.swing.JLabel();
        lPlaceofBirth = new javax.swing.JLabel();
        lAddress = new javax.swing.JLabel();
        lCity = new javax.swing.JLabel();
        lProvince = new javax.swing.JLabel();
        lScheme = new javax.swing.JLabel();
        lDistrict = new javax.swing.JLabel();
        lBarangay = new javax.swing.JLabel();
        lEmail = new javax.swing.JLabel();
        lSex = new javax.swing.JLabel();
        lCivil = new javax.swing.JLabel();
        lDateofBirth = new javax.swing.JLabel();
        lZip = new javax.swing.JLabel();
        lNationality = new javax.swing.JLabel();
        lReligion = new javax.swing.JLabel();
        lContactNo = new javax.swing.JLabel();
        lMobileNo = new javax.swing.JLabel();
        lHeight = new javax.swing.JLabel();
        lWeight = new javax.swing.JLabel();
        dontmind = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        header3 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        loccupationfather = new javax.swing.JLabel();
        lguardianrelationship = new javax.swing.JLabel();
        loccupationmother = new javax.swing.JLabel();
        lfathername = new javax.swing.JLabel();
        lfatheraddress = new javax.swing.JLabel();
        lmothername = new javax.swing.JLabel();
        lmotheraddress = new javax.swing.JLabel();
        lguardianname = new javax.swing.JLabel();
        lnumberfather = new javax.swing.JLabel();
        lmothernumber = new javax.swing.JLabel();
        lguardiannumber = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 145, 77));
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        setPreferredSize(new java.awt.Dimension(1920, 1080));
        setResizable(false);
        setSize(new java.awt.Dimension(1920, 1080));

        jPanel3.setBackground(new java.awt.Color(255, 145, 77));
        jPanel3.setAlignmentX(0.0F);
        jPanel3.setAlignmentY(0.0F);

        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.setBackground(new java.awt.Color(214, 237, 198));
        jTabbedPane1.setForeground(new java.awt.Color(214, 237, 198));
        jTabbedPane1.setOpaque(true);
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(1000, 750));

        jLabel5.setText("Name");

        jLabel6.setText("Welcome,");

        jLabel9.setText("ID");

        jLabel10.setText("Student ID:  ");

        jTabbedPane2.setBackground(new java.awt.Color(234, 245, 226));

        jPanel7.setBackground(new java.awt.Color(234, 245, 226));

        Tgrade.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "STUDENT ID", "SUBJECT ID", "SUBJECT NAME", "MIDTERM", "FINALTERM", "TOTAL", "UNITS"
            }
        ));
        Tgrade.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(Tgrade);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("GWA:");

        Lgwa.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        Lgwa.setText("gwa here");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1367, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Lgwa, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Lgwa, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane2.addTab("Grade", jPanel7);

        jPanel6.setBackground(new java.awt.Color(234, 245, 226));

        TAttendance.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "STUDENT ID", "PATHFIT", "FINALS PATHFIT", "COMPPROG", "FINALS COMPPROG", "FREEHAND", "FINALS FREEHAND", "MATH", "FINALS MATH", "NSTP", "FINALS NSTP", "BAS", "FINALS BAS", "GAMEDEV", "FINALS GAMEDEV", "TOTAL ABSENCES"
            }
        ));
        TAttendance.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane2.setViewportView(TAttendance);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Attendance", jPanel6);

        jLayeredPane4.setLayer(jLabel5, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane4.setLayer(jLabel6, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane4.setLayer(jLabel9, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane4.setLayer(jLabel10, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane4.setLayer(jTabbedPane2, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane4Layout = new javax.swing.GroupLayout(jLayeredPane4);
        jLayeredPane4.setLayout(jLayeredPane4Layout);
        jLayeredPane4Layout.setHorizontalGroup(
            jLayeredPane4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLayeredPane4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jLayeredPane4Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jLayeredPane4Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jLayeredPane4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jTabbedPane2)
                .addContainerGap())
        );
        jLayeredPane4Layout.setVerticalGroup(
            jLayeredPane4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jLayeredPane4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLayeredPane4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jLayeredPane4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane2)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Section And Program", jLayeredPane4);

        jLabel15.setText("Student ID:  ");

        jLabel16.setText("ID");

        jLabel8.setText("Welcome,");

        jLabel17.setText("Name");

        jPanel1.setBackground(new java.awt.Color(217, 217, 217));
        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.setForeground(new java.awt.Color(214, 237, 198));

        jScrollPane3.setMinimumSize(new java.awt.Dimension(1330, 715));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(1330, 715));

        jPanel4.setMinimumSize(new java.awt.Dimension(1990, 1094));
        jPanel4.setPreferredSize(new java.awt.Dimension(1330, 1094));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setMinimumSize(new java.awt.Dimension(1330, 715));
        jPanel2.setPreferredSize(new java.awt.Dimension(1330, 715));

        jLabel18.setBackground(new java.awt.Color(0, 0, 0));
        jLabel18.setText("Campus");

        jLabel20.setBackground(new java.awt.Color(0, 0, 0));
        jLabel20.setText("Program/Course");

        jLabel23.setBackground(new java.awt.Color(0, 0, 0));
        jLabel23.setText("Year Level");

        jPanel5.setForeground(new java.awt.Color(153, 153, 153));
        jPanel5.setMinimumSize(new java.awt.Dimension(952, 16));
        jPanel5.setPreferredSize(new java.awt.Dimension(952, 16));

        jLabel24.setBackground(new java.awt.Color(0, 0, 0));
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("Personal Background");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel24)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel24)
        );

        jLabel25.setBackground(new java.awt.Color(0, 0, 0));
        jLabel25.setText("Name");

        jLabel26.setBackground(new java.awt.Color(0, 0, 0));
        jLabel26.setText("Place of Birth");

        jLabel27.setBackground(new java.awt.Color(0, 0, 0));
        jLabel27.setText("Province");

        jLabel28.setBackground(new java.awt.Color(0, 0, 0));
        jLabel28.setText("Home Address");

        jLabel29.setBackground(new java.awt.Color(0, 0, 0));
        jLabel29.setText("Town/City");

        jLabel31.setBackground(new java.awt.Color(0, 0, 0));
        jLabel31.setText("Barangay");

        jLabel32.setBackground(new java.awt.Color(0, 0, 0));
        jLabel32.setText("District");

        jLabel33.setBackground(new java.awt.Color(0, 0, 0));
        jLabel33.setText("E-mail");

        jLabel30.setBackground(new java.awt.Color(0, 0, 0));
        jLabel30.setText("Sex At Birth");

        jLabel34.setBackground(new java.awt.Color(0, 0, 0));
        jLabel34.setText("Scheme");

        jLabel35.setBackground(new java.awt.Color(0, 0, 0));
        jLabel35.setText("Civil Status");

        jLabel36.setBackground(new java.awt.Color(0, 0, 0));
        jLabel36.setText("Date of Birth");

        jLabel37.setBackground(new java.awt.Color(0, 0, 0));
        jLabel37.setText("ZIP Code");

        jLabel38.setBackground(new java.awt.Color(0, 0, 0));
        jLabel38.setText("Nationality");

        jLabel39.setBackground(new java.awt.Color(0, 0, 0));
        jLabel39.setText("Religion");

        jLabel40.setBackground(new java.awt.Color(0, 0, 0));
        jLabel40.setText("Contact no.");

        jLabel41.setBackground(new java.awt.Color(0, 0, 0));
        jLabel41.setText("Mobile no.");

        jLabel42.setBackground(new java.awt.Color(0, 0, 0));
        jLabel42.setText("Height(in cm)");

        jLabel43.setBackground(new java.awt.Color(0, 0, 0));
        jLabel43.setText("Weight(in kg)");

        photoPreview.setBackground(new java.awt.Color(255, 255, 255));
        photoPreview.setForeground(new java.awt.Color(255, 255, 255));
        photoPreview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel19.setText("Section");

        jLabel22.setText("GWA:");

        lCampus.setBackground(new java.awt.Color(0, 0, 0));
        lCampus.setText("Campus");

        lCourse.setBackground(new java.awt.Color(0, 0, 0));
        lCourse.setText("Campus");

        lYearLevel.setBackground(new java.awt.Color(0, 0, 0));
        lYearLevel.setText("Campus");

        lSection.setBackground(new java.awt.Color(0, 0, 0));
        lSection.setText("Campus");

        lfname.setBackground(new java.awt.Color(0, 0, 0));
        lfname.setText("Campus");

        lmname.setBackground(new java.awt.Color(0, 0, 0));
        lmname.setText("Campus");

        llname.setBackground(new java.awt.Color(0, 0, 0));
        llname.setText("Campus");

        lPlaceofBirth.setBackground(new java.awt.Color(0, 0, 0));
        lPlaceofBirth.setText("Campus");

        lAddress.setBackground(new java.awt.Color(0, 0, 0));
        lAddress.setText("Campus");

        lCity.setBackground(new java.awt.Color(0, 0, 0));
        lCity.setText("Campus");

        lProvince.setBackground(new java.awt.Color(0, 0, 0));
        lProvince.setText("Campus");

        lScheme.setBackground(new java.awt.Color(0, 0, 0));
        lScheme.setText("Campus");

        lDistrict.setBackground(new java.awt.Color(0, 0, 0));
        lDistrict.setText("Campus");

        lBarangay.setBackground(new java.awt.Color(0, 0, 0));
        lBarangay.setText("Campus");

        lEmail.setBackground(new java.awt.Color(0, 0, 0));
        lEmail.setText("Campus");

        lSex.setBackground(new java.awt.Color(0, 0, 0));
        lSex.setText("Campus");

        lCivil.setBackground(new java.awt.Color(0, 0, 0));
        lCivil.setText("Campus");

        lDateofBirth.setBackground(new java.awt.Color(0, 0, 0));
        lDateofBirth.setText("Campus");

        lZip.setBackground(new java.awt.Color(0, 0, 0));
        lZip.setText("Campus");

        lNationality.setBackground(new java.awt.Color(0, 0, 0));
        lNationality.setText("Campus");

        lReligion.setBackground(new java.awt.Color(0, 0, 0));
        lReligion.setText("Campus");

        lContactNo.setBackground(new java.awt.Color(0, 0, 0));
        lContactNo.setText("Campus");

        lMobileNo.setBackground(new java.awt.Color(0, 0, 0));
        lMobileNo.setText("Campus");

        lHeight.setBackground(new java.awt.Color(0, 0, 0));
        lHeight.setText("Campus");

        lWeight.setBackground(new java.awt.Color(0, 0, 0));
        lWeight.setText("Campus");

        dontmind.setBackground(new java.awt.Color(0, 0, 0));
        dontmind.setText("Campus");

        jPanel10.setBackground(new java.awt.Color(0, 0, 0));
        jPanel10.setForeground(new java.awt.Color(0, 0, 0));
        jPanel10.setMinimumSize(new java.awt.Dimension(952, 16));

        header3.setBackground(new java.awt.Color(0, 0, 0));
        header3.setForeground(new java.awt.Color(255, 255, 255));
        header3.setText("Family  Background");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(header3, javax.swing.GroupLayout.PREFERRED_SIZE, 1276, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(header3)
        );

        jLabel3.setText("Father's Name");

        jLabel4.setText("Father's Address");

        jLabel7.setText("Mother's Name");

        jLabel11.setText("Mother's Address");

        jLabel12.setText("Guardian's Name");

        jLabel13.setText("Occupation");

        jLabel14.setText("Occupation");

        jLabel44.setText("Relationship");

        jLabel45.setText("Tel.No.");

        jLabel46.setText("Tel.No.");

        jLabel47.setText("Tel.No");

        loccupationfather.setText("Default");

        lguardianrelationship.setText("Default");

        loccupationmother.setText("Default");

        lfathername.setText("Default");

        lfatheraddress.setText("Default");

        lmothername.setText("Default");

        lmotheraddress.setText("Default");

        lguardianname.setText("Default");

        lnumberfather.setText("Default");

        lmothernumber.setText("Default");

        lguardiannumber.setText("Default");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1303, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(64, 64, 64)
                                .addComponent(lfathername, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel12))
                                .addGap(48, 48, 48)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lmothername, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lfatheraddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lmotheraddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lguardianname, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(9, 9, 9)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel14)
                                    .addComponent(jLabel44))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lguardianrelationship, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(loccupationmother, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(loccupationfather, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(55, 55, 55)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel47)
                                .addGap(18, 18, 18)
                                .addComponent(lmothernumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel45)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lnumberfather, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel46)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lguardiannumber, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel25)
                                                .addGap(91, 91, 91))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel26)
                                                .addGap(53, 53, 53)))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel31)
                                                .addComponent(jLabel28)
                                                .addComponent(jLabel27)
                                                .addComponent(jLabel29))
                                            .addGap(45, 45, 45)))
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(lPlaceofBirth, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lAddress, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(lfname, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(lmname, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(llname, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(lCity, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lProvince, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lBarangay, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel34)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lScheme, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(164, 164, 164)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel32)
                                    .addComponent(jLabel33))
                                .addGap(84, 84, 84)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lDistrict, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel43)
                                    .addGap(56, 56, 56)
                                    .addComponent(lWeight, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel42)
                                    .addGap(56, 56, 56)
                                    .addComponent(lHeight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel30)
                                    .addGap(70, 70, 70)
                                    .addComponent(lSex, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel35)
                                    .addGap(70, 70, 70)
                                    .addComponent(lCivil, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel36)
                                    .addGap(64, 64, 64)
                                    .addComponent(lDateofBirth, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel40)
                                    .addComponent(jLabel37)
                                    .addComponent(jLabel38))
                                .addGap(68, 68, 68)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lZip, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lNationality, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lContactNo, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel41)
                                .addGap(73, 73, 73)
                                .addComponent(lMobileNo, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel39)
                                .addGap(86, 86, 86)
                                .addComponent(lReligion, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(21, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18)
                    .addComponent(jLabel20)
                    .addComponent(jLabel23)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(dontmind, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lSection, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lYearLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lCampus, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(photoPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(65, 65, 65))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18)
                            .addComponent(lCampus))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(lCourse))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(lYearLevel))
                        .addGap(47, 47, 47)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lSection)
                            .addComponent(jLabel19)))
                    .addComponent(photoPreview, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(48, 48, 48)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(dontmind))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lfname)
                            .addComponent(lmname)
                            .addComponent(llname)
                            .addComponent(jLabel25)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel30)
                            .addComponent(lSex))))
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel26)
                        .addComponent(lPlaceofBirth))
                    .addComponent(lCivil)
                    .addComponent(jLabel35))
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel28)
                            .addComponent(lAddress))
                        .addGap(41, 41, 41)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel27)
                            .addComponent(lProvince)
                            .addComponent(jLabel37)
                            .addComponent(lZip)))
                    .addComponent(lDateofBirth)
                    .addComponent(jLabel36))
                .addGap(43, 43, 43)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lCity)
                    .addComponent(jLabel29)
                    .addComponent(jLabel38)
                    .addComponent(lNationality))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel31)
                            .addComponent(lBarangay))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel39)
                            .addComponent(lReligion))
                        .addGap(42, 42, 42)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel32)
                            .addComponent(lDistrict)
                            .addComponent(jLabel40)
                            .addComponent(lContactNo))
                        .addGap(36, 36, 36)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel41)
                    .addComponent(lMobileNo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel33)
                    .addComponent(lEmail))
                .addGap(7, 7, 7)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel42)
                    .addComponent(lHeight))
                .addGap(33, 33, 33)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel43)
                    .addComponent(lWeight)
                    .addComponent(jLabel34)
                    .addComponent(lScheme))
                .addGap(10, 10, 10)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(lfathername))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(lfatheraddress))
                        .addGap(31, 31, 31)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(lmothername))
                        .addGap(33, 33, 33)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(lmotheraddress))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(lguardianname)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel45)
                                .addComponent(lnumberfather))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel13)
                                .addComponent(loccupationfather)))
                        .addGap(80, 80, 80)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel47)
                                .addComponent(lmothernumber))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel14)
                                .addComponent(loccupationmother)))
                        .addGap(67, 67, 67)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel44)
                                .addComponent(lguardianrelationship))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel46)
                                .addComponent(lguardiannumber))))))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 1082, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jScrollPane3.setViewportView(jPanel4);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel21))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel21)
                .addGap(39, 39, 39)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLayeredPane2.setLayer(jLabel15, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jLabel16, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jLabel8, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jLabel17, javax.swing.JLayeredPane.DEFAULT_LAYER);
        jLayeredPane2.setLayer(jPanel1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jLayeredPane2Layout = new javax.swing.GroupLayout(jLayeredPane2);
        jLayeredPane2.setLayout(jLayeredPane2Layout);
        jLayeredPane2Layout.setHorizontalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jLayeredPane2Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jLayeredPane2Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(801, 801, 801))
                    .addGroup(jLayeredPane2Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jLayeredPane2Layout.setVerticalGroup(
            jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLayeredPane2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jLayeredPane2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Profile Info", null, jLayeredPane2, "Profile Info");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    this.dispose();
    new testing_1().setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     * 
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(StudentPanel_1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StudentPanel_1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StudentPanel_1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StudentPanel_1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
      java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
            // Provide test values
            new StudentPanel_1(1, "bsit_students").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Lgwa;
    private javax.swing.JTable TAttendance;
    private javax.swing.JTable Tgrade;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel dontmind;
    private javax.swing.JLabel header3;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JLayeredPane jLayeredPane4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JLabel lAddress;
    private javax.swing.JLabel lBarangay;
    private javax.swing.JLabel lCampus;
    private javax.swing.JLabel lCity;
    private javax.swing.JLabel lCivil;
    private javax.swing.JLabel lContactNo;
    private javax.swing.JLabel lCourse;
    private javax.swing.JLabel lDateofBirth;
    private javax.swing.JLabel lDistrict;
    private javax.swing.JLabel lEmail;
    private javax.swing.JLabel lHeight;
    private javax.swing.JLabel lMobileNo;
    private javax.swing.JLabel lNationality;
    private javax.swing.JLabel lPlaceofBirth;
    private javax.swing.JLabel lProvince;
    private javax.swing.JLabel lReligion;
    private javax.swing.JLabel lScheme;
    private javax.swing.JLabel lSection;
    private javax.swing.JLabel lSex;
    private javax.swing.JLabel lWeight;
    private javax.swing.JLabel lYearLevel;
    private javax.swing.JLabel lZip;
    private javax.swing.JLabel lfatheraddress;
    private javax.swing.JLabel lfathername;
    private javax.swing.JLabel lfname;
    private javax.swing.JLabel lguardianname;
    private javax.swing.JLabel lguardiannumber;
    private javax.swing.JLabel lguardianrelationship;
    private javax.swing.JLabel llname;
    private javax.swing.JLabel lmname;
    private javax.swing.JLabel lmotheraddress;
    private javax.swing.JLabel lmothername;
    private javax.swing.JLabel lmothernumber;
    private javax.swing.JLabel lnumberfather;
    private javax.swing.JLabel loccupationfather;
    private javax.swing.JLabel loccupationmother;
    private javax.swing.JLabel photoPreview;
    // End of variables declaration//GEN-END:variables
}
