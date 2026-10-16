package com.nutrisync.repository;

import com.nutrisync.domain.AvailabilitySlot;
import com.nutrisync.domain.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    @Query("select s from AvailabilitySlot s join fetch s.nutritionist where s.status = :status and s.startsAt > :now order by s.startsAt asc")
    List<AvailabilitySlot> findOpenFutureWithNutritionist(@Param("status") SlotStatus status, @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AvailabilitySlot s where s.id = :id")
    Optional<AvailabilitySlot> findLockedById(@Param("id") Long id);

    @Query("select case when count(s) > 0 then true else false end from AvailabilitySlot s " +
           "where s.nutritionist.id = :nutritionistId and s.startsAt < :end and s.endsAt > :start")
    boolean hasOverlappingSlot(@Param("nutritionistId") Long nutritionistId,
                               @Param("start") Instant start, @Param("end") Instant end);

    @Query("select s from AvailabilitySlot s join fetch s.nutritionist where s.nutritionist.id = :nutritionistId order by s.startsAt asc")
    List<AvailabilitySlot> findAllForNutritionist(@Param("nutritionistId") Long nutritionistId);

    @Query("select s from AvailabilitySlot s join fetch s.nutritionist order by s.startsAt desc")
    List<AvailabilitySlot> findAllWithNutritionistOrderByStartsAtDesc();

    long countByStatus(SlotStatus status);
    long countByStartsAtAfter(Instant instant);
}
