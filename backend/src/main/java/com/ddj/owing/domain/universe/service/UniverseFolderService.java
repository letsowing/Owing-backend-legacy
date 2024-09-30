package com.ddj.owing.domain.universe.service;

import com.ddj.owing.domain.universe.error.code.UniverseFolderErrorCode;
import com.ddj.owing.domain.universe.error.exception.UniverseFolderException;
import com.ddj.owing.domain.universe.model.UniverseFolder;
import com.ddj.owing.domain.universe.model.dto.UniverseFolderCreateDto;
import com.ddj.owing.domain.universe.repository.UniverseFolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniverseFolderService {

    private final UniverseFolderRepository universeFolderRepository;

    @Transactional
    public ResponseEntity<Void> createFolder(UniverseFolderCreateDto universeFolderCreateDto) {

        UniverseFolder universeFolder = universeFolderCreateDto.toEntity();
        universeFolderRepository.save(universeFolder);

        return ResponseEntity.ok().build();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<UniverseFolder>> getAllFolders() {

        return ResponseEntity.ok(universeFolderRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<UniverseFolder> getFolderById(Long id) {

        return ResponseEntity.ok(universeFolderRepository.findById(id)
                .orElseThrow(() -> UniverseFolderException.of(UniverseFolderErrorCode.UNIVERSE_FOLDER_NOT_FOUND)));
    }

    @Transactional
    public ResponseEntity<Void> deleteFolder(Long id) {

        UniverseFolder universeFolder = universeFolderRepository.findById(id)
                .orElseThrow(() -> UniverseFolderException.of(UniverseFolderErrorCode.UNIVERSE_FOLDER_NOT_FOUND));
        universeFolderRepository.delete(universeFolder);

        return ResponseEntity.ok().build();
    }
}
