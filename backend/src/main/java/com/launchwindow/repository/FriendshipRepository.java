package com.launchwindow.repository;

import com.launchwindow.model.Friendship;
import com.launchwindow.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("""
            SELECT friendship
            FROM Friendship friendship
            JOIN FETCH friendship.firstUser
            JOIN FETCH friendship.secondUser
            JOIN FETCH friendship.requester
            WHERE friendship.firstUser.id = :firstUserId
              AND friendship.secondUser.id = :secondUserId
            """)
    Optional<Friendship> findBetweenUsers(@Param("firstUserId") Long firstUserId, @Param("secondUserId") Long secondUserId);

    @Query("""
            SELECT friendship
            FROM Friendship friendship
            JOIN FETCH friendship.firstUser
            JOIN FETCH friendship.secondUser
            JOIN FETCH friendship.requester
            WHERE friendship.id = :friendshipId
              AND (
                    friendship.firstUser.id = :userId
                    OR friendship.secondUser.id = :userId
                  )
            """)
    Optional<Friendship> findForUser(@Param("friendshipId") Long friendshipId, @Param("userId") Long userId);

    @Query("""
            SELECT friendship
            FROM Friendship friendship
            JOIN FETCH friendship.firstUser
            JOIN FETCH friendship.secondUser
            JOIN FETCH friendship.requester
            WHERE (
                    friendship.firstUser.id = :userId
                    OR friendship.secondUser.id = :userId
                  )
              AND friendship.status = :status
            ORDER BY friendship.createdAt DESC,
                     friendship.id DESC
            """)
    List<Friendship> findAllForUser(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    @Query("""
            SELECT friendship
            FROM Friendship friendship
            JOIN FETCH friendship.firstUser
            JOIN FETCH friendship.secondUser
            JOIN FETCH friendship.requester
            WHERE (
                    friendship.firstUser.id = :userId
                    OR friendship.secondUser.id = :userId
                  )
              AND friendship.requester.id <> :userId
              AND friendship.status = :status
            ORDER BY friendship.createdAt DESC,
                     friendship.id DESC
            """)
    List<Friendship> findReceivedRequests(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    @Query("""
            SELECT friendship
            FROM Friendship friendship
            JOIN FETCH friendship.firstUser
            JOIN FETCH friendship.secondUser
            JOIN FETCH friendship.requester
            WHERE friendship.requester.id = :userId
              AND friendship.status = :status
            ORDER BY friendship.createdAt DESC,
                     friendship.id DESC
            """)
    List<Friendship> findSentRequests(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    @Query("""
            SELECT CASE
                       WHEN COUNT(friendship) > 0
                       THEN true
                       ELSE false
                   END
            FROM Friendship friendship
            WHERE friendship.firstUser.id = :firstUserId
              AND friendship.secondUser.id = :secondUserId
              AND friendship.status = :status
            """)
    boolean existsBetweenUsersWithStatus(@Param("firstUserId") Long firstUserId, @Param("secondUserId") Long secondUserId,
                                         @Param("status") FriendshipStatus status);

    @Query("""
        SELECT COUNT(friendship)
        FROM Friendship friendship
        WHERE friendship.status = :status
          AND (
                friendship.firstUser.id = :userId
                OR friendship.secondUser.id = :userId
              )
        """)
    long countForUserWithStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);
}