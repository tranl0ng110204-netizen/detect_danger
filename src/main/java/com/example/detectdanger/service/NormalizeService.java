package com.example.detectdanger.service;

import com.example.detectdanger.entity.Enum.InputType;
import com.example.detectdanger.normalization.InputNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NormalizeService {
    private final List<InputNormalizer> normalizers;

    public String normalize(InputType inputType, String input){
        InputNormalizer inputNormalizer = normalizers.stream()
                .filter(n->n.supports(inputType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unsupported input type"));
        return inputNormalizer.normalize(input);
    }
}
