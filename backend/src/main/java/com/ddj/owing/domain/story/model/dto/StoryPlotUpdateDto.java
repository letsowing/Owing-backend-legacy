package com.ddj.owing.domain.story.model.dto;

import jakarta.validation.constraints.NotBlank;

public record StoryPlotUpdateDto(
	@NotBlank String name,
	String description
) {
}