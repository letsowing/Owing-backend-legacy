package com.ddj.owing.domain.story.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ddj.owing.domain.story.error.code.StoryBlockErrorCode;
import com.ddj.owing.domain.story.error.code.StoryPlotErrorCode;
import com.ddj.owing.domain.story.error.exception.StoryBlockException;
import com.ddj.owing.domain.story.error.exception.StoryPlotException;
import com.ddj.owing.domain.story.model.Content;
import com.ddj.owing.domain.story.model.StoryBlock;
import com.ddj.owing.domain.story.model.StoryPlot;
import com.ddj.owing.domain.story.model.dto.storyBlock.ContentDto;
import com.ddj.owing.domain.story.model.dto.storyBlock.StoryBlockCreateDto;
import com.ddj.owing.domain.story.model.dto.storyBlock.StoryBlockDto;
import com.ddj.owing.domain.story.model.dto.storyBlock.StoryBlockPositionUpdateDto;
import com.ddj.owing.domain.story.model.dto.storyBlock.StoryBlockUpdateDto;
import com.ddj.owing.domain.story.repository.StoryBlockRepository;
import com.ddj.owing.domain.story.repository.StoryPlotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryBlockService {
	private final StoryBlockRepository storyBlockRepository;
	private final StoryPlotRepository storyPlotRepository;
	// private final DailyTextCountRepository dailyTextCountRepository;

	private StoryBlock findById(Long id) {
		return storyBlockRepository.findById(id)
			.orElseThrow(() -> StoryBlockException.of(StoryBlockErrorCode.BLOCK_NOT_FOUND));
	}

	public List<StoryBlockDto> getStoryBlockList(Long plotId) {
		List<StoryBlock> storyBlockList = storyBlockRepository.findTopLevelBlocksByPlotId(plotId);
		return storyBlockList.stream().map(StoryBlockDto::from).toList();
	}

	public StoryBlockDto getStoryBlock(Long id) {
		StoryBlock block = findById(id);
		return StoryBlockDto.from(block);
	}

	public int getTextCount(List<Content> contents) {
		return contents == null ? 0 : contents.stream().mapToInt(content -> content.getText().length()).sum();
	}

	@Transactional
	public StoryBlockDto createStoryBlock(StoryBlockCreateDto storyBlockCreateDto) {
		StoryBlock parentBlock =
			storyBlockCreateDto.parentBlockId() != null ? findById(storyBlockCreateDto.parentBlockId()) : null;

		StoryPlot storyPlot = storyPlotRepository.findById(storyBlockCreateDto.storyPlotId())
			.orElseThrow(() -> StoryPlotException.of(StoryPlotErrorCode.PLOT_NOT_FOUND));

		Integer position = storyBlockRepository.findMaxOrderByStoryPlotId(storyBlockCreateDto.storyPlotId()) + 1;

		StoryBlock newBlock = storyBlockCreateDto.toEntity(storyPlot, parentBlock, position);
		int textCount = getTextCount(newBlock.getContent());

		storyPlot.updateTextCount(textCount);

		// LocalDate now = LocalDate.now();
		// LocalDateTime start = now.atStartOfDay();
		// LocalDateTime end = LocalTime.MAX.atDate(now);
		// DailyTextCount dailyTextCount = dailyTextCountRepository.findByCreatedAtBetween(start, end)
		// 	.orElseGet(() -> DailyTextCount.builder().build());
		// dailyTextCount.updateDailyTextCount(textCount);

		return StoryBlockDto.from(storyBlockRepository.save(newBlock));
	}

	@Transactional
	public StoryBlockDto updateStoryBlock(Long id, StoryBlockUpdateDto storyBlockUpdateDto) {
		// todo: projectId & permission check
		// todo: validation
		StoryBlock storyBlock = findById(id);
		List<Content> contents = storyBlockUpdateDto.contents().stream().map(ContentDto::toEntity).toList();
		int textCountDiff = getTextCount(contents) - getTextCount(storyBlock.getContent());

		storyBlock.update(storyBlockUpdateDto.type(), storyBlockUpdateDto.props(), contents);
		storyBlock.getStoryPlot().updateTextCount(textCountDiff);

		// LocalDate now = LocalDate.now();
		// LocalDateTime start = now.atStartOfDay();
		// LocalDateTime end = LocalTime.MAX.atDate(now);
		// DailyTextCount dailyTextCount = dailyTextCountRepository.findByCreatedAtBetween(start, end)
		// 	.orElseGet(() -> DailyTextCount.builder().build());
		// dailyTextCount.updateDailyTextCount(textCountDiff);

		return StoryBlockDto.from(storyBlockRepository.save(storyBlock));
	}

	@Transactional
	public void deleteStoryBlock(Long id) {
		StoryBlock storyBlock = findById(id);

		storyBlockRepository.decrementPositionAfter(storyBlock.getPosition(), storyBlock.getStoryPlot().getId());
		int textCount = -getTextCount(storyBlock.getContent());
		storyBlock.getStoryPlot().updateTextCount(textCount);
		//
		// LocalDate now = LocalDate.now();
		// LocalDateTime start = now.atStartOfDay();
		// LocalDateTime end = LocalTime.MAX.atDate(now);
		// DailyTextCount dailyTextCount = dailyTextCountRepository.findByCreatedAtBetween(start, end)
		// 	.orElseGet(() -> DailyTextCount.builder().build());
		// dailyTextCount.updateDailyTextCount(textCount);

		storyBlockRepository.deleteById(id);
	}

	@Transactional
	public StoryBlockDto updateStoryBlockPosition(Long id, StoryBlockPositionUpdateDto dto) {
		StoryBlock storyBlock = findById(id);

		StoryBlock oldParentBlock = storyBlock.getParentBlock();
		StoryBlock newParentBlock = findById(dto.parentBlockId());

		Integer oldPosition = storyBlock.getPosition();
		Integer newPosition = dto.position();

		if (oldParentBlock.getId().equals(dto.parentBlockId()) && oldPosition.equals(newPosition)) {
			return StoryBlockDto.from(storyBlock);
		}

		if (newPosition < 1 || newPosition > newParentBlock.getChildren().size() + 1) {
			throw StoryBlockException.of(StoryBlockErrorCode.INVALID_POSITION);
		}

		if (oldParentBlock.getId().equals(dto.parentBlockId())) {
			if (newPosition < oldPosition) {
				storyBlockRepository.decrementPositionBetween(oldPosition, newPosition, oldParentBlock.getId());
			} else {
				storyBlockRepository.incrementPositionBetween(newPosition, oldPosition - 1, oldParentBlock.getId());
			}
		} else {
			storyBlockRepository.decrementPositionAfter(oldPosition, oldParentBlock.getId());
			storyBlockRepository.incrementPositionAfter(newPosition, newParentBlock.getId());
			storyBlock.updateParentBlock(newParentBlock);
		}

		storyBlock.updatePosition(newPosition);
		return StoryBlockDto.from(storyBlockRepository.save(storyBlock));

	}
}