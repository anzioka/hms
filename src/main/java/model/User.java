package main.java.model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Created by alfonce on 20/04/2017.
 */
public class User {
    private IntegerProperty userId;
    private StringProperty firstName;
    private StringProperty lastName;
    private StringProperty loginName;
    private StringProperty password;
    private UserCategory category;
    private ObjectProperty<LocalDate> dateCreated;

    public User() {
        userId = new SimpleIntegerProperty(0);
        loginName = new SimpleStringProperty();
        firstName = new SimpleStringProperty("");
        lastName = new SimpleStringProperty("");
        password = new SimpleStringProperty("");
        dateCreated = new SimpleObjectProperty<>();
    }

    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public String getLoginName() {
        return loginName.get();
    }

    public void setLoginName(String loginName) {
        this.loginName.set(loginName);
    }

    public StringProperty loginNameProperty() {
        return loginName;
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public LocalDate getDateCreated() {
        return dateCreated.get();
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated.set(dateCreated);
    }

    public ObjectProperty<LocalDate> dateCreatedProperty() {
        return dateCreated;
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public UserCategory getCategory() {
        return category;
    }

    public void setCategory(UserCategory category) {
        this.category = category;
    }

    public StringProperty categoryProperty() {
        return new SimpleStringProperty(category.toString());
    }

    @Override
    public boolean equals(Object object) {
        boolean result = false;
        if (object instanceof User) {
            result = (this.getUserId() == ((User) object).getUserId());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return getUserId();
    }

    @Override
    public String toString() {
        switch (category) {
            case DOCTOR:
                return "Dr. " + lastName.get();
            default:
                return firstName.get() + " " + lastName.get();
        }
    }

    public String getDescription() {
        return "{ username : " + getLoginName() +
                ", name : " + getFirstName() + " " + getLastName() +
                " }";
    }
}


