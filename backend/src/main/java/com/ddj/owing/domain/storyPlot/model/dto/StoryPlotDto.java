package com.ddj.owing.domain.storyPlot.model.dto;

import com.ddj.owing.domain.storyPlot.model.StoryPlot;

import lombok.Builder;

@Builder
public record StoryPlotDto(
	String name,
	String description,
	Integer position
) {

	public static StoryPlotDto from(StoryPlot storyPlot) {
		return StoryPlotDto.builder()
			.name(storyPlot.getName())
			.description(storyPlot.getDescription())
			.position(storyPlot.getPosition())
			.build();
	}

}
