package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
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

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(long userId) {
        Set<Long> friendIds = getUserById(userId).getFriends();
        List<User> friends = friendIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
        log.info("Список друзей пользователя {}: {}", userId, friends);
        return friends;
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        Set<Long> userFriends = getUserById(userId).getFriends();
        Set<Long> otherFriends = getUserById(otherId).getFriends();

        Set<Long> commonFriendIds = userFriends.stream()
                .filter(otherFriends::contains)
                .collect(Collectors.toSet());

        List<User> commonFriends = commonFriendIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());

        log.info("Общие друзья между {} и {}: {}", userId, otherId, commonFriends);
        return commonFriends;
    }
}
