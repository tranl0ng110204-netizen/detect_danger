package com.example.detectdanger.validation;

import com.example.detectdanger.entity.Enum.InputType;
import org.springframework.stereotype.Component;

@Component
public class MessageValidation implements InputValidation {
    private static final int MAX_MESSAGE_LENGTH = 5000;


    @Override
    public boolean supports(InputType inputType){
        return inputType == InputType.MESSAGE;
    }

    @Override
    public void validate(String input){
        if (input.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                    "Message must not exceed 5000 characters"
            );
        }

    }
}
