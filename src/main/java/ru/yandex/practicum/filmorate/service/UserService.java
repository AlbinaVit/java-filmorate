package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }


    public User addUser(User user) {
        setDefaultName(user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        setDefaultName(user);
        return userStorage.updateUser(user);
    }

    public User getUserById(long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    private void setDefaultName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public void addFriend(long userId, long friendId) {
        getUserById(userId);
        getUserById(friendId);

        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (friend == null || user == null) {
            throw new NotFoundException("Один из указанных пользователей не существует.");
        }

        userStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Один из указанных пользователей не существует.");
        }
        Set<Long> friendIds = userStorage.getFriends(userId);
        List<User> friends = friendIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
        log.info("Список друзей пользователя {}: {}", userId, friends);
        return friends;
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        return userStorage.getCommonFriends(userId, otherId);
    }

}
