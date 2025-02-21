package controller.auth;


import model.authlogic.LoginService;
import model.entities.User;
import model.entities.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class LoginController {

        private final LoginService loginService ;

        @Autowired
        public LoginController(LoginService loginService) {
            this.loginService = loginService;
        }



        @PostMapping("/login")
        public ResponseEntity<?> loginUser(@RequestParam String username, @RequestParam String password){
            try {
                UserDTO loggedUser = loginService.loginUser(username, password);
                return ResponseEntity.ok(loggedUser); //se retorna el usuario ( unicamente su nombre e email )
            }catch (IllegalArgumentException e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }catch (Exception e) {
                return ResponseEntity.internalServerError().body("Error en el servidor");
            }
        }

}
