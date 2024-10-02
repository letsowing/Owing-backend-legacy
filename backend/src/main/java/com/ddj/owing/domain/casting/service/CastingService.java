package com.ddj.owing.domain.casting.service;

import com.ddj.owing.domain.casting.error.code.CastingErrorCode;
import com.ddj.owing.domain.casting.error.exception.CastingException;
import com.ddj.owing.domain.casting.model.Casting;
import com.ddj.owing.domain.casting.model.CastingNode;
import com.ddj.owing.domain.casting.model.CastingRelationship;
import com.ddj.owing.domain.casting.model.ConnectionType;
import com.ddj.owing.domain.casting.model.dto.*;
import com.ddj.owing.domain.casting.repository.CastingNodeRepository;
import com.ddj.owing.domain.casting.repository.CastingRepository;
import com.ddj.owing.global.util.OpenAiUtil;
import com.ddj.owing.global.util.Parser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CastingService {

    private final CastingRepository castingRepository;
    private final OpenAiUtil openAiUtil;

    private final CastingNodeRepository castingNodeRepository;

    @Transactional
    public ResponseEntity<String> generateCharacterImage(CastingRequestDto castingRequestDto) {

        String prompt = openAiUtil.createPrompt(castingRequestDto);
        String jsonString = openAiUtil.createImage(prompt);
        String imageUrl = Parser.extractUrl(jsonString);
        Casting casting = castingRequestDto.toEntity(imageUrl);

        castingRepository.save(casting);

        return ResponseEntity.ok(imageUrl);
    }

    public CastingDto getCasting(Long id) {
        Casting casting = castingRepository.findById(id)
                .orElseThrow(() -> CastingException.of(CastingErrorCode.CASTING_NOT_FOUND));
        return CastingDto.from(casting);
    }

    @Transactional
    public CastingDto createCasting(CastingCreateDto castingCreateDto) {
        Casting casting = castingCreateDto.toEntity();
        Casting savedCasting = castingRepository.save(casting);

        CastingNode castingNode = castingCreateDto.toNodeEntity(savedCasting);
        CastingNode savedCastingNode = castingNodeRepository.save(castingNode);

        return CastingDto.from(savedCastingNode);
    }

    @Transactional
    public CastingDto updateCastingInfo(Long id, CastingInfoUpdateDto castingInfoUpdateDto) {
        Casting casting = castingRepository.findById(id)
                .orElseThrow(() -> CastingException.of(CastingErrorCode.CASTING_NOT_FOUND));
        casting.updateInfo(
                castingInfoUpdateDto.name(),
                castingInfoUpdateDto.age(),
                castingInfoUpdateDto.name(),
                castingInfoUpdateDto.role(),
                castingInfoUpdateDto.detail(),
                castingInfoUpdateDto.imageUrl()
        );

        CastingNode castingNode = castingNodeRepository.findById(id)
                .orElseThrow(() -> CastingException.of(CastingErrorCode.CASTING_NODE_NOT_FOUND));
        castingNode.updateInfo(
                castingInfoUpdateDto.name(),
                castingInfoUpdateDto.age(),
                castingInfoUpdateDto.name(),
                castingInfoUpdateDto.role(),
                castingInfoUpdateDto.imageUrl()
        );
        CastingNode updatedCastingNode = castingNodeRepository.save(castingNode);

        return CastingDto.from(updatedCastingNode);
    }

    @Transactional
    public CastingDto updateCastingCoord(Long id, CastingCoordUpdateDto coordUpdateDto) {
        Casting casting = castingRepository.findById(id)
                .orElseThrow(() -> CastingException.of(CastingErrorCode.CASTING_NOT_FOUND));
        casting.updateCoord(
                coordUpdateDto.coordX(),
                coordUpdateDto.coordY()
        );

        CastingNode castingNode = castingNodeRepository.findById(id)
                .orElseThrow(() -> CastingException.of(CastingErrorCode.CASTING_NODE_NOT_FOUND));
        castingNode.updateCoord(
                coordUpdateDto.coordX(),
                coordUpdateDto.coordY()
        );
        CastingNode updatedCastingNode = castingNodeRepository.save(castingNode);

        return CastingDto.from(updatedCastingNode);
    }

    @Transactional
    public void deleteCasting(Long id) {
        castingRepository.deleteById(id);
        CastingNode castingNode = castingNodeRepository.findById(id)
                .orElseThrow(() -> CastingException.of(CastingErrorCode.CASTING_NODE_NOT_FOUND));
        castingNode.delete();
        castingNodeRepository.save(castingNode);
    }

    /**
     * Casting간 Relation을 생성하는 메서드
     *
     * @param connectionCreateDto connectionType()을 통해 단방향, 양방향 지정
     * @return
     */
    @Transactional
    public CastingRelationshipDto createConnection(CastingConnectionCreateDto connectionCreateDto) {
        CastingNode fromCasting = castingNodeRepository.findById(connectionCreateDto.fromId())
                .orElseThrow(() -> CastingException.of(CastingErrorCode.CASTING_NOT_FOUND));
        CastingNode toCasting = castingNodeRepository.findById(connectionCreateDto.toId())
                .orElseThrow(() -> CastingException.of(CastingErrorCode.CASTING_NOT_FOUND));

        fromCasting.addConnection(connectionCreateDto.uuid(), toCasting, connectionCreateDto.name());
        if (ConnectionType.BIDIRECTIONAL.equals(connectionCreateDto.connectionType())) {
            toCasting.addConnection(connectionCreateDto.uuid(), fromCasting, connectionCreateDto.name());
        }

        CastingNode updatedCastingNode = castingNodeRepository.save(fromCasting);
        Set<CastingRelationship> outConnections = updatedCastingNode.getOutConnections();
        CastingRelationship castingRelationship = outConnections.stream()
                .filter(connection -> connection.getCastingNode().getId().equals(connectionCreateDto.toId()))
                .filter(connection -> connection.getName().equals(connectionCreateDto.name()))
                .findFirst()
                .orElseThrow(() -> CastingException.of(CastingErrorCode.CONNECTION_NOT_FOUND));

        return new CastingRelationshipDto(
                castingRelationship.getUuid(),
                connectionCreateDto.fromId(),
                connectionCreateDto.toId(),
                connectionCreateDto.connectionType()
        );
    }

    /**
     * connectionType에 따라 단방향, 양방향 관계 이름을 변경.
     * @param uuid
     * neo4j에 쿼리를 직접 실행하기 때문에 사용하지 않음.
     * 추후 객체 수준에서 이름을 변경할 수 있다면 사용.
     * @param connectionUpdateDto
     * @return
     * 관계 id, 시작객체 id, 끝객체 id, connectionType이 포함된 CastingRelationshipDto
     */
    @Transactional
    public CastingRelationshipDto updateConnectionName(String uuid, CastingConnectionUpdateDto connectionUpdateDto) {
        CastingNode fromCasting = castingNodeRepository.findById(connectionUpdateDto.fromId())
                .orElseThrow(() -> CastingException.of(CastingErrorCode.CASTING_NOT_FOUND));
        CastingNode toCasting = castingNodeRepository.findById(connectionUpdateDto.toId())
                .orElseThrow(() -> CastingException.of(CastingErrorCode.CASTING_NOT_FOUND));

        switch (connectionUpdateDto.connectionType()) {
            case ConnectionType.DIRECTIONAL ->
                    castingNodeRepository.updateDirectionalConnectionName(fromCasting.getId(), toCasting.getId(), connectionUpdateDto.name());
            case ConnectionType.BIDIRECTIONAL ->
                    castingNodeRepository.updateBidirectionalConnectionName(fromCasting.getId(), toCasting.getId(), connectionUpdateDto.name());
        }

        return new CastingRelationshipDto(
                uuid,
                connectionUpdateDto.fromId(),
                connectionUpdateDto.toId(),
                connectionUpdateDto.connectionType()
        );
    }
}
