package ink.starblog.authservice.service;

import ink.starblog.authservice.model.User;
import ink.starblog.authservice.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class UserService {

    UserRepository userRepository;

    UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    Optional<User> findByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
