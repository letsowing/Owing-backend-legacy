package com.ddj.owing.domain.casting.model;

import com.ddj.owing.global.entity.BaseTimeGraph;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Cast")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CastingNode extends BaseTimeGraph {
    @Id
    @NotNull
    private Long id;
    private String name;
    private Long age;
    private String gender;
    private String role;
    private String imageUrl;
    private Integer coordX;
    private Integer coordY;

    public void updateInfo(String name, Long age, String gender, String role, String imageUrl) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.role = role;
        this.imageUrl = imageUrl;
    }

    public void updateCoord(Integer coordX, Integer coordY) {
        this.coordX = coordX;
        this.coordY = coordY;
    }
}
