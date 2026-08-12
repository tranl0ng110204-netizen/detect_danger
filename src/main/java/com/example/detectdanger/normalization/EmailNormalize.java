package com.example.detectdanger.normalization;


import com.example.detectdanger.entity.InputType;
import org.springframework.stereotype.Component;

@Component
public class EmailNormalize implements InputNormalizer {
    @Override
    public boolean supports(InputType inputType){
        return inputType == InputType.EMAIL;
    }

    @Override
    public String normalize(String input){
        return input.trim()
                .toLowerCase();

    }
}
