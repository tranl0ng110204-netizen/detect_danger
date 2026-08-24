package com.example.detectdanger.validation;

import com.example.detectdanger.entity.Enum.InputType;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PhoneValidation implements InputValidation {
    private static final int MAX_PHONE_LENGTH = 20;
    private static final Pattern PHONE_PATTERN =
            Pattern.compile(
                    "^\\+?[0-9]{7,20}$"
            );
    @Override
    public boolean supports(InputType inputType){
        return inputType == InputType.PHONE;
    }

    @Override
    public void validate(String input){

        if (input.length() > MAX_PHONE_LENGTH) {
            throw new IllegalArgumentException(
                    "Phone number is too long"
            );
        }

        if (!PHONE_PATTERN.matcher(input).matches()) {
            throw new IllegalArgumentException(
                    "Invalid phone number format"
            );
        }

    }

}
