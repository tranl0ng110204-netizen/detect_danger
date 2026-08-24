package com.example.detectdanger.repository;

import com.example.detectdanger.entity.BlackList;
import com.example.detectdanger.entity.Enum.InputType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlackListRepository extends JpaRepository<BlackList,Long> {
    boolean existsByInputTypeAndNormalizedValueAndActiveTrue(
            InputType inputType,
            String normalizedValue
    );

    Optional<BlackList>
    findByInputTypeAndNormalizedValueAndActiveTrue(
            InputType inputType,
            String normalizedValue
    );

    List<BlackList>
    findByInputTypeAndActiveTrue(
            InputType inputType
    );
}
