package com.example.detectdanger.dto.scan;

import com.example.detectdanger.entity.Enum.InputType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScanRequest {
    @NotNull
    private InputType inputType;

    @NotBlank
    @Size(max = 500)
    private String content;
}
