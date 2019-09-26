package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.Bill;
import main.java.model.PatientCategory;
import main.java.model.Payment;
import main.java.model.PaymentMode;
import main.java.util.CurrencyUtil;
import main.java.util.DBUtil;
import main.java.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static main.java.controller.BillingController.*;

public class BillingDao {
    public static int getNextBillNumber() {
        int initial = 1000;
        String sql = "select bill_number from inpatients order by bill_number desc";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                initial = resultSet.getInt("bill_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sql = "select bill_number from queues order by bill_number desc";
        resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                if (resultSet.getInt("bill_number") > initial) {
                    initial = resultSet.getInt("bill_number");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return initial + 1;
    }

    public static ObservableList<Map<String, String>> getAllPatientBills(PaymentMode paymentMode) throws SQLException {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();

        //get outpatients first
        String string = "select billing.bill_number, billing.patient_id, billing.insurer, billing.insurance_id, sum(amount - paid) as pending, sum(paid) as paid, queues.DateCreated, Queues.DoctorId, patients.FirstName, Patients.LastName " +
                "from billing " +
                "inner join patients on billing.patient_id = patients.PatientId " +
                "inner join queues on queues.VisitId = billing.queue_num " +
                "where queues.payment_mode = '" + paymentMode + "' " +
                "and exists(select status from billing where status = '" + Bill.Status.PENDING + "') " +
                "group by bill_number, patient_id, insurer, insurance_id, firstName, LastName, DateCreated, DoctorId";
        ResultSet resultSet = DBUtil.executeQuery(string);
        if (resultSet != null) {
            while (resultSet.next()) {
                //TODO : get results
                Map<String, String> map = new HashMap<>();
                if (resultSet.getString("doctorId").equals("0")) {
                    map.put(CATEGORY, PatientCategory.WALK_IN.toString());
                } else {
                    map.put(CATEGORY, PatientCategory.OUTPATIENT.toString());
                }
                map.put(BILL_NUM, resultSet.getString("bill_number"));
                map.put(PATIENT_NUM, resultSet.getString("patient_id"));
                map.put(DATE, DateUtil.formatDate(resultSet.getObject("DateCreated", LocalDate.class)));
                map.put(PATIENT_NAME, resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                map.put(INSURER, resultSet.getString("insurer"));
                map.put(INSURANCE_ID, resultSet.getString("insurance_id"));
                map.put(OUTSTANDING_AMOUNT, CurrencyUtil.formatCurrency(resultSet.getDouble("pending")));
                map.put(AMOUNT_PAID, CurrencyUtil.formatCurrency(resultSet.getDouble("paid")));
                map.put(REBATE, "0");
                if (!map.get(OUTSTANDING_AMOUNT).equals("0")) {
                    list.add(map);

                }
            }
        }
        list.addAll(getAllInpatientBills(list, paymentMode));
        return list;
    }

    private static ObservableList<Map<String, String>> getAllInpatientBills(ObservableList<Map<String, String>> outpatientBills, PaymentMode paymentMode) throws SQLException {
        ObservableList<Map<String, String>> bills = FXCollections.observableArrayList();
        String sql = "select billing.bill_number, billing.insurer, billing.insurance_id, sum(paid) as paid, sum(amount - paid) as pending, " +
                " inpatients.inpatient_num, inpatients.nhif_applicable, inpatients.date_admitted, " +
                " inpatients.date_discharged, inpatients.ward_id, patients.FirstName, " +
                " patients.LastName, rate, corporate_rate, nurse_charge, corporate_nurse_charge, " +
                " doctor_charge, corporate_doctor_charge " +
                "from billing " +
                "  inner join patients on  patients.patientId = billing.patient_id " +
                "  inner join inpatients on inpatients.admission_num = billing.admission_num " +
                "  inner join wards on wards.ward_id = inpatients.ward_id " +
                "where " +
                "exists(select status from billing where status = '" + Bill.Status.PENDING + "') " +
                "and inpatients.payment_mode = '" + paymentMode + "' " +
                "group by bill_number, ward_id, FirstName, LastName, insurer, insurance_id, date_admitted, date_discharged, inpatient_num, nhif_applicable";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Map<String, String> data = new HashMap<>();
                data.put(CATEGORY, PatientCategory.INPATIENT.toString());
                data.put(INSURANCE_ID, resultSet.getString("insurance_id"));
                data.put(INSURER, resultSet.getString("insurer"));
                data.put(PATIENT_NUM, resultSet.getString("inpatient_num"));
                data.put(PATIENT_NAME, resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                data.put(DATE, DateUtil.formatDate(resultSet.getObject("date_admitted", LocalDate.class)));

                LocalDate dateAdmitted = null, dateDischarged = null;
                if (resultSet.getObject("date_admitted") != null) {
                    dateAdmitted = resultSet.getObject("date_admitted", LocalDate.class);
                }
                if (resultSet.getObject("date_discharged") != null) {
                    dateDischarged = resultSet.getObject("date_discharged", LocalDate.class);
                }

                if (resultSet.getBoolean("nhif_applicable")) {
                    double dailyRebate = getRebate(resultSet.getString("bill_number"));
                    if (dateDischarged == null) {
                        dateDischarged = LocalDate.now();
                    }
                    data.put(REBATE, CurrencyUtil.formatCurrency(dailyRebate * DateUtil.getNumDaysDiff(dateAdmitted, dateDischarged)));
                } else{
                    data.put(REBATE, "0");
                }

                double[] rates = new double[]{resultSet.getDouble("rate"), resultSet.getDouble("corporate_rate"), resultSet.getDouble("nurse_charge"), resultSet.getDouble("corporate_nurse_charge"), resultSet.getDouble("doctor_charge"), resultSet.getDouble("corporate_doctor_charge")};

                double pendingDailyCharges = resultSet.getDouble("pending") + getTotalInpatientOutstandingDailyCharges(dateAdmitted, dateDischarged, rates, resultSet.getString("insurer"), resultSet.getString("bill_number"));


                double dailyChargePayments = resultSet.getDouble("paid") + getDailyChargesPayments(resultSet.getString("bill_number"));

                Map<String, String> duplicate = duplicateEntry(resultSet.getString("bill_number"), outpatientBills);
                if (duplicate != null) {
                    duplicate.put(AMOUNT_PAID, CurrencyUtil.formatCurrency(dailyChargePayments + CurrencyUtil.parseCurrency(duplicate.get(AMOUNT_PAID))));
                    duplicate.put(OUTSTANDING_AMOUNT, CurrencyUtil.formatCurrency(pendingDailyCharges + CurrencyUtil.parseCurrency(duplicate.get(OUTSTANDING_AMOUNT))));

                } else {
                    data.put(BILL_NUM, resultSet.getString("bill_number"));
                    data.put(OUTSTANDING_AMOUNT, CurrencyUtil.formatCurrency(pendingDailyCharges));
                    data.put(AMOUNT_PAID, CurrencyUtil.formatCurrency(dailyChargePayments));
                }
                data.put(OUTSTANDING_AMOUNT, CurrencyUtil.formatCurrency(CurrencyUtil.parseCurrency(data.get(OUTSTANDING_AMOUNT)) - CurrencyUtil.parseCurrency(data.get(REBATE))));
                if (CurrencyUtil.parseCurrency(data.get(OUTSTANDING_AMOUNT)) != 0) {
                    bills.add(data);
                }
            }
        }
        return bills;
    }

    private static Map<String, String> duplicateEntry(String bill_number, ObservableList<Map<String, String>> list) {
        for (Map<String, String> data : list) {
            if (data.get(BILL_NUM).equals(bill_number)) {
                return data;
            }
        }
        return null;
    }

    public static ObservableList<Bill> getPatientBill(String billNum, boolean inpatient) {
        ObservableList<Bill> bills = FXCollections.observableArrayList();
        String sql = "select id, (amount - paid) as amount, category, description " +
                "from billing " +
                "where status = '" + Bill.Status.PENDING + "' and bill_number = " + billNum;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    Bill bill = new Bill();
                    bill.setBillNumber(Integer.parseInt(billNum));
                    bill.setId(resultSet.getInt("id"));
                    bill.setAmount(resultSet.getDouble("amount"));
                    bill.setCategory(Bill.Category.valueOf(resultSet.getString("category")));
                    bill.setDescription(resultSet.getString("description"));
                    bills.add(bill);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (inpatient) {
            bills.addAll(getInpatientBills(billNum));
        }
        return bills;
    }

    private static ObservableList<Bill> getInpatientBills(String billNum) {
        ObservableList<Bill> bills = FXCollections.observableArrayList();
        String sql = "select payment_mode, date_admitted, date_discharged, rate, corporate_rate, nurse_charge, " +
                "corporate_nurse_charge, doctor_charge, corporate_doctor_charge " +
                "from inpatients " +
                "inner join wards on wards.ward_id = inpatients.ward_id " +
                "where bill_number = " + billNum;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            try {
                if (resultSet.next()) {
                    PaymentMode paymentMode = PaymentMode.valueOf(resultSet.getString("payment_mode"));
                    LocalDate dateAdmitted = resultSet.getObject("date_admitted", LocalDate.class);
                    LocalDate dateDischarged = LocalDate.now();
                    if (resultSet.getObject("date_discharged") != null) {
                        dateDischarged = resultSet.getObject("date_discharged", LocalDate.class);
                    }
                    int numDays = DateUtil.getNumDaysDiff(dateAdmitted, dateDischarged);

                    Bill bedCharges = new Bill();
                    bedCharges.setDescription("Bed charges");
                    bedCharges.setCategory(Bill.Category.BED_CHARGES);

                    Bill doctorCharges = new Bill();
                    doctorCharges.setCategory(Bill.Category.DOCTOR_CHARGES);
                    doctorCharges.setDescription("Doctor visit charges");

                    Bill nurseCharge = new Bill();
                    nurseCharge.setCategory(Bill.Category.NURSING_CHARGES);
                    nurseCharge.setDescription("Nurse visit charges");

                    for (Bill bill : new Bill[]{bedCharges, doctorCharges, nurseCharge}) {
                        bill.setAmount(getDailyOutstandingAmount(paymentMode, resultSet, numDays, billNum, bill.getCategory()));
                        bill.setBillNumber(Integer.parseInt(billNum));
                        if (bill.getAmount() > 0) {
                            bills.add(bill);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return bills;
    }

    private static double getDailyOutstandingAmount(PaymentMode paymentMode, ResultSet resultSet, int numDays, String billNum, Bill.Category category) throws SQLException {
        //get paid
        String sql = "select paid from daily_charges where bill_number = " + billNum + " and category = '" + category.name() + "'";
        double paid = 0, dailyCharge = 0;
        ResultSet amountPaidResultSet = DBUtil.executeQuery(sql);
        if (amountPaidResultSet != null) {
            if (amountPaidResultSet.next()) {
                paid = amountPaidResultSet.getDouble("paid");
            }
        }
        switch (category) {
            case DOCTOR_CHARGES:
                if (paymentMode == PaymentMode.INSURANCE) {
                    dailyCharge = resultSet.getDouble("corporate_doctor_charge");
                } else {
                    dailyCharge = resultSet.getDouble("doctor_charge");
                }
                break;
            case NURSING_CHARGES:
                if (paymentMode == PaymentMode.INSURANCE) {
                    dailyCharge = resultSet.getDouble("corporate_nurse_charge");
                } else {
                    dailyCharge = resultSet.getDouble("nurse_charge");
                }
                break;
            case BED_CHARGES:
                if (paymentMode == PaymentMode.INSURANCE) {
                    dailyCharge = resultSet.getDouble("corporate_rate");
                } else {
                    dailyCharge = resultSet.getDouble("rate");
                }

                break;
        }
        return dailyCharge * numDays - paid;
    }

    public static double getRebate(String billNum) throws SQLException {
        String sql = "select nhif_applicable from inpatients where bill_number = " + billNum;
        boolean nhif_applicable = false;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null && resultSet.next()) {
            nhif_applicable = resultSet.getBoolean("nhif_applicable");
        }
        if (nhif_applicable) {
            sql = "select nhif_rebate from general_settings";
            resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null && resultSet.next()) {
                return resultSet.getDouble("nhif_rebate");
            }
        }
        return 0;
    }

    private static double getTotalInpatientOutstandingDailyCharges(LocalDate dateAdmitted, LocalDate dateDischarged, double[] rates, String insurer, String bill_number) throws SQLException {
        if (dateAdmitted == null) {
            return 0;
        }
        if (dateDischarged == null) {
            dateDischarged = LocalDate.now();

        }
        long numDays = DateUtil.getNumDaysDiff(dateAdmitted, dateDischarged);
        double total = 0;
        for (int i = 0; i < rates.length; i++) {
            if (insurer != null) {
                if (i % 2 != 0) {
                    total += rates[i] * numDays;
                }
            } else {
                if (i % 2 == 0) {
                    total += rates[i] * numDays;
                }
            }

        }
        return total - getDailyChargesPayments(bill_number);
    }

    private static double getDailyChargesPayments(String bill_number) throws SQLException {
        String sql = "select sum(paid) as paid from daily_charges where bill_number = " + bill_number;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null && resultSet.next()) {
            return resultSet.getDouble("paid");
        }
        return 0;
    }

    public static ObservableList<Payment> getBillPayments(String billNumber) throws Exception {
        ObservableList<Payment> payments = FXCollections.observableArrayList();
        String sql = "select receipt_no, sum(amount) as amount, account, payment_means, date_created, time_created, users.FirstName, users.LastName from payments " +
                " inner join users on users.Id = payments.user_id" +
                " where bill_number = " + billNumber +
                " group by receipt_no, date_created, time_created, account, payment_means, FirstName, LastName" +
                " order by receipt_no desc";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Payment payment = new Payment();
                payment.setReceiptNumber(resultSet.getInt("receipt_no"));
                payment.setAmount(resultSet.getDouble("amount"));
                payment.setAccountName(resultSet.getString("account"));
                payment.setPaymentMeans(Payment.PaymentMeans.valueOf(resultSet.getString("payment_means")));
                payment.setDateCreated(resultSet.getObject("date_created", LocalDate.class));
                payment.setTimeCreated(resultSet.getObject("time_created", LocalTime.class));
                payment.setReceivedBy(resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                payments.add(payment);
            }
        }
        return payments;
    }

    public static ObservableList<Payment> getPaymentDetails(int receiptNumber) throws SQLException {
        ObservableList<Payment> payments = FXCollections.observableArrayList();
        String sql = "select amount, description, category from payments " +
                "where receipt_no = " + receiptNumber;
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Payment payment = new Payment();
                payment.setAmount(resultSet.getDouble("amount"));
                payment.setCategory(resultSet.getString("category"));
                payment.setDescription(resultSet.getString("description"));
                payments.add(payment);
            }
        }
        return payments;
    }

    public static int getNextReceiptNumber() {
        String sql = "select receipt_no from payments order by receipt_no desc";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("receipt_no") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1001;
    }

    public static ObservableList<Payment> getPaymentsForPeriod(LocalDate start, LocalDate end) throws SQLException {
        ObservableList<Payment> payments = FXCollections.observableArrayList();
        String sql = "select receipt_no, sum(amount) as amount, payment_means, date_created, time_created, patient_name " +
                "from payments " +
                "where date_created between '" + start + "' and '" + end + "'" +
                "group by receipt_no, payment_means, date_created, time_created, patient_name";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            while (resultSet.next()) {
                Payment payment = new Payment();
                payment.setPatient(resultSet.getString("patient_name"));
                payment.setReceiptNumber(resultSet.getInt("receipt_no"));
                payment.setAmount(resultSet.getDouble("amount"));
                payment.setPaymentMeans(Payment.PaymentMeans.valueOf(resultSet.getString("payment_means")));
                payment.setTimeCreated(resultSet.getObject("time_created", LocalTime.class));
                payment.setDateCreated(resultSet.getObject("date_created", LocalDate.class));
                payments.add(payment);
            }
        }
        return payments;
    }

    public static ObservableList<Map<String, String>> getReceipts(LocalDate start, LocalDate end) {
        ObservableList<Map<String, String>> list = FXCollections.observableArrayList();
        String sql = "select visit_date, sum(payments.amount) as amount_paid, payments.bill_number, " +
                "users.FirstName, users.LastName, patient_name " +
                "from payments " +
                "inner join users on users.Id = payments.user_id " +
                "where visit_date between '" + start + "' and '" + end + "' " +
                "group by visit_date, bill_number, patient_name, FirstName, LastName";
        ResultSet resultSet = DBUtil.executeQuery(sql);
        try {
            if (resultSet != null) {
                while (resultSet.next()) {
                    Map<String, String> data = new HashMap<>();
                    data.put(SERVER, resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
                    data.put(PATIENT_NAME, resultSet.getString("patient_name"));
                    data.put(DATE, DateUtil.formatDateLong(resultSet.getObject("visit_date", LocalDate.class)));
                    data.put(AMOUNT_PAID, CurrencyUtil.formatCurrency(resultSet.getDouble("amount_paid")));
                    data.put(BILL_NUM, resultSet.getString("bill_number"));
                    list.add(data);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
