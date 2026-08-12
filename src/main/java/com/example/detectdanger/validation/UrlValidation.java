package com.example.detectdanger.validation;

import com.example.detectdanger.entity.InputType;
import org.springframework.stereotype.Component;

@Component
public class UrlValidation implements InputValidation {

    private static final int MAX_URL_LENGTH = 2048;

    @Override
    public boolean supports(InputType inputType){
        return inputType == InputType.URL;
    }

    @Override
    public void validate(String input){
        if (input.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException(
                    "URL must not exceed 2048 characters"
            );
        }

        if (!input.startsWith("http://")
                && !input.startsWith("https://")) {

            throw new IllegalArgumentException(
                    "URL must start with http:// or https://"
            );
        }
    }

}
