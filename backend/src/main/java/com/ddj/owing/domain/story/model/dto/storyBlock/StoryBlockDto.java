package com.ddj.owing.domain.story.model.dto.storyBlock;

import java.util.List;
import java.util.Map;

import com.ddj.owing.domain.story.model.StoryBlock;

import lombok.Builder;

@Builder
public record StoryBlockDto(
	Long id,
	String type,
	Map<String, Object> props,
	List<ContentDto> content,
	Long parentBlockId,
	Integer position,
	Long storyPlotId,
	List<StoryBlockDto> children
) {

	public static StoryBlockDto from(StoryBlock storyBlock) {
		return StoryBlockDto.builder()
			.id(storyBlock.getId())
			.type(storyBlock.getType())
			.props(storyBlock.getProps())
			.content(storyBlock.getContent() == null ? null :
				storyBlock.getContent().stream().map(ContentDto::from).toList())
			.parentBlockId(storyBlock.getParentBlock() == null ? null : storyBlock.getParentBlock().getId())
			.position(storyBlock.getPosition())
			.storyPlotId(storyBlock.getStoryPlot().getId())
			.children(storyBlock.getChildren().stream().map(StoryBlockDto::from).toList())
			.build();
	}

}
