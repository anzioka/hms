package main.java.model;

/**
 * Created by alfonce on 19/07/2017.
 */
public enum Permission {
    EDIT_EXISTING_PATIENTS("Edit existing patient information"),
    CREATE_DRUGS("Add, modify (name, reorder_level, price) and delete drugs"),
    CREATE_PURCHASE_ORDERS("Create purchase orders"),
    CANCEL_PURCHASE_ORDERS("Cancel purchase orders"),
    TRANSFER_DRUGS("Transfer drugs between store and pharmacy"),
    RETURN_PURCHASES("Return drugs to supplier"),
    RECEIVE_PURCHASES("Receive drug purchases"),
    STOCK_TAKE("Stock take"),
    EDIT_LAB_TESTS("Add, edit and delete lab tests"),
    EDIT_RADIOLOGY_ITEMS("Add, upload, edit, or delete radiology items"),
    CHANGE_INVOICE_PAYMENT_STATUS("Change payment status of invoices"),
    CREATE_INSURANCE("Add, edit and delete drug suppliers"),
    CREATE_USERS("Create, edit and delete system users"),
    SET_PERMISSIONS("Set or change user permission or access settings"),
    VIEW_ACTIVITY("View other users' activity"),
    EDIT_WARD("Create wards and modify ward details (daily charges etc)");
    private String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    @Override
    public String toString() {
        return this.permission;
    }
}
