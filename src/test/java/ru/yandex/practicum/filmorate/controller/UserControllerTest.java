package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    private User user1;
    private User user2;
    private User commonFriend;


    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .email("test@example.com")
                .login("testLogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        user2 = User.builder()
                .email("friend@example.com")
                .login("friendLogin")
                .name("Friend Name")
                .birthday(LocalDate.of(1995, 2, 2))
                .build();
        commonFriend = User.builder()
                .email("common@example.com")
                .login("commonLogin")
                .name("Common Name")
                .birthday(LocalDate.of(2000, 3, 3))
                .build();
    }

    @Test
    void createUser_shouldReturnCreatedUser() {
        ResponseEntity<User> response = restTemplate.postForEntity(
                "/users", user1, User.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(user1.getEmail(), response.getBody().getEmail());
        assertEquals(user1.getLogin(), response.getBody().getLogin());
    }

    @Test
    void createUser_withEmptyName_shouldUseLoginAsName() {
        user1.setName("");

        ResponseEntity<User> response = restTemplate.postForEntity(
                "/users", user1, User.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(user1.getLogin(), response.getBody().getName());
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() {
        User createdUser = restTemplate.postForObject("/users", user1, User.class);
        createdUser.setName("Updated Name");

        ResponseEntity<User> response = restTemplate.exchange(
                "/users", HttpMethod.PUT, new HttpEntity<>(createdUser), User.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Name", response.getBody().getName());
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        restTemplate.postForEntity("/users", user1, User.class);

        ResponseEntity<List> response = restTemplate.getForEntity(
                "/users", List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void addFriend_shouldAddFriend() {
        User user1 = restTemplate.postForObject("/users", this.user1, User.class);
        User friend = restTemplate.postForObject("/users", this.user2, User.class);


        restTemplate.put("/users/{id}/friends/{friendId}", null, user1.getId(), friend.getId());

        ResponseEntity<List> friendsResponse = restTemplate.getForEntity(
                "/users/{id}/friends", List.class, user1.getId());

        assertEquals(HttpStatus.OK, friendsResponse.getStatusCode());
        assertNotNull(friendsResponse.getBody());
        assertEquals(1, friendsResponse.getBody().size());
    }

    @Test
    void removeFriend_shouldRemoveFriend() {
        User user1 = restTemplate.postForObject("/users", this.user1, User.class);
        User friend = restTemplate.postForObject("/users", this.user2, User.class);

        restTemplate.put("/users/{id}/friends/{friendId}", null, user1.getId(), friend.getId());
        restTemplate.delete("/users/{id}/friends/{friendId}", user1.getId(), friend.getId());

        ResponseEntity<List> friendsResponse = restTemplate.getForEntity(
                "/users/{id}/friends", List.class, user1.getId());

        assertEquals(HttpStatus.OK, friendsResponse.getStatusCode());
        assertTrue(friendsResponse.getBody().isEmpty());
    }

    @Test
    void getCommonFriends_shouldReturnCommonFriends() {
        User user1 = restTemplate.postForObject("/users", this.user1, User.class);
        User friend = restTemplate.postForObject("/users", this.user2, User.class);
        User commonFriend = restTemplate.postForObject("/users", this.commonFriend, User.class);

        restTemplate.put("/users/{id}/friends/{friendId}", null, user1.getId(), commonFriend.getId());
        restTemplate.put("/users/{id}/friends/{friendId}", null, friend.getId(), commonFriend.getId());

        ResponseEntity<List> commonFriendsResponse = restTemplate.getForEntity(
                "/users/{id}/friends/common/{otherId}", List.class, user1.getId(), friend.getId());

        assertEquals(HttpStatus.OK, commonFriendsResponse.getStatusCode());
        assertNotNull(commonFriendsResponse.getBody());
        assertEquals(1, commonFriendsResponse.getBody().size());
    }

    @Test
    void getUserFriends_shouldReturnFriendsList() {
        User user = restTemplate.postForObject("/users", this.user1, User.class);
        User friend = restTemplate.postForObject("/users", this.user2, User.class);

        restTemplate.put("/users/{id}/friends/{friendId}", null, user.getId(), friend.getId());

        ResponseEntity<List> response = restTemplate.getForEntity(
                "/users/{id}/friends", List.class, user.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

}