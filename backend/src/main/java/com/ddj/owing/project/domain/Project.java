package com.ddj.owing.project.domain;

import com.ddj.owing.global.entity.BaseTimeEntity;
import com.ddj.owing.project.domain.enums.Category;
import com.ddj.owing.project.domain.enums.Genre;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private Category category;

    @Column
    private Genre genre;

    @Lob
    @Column
    private String coverImage;
}
