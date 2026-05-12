package org.dam.search.backend.application.dtos.request;

import lombok.Data;

@Data
public class LoginRequest {
    String username;
    String password;
}
