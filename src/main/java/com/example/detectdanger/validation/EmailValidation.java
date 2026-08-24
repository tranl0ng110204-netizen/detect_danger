package com.example.detectdanger.validation;

import com.example.detectdanger.entity.Enum.InputType;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailValidation implements InputValidation {
    private static final int MAX_LENGTH = 254;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

    @Override
    public boolean supports(InputType inputType){
        return inputType == InputType.EMAIL;
    }

    @Override
    public void validate(String input){
        if(input.length() > MAX_LENGTH){
            throw new IllegalArgumentException("Email must not exceed 254 characters");
        }
        if(!EMAIL_PATTERN.matcher(input).matches()){
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}
