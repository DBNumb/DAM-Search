package org.dam.search.backend.application.rest;

import org.dam.search.backend.application.dtos.request.LoginRequest;
import org.dam.search.backend.application.dtos.response.BooleanResponse;
import org.dam.search.backend.domain.entities.User;
import org.dam.search.backend.domain.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController extends BaseCrudController<User, Long> {
    final UserService userService;

     public UserController(UserService userService) {
        super(userService);
         this.userService = userService;
     }

    @PostMapping("/login")
    public ResponseEntity<BooleanResponse> login(@RequestBody LoginRequest request) {
        boolean result = userService.loggin(request.getUsername(), request.getPassword());
        if(!result) {
            return ResponseEntity.status(401).body(BooleanResponse.builder().data(result).build());
        }
        return ResponseEntity.ok(BooleanResponse.builder().data(result).build());
    }
}
