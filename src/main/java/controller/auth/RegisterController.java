package controller.auth;


import model.authlogic.Register;

public class RegisterController {
    private Register register;
    public RegisterController (){
        this.register = new Register();
    }

    public void register(){
        register.registerUser();
    }
}
