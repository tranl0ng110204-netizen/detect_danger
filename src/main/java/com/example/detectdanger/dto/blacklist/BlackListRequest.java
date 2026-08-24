package com.example.detectdanger.dto.blacklist;

import com.example.detectdanger.entity.Enum.InputType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BlackListRequest(
        @NotNull
        InputType inputType,

        @NotBlank
        @Size(max = 2000)
        String value,

        @NotBlank
        @Size(max = 500)
        String reason
) {
}
