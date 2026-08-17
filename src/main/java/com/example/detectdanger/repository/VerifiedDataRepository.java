package com.example.detectdanger.repository;

import com.example.detectdanger.entity.InputType;
import com.example.detectdanger.entity.VerifiedData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerifiedDataRepository extends JpaRepository<VerifiedData,Long> {
    boolean existsByInputTypeAndNormalizedValue(
            InputType inputType,
            String normalizedValue
    );
}
