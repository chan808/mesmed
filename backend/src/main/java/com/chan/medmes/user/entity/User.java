package com.chan.medmes.user.entity;

import com.chan.medmes.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public User(String username, String password, String displayName, UserRole role) {
        this.username = username;
        this.password = password;
        this.displayName = (displayName != null) ? displayName : username;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }
}
