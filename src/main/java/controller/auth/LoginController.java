package controller.auth;

import model.authlogic.Login;
import model.entities.User;

public class LoginController {
        private Login login ;

        public LoginController() {
           this.login = new Login();
        }

        public boolean login() {
            return login.loginUser();
        }

        public User getActualUser(){
            return login.getActualUser();
        }
}
