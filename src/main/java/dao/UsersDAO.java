package main.java.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.java.model.*;
import main.java.util.DBUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alfonce on 20/04/2017.
 */
public class UsersDAO {

    //search a user by username;

    public static ObservableList<User> getUserObservableList(String sql) {
        ObservableList<User> list = FXCollections.observableArrayList();

        ResultSet resultSet = DBUtil.executeQuery(sql);
        if (resultSet != null) {
            getUsersFromResultSet(list, resultSet);
        }
        return list;
    }

    private static void getUsersFromResultSet(ObservableList<User> list, ResultSet resultSet) {
        try {
            while (resultSet.next()) {
                User user = new User();
                user.setLoginName(resultSet.getString("UserName"));
                user.setUserId(resultSet.getInt("ID"));
                user.setFirstName(resultSet.getString("FirstName"));
                user.setLastName(resultSet.getString("LastName"));
                user.setCategory(UserCategory.valueOf(resultSet.getString("UserCategory")));
                user.setDateCreated(resultSet.getObject("DateCreated", LocalDate.class));
                list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static User getUser(String getUserSql) {
        ObservableList<User> list = FXCollections.observableArrayList();
        ResultSet resultSet = DBUtil.executeQuery(getUserSql);
        if (resultSet != null) {
            getUsersFromResultSet(list, resultSet);
        }

        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public static ObservableList<UserModule> getUserModules(int userId) {
        ObservableList<UserModule> list = FXCollections.observableArrayList();
        if (userId == 1) {
            //mysql root user can access all modules
            for (Module module : Module.values()) {
                list.add(new UserModule(module, true));
            }
            return list;
        }

        String sql = "select * from user_modules where userId = " + userId;
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    list.add(new UserModule(Module.valueOf(resultSet.getString("module")), resultSet
                            .getBoolean("allowed")));

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;

    }

    public static int getNextUserId() {
        String sql = "select id from users order by id desc limit 1";
        try {
            ResultSet resultSet = DBUtil.executeQuery(sql);
            if (resultSet != null && resultSet.next()) {
                return resultSet.getInt("id") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;

    }



    public static Map<Permission, Boolean> getUserPermissionsMap(int userId) {
        Map<Permission, Boolean> userPermissionsMap = new HashMap<>();
        if (userId == 1) {
            //mysql root user has all permissions
            for (Permission permission : Permission.values()) {
                userPermissionsMap.put(permission, true);
            }
            return userPermissionsMap;
        }

        ResultSet resultSet = DBUtil.executeQuery("select * from user_permissions where UserId = " + userId);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    userPermissionsMap.put(Permission.valueOf(resultSet.getString("permission")), resultSet.getBoolean("value"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return userPermissionsMap;
    }

    public static ObservableList<UserPermission> getUserPermissions(int userId) {
        ObservableList<UserPermission> list = FXCollections.observableArrayList();
        ResultSet resultSet = DBUtil.executeQuery("select * from user_permissions " +
                "where UserId = " + userId);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    UserPermission userPermission = new UserPermission(Permission.valueOf(resultSet.getString("permission")), resultSet.getBoolean("value"));
                    list.add(userPermission);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
