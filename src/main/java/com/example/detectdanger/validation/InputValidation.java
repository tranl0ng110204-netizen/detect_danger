package com.example.detectdanger.validation;

import com.example.detectdanger.entity.InputType;

public interface InputValidation {
    boolean supports (InputType inputType);

    void validate(String input);
}
