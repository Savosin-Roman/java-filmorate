package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.JdbcUserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({JdbcUserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {

    private final JdbcTemplate jdbcTemplate;
    private final JdbcUserDbStorage userStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM friends");
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM users");

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setLogin("testuser");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        testUser = userStorage.create(testUser);
    }

    @Test
    void testFindUserById_ShouldReturnUser() {
        Optional<User> userOptional = userStorage.findUserById(testUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(testUser.getId());
                    assertThat(user.getEmail()).isEqualTo("test@example.com");
                    assertThat(user.getLogin()).isEqualTo("testuser");
                    assertThat(user.getName()).isEqualTo("Test User");
                    assertThat(user.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    void testFindUserById_NotFound_ShouldReturnEmpty() {
        Optional<User> userOptional = userStorage.findUserById(999L);
        assertThat(userOptional).isEmpty();
    }

    @Test
    void testCreateUser_ShouldGenerateId() {
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setLogin("newuser");
        newUser.setName("New User");
        newUser.setBirthday(LocalDate.of(1995, 5, 5));

        User created = userStorage.create(newUser);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("new@example.com");
        assertThat(created.getLogin()).isEqualTo("newuser");
        assertThat(created.getName()).isEqualTo("New User");
        assertThat(created.getBirthday()).isEqualTo(LocalDate.of(1995, 5, 5));
    }

    @Test
    void testCreateUser_WithNullName_UsesLogin() {
        User newUser = new User();
        newUser.setEmail("noname@example.com");
        newUser.setLogin("noname");
        newUser.setName(null);
        newUser.setBirthday(LocalDate.of(1995, 5, 5));

        User created = userStorage.create(newUser);

        assertThat(created.getName()).isEqualTo("noname");
    }

    @Test
    void testCreateUser_WithBlankName_UsesLogin() {
        User newUser = new User();
        newUser.setEmail("blankname@example.com");
        newUser.setLogin("blankname");
        newUser.setName("");
        newUser.setBirthday(LocalDate.of(1995, 5, 5));

        User created = userStorage.create(newUser);

        assertThat(created.getName()).isEqualTo("blankname");
    }

    @Test
    void testCreateUser_DuplicateEmail_ShouldThrowException() {
        User duplicateUser = new User();
        duplicateUser.setEmail("test@example.com"); // Тот же email что и у testUser
        duplicateUser.setLogin("duplicate");
        duplicateUser.setName("Duplicate User");
        duplicateUser.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(Exception.class, () -> userStorage.create(duplicateUser));
    }

    @Test
    void testUpdateUser_ShouldUpdateAllFields() {
        testUser.setEmail("updated@example.com");
        testUser.setLogin("updateduser");
        testUser.setName("Updated User");
        testUser.setBirthday(LocalDate.of(1985, 5, 5));

        User updated = userStorage.update(testUser);

        assertThat(updated.getId()).isEqualTo(testUser.getId());
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getLogin()).isEqualTo("updateduser");
        assertThat(updated.getName()).isEqualTo("Updated User");
        assertThat(updated.getBirthday()).isEqualTo(LocalDate.of(1985, 5, 5));
    }

    @Test
    void testUpdateUser_WithNullName_UsesLogin() {
        testUser.setLogin("newlogin");
        testUser.setName(null);

        User updated = userStorage.update(testUser);

        assertThat(updated.getName()).isEqualTo("newlogin");
    }

    @Test
    void testUpdateUser_NotFound_ShouldThrowException() {
        User nonExistentUser = new User();
        nonExistentUser.setId(999L);
        nonExistentUser.setEmail("nonexistent@example.com");
        nonExistentUser.setLogin("nonexistent");
        nonExistentUser.setName("Non Existent");
        nonExistentUser.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(NotFoundException.class, () -> userStorage.update(nonExistentUser));
    }

    @Test
    void testDeleteUser_ShouldRemoveUser() {
        userStorage.delete(testUser.getId());

        Optional<User> found = userStorage.findUserById(testUser.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void testDeleteUser_NotFound_ShouldThrowException() {
        assertThrows(NotFoundException.class, () -> userStorage.delete(999L));
    }

    @Test
    void testDeleteUser_WithFriendsAndLikes_ShouldCascadeDelete() {

        User friend = createFriendUser();

        // Добавляем в друзья
        jdbcTemplate.update(
                "INSERT INTO friends (user_id, friend_id, confirmed) VALUES (?, ?, ?)",
                testUser.getId(), friend.getId(), true
        );
        jdbcTemplate.update(
                "INSERT INTO friends (user_id, friend_id, confirmed) VALUES (?, ?, ?)",
                friend.getId(), testUser.getId(), true
        );


        jdbcTemplate.update(
                "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                "Test Film", "Description", LocalDate.of(2020, 1, 1), 120, 1
        );
        jdbcTemplate.update(
                "INSERT INTO likes (film_id, user_id) VALUES (?, ?)",
                1L, testUser.getId()
        );

        userStorage.delete(testUser.getId());

        Integer friendsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friends WHERE user_id = ? OR friend_id = ?",
                Integer.class, testUser.getId(), testUser.getId()
        );
        assertThat(friendsCount).isZero();

        Integer likesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE user_id = ?",
                Integer.class, testUser.getId()
        );
        assertThat(likesCount).isZero();

        Optional<User> friendFound = userStorage.findUserById(friend.getId());
        assertThat(friendFound).isPresent();
    }

    @Test
    void testFindAll_WhenNoUsers_ShouldReturnEmptyList() {
        // Удаляем всех пользователей
        List<User> allUsers = userStorage.findAll();
        for (User user : allUsers) {
            userStorage.delete(user.getId());
        }

        List<User> users = userStorage.findAll();
        assertThat(users.isEmpty()).isTrue();
    }

    @Test
    void testFindById_ShouldReturnUser() {
        User found = userStorage.findById(testUser.getId());
        assertThat(found.getId()).isEqualTo(testUser.getId());
        assertThat(found.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testFindById_NotFound_ShouldThrowException() {
        assertThrows(NotFoundException.class, () -> userStorage.findById(999L));
    }

    @Test
    void testGetFriends_ShouldReturnFriends() {
        User friend = createFriendUser();

        addFriend(testUser.getId(), friend.getId());

        List<User> friends = userStorage.getFriends(testUser.getId());

        assertThat(friends.size()).isEqualTo(1);
        assertThat(friends.get(0).getId()).isEqualTo(friend.getId());
        assertThat(friends.get(0).getLogin()).isEqualTo("frienduser");
    }

    @Test
    void testGetFriends_WhenNoFriends_ShouldReturnEmptyList() {
        List<User> friends = userStorage.getFriends(testUser.getId());
        assertThat(friends.isEmpty()).isTrue();
    }

    @Test
    void testGetFriends_UserNotFound_ShouldThrowException() {
        assertThrows(NotFoundException.class, () -> userStorage.getFriends(999L));
    }

    @Test
    void testGetCommonFriends_ShouldReturnCommonFriends() {
        User commonFriend = createUser("common@example.com", "common", "Common Friend");
        User user2 = createUser("user2@example.com", "user2", "User Two");

        addFriend(testUser.getId(), commonFriend.getId());
        addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userStorage.getCommonFriends(testUser.getId(), user2.getId());

        assertThat(commonFriends.size()).isEqualTo(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(commonFriend.getId());
        assertThat(commonFriends.get(0).getLogin()).isEqualTo("common");
    }

    @Test
    void testGetCommonFriends_NoCommonFriends_ShouldReturnEmptyList() {
        User user2 = createUser("user2@example.com", "user2", "User Two");

        List<User> commonFriends = userStorage.getCommonFriends(testUser.getId(), user2.getId());

        assertThat(commonFriends.isEmpty()).isTrue();
    }

    @Test
    void testGetCommonFriends_FirstUserNotFound_ShouldThrowException() {
        User user2 = createUser("user2@example.com", "user2", "User Two");
        assertThrows(NotFoundException.class, () -> userStorage.getCommonFriends(999L, user2.getId()));
    }

    @Test
    void testGetCommonFriends_SecondUserNotFound_ShouldThrowException() {
        assertThrows(NotFoundException.class, () -> userStorage.getCommonFriends(testUser.getId(), 999L));
    }

    @Test
    void testFindUserById_WithNullId_ShouldReturnEmpty() {
        Optional<User> userOptional = userStorage.findUserById(null);
        assertThat(userOptional).isEmpty();
    }

    @Test
    void testCreateUser_WithDuplicateLogin_ShouldThrowException() {
        User duplicateUser = new User();
        duplicateUser.setEmail("different@example.com");
        duplicateUser.setLogin("testuser"); // Тот же логин что и у testUser
        duplicateUser.setName("Duplicate User");
        duplicateUser.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(Exception.class, () -> userStorage.create(duplicateUser));
    }

    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }

    private User createFriendUser() {
        User friend = new User();
        friend.setEmail("friend@example.com");
        friend.setLogin("frienduser");
        friend.setName("Friend User");
        friend.setBirthday(LocalDate.of(1995, 1, 1));
        return userStorage.create(friend);
    }

    private void addFriend(Long userId, Long friendId) {
        jdbcTemplate.update(
                "INSERT INTO friends (user_id, friend_id, confirmed) VALUES (?, ?, ?)",
                userId, friendId, true
        );
        jdbcTemplate.update(
                "INSERT INTO friends (user_id, friend_id, confirmed) VALUES (?, ?, ?)",
                friendId, userId, true
        );
    }
}