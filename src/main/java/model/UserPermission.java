package main.java.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Objects;

/**
 * Created by alfonce on 29/06/2017.
 */
public class UserPermission {
    private Permission permission;
    private BooleanProperty allowed;


    public UserPermission(Permission permission, boolean allowed) {
        this.allowed = new SimpleBooleanProperty(allowed);
        this.permission = permission;
    }

    @Override
    public String toString() {
        return permission.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof UserPermission) {
            return (Objects.equals(this.getPermission(), ((UserPermission) object).getPermission()));
        }
        return false;
    }


    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public boolean isAllowed() {
        return allowed.get();
    }

    public void setAllowed(boolean allowed) {
        this.allowed.set(allowed);
    }

    public BooleanProperty allowedProperty() {
        return allowed;
    }
}


