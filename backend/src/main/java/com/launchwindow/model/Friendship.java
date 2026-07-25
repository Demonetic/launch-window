package com.launchwindow.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "friendship",
        uniqueConstraints = @UniqueConstraint(name = "uk_friendship_user_pair", columnNames = {"first_user_id", "second_user_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "first_user_id", nullable = false)
    private AppUser firstUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "second_user_id", nullable = false)
    private AppUser secondUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private AppUser requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendshipStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    public Friendship(AppUser requester, AppUser recipient) {
        validateUsers(requester, recipient);

        if (requester.getId() < recipient.getId()) {
            this.firstUser = requester;
            this.secondUser = recipient;
        } else {
            this.firstUser = recipient;
            this.secondUser = requester;
        }

        this.requester = requester;
        this.status = FriendshipStatus.PENDING;
    }

    public void accept(Instant respondedAt) {
        this.status = FriendshipStatus.ACCEPTED;
        this.respondedAt = respondedAt;
    }

    public void decline(Instant respondedAt) {
        this.status = FriendshipStatus.DECLINED;
        this.respondedAt = respondedAt;
    }

    public AppUser getOtherUser(Long userId) {
        if (Objects.equals(firstUser.getId(), userId)) {
            return secondUser;
        }

        if (Objects.equals(secondUser.getId(), userId)) {
            return firstUser;
        }

        throw new IllegalArgumentException("User is not part of this friendship");
    }

    public boolean wasRequestedBy(Long userId) {
        return Objects.equals(requester.getId(), userId);
    }

    private void validateUsers(AppUser requester, AppUser recipient) {
        if (requester == null || recipient == null || requester.getId() == null || recipient.getId() == null) {
            throw new IllegalArgumentException("Friendship users must be persisted");
        }

        if (Objects.equals(requester.getId(), recipient.getId())) {
            throw new IllegalArgumentException("A user cannot befriend themselves");
        }
    }
}