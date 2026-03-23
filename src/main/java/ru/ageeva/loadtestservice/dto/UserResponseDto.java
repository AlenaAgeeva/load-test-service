package ru.ageeva.loadtestservice.dto;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String passport
) {}
