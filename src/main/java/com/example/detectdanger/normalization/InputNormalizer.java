package com.example.detectdanger.normalization;

import com.example.detectdanger.entity.Enum.InputType;

public interface InputNormalizer {
    boolean supports(InputType inputType);
    String normalize(String input);

}
