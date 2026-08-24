package com.example.detectdanger.validation;

import com.example.detectdanger.entity.Enum.InputType;

public interface InputValidation {
    boolean supports (InputType inputType);

    void validate(String input);
}
