package com.ddj.owing.domain.story.model.dto.storyPlot;

import com.ddj.owing.domain.story.model.StoryPlot;

import lombok.Builder;

@Builder
public record StoryPlotDto(
	Long id,
	String name,
	String description,
	Integer position,
	int textCount
) {

	public static StoryPlotDto from(StoryPlot storyPlot) {
		return StoryPlotDto.builder()
			.id(storyPlot.getId())
			.name(storyPlot.getName())
			.description(storyPlot.getDescription())
			.position(storyPlot.getPosition())
			.textCount(storyPlot.getTextCount())
			.build();
	}

}
