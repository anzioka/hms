package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.*;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class InpatientDao {
    public static ObservableList<Bed> getBedsInWards(int wardId) {
        ObservableList<Bed> beds = FXCollections.observableArrayList();
        String sql = "select * from beds where ward_id =" + wardId;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Bed bed = new Bed();
                    bed.setBedId(resultSet.getInt("bed_id"));
                    bed.setLabel(resultSet.getString("bed_label"));
                    bed.setBedStatus(Bed.BedStatus.valueOf(resultSet.getString("status")));
                    beds.add(bed);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return beds;
    }


    public static ObservableList<Ward> getWards() {
        ObservableList<Ward> wards = FXCollections.observableArrayList();
        String sql = "select * from wards " +
                "order by ward_name asc";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Ward ward = new Ward();
                    ward.setName(resultSet.getString("ward_name"));
                    ward.setId(resultSet.getInt("ward_id"));
                    ward.setRate(resultSet.getDouble("rate"));
                    ward.setNumBeds(resultSet.getInt("num_beds"));
                    ward.setNumOccupiedBeds(resultSet.getInt("occupied"));
                    ward.setCorporateRate(resultSet.getDouble("corporate_rate"));
                    ward.setNurseCharge(resultSet.getDouble("nurse_charge"));
                    ward.setCorporateNurseCharge(resultSet.getDouble("corporate_nurse_charge"));
                    ward.setDoctorCharge(resultSet.getDouble("doctor_charge"));
                    ward.setCorporateDoctorCharge(resultSet.getDouble("corporate_doctor_charge"));
                    ward.setAdmissionCharge(resultSet.getDouble("admission_charge"));
                    ward.setCorporateAdmissionCharge(resultSet.getDouble("corporate_admission_charge"));
                    ward.setBedsPerRow(resultSet.getInt("beds_per_row"));
                    wards.add(ward);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return wards;
    }

    public static ObservableList<Inpatient> getAdmittedPatients() {
        ObservableList<Inpatient> admittedPatients = FXCollections.observableArrayList();
        String sql = "select admission_num, nhif_applicable, bill_number, inpatient_num,payment_mode, date_admitted, wards.*, inpatients.bed_id, " +
                "FirstName, LastName, DateOfBirth, PatientId, NHIFNumber, phoneNumber, sex, InsuranceProvider, InsuranceId " +
                "from " +
                "((inpatients inner join wards on inpatients.ward_id = wards.ward_id) " +
                "right join patients on inpatients.patient_id = patients.PatientID) " +
                "where inpatients.status = '" + Inpatient.Status.ADMITTED + "'";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Inpatient inpatient = new Inpatient();
                    inpatient.setPatientId(resultSet.getString("patientId"));
                    inpatient.setAdmissionNumber(resultSet.getInt("admission_num"));
                    inpatient.setInpatientNumber(resultSet.getString("inpatient_num"));
                    inpatient.setDateAdmitted(resultSet.getObject("date_admitted", LocalDate.class));
                    inpatient.setNhifApplicable(resultSet.getBoolean("nhif_applicable"));
                    inpatient.setAssignedWard(resultSet.getString("ward_name"));
                    inpatient.setBedId(resultSet.getInt("bed_id"));
                    inpatient.setFirstName(resultSet.getString("FirstName"));
                    inpatient.setInsurer(resultSet.getString("InsuranceProvider"));
                    inpatient.setInsuranceID(resultSet.getString("InsuranceId"));
                    inpatient.setLastName(resultSet.getString("LastName"));
                    inpatient.setDateOfBirth(resultSet.getObject("DateOfBirth", LocalDate.class));
                    inpatient.setTelephoneNumber(resultSet.getString("phoneNumber"));
                    inpatient.setNHIFNumber(resultSet.getString("NHIFNumber"));

                    inpatient.setSexuality(resultSet.getString("sex"));
                    inpatient.setBillNumber(resultSet.getInt("bill_number"));
                    inpatient.setPaymentMode(PaymentMode.valueOf(resultSet.getString("payment_mode")));
                    admittedPatients.add(inpatient);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return admittedPatients;
    }

    public static ObservableList<InpatientVisit> getVisitNotes(InpatientVisit.Category category, int admissionNum) {
        ObservableList<InpatientVisit> list = FXCollections.observableArrayList();
        String sql = "select inpatient_visits.*, users.FirstName, users.LastName from inpatient_visits " +
                "inner join users on users.Id = inpatient_visits.user_id " +
                "where admission_num = " + admissionNum + " and category = '" + category + "'";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    InpatientVisit visit = new InpatientVisit();
                    visit.setAdmissionNum(admissionNum);
                    visit.setTimeCreated(resultSet.getObject("time_created", LocalTime.class));
                    visit.setDateCreated(resultSet.getObject("date_created", LocalDate.class));
                    if (category == InpatientVisit.Category.DOCTOR) {
                        visit.setUserName("Dr. " + resultSet.getString("LastName"));
                    } else{
                        visit.setUserName(resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                    }
                    visit.setUserId(resultSet.getInt("user_id"));
                    visit.setNotes(resultSet.getString("notes"));
                    list.add(visit);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static ObservableList<Operation> getOperations(int admissionNumber) {
        ObservableList<Operation> operations = FXCollections.observableArrayList();
        String sql = "select * from operations where admission_num = " + admissionNumber + " order by " +
                "date desc";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Operation operation = new Operation();
                    operation.setOperation(resultSet.getString("operation"));
                    operation.setIndication(resultSet.getString("indication"));
                    operation.setSurgeon(resultSet.getString("surgeon"));
                    operation.setAssistants(resultSet.getString("assistants"));
                    operation.setAnaesthetist(resultSet.getString("anaesthetist"));
                    operation.setAnaesthesia(resultSet.getString("anaesthesia"));
                    operation.setIncision(resultSet.getString("incision"));
                    operation.setProcedure(resultSet.getString("procedure"));
                    operation.setTime(resultSet.getObject("time", LocalTime.class));
                    operation.setDate(resultSet.getObject("date", LocalDate.class));
                    operations.add(operation);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return operations;
    }


    public static ClinicalSummary getClinicalSummary(int admissionNumber) {
        String sql = "select summary, date_modified from clinical_summary " +
                "where admission_num = " + admissionNumber;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                if (resultSet.next()) {
                    ClinicalSummary summary = new ClinicalSummary();
                    summary.setDateModified(resultSet.getObject("date_modified", LocalDate.class));
                    summary.setSummary(resultSet.getString("summary"));
                    return summary;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Inpatient getPatient(int admissionNumber) throws SQLException {
        String sql = "select inpatient_num, date_admitted, date_discharged, payment_mode, nhif_applicable, bill_number, FirstName, LastName, ward_name from inpatients " +
                "inner join patients on patients.PatientId = inpatients.patient_id " +
                "inner join wards on wards.ward_id = inpatients.ward_id " +
                "where admission_num = " + admissionNumber;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null && resultSet.next()) {
            Inpatient inpatient = new Inpatient();
            inpatient.setInpatientNumber(resultSet.getString("inpatient_num"));
            inpatient.setDateAdmitted(resultSet.getObject("date_admitted", LocalDate.class));
            if (resultSet.getObject("date_discharged") != null) {
                inpatient.setDateDischarged(resultSet.getObject("date_discharged", LocalDate.class));
            }
            inpatient.setPaymentMode(PaymentMode.valueOf(resultSet.getString("payment_mode")));
            inpatient.setNhifApplicable(resultSet.getBoolean("nhif_applicable"));
            inpatient.setFirstName(resultSet.getString("FirstName"));
            inpatient.setLastName(resultSet.getString("LastName"));
            inpatient.setAssignedWard(resultSet.getString("ward_name"));
            inpatient.setBillNumber(resultSet.getInt("bill_number"));
            return inpatient;
        }
        return null;
    }
}
