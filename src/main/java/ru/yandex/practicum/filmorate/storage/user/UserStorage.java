package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(long id);

    List<User> getAllUsers();

    void addFriend(long userId, long friendId);

    void removeFriend(long userId, long friendId);

    Set<Long> getFriends(long userId);

    List<User> getCommonFriends(long userId, long otherId);

}
