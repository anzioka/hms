package main.java.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class UserModule {
    private Module module;
    private BooleanProperty allowed;

    public UserModule(Module module, boolean allowed) {
        this.allowed = new SimpleBooleanProperty(allowed);
        this.module = module;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
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

    @Override
    public String toString() {
        return this.module.toString();
    }
}
