package com.agrocenter.bff.dto.inventory;

import jakarta.validation.constraints.NotNull;

public record ProductStateRequest(@NotNull Boolean activo) {
}
