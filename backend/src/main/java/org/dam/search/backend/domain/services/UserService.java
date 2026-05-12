package org.dam.search.backend.domain.services;

import org.dam.search.backend.domain.entities.User;
import org.dam.search.backend.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseCRUDService<User, Long>{
    private final PasswordEncoder passwordEncoder;
    final UserRepository repository;
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        super(repository);
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
    }

    @Override
    public User create(User entity) {
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        return super.save(entity);
    }

    public boolean loggin(String username, String password) {
        var userOpt = repository.findByUsername(username);
        if (userOpt.isEmpty()) return false;
        var user = userOpt.get();
        return passwordEncoder.matches(password, user.getPassword());
    }
}
