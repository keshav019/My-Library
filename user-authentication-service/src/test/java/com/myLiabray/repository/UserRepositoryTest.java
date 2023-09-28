package com.myLiabray.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.myLiabray.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;



@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setPassword("password");
        user.setEnable(true);
        user.setToken("token");
        user.setCreatedTimeStamp(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        assertThat(savedUser.getUserId()).isNotNull();
    }

    @Test
    public void testReadUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setPassword("password");
        user.setEnable(true);
        user.setToken("token");
        user.setCreatedTimeStamp(LocalDateTime.now());

        User savedUser = entityManager.persist(user);

        Optional<User> retrievedUserOptional = userRepository.findById(savedUser.getUserId());
        assertThat(retrievedUserOptional).isPresent();

        User retrievedUser = retrievedUserOptional.get();
        assertThat(retrievedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setPassword("password");
        user.setEnable(true);
        user.setToken("token");
        user.setCreatedTimeStamp(LocalDateTime.now());

        User savedUser = entityManager.persist(user);

        savedUser.setEmail("updated@example.com");
        User updatedUser = userRepository.save(savedUser);

        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    public void testDeleteUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setPassword("password");
        user.setEnable(true);
        user.setToken("token");
        user.setCreatedTimeStamp(LocalDateTime.now());

        User savedUser = entityManager.persist(user);

        userRepository.deleteById(savedUser.getUserId());

        Optional<User> deletedUserOptional = userRepository.findById(savedUser.getUserId());
        assertThat(deletedUserOptional).isEmpty();
    }
}

