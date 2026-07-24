package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class User {
    private Long id;
    private Set<Long> friends  = new HashSet<>();
    private Set<Long> friendRequests = new HashSet<>();

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Логин не должен быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    public User(User other) {
        this.id = other.id;
        this.email = other.email;
        this.login = other.login;
        this.name = other.name;
        this.birthday = other.birthday;
        if (other.friends != null) {
            this.friends = new HashSet<>(other.friends);
        } else {
            this.friends = new HashSet<>();
        }
        if (other.friendRequests != null) {
            this.friendRequests = new HashSet<>(other.friendRequests);
        } else {
            this.friendRequests = new HashSet<>();
        }
    }

    public void addFriend(Long friendId) {
        if (friends == null) {
            friends = new HashSet<>();
        }
        friends.add(friendId);
    }

    public void removeFriend(Long friendId) {
        if (friends != null) {
            friends.remove(friendId);
        }
    }

    public boolean isFriend(Long userId) {
        return friends != null && friends.contains(userId);
    }

    public void addFriendRequest(Long userId) {
        if (friendRequests == null) {
            friendRequests = new HashSet<>();
        }
        friendRequests.add(userId);
    }

    public void removeFriendRequest(Long userId) {
        if (friendRequests != null) {
            friendRequests.remove(userId);
        }
    }

    public boolean hasFriendRequestFrom(Long userId) {
        return friendRequests != null && friendRequests.contains(userId);
    }
}
