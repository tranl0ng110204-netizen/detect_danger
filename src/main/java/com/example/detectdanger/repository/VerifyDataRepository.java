package com.example.detectdanger.repository;

import com.example.detectdanger.entity.InputType;
import com.example.detectdanger.entity.VerifiedData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerifyDataRepository extends JpaRepository<VerifiedData,Long> {
    Optional<VerifiedData> findByInputTypeAndNormalizedValue(
            InputType inputType,
            String normalizedValue
    );

    boolean existsByInputTypeAndNormalizedValue(
            InputType inputType,
            String normalizedValue
    );
}
