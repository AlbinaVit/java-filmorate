package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User addUser(User user) {
        setDefaultName(user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        setDefaultName(user);
        return userStorage.updateUser(user);
    }

    public User getUserById(long id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new ValidationException("Пользователь с id " + id + " не найден");
        }
        return userStorage.getUserById(id);
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
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (user == null || friend == null) {
            throw new ValidationException("Один из пользователей не найден");
        }
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (user == null || friend == null) {
            throw new ValidationException("Один из пользователей не найден");
        }
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Set<Long> getFriends(long userId) {
        return getUserById(userId).getFriends();
    }

    public Set<Long> getCommonFriends(long userId, long otherId) {
        Set<Long> userFriends = getFriends(userId);
        Set<Long> otherFriends = getFriends(otherId);
        userFriends.retainAll(otherFriends);
        return userFriends;
    }
}
