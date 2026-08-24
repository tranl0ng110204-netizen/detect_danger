package com.example.detectdanger.normalization;

import com.example.detectdanger.entity.Enum.InputType;
import org.springframework.stereotype.Component;

@Component
public class UrlNormalize implements InputNormalizer {
    @Override
    public boolean supports(InputType inputType){
        return inputType == InputType.URL;
    }

    @Override
    public String normalize(String input){
        return input.trim().toLowerCase();

    }

}
