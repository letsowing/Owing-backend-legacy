package com.ddj.owing.domain.storyPlot.error.code;

import org.springframework.http.HttpStatus;

import com.ddj.owing.global.error.code.OwingErrorCode;

import lombok.Getter;

@Getter
public enum StoryPlotErrorCode implements OwingErrorCode {
	PLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "001", "플롯을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	StoryPlotErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = "Plot" + code;
		this.message = message;
	}
}
