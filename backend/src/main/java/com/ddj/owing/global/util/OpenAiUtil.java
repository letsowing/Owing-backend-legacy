package com.ddj.owing.global.util;

import java.util.*;

import com.ddj.owing.domain.casting.model.dto.casting.CastingSummaryDto;
import com.ddj.owing.global.error.code.OpenAiErrorCode;
import com.ddj.owing.global.error.exception.OpenAiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImageMessage;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.ResponseFormat;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.stereotype.Component;

import com.ddj.owing.domain.casting.model.dto.casting.CastingRequestDto;
import com.ddj.owing.domain.project.model.dto.ProjectRequestDto;
import com.ddj.owing.domain.universe.model.dto.UniverseFileRequestDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenAiUtil {

	private final ImageModel imageModel;
	private final ChatModel chatModel;
	private final ObjectMapper objectMapper;

	/**
	 * OpenAI API 를 이용해 이미지를 생성하는 메서드
	 *
	 * @param prompt 생성할 이미지에 대한 설명
	 * @return 생성된 이미지 URL
	 */
	public String createImage(String prompt) {

		ImageMessage imageMessage = new ImageMessage(prompt, 1.0f);
		OpenAiImageOptions imageOptions = OpenAiImageOptions.builder()
				.withModel(OpenAiImageApi.ImageModel.DALL_E_3.getValue())
				.withResponseFormat("b64_json")
				.withQuality("hd")
				.build();

		ImagePrompt imagePrompt = new ImagePrompt(imageMessage, imageOptions);
		ImageResponse response = imageModel.call(imagePrompt);

		if (response.getResults() != null && !response.getResults().isEmpty()) {
			return response.getResult().getOutput().toString();
		} else {
			throw OpenAiException.of(OpenAiErrorCode.IMAGE_GENERATION_FAIL);
		}
	}

	/**
	 * OpenAI API를 사용해 원고에 출연한 캐릭터를 추출하는 메서드
	 *
	 * @param prompt 원고와 캐릭터 정보가 담긴 프롬프트
	 * @return 추출된 캐릭터 요약 정보
	 */
	public List<CastingSummaryDto> extractCast(Prompt prompt) {
		ChatResponse chatResponse = chatModel.call(prompt);
		String extractResult = chatResponse.getResult().getOutput().getContent();
        try {
			Map<String, ArrayList<CastingSummaryDto>> mappedResponse =
					objectMapper.readValue(extractResult, new TypeReference<>() {});

			return mappedResponse.values().stream().findFirst()
					.orElseThrow(() -> OpenAiException.of(OpenAiErrorCode.CASTING_EXTRACT_FAIL));

        } catch (JsonProcessingException e) {
            throw OpenAiException.of(OpenAiErrorCode.CASTING_PARSE_FAIL);
        }
	}

	/**
	 * CastingRequestDto 를 기반으로 캐릭터 설명 프롬프트를 생성하는 메서드
	 *
	 * @param castingRequestDto 캐릭터 정보가 담긴 DTO
	 * @return 생성된 캐릭터 설명 프롬프트
	 */
	public String createPrompt(CastingRequestDto castingRequestDto) {

		return String.format(
			"다음 정보에 따라 자세한 인물 설명을 만드세요: 인물 이름: [%s] \n" +
				"나이: [%d] \n" +
				"성별: [%s] \n" +
				"직업/역할: [%s] \n" +
				"제공된 세부 정보: [%s] \n" +
				"특성: - 스타일: 사실적인 디테일이 있는 인물 \n" +
				"- 배경: 사실적이고 인물 직업 또는 역할과 관련이 있어야 합니다. \n" +
				"인물의 외모, 성격, 주변 환경에 초점을 맞춰 이러한 세부 사항을 통합한 설명 텍스트를 생생하고 몰입감 있는 인물 콘셉트를 만들어 주세요. \n" +
				"이때, 1개의 이미지만 나오도록 해주세요. \n" +
				"그리고 인물의 상반신이 사진을 대부분 차지하도록 출력해주세요.\n",
			castingRequestDto.name(),
			castingRequestDto.age(),
			castingRequestDto.gender(),
			castingRequestDto.role(),
			castingRequestDto.detail()
		);
	}

	/**
	 * ProjectRequestDto 를 기반으로 작품 표지 이미지를 위한 프롬프트를 생성하는 메서드
	 *
	 * @param projectRequestDto 작품 정보가 담긴 DTO
	 * @return 생성된 작품 표지 이미지 프롬프트
	 */
	public String createPrompt(ProjectRequestDto projectRequestDto) {

		return String.format(
				"다음 정보에 따라 작품의 표지 이미지를 만드세요. 작품의 분위기와 주요 내용을 시각적으로 표현해야 합니다. 다음은 작품의 정보입니다: "
						+
						"작품 제목: [%s] \n" +
						"작품 설명: [%s] \n" +
						"분류(Category): [%s] \n" +
						"장르(Genre): [%s] \n\n" +
						"이미지는 작품의 주인공이나 주요 캐릭터를 중심으로, 핵심 장면이나 스토리 전개, 전반적인 분위기, 그리고 작품의 주제를 모두 시각적으로 나타내야 합니다. \n" +
						"배경이나 상징적인 이미지 또한 포함하여, 작품의 주제와 분위기를 강하게 전달하세요. \n" +
						"이미지 스타일: 전반적으로 통일된 분위기와 색감을 유지하며, 현실적이면서도 작품의 장르와 분류에 맞는 예술적 디테일을 적용하세요. \n" +
						"밝고 선명한 색감을 주로 사용하되, 장르나 분류에 따라 어두운 색상도 적절히 혼합하세요. \n\n" +
						"제작된 표지 이미지는 독자의 관심을 끌 수 있도록 세밀하고 몰입감 있게 표현해주세요.",
				projectRequestDto.title(),
				projectRequestDto.description(),
				projectRequestDto.category(),
				projectRequestDto.genres().toString()
		);
	}

//	/**
//	 * ProjectRequestDto 를 기반으로 인생네컷 스타일의 4개 이미지로 작품 표지 이미지를 위한 프롬프트를 생성하는 메서드
//	 *
//	 * @param projectRequestDto 작품 정보가 담긴 DTO
//	 * @return 생성된 작품 표지 이미지 프롬프트
//	 */
//	public String createPrompt(ProjectRequestDto projectRequestDto) {
//
//		return String.format(
//			"다음 정보에 따라 작품의 표지 이미지를 만드세요. 총 4개의 이미지로, 각 이미지는 인생네컷 스타일로 제작되며, 작품의 분위기와 주요 내용을 시각적으로 표현해야 합니다. 다음은 작품의 정보입니다: "
//				+
//				"작품 제목: [%s] \n" +
//				"작품 설명: [%s] \n" +
//				"분류(Category): [%s] \n" +
//				"장르(Genre): [%s] \n\n" +
//				"각 이미지는 캐릭터, 배경, 주요 사건, 또는 작품의 분위기를 시각적으로 나타내야 합니다. \n" +
//				"첫 번째 이미지는 주인공이나 주요 캐릭터를 중심으로 사실적으로 묘사합니다. \n" +
//				"두 번째 이미지는 작품의 핵심 장면이나 스토리 전개를 시각화합니다. \n" +
//				"세 번째 이미지는 작품의 전반적인 분위기나 장르적 특성을 반영합니다. \n" +
//				"네 번째 이미지는 배경이나 상징적인 이미지를 활용하여 작품의 주제를 나타냅니다. \n" +
//				"이미지 스타일: 각 이미지가 인생네컷처럼 서로 연속된 분위기와 색감을 유지해야 하며, 전반적으로 현실적이면서도 작품의 장르와 분류에 맞는 예술적 디테일을 적용하세요. \n" +
//				"이미지들은 주로 밝고 선명한 색감을 사용하되, 장르나 분류에 따라 어두운 색상도 적절히 혼합하세요. \n\n" +
//				"제작된 표지 이미지는 독자의 관심을 끌 수 있도록 세밀하고 몰입감 있게 표현해주세요.",
//			projectRequestDto.title(),
//			projectRequestDto.description(),
//			projectRequestDto.category(),
//			projectRequestDto.genres().toString()
//		);
//	}

	/**
	 * UniverseFileRequestDto 를 기반으로 작품 세계관 일러스트레이션 이미지를 위한 프롬프트를 생성하는 메서드
	 *
	 * @param universeFileRequestDto 작품 정보가 담긴 DTO
	 * @return 생성된 작품 세계관 일러스트레이션 프롬프트
	 */
	public String createPrompt(UniverseFileRequestDto universeFileRequestDto) {

		return String.format(
			"다음 정보에 따라 작품의 표지 일러스트레이션 이미지를 만드세요. 작품의 분위기와 주요 내용을 시각적으로 표현해야 합니다. 다음은 작품의 정보입니다 " +
				"작품 제목: [%s] \n" +
				"작품 설명: [%s] \n" +
				"이미지는 캐릭터, 배경, 주요 사건, 또는 작품의 분위기를 시각적으로 나타내야 합니다. \n" +
				"이미지 스타일: 전반적으로 현실적이면서도 작품의 장르와 분류에 맞는 예술적 디테일을 적용하세요. \n" +
				"이미지는 주로 밝고 선명한 색감을 사용하되, 장르나 분류에 따라 어두운 색상도 적절히 혼합하세요. \n\n" +
				"이미지는 하나이고, 제작된 표지 이미지는 독자의 관심을 끌 수 있도록 세밀하고 몰입감 있게 표현해주세요.",
			universeFileRequestDto.title(),
			universeFileRequestDto.description()
		);
	}

	/**
	 * 원고에 출연한 캐릭터 추출을 위한 프롬프트를 생성하는 메서드
	 *
	 * @param storyPlotTextList 원고의 내용이 담긴 list
	 * @param castingSummaryList 프로젝트에 포함된 모든 캐릭터의 요약 정보(id, name, gender)
	 * @return 원고에 출연한 캐릭터 추출 프롬프트
	 */
	public Prompt creatPrompt(String storyPlotTextList, List<CastingSummaryDto> castingSummaryList) {
		String promptStr = String.format(
				"아래 <원고>를 꼼꼼히 읽고, <캐릭터 정보>를 참고하여 원고에 출연한 캐릭터의 정보를 JSON 리스트로 작성해주세요. " +
				"각 캐릭터의 id, name, gender 정보를 포함해야 합니다. 출력 양식은 <JSON list 예시>를 참고하세요. " +
				"필요한 정보는 다음과 같습니다: \n" +
					"<원고>: \n[%s]\n" +
					"<캐릭터 정보>: \n[%s]\n" +
					"<JSON list 예시>: \n[%s]\n" +
				"필수 준수 사항:\n" +
				"1. 출력해야 할 JSON 리스트는 위와 같은 형식을 따라야 한다.\n" +
				"2. 원고에 등장한 캐릭터만 JSON 리스트에 포함되어야 한다.\n" +
				"3. 캐릭터가 여러 번 등장하더라도 JSON 리스트에는 중복되지 않게 하나만 포함한다.\n" +
				"4. 원고에 등장하는 캐릭터가 없다면, 빈 JSON 리스트 ([])를 반환한다.",
				storyPlotTextList.toString(),
				castingSummaryList.toString(),
				"[ { \"id\": 1, \"name\": \"John Doe\", \"gender\": \"male\" }, { \"id\": 2, \"name\": \"Jane Doe\", \"gender\": \"female\" }, ... ]"
		);

		OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
				.withModel("gpt-4o-mini")
				.withTemperature(0.8F)
				.withResponseFormat(new ResponseFormat("json_object"))
				.build();
		Prompt prompt = new Prompt(promptStr, chatOptions);
		return prompt;
	}
}
