package model;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String userName;
    private String password;
    private List<Note> taskList;

    public User(String userName, String password) {
        this.userName = userName;
        this.password = hashPassword(password);
        taskList = new ArrayList<>();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = hashPassword(password);
    }

    public List<Note> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Note> taskList) {
        this.taskList = taskList;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
