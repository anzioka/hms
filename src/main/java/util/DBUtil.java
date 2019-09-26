package main.java.util;

import javafx.collections.ObservableList;
import main.Main;
import main.java.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alfonce on 20/04/2017.
 */

public class DBUtil {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final String CLASS_TAG = DBUtil.class.getSimpleName();
    private static Connection connection = null;

    //TODO : fix this
    public static boolean getConnection(String userName, String password) {

        //initialize logger
        try {
            LoggerUtil.setup();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String connectUrl = "jdbc:mysql://localhost:3306/";
        Properties properties = new Properties();
        properties.put("user", userName);
        properties.put("password", password);

        //get connection
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(connectUrl, properties);
            if (connection != null) {
                String sql = "USE hmsDb";
                executeStatement(sql);
                return true;
            } else {
                System.out.print("No connection\n");
                return false;

            }
        } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LOGGER.logp(Level.SEVERE, "LoginController", "", "error", e);
        }
        return false;
    }

    public static ResultSet executeQuery(String sql) {
        ResultSet resultSet;
        Statement statement;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            return resultSet;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean executeStatement(String sql) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.logp(Level.SEVERE, CLASS_TAG, "", "Db error", e);
                }
            }
        }
        return true;
    }

    public static boolean addQueue(PatientQueue queue) {
        PreparedStatement statement = null;
        String sql = "INSERT INTO hmsDb.Queues VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, 0);
            statement.setString(2, queue.getPatientId());
            statement.setString(3, queue.getStatus().name());
            statement.setInt(4, queue.getDoctorId());
            statement.setString(5, queue.getPaymentMode().name());
            statement.setInt(6, queue.getBillNumber());
            statement.setString(7, queue.getServiceType().name());
            statement.setObject(8, queue.getDateCreated());
            statement.setObject(9, LocalTime.now());

            statement.execute();

        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error adding patient to queue", e);
            return false;
        } finally {
            closeStatement(statement);
        }
        return true;
    }

    public static boolean addPatientVitals(PatientVitals vitals) {
        PreparedStatement statement = null;
        String sql = "INSERT INTO hmsDb.vitals VALUES (?, ?, ?, ?,?, ?, ?,?, ?, ?,?, ?, ?,?,?,?)";
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, vitals.getAdmissionNum());
            statement.setInt(2, vitals.getQueueId());
            statement.setDouble(3, vitals.getBmi());
            statement.setDouble(4, vitals.getWeight());
            statement.setDouble(5, vitals.getHeight());
            statement.setDouble(6, vitals.getSystolicBp());
            statement.setDouble(7, vitals.getDiastolicBp());
            statement.setDouble(8, vitals.getBodyTemp());
            statement.setDouble(9, vitals.getRespiratoryRate());
            statement.setDouble(10, vitals.getPulseRate());
            statement.setDouble(11, vitals.getSpo2());
            statement.setString(12, vitals.getBloodGroup());
            statement.setString(13, vitals.getRhesusFactor());
            statement.setString(14, vitals.getColorCode());
            statement.setObject(15, LocalDate.now());
            statement.setObject(16, LocalTime.now());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeStatement(statement);

        }
        return true;
    }

    public static boolean addClinicVisitNotes(ClinicVisitNotes notes) {
        PreparedStatement preparedStatement = null;
        String modifyRecord = "INSERT INTO hmsDb.VisitNotes VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "Complains=?, History=?, PhysicalExam=?," +
                " Investigation=?, Treatment=?";

        try {

            preparedStatement = connection.prepareStatement(modifyRecord);
            preparedStatement.setInt(1, notes.getVisitId());
            preparedStatement.setString(2, notes.getPrimaryComplains());
            preparedStatement.setString(3, notes.getMedicalHistory());
            preparedStatement.setString(4, notes.getPhysicalExam());
            preparedStatement.setString(5, notes.getInvestigation());
            preparedStatement.setString(6, notes.getTreatment());
            preparedStatement.setString(7, notes.getPrimaryComplains());
            preparedStatement.setString(8, notes.getMedicalHistory());
            preparedStatement.setString(9, notes.getPhysicalExam());
            preparedStatement.setString(10, notes.getInvestigation());
            preparedStatement.setString(11, notes.getTreatment());

            preparedStatement.execute();

        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error saving clinic notes", e);
            return false;
        } finally {
            closeStatement(preparedStatement);
        }
        return true;
    }

    public static int getNextUserId() {
        String sql = "select * from Users order by ID desc limit 1";
        int id = 1;
        try {
            ResultSet resultSet = executeQuery(sql);
            if (resultSet != null) {
                if (resultSet.next()) {
                    id = resultSet.getInt("ID") + 1;
                }
            }
        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error getting next user Id", e);
        }
        return id;
    }

    private static boolean createUser(User user) {
        Statement statement = null;
        String sql = "create user  '" + user.getLoginName() + "'@'localhost' IDENTIFIED  by '" + user.getPassword() +
                "'";

        try {
            statement = connection.createStatement();
            statement.addBatch(sql);

            sql = "GRANT SELECT, INSERT, UPDATE , DELETE , CREATE ON hmsdb.* TO '" + user.getLoginName()
                    + "'@'localhost'";
            statement.addBatch(sql);

            sql = "FLUSH PRIVILEGES";
            statement.addBatch(sql);

            return statement.executeBatch().length == 3;

        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error creating user", e);
            return false;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean addPatient(Patient patient) {
        String sql = "INSERT INTO patients VALUES (?, ?, ?, ? ,?, ?, ?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY " +
                "UPDATE " +
                "FirstName=?, LastName=?, DateOfBirth=?, PhoneNumber=?, Sex=?, NHIFNumber=?, MaritalStatus=?," +
                " Residence=?, ContactFirstName=?, ContactLastName=?, ContactPhoneNumber=?, ContactRelationShip=?, " +
                "InsuranceProvider=?, InsuranceId=?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, patient.getPatientId());
            statement.setString(2, patient.getFirstName());
            statement.setString(3, patient.getLastName());
            statement.setObject(4, patient.getDateOfBirth());
            statement.setString(5, patient.getTelephoneNumber());
            statement.setString(6, patient.getSexuality());
            statement.setString(7, patient.getNHIFNumber());
            statement.setString(8, patient.getMaritalStatus().name());
            statement.setString(9, patient.getResidence());
            statement.setString(10, patient.getContactFirstName());
            statement.setString(11, patient.getContactLastName());
            statement.setString(12, patient.getContactTelephone());
            if (patient.getContactRelationship() != null) {
                statement.setString(13, patient.getContactRelationship().name());
                statement.setString(28, patient.getContactRelationship().name());

            } else {
                statement.setString(13, null);
                statement.setString(28, null);
            }
            statement.setString(14, patient.getInsurer());
            statement.setString(15, patient.getInsuranceID());
            statement.setObject(16, patient.getDateCreated());
            statement.setString(17, patient.getFirstName());
            statement.setString(18, patient.getLastName());
            statement.setObject(19, patient.getDateOfBirth());
            statement.setString(20, patient.getTelephoneNumber());
            statement.setString(21, patient.getSexuality());
            statement.setString(22, patient.getNHIFNumber());
            statement.setString(23, patient.getMaritalStatus().name());
            statement.setString(24, patient.getResidence());
            statement.setString(25, patient.getContactFirstName());
            statement.setString(26, patient.getContactLastName());
            statement.setString(27, patient.getContactTelephone());

            statement.setString(29, patient.getInsurer());
            statement.setString(30, patient.getInsuranceID());
//            statement.setString(31, patient.getDateCreated());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error adding patient", e);
            return false;

        } finally {
            closeStatement(statement);
        }
    }

    public static int getNextQueueId() {
        String sql = "select * from Queues order by VisitId desc limit 1";
        try {
            ResultSet resultSet = executeQuery(sql);
            if (resultSet != null) {
                if (resultSet.next()) {
                    return resultSet.getInt("VisitId") + 1;
                } else {
                    return 1;
                }
            }
            return 1;
        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error getting next queue id", e);
            return 1;
        }
    }

    public static boolean addMedicine(Medicine medicine) {
        PreparedStatement statement = null;
        String sql = "INSERT hmsDb.Drugs VALUES(?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Name = ?, " +
                "SellingPrice = ?, BuyingPrice =?, ReorderLevel =? ";
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, medicine.getDrugCode());
            statement.setString(2, medicine.getName());
            statement.setInt(3, medicine.getStoreQuantity());
            statement.setInt(4, medicine.getShopQuantity());
            statement.setDouble(5, medicine.getSellingPrice());
            statement.setDouble(6, medicine.getBuyingPrice());
            statement.setInt(7, medicine.getReorderLevel());
            statement.setString(8, medicine.getName());
            statement.setDouble(9, medicine.getSellingPrice());
            statement.setDouble(10, medicine.getBuyingPrice());
            statement.setInt(11, medicine.getReorderLevel());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeStatement(statement);
        }
    }

    public static boolean addProcedures(ObservableList<HospitalProcedure> hospitalProcedures) {
        PreparedStatement statement = null;
        String sql = "INSERT INTO hmsDb.Procedures VALUES(?, ?) ON DUPLICATE KEY UPDATE COST=?";

        try {
            statement = connection.prepareStatement(sql);
            for (HospitalProcedure procedure : hospitalProcedures) {
                statement.setString(1, procedure.getName());
                statement.setDouble(2, procedure.getCost());
                statement.setDouble(3, procedure.getCost());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error saving treatment procedures", e);
            return false;
        } finally {
            closeStatement(statement);
        }
        return true;

    }

    public static int getNextInvoiceNumber() {
        String sql = "select * from Invoices order by InvoiceNumber desc limit 1";
        try {
            ResultSet resultSet = executeQuery(sql);
            if (resultSet != null) {
                if (resultSet.next()) {
                    return resultSet.getInt("InvoiceNumber") + 1;
                }
                return 1;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error getting invoice number", e);
            return 1;
        }
    }

    public static int getNextReceiptNumber() {
        String sql = "select * from CashSales order by ReceiptNumber desc limit 1";
        try {
            ResultSet resultSet = executeQuery(sql);
            if (resultSet != null) {
                if (resultSet.next()) {
                    return resultSet.getInt("ReceiptNumber") + 1;
                }
                return 1;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error getting receipt number", e);
            return 1;
        }
    }

    public static boolean markCashPaymentsAsPaid(ObservableList<CashSale> checkedItems) {
        String sql = "UPDATE hmsDb.cashsales SET Status = 'Paid' WHERE ID = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (CashSale cashSale : checkedItems) {
                statement.setInt(1, cashSale.getId());
                statement.addBatch();
            }
            statement.executeBatch();

        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error marking payments as paid", e);
            return false;
        } finally {
            closeStatement(statement);
        }
        return true;
    }

    public static boolean addHospital(Hospital hospital) {
        String sql = "INSERT INTO hmsDb.hospital(ID, Name, Address, City, PhoneNumber) VALUES (?, ?, ?, ?, ?) on duplicate key update Name =?, " +
                "Address =?, PhoneNumber =? , City =? ";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, 0);
            statement.setString(2, hospital.getName());
            statement.setString(3, hospital.getAddress());
            statement.setString(4, hospital.getCity());
            statement.setString(5, hospital.getPhoneNumber());
            statement.setString(6, hospital.getName());
            statement.setString(7, hospital.getAddress());
            statement.setString(8, hospital.getPhoneNumber());
            statement.setString(9, hospital.getCity());

            statement.execute();
            return true;

        } catch (SQLException ignored) {

        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean addRefillRequest(RefillRequest refillRequest) {
        String sql = "INSERT INTO hmsDb.Refills(ID, RequesterId, Name, DateCreated, AmountRequested, AmountReceived, " +
                "Status) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, refillRequest.getRefillId());
            statement.setInt(2, refillRequest.getRequesterId());
            statement.setString(3, refillRequest.getMedicineName());
            statement.setObject(4, refillRequest.getDateRequested());
            statement.setInt(5, refillRequest.getAmountRequested());
            statement.setInt(6, refillRequest.getAmountReceived());
            statement.setString(7, refillRequest.getStatus());

            statement.execute();

        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error saving drug transfer request", e);
            return false;
        } finally {
            closeStatement(statement);
        }
        return true;
    }

    private static void closeStatement(PreparedStatement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean addSupplier(Supplier supplier) {
        String sql = "insert into suppliers values (?,?,?,?,?,?) on duplicate key update name=?, address=?, " +
                "phonenumber=?, email=?, contact=?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, supplier.getSupplierId());
            statement.setString(2, supplier.getName());
            statement.setString(3, supplier.getAddress());
            statement.setString(4, supplier.getPhoneNumber());
            statement.setString(5, supplier.getEmail());
            statement.setString(6, supplier.getContactPerson());
            statement.setString(7, supplier.getName());
            statement.setString(8, supplier.getAddress());
            statement.setString(9, supplier.getPhoneNumber());
            statement.setString(10, supplier.getEmail());
            statement.setString(11, supplier.getContactPerson());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean addInsurances(ObservableList<Insurance> list) {
        String sql = "Insert into hmsDb.Insurance values(?,?) " +
                "ON DUPLICATE KEY Update InsuranceGroup = ? ";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (Insurance insurance : list) {
                statement.setString(1, insurance.getName());
                statement.setString(2, insurance.getGroup());
                statement.setString(3, insurance.getGroup());
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error adding insurance companies", e);
            return false;
        } finally {
            closeStatement(statement);
        }
        return true;
    }

    public static boolean addInsuranceGroup(InsuranceGroup group) {
        String sql = "Insert into hmsdb.InsuranceGroups values (?, ?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, group.getName());
            statement.setDouble(2, group.getConsultationFee());
            statement.execute();
        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error creating insurance group", e);
            return false;
        } finally {
            closeStatement(statement);
        }
        return true;
    }

    public static boolean addItemsToInsuranceGroup(String group, List<String> insuranceNames) {
        String sql = "Update hmsDb.Insurance set InsuranceGroup = ? " +
                "WHERE Name = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (String name : insuranceNames) {
                statement.setString(1, group);
                statement.setString(2, name);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error updating insurance group", e);
            return false;
        } finally {
            closeStatement(statement);
        }
        return true;
    }

    public static boolean unassignGroup(ObservableList<Insurance> items) {
        String sql = "Update hmsDb.Insurance set InsuranceGroup = NULL " +
                "WHERE Name = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (Insurance insurance : items) {
                statement.setString(1, insurance.getName());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {

            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error removing insurance company from group", e);
            return false;
        } finally {
            closeStatement(statement);
        }
        return true;
    }

    public static boolean updateProcedure(HospitalProcedure procedure, String initialName) {
        PreparedStatement statement = null;
        String sql = "update hmsDb.Procedures set Name =?, Cost = ? " +
                "where Name = ?";
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, procedure.getName());
            statement.setDouble(2, procedure.getCost());
            statement.setString(3, initialName);

            statement.executeUpdate();
        } catch (SQLException e) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error updating procedure", e);
            return false;
        } finally {
            closeStatement(statement);
        }
        return true;
    }

    public static boolean updateInsurance(Insurance insurance, String initialName) {
        String sql = "update hmsDb.Insurance set Name = ?, InsuranceGroup = ? where " +
                "Name = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, insurance.getName());
            statement.setString(2, insurance.getGroup());
            statement.setString(3, initialName);
            statement.execute();
        } catch (SQLException e) {

            String caller = new Exception().getStackTrace()[1].getClassName();
            LOGGER.logp(Level.SEVERE, caller, "", "Error updating insurance info", e);

            return false;
        } finally {
            closeStatement(statement);
        }
        return true;
    }

    public static void saveUserDetails(User user) {

        String sql = "insert into users values(? ,?,?,?,?,?) ON DUPLICATE KEY UPDATE UserName=?, FirstName=?, " +
                "LastName=?, UserCategory=?, DateCreated=?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, user.getUserId());
            statement.setString(2, user.getLoginName());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
            if (user.getCategory() != null) {
                statement.setString(5, user.getCategory().name());
                statement.setString(10, user.getCategory().name());
            } else {
                statement.setString(5, null);
                statement.setString(10, null);
            }
            statement.setObject(6, user.getDateCreated());
            statement.setString(7, user.getLoginName());
            statement.setString(8, user.getFirstName());
            statement.setString(9, user.getLastName());
            statement.setObject(11, user.getDateCreated());

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
    }

    public static void enableCreateUserPermission(String userName) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.addBatch("grant create user on *.* to '" + userName + "'@'localhost'");
            statement.addBatch("grant update, delete  on mysql.* to '" + userName + "'@'localhost'");
            statement.addBatch("grant reload on *.* to '" + userName + "'@'localhost'");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean createUser(String userName, String password) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.addBatch("create user '" + userName + "'@'localhost' identified  by '" + password + "'");
            statement.addBatch("grant select, insert , update ,delete on hmsdb.* to '" + userName + "'@'localhost'");
            statement.addBatch("flush privileges");
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveActivity(String description) {
        PreparedStatement statement = null;
        String sql = "insert into activity_log values(? ,?, ?, ?)";
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, Main.currentUser.getUserId());
            statement.setObject(2, description);
            statement.setObject(3, LocalDate.now());
            statement.setObject(4, LocalTime.now());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }

    }

    public static void saveUserPermissions(ObservableList<UserPermission> items, String userId) {
        PreparedStatement statement = null;
        try {
            String sql = "insert into user_permissions values (?, ?, ?) on duplicate  key update permission =?, " +
                    "value =?";
            statement = connection.prepareStatement(sql);

            for (UserPermission permission : items) {
                statement.setInt(1, Integer.parseInt(userId));
                statement.setString(2, permission.getPermission().name());
                statement.setBoolean(3, permission.isAllowed());
                statement.setString(4, permission.getPermission().name());
                statement.setBoolean(5, permission.isAllowed());

                statement.addBatch();
            }

            statement.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
    }

    public static void saveModuleAccess(ObservableList<UserModule> items, String userId) {
        PreparedStatement statement = null;
        try {
            String sql = "insert into user_modules values (?, ?, ?) on duplicate key update MODULE=?, Allowed =?";
            statement = connection.prepareStatement(sql);

            for (UserModule module : items) {
                statement.setInt(1, Integer.parseInt(userId));
                statement.setString(2, module.getModule().name());
                statement.setBoolean(3, module.isAllowed());
                statement.setString(4, module.getModule().name());
                statement.setBoolean(5, module.isAllowed());

                statement.addBatch();
            }
            statement.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
    }

    public static int getNextAutoIncrementId(String tableName) {
        String sql = "select auto_increment from information_schema.tables where table_name ='" + tableName + "' and " +
                "table_schema = DATABASE( )";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("auto_increment");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean saveLabTest(LabTest test) {
        String sql = "insert into labtests values(?,?,?) on duplicate key update TestName = ?, Cost = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, test.getTestId());
            statement.setString(2, test.getName());
            statement.setDouble(3, test.getCost());
            statement.setString(4, test.getName());
            statement.setDouble(5, test.getCost());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveLabTestFlags(ObservableList<LabTestFlag> items) {
        String sql = "insert into lab_test_flags values(?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (LabTestFlag flag : items) {
                statement.setInt(1, flag.getTestId());
                statement.setString(2, flag.getName());
                statement.setString(3, flag.getDefaultVal());
                statement.addBatch();
            }
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveLabTestResults(LabTestResult result) {

        String sql = "insert into lab_test_results values(?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, result.getRequestId());
            statement.setString(2, result.getResult());
            statement.setString(3, result.getComment());
            statement.setString(4, result.getSpecimen().name());
            statement.setObject(5, LocalDate.now());
            statement.setObject(6, LocalTime.now());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean savePurchase(ObservableList<Purchase> items) {
        String sql = "insert into purchases values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (Purchase purchase : items) {
                statement.setInt(1, purchase.getPurchaseId());
                statement.setInt(2, purchase.getDrugId());
                statement.setString(3, purchase.getBatchNo());
                statement.setInt(4, purchase.getQuantity());
                statement.setDouble(5, purchase.getUnitPrice());
                statement.setDouble(6, purchase.getDiscount());
                statement.setInt(7, purchase.getSupplierId());
                statement.setString(8, purchase.getInvoiceNumber());
                statement.setString(9, purchase.getLocation().name());
                statement.setObject(10, purchase.getDateDelivered());
                statement.setObject(11, purchase.getExpiryDate());
                statement.setInt(12, Main.currentUser.getUserId());
                statement.setInt(13, purchase.getOrderId());
                statement.addBatch();
            }
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;

    }

    public static boolean savePurchaseOrder(ObservableList<PurchaseOrder> items) {
        String sql = "insert into orders values(?,?,?,?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (PurchaseOrder purchaseOrder : items) {
                statement.setInt(1, purchaseOrder.getOrderId());
                statement.setInt(2, purchaseOrder.getSupplierId());
                statement.setInt(3, Main.currentUser.getUserId());
                statement.setInt(4, purchaseOrder.getQuantity());
                statement.setInt(5, purchaseOrder.getDrugId());
                statement.setDouble(6, purchaseOrder.getUnitPrice());
                statement.setObject(7, purchaseOrder.getOrderDate());
                statement.setObject(8, null);
                statement.setString(9, purchaseOrder.getOrderStatus().name());
                statement.addBatch();
            }
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveStockTake(ObservableList<StockTake> stockTakes) {
        String sql = "insert into stock_take values(?,?,?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (StockTake stockTake : stockTakes) {
                statement.setInt(1, stockTake.getStockTakeId());
                statement.setInt(2, Main.currentUser.getUserId());
                statement.setInt(3, stockTake.getDrugId());
                statement.setInt(4, stockTake.getCountedQty());
                statement.setInt(5, stockTake.getQtyOnHand());
                statement.setDouble(6, stockTake.getValueChange());
                statement.setString(7, stockTake.getMedicineLocation().name());
                statement.setObject(8, LocalDate.now());
                statement.addBatch();
            }
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;

    }

    public static boolean saveStockTransfer(ObservableList<StockTransfer> stockTransfers) {
        String sql = "insert into stock_transfer values(?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (StockTransfer stockTransfer : stockTransfers) {
                statement.setInt(1, stockTransfer.getTransferNo());
                statement.setInt(2, stockTransfer.getDrugId());
                statement.setInt(3, Main.currentUser.getUserId());
                statement.setInt(4, stockTransfer.getQuantity());
                statement.setString(5, stockTransfer.getOrigin().name());
                statement.setObject(6, stockTransfer.getDateCreated());
                statement.addBatch();
            }
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveWardInfo(Ward ward) {
        String sql = "insert into wards values(?,?,?,?,?,?,?,?,?,?,?,?,?) on duplicate key update " +
                "ward_name =?, num_beds =?, beds_per_row =?, rate =?, corporate_rate =?, nurse_charge=?, " +
                "corporate_nurse_charge =?, doctor_charge =?, corporate_doctor_charge =?, admission_charge =?, " +
                "corporate_admission_charge =?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, ward.getId());
            statement.setString(2, ward.getName());
            statement.setInt(3, ward.getNumBeds());
            statement.setInt(4, ward.getNumOccupiedBeds());
            statement.setInt(5, ward.getBedsPerRow());
            statement.setDouble(6, ward.getRate());
            statement.setDouble(7, ward.getCorporateRate());
            statement.setDouble(8, ward.getNurseCharge());
            statement.setDouble(9, ward.getCorporateNurseCharge());
            statement.setDouble(10, ward.getDoctorCharge());
            statement.setDouble(11, ward.getCorporateDoctorCharge());
            statement.setDouble(12, ward.getAdmissionCharge());
            statement.setDouble(13, ward.getCorporateAdmissionCharge());
            statement.setString(14, ward.getName());
            statement.setInt(15, ward.getNumBeds());
            statement.setInt(16, ward.getBedsPerRow());
            statement.setDouble(17, ward.getRate());
            statement.setDouble(18, ward.getCorporateRate());
            statement.setDouble(19, ward.getNurseCharge());
            statement.setDouble(20, ward.getCorporateNurseCharge());
            statement.setDouble(21, ward.getDoctorCharge());
            statement.setDouble(22, ward.getCorporateDoctorCharge());
            statement.setDouble(23, ward.getAdmissionCharge());
            statement.setDouble(24, ward.getCorporateAdmissionCharge());
            statement.execute();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveSettings(Setting setting) {
        String sql = "insert into general_settings values(?, ?,?,?,?, ?, ?) on duplicate key update " +
                "consultation_fee = ?, corporate_consultation_fee =?, lab_prepay = ?, pharmacy_prepay =?, nhif_rebate" +
                " = ?, radiology_prepay = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, setting.getId());
            statement.setDouble(2, setting.getConsultationFee());
            statement.setDouble(3, setting.getCorporateConsultationFee());
            statement.setDouble(4, setting.getNHIFRebate());
            statement.setBoolean(5, setting.isLabPrepay());
            statement.setBoolean(6, setting.isPharmacyPrepay());
            statement.setBoolean(7, setting.isRadiologyPrepay());
            statement.setDouble(8, setting.getConsultationFee());
            statement.setDouble(9, setting.getCorporateConsultationFee());
            statement.setBoolean(10, setting.isLabPrepay());
            statement.setBoolean(11, setting.isPharmacyPrepay());
            statement.setDouble(12, setting.getNHIFRebate());
            statement.setBoolean(13, setting.isRadiologyPrepay());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }

        return false;
    }

    public static boolean admitPatient(Inpatient inpatient) {
        String sql = "insert into inpatients values(?,?,?,?,?,?,?,?,?,?,?,?, ?, ?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, inpatient.getAdmissionNumber());
            statement.setString(2, inpatient.getInpatientNumber());
            statement.setString(3, inpatient.getPatientId());
            statement.setObject(4, inpatient.getDateAdmitted());
            statement.setObject(5, inpatient.getTimeAdmitted());
            statement.setInt(6, inpatient.getWardId());
            statement.setInt(7, inpatient.getBedId());
            statement.setString(8, inpatient.getStatus().name());
            statement.setString(9, inpatient.getPaymentMode().name());
            statement.setInt(10, inpatient.getDoctorId());
            statement.setBoolean(11, inpatient.isNhifApplicable());
            statement.setInt(12, inpatient.getBillNumber());
            statement.setObject(13, null);
            statement.setObject(14, null);
            statement.execute();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveBill(Bill bill) {
        String sql = "insert into billing values (?, ?,?,?,?,?,?,?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, bill.getId());
            statement.setInt(2, bill.getBillNumber());
            statement.setString(3, bill.getPatientNumber());
            if (bill.getInsurer() == null || bill.getInsurer().isEmpty()) {
                statement.setString(4, null);
                statement.setString(5, null);
            } else {
                statement.setString(4, bill.getInsurer());
                statement.setString(5, bill.getInsuranceId());
            }

            statement.setString(6, bill.getDescription());
            statement.setDouble(7, bill.getAmount());
            statement.setString(8, bill.getStatus().name());
            statement.setString(9, bill.getCategory().name());
            statement.setInt(10, bill.getAdmissionNumber());
            statement.setInt(11, bill.getQueueNumber());
            statement.setObject(12, bill.getDateCreated());
            statement.setDouble(13, 0);
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean addLabRequest(LabRequest request) {
        String sql = "INSERT INTO hmsDb.lab_requests VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, request.getId());
            statement.setInt(2, request.getQueueNum());
            statement.setInt(3, request.getAdmissionNum());
            statement.setInt(4, request.getTestId());
            statement.setString(5, request.getStatus().name());
            if (request.getSpecimen() != null) {
                statement.setString(6, request.getSpecimen().name());
            } else {
                statement.setString(6, null);
            }
            statement.setObject(7, request.getTimeCreated());
            statement.setObject(8, request.getDateCreated());
            statement.setInt(9, Main.currentUser.getUserId());
            statement.setString(10, request.getPatientId());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveInpatientVisitNotes(InpatientVisit inpatientVisit) {
        String sql = "insert into inpatient_visits values(?,?,?,?,?,?,?) on duplicate key update " +
                "notes = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, inpatientVisit.getVisitId());
            statement.setString(2, inpatientVisit.getNotes());
            statement.setInt(3, inpatientVisit.getUserId());
            statement.setInt(4, inpatientVisit.getAdmissionNum());
            statement.setObject(5, inpatientVisit.getDateCreated());
            statement.setObject(6, inpatientVisit.getTimeCreated());
            statement.setString(7, inpatientVisit.getCategory().name());
            statement.setObject(8, inpatientVisit.getNotes());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }

        return false;
    }

    public static boolean saveAllergy(String allergy, String patientId) {
        String sql = "insert into allergies values (?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, patientId);
            statement.setString(2, allergy);
            statement.execute();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveOperation(Operation operation) {
        String sql = "insert into operations values(?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, operation.getAdmissionNum());
            statement.setString(2, operation.getOperation());
            statement.setString(3, operation.getIndication());
            statement.setString(4, operation.getSurgeon());
            statement.setString(5, operation.getAssistants());
            statement.setString(6, operation.getAnaesthetist());
            statement.setString(7, operation.getAnaesthesia());
            statement.setString(8, operation.getIncision());
            statement.setString(9, operation.getProcedure());
            statement.setObject(10, operation.getDate());
            statement.setObject(11, operation.getTime());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean savePatientProcedure(PatientProcedure patientProcedure) {
        String sql = "insert into patient_procedures values(?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, patientProcedure.getId());
            statement.setInt(2, patientProcedure.getVisitId());
            statement.setInt(3, patientProcedure.getAdmissionNum());
            statement.setString(4, patientProcedure.getName());
            statement.setInt(5, patientProcedure.getUserId());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveDiagnosis(Diagnosis diagnosis) {
        String sql = "insert into diagnosis values(?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, diagnosis.getId());
            statement.setString(2, diagnosis.getCode());
            statement.setInt(3, diagnosis.getUserId());
            statement.setInt(4, diagnosis.getAdmissionNum());
            statement.setInt(5, diagnosis.getVisitId());
            statement.setObject(6, diagnosis.getDateCreated());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveClinicalSummary(ClinicalSummary summary) {
        String sql = "insert into clinical_summary values(?,?,?) on duplicate key update " +
                "summary = ?, date_modified =?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, summary.getAdmissionNum());
            statement.setString(2, summary.getSummary());
            statement.setObject(3, summary.getDateModified());
            statement.setString(4, summary.getSummary());
            statement.setObject(5, summary.getDateModified());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean updatePatientBill(ObservableList<Bill> items) {
        String sql = "update billing set paid = paid + ?, status = ?  where id = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            for (Bill bill : items) {
                if (bill.getCategory() != Bill.Category.BED_CHARGES && bill.getCategory() != Bill.Category.NURSING_CHARGES && bill.getCategory() != Bill.Category.DOCTOR_CHARGES) {
                    statement.setDouble(1, bill.getAmountPaid());
                    if (bill.getAmount() == bill.getAmountPaid()) {
                        statement.setString(2, Bill.Status.PAID.name());
                    } else {
                        statement.setString(2, Bill.Status.PENDING.name());
                    }
                    statement.setInt(3, bill.getId());
                    statement.execute();
                }
            }
            sql = "insert into daily_charges values (?,?,?) on duplicate key update paid = paid +  ?";
            statement = connection.prepareStatement(sql);
            for (Bill bill : items) {
                if (bill.getCategory() == Bill.Category.NURSING_CHARGES || bill.getCategory() == Bill.Category.BED_CHARGES || bill.getCategory() == Bill.Category.DOCTOR_CHARGES) {
                    statement.setInt(1, bill.getBillNumber());
                    statement.setString(2, bill.getCategory().name());
                    statement.setDouble(3, bill.getAmountPaid());
                    statement.setDouble(4, bill.getAmountPaid());
                    statement.execute();
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean savePayment(List<Payment> payments) {
        String sql = "insert into payments values(?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            LocalDate date = LocalDate.now(); //date when the payment was made
            //payment.dateCreated() corresponds the date when the patient visited the hospital
            for (Payment payment : payments) {
                statement.setInt(1, payment.getBillId());
                statement.setInt(2, payment.getReceiptNumber());
                statement.setInt(3, Main.currentUser.getUserId());
                statement.setDouble(4, payment.getAmount());
                statement.setInt(5, payment.getBillNumber());
                statement.setString(6, payment.getAccountName());
                statement.setString(7, payment.getPaymentMeans().name());
                statement.setObject(8, payment.getDateCreated());
                statement.setString(9, payment.getDescription());
                statement.setString(10, payment.getCategory());
                statement.setString(11, payment.getPatient());
                statement.setObject(12, date);
                statement.addBatch();
            }
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean savePrescription(Prescription prescription) {
        String sql = "insert into prescriptions values(?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1, prescription.getId());
            statement.setInt(2, prescription.getVisitId());
            statement.setInt(3, prescription.getAdmissionNum());
            statement.setInt(4, prescription.getDrugId());
            statement.setInt(5, Main.currentUser.getUserId());
            statement.setInt(6, prescription.getQuantity());
            statement.setInt(7, prescription.getDuration());
            statement.setString(8, prescription.getDosage().name());
            if (prescription.getFormulation() != null) {
                statement.setString(9, prescription.getFormulation().name());
            } else {
                statement.setString(9, null);
            }
            statement.setString(10, prescription.getStatus().name());
            statement.setObject(11, prescription.getDateCreated());
            statement.setObject(12, prescription.getTimeCreated());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean savePurchaseReturns(ObservableList<PurchaseReturn> items) {
        String sql = "insert into purchase_returns values(?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (PurchaseReturn purchaseReturn : items) {
                statement.setInt(1, purchaseReturn.getReturnId());
                statement.setString(2, purchaseReturn.getInvoiceNo());
                statement.setInt(3, purchaseReturn.getSupplierId());
                statement.setInt(4, purchaseReturn.getDrugId());
                statement.setInt(5, purchaseReturn.getQuantity());
                statement.setDouble(6, purchaseReturn.getBuyingPrice());
                statement.setString(7, purchaseReturn.getNote());
                statement.setObject(8, purchaseReturn.getDate());
                statement.setInt(9, Main.currentUser.getUserId());
                statement.setString(10, purchaseReturn.getLocation().name());
                statement.addBatch();
            }
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void saveHospitalLetterHead(File file) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into hospital(ID, letter_head) values (?,?) on duplicate key update letter_head = ?");
            statement.setInt(1, 0);
            statement.setBlob(2, new FileInputStream(file));
            statement.setBlob(3, new FileInputStream(file));

            statement.execute();
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
    }

    public static boolean saveDiseases(ObservableList<ICD10_Diagnosis> list) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into icd10_diagnoses values (?,?) on duplicate key update name = ?");
            for (ICD10_Diagnosis ICD10Diagnosis : list) {
                statement.setString(1, ICD10Diagnosis.getCode());
                statement.setString(2, ICD10Diagnosis.getName());
                statement.setString(3, ICD10Diagnosis.getName());
                statement.addBatch();
            }
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveAppointment(Appointment appointment) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into appointments values (?,?,?,?,?,?)");
            statement.setInt(1, 0);
            statement.setInt(2, appointment.getDoctorId());
            statement.setString(3, appointment.getPatientId());
            statement.setObject(4, appointment.getDate());
            statement.setObject(5, appointment.getTime());
            statement.setString(6, appointment.getNote());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static void saveLabResultNotification(int visitId) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into lab_result_notifications values (?,?,?,?)");
            statement.setInt(1, 0);
            statement.setInt(2, visitId);
            statement.setBoolean(3, false);
            statement.setObject(4, LocalDateTime.now());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
    }

    public static boolean saveRadiologyItem(RadiologyItem radiologyItem) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into radiology_items values(?,?,?,?) on duplicate key " +
                    "update category = ?, description = ?, cost = ?");
            statement.setInt(1, radiologyItem.getId());
            statement.setString(2, radiologyItem.getCategory());
            statement.setString(3, radiologyItem.getDescription());
            statement.setDouble(4, radiologyItem.getCost());
            statement.setString(5, radiologyItem.getCategory());
            statement.setString(6, radiologyItem.getDescription());
            statement.setDouble(7, radiologyItem.getCost());
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeStatement(statement);
        }
        return false;
    }

    public static boolean saveRadiologyRequest(RadiologyRequest request) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("insert into radiology_requests values (?, ?,?,?,?,?,?,?,?,?)");
            statement.setInt(1, request.getRequestId());
            statement.setInt(2, request.getId());
            statement.setString(3, request.getPatientId());
            statement.setInt(4, request.getAdmissionId());
            statement.setInt(5, request.getVisitId());
            statement.setInt(6, Main.currentUser.getUserId());
            statement.setObject(7, LocalDate.now());
            statement.setObject(8, LocalTime.now());
            statement.setString(9, RadiologyRequest.Status.PENDING.name());
            statement.setString(10, null);
            statement.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeStatement(statement);
        }
        return false;
    }
}
