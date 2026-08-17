package com.example.detectdanger.service;

import com.example.detectdanger.entity.InputType;
import com.example.detectdanger.validation.InputValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationService {
    private final List<InputValidation> validators;

    public String validate(InputType inputType, String content){
        InputValidation inputValidation = validators.stream()
                .filter(v->v.supports(inputType))
                .findFirst()
                .orElseThrow(() ->new IllegalArgumentException("Input type khong duoc ho tro"));

        inputValidation.validate(content);
        return content;
    }
}
