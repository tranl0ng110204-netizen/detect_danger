package com.example.detectdanger.normalization;

import com.example.detectdanger.entity.Enum.InputType;
import org.springframework.stereotype.Component;

@Component
public class MessageNormalize implements InputNormalizer {
    @Override
    public boolean supports(InputType inputType){
        return inputType == InputType.MESSAGE;
    }

    @Override
    public String normalize(String input){
        return input.strip()
                .replaceAll("\\p{Z}++", " ")
                .toLowerCase();


    }
}
