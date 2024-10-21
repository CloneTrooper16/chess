package service;

import java.util.UUID;

public class AuthService {


    static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
