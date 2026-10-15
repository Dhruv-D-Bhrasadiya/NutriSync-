package com.nutrisync.repository;

import com.nutrisync.domain.Booking;
import com.nutrisync.domain.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b from Booking b join fetch b.client join fetch b.slot s join fetch s.nutritionist where b.client.id = :clientId order by b.createdAt desc")
    List<Booking> findAllForClient(@Param("clientId") Long clientId);

    @Query("select b from Booking b join fetch b.client join fetch b.slot s join fetch s.nutritionist where b.status = :status order by b.createdAt asc")
    List<Booking> findAllByStatusWithDetails(@Param("status") BookingStatus status);

    @Query("select b from Booking b join fetch b.client join fetch b.slot s join fetch s.nutritionist where b.slot.nutritionist.id = :nutritionistId and b.status = :status order by b.createdAt asc")
    List<Booking> findByNutritionistAndStatus(@Param("nutritionistId") Long nutritionistId, @Param("status") BookingStatus status);

    @Query("select b from Booking b join fetch b.client join fetch b.slot s join fetch s.nutritionist where b.id = :id")
    Optional<Booking> findDetailedById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findLockedById(@Param("id") Long id);
}
