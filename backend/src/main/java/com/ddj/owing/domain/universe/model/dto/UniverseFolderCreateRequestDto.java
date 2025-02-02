package com.ddj.owing.domain.universe.model.dto;

import com.ddj.owing.domain.universe.model.UniverseFolder;

public record UniverseFolderCreateRequestDto(
	String name,
	String description,
	Long projectId
) {

	public UniverseFolder toEntity() {

		return UniverseFolder.builder()
			.name(name)
			.description(description)
			.projectId(projectId)
			.build();
	}
}
