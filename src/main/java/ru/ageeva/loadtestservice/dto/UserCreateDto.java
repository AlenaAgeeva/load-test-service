package ru.ageeva.loadtestservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreateDto(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String passport
) {}
