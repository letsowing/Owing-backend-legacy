package com.ddj.owing.domain.storyBlock.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ddj.owing.domain.storyBlock.service.StoryBlockService;
import com.ddj.owing.domain.storyBlock.model.dto.StoryBlockCreateDto;
import com.ddj.owing.domain.storyBlock.model.dto.StoryBlockDto;
import com.ddj.owing.domain.storyBlock.model.dto.StoryBlockPositionUpdateDto;
import com.ddj.owing.domain.storyBlock.model.dto.StoryBlockUpdateDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/storyBlock")
@RequiredArgsConstructor
public class StoryBlockController {
	private final StoryBlockService storyBlockService;

	@GetMapping
	public ResponseEntity<List<StoryBlockDto>> getAllStories(@RequestParam Long plotId) {
		List<StoryBlockDto> stories = storyBlockService.getStoryBlockList(plotId);
		return ResponseEntity.ok(stories);
	}

	@GetMapping("/{id}")
	public ResponseEntity<StoryBlockDto> getStoryById(@PathVariable Long id) {
		StoryBlockDto storyFolder = storyBlockService.getStoryBlock(id);
		return ResponseEntity.ok(storyFolder);
	}

	@PostMapping
	public ResponseEntity<StoryBlockDto> createStory(@RequestBody StoryBlockCreateDto storyFolderDto) {
		StoryBlockDto createdStory = storyBlockService.createStoryBlock(storyFolderDto);
		return ResponseEntity.ok(createdStory);
	}

	@PutMapping("/{id}")
	public ResponseEntity<StoryBlockDto> updateStory(@PathVariable Long id,
		@RequestBody StoryBlockUpdateDto storyBlockUpdateDto) {
		StoryBlockDto updatedStory = storyBlockService.updateStoryBlock(id, storyBlockUpdateDto);
		return ResponseEntity.ok(updatedStory);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<StoryBlockDto> updateStoryBlockPosition(@PathVariable Long id,
		@RequestBody StoryBlockPositionUpdateDto storyBlockPositionUpdateDto) {
		StoryBlockDto updatedStory = storyBlockService.updateStoryBlockPosition(id, storyBlockPositionUpdateDto);
		return ResponseEntity.ok(updatedStory);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
		storyBlockService.deleteStoryBlock(id);
		return ResponseEntity.noContent().build();
	}
}
