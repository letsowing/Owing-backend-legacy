package com.ddj.owing.domain.casting.repository;

import com.ddj.owing.domain.casting.model.CastingNode;
import com.ddj.owing.domain.casting.model.CastingRelationship;
import com.ddj.owing.domain.casting.model.dto.CastingRelationshipInfoDto;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CastingNodeRepository extends Neo4jRepository<CastingNode, Long> {

    @Query("MATCH (n1:Cast{id: $id}) " +
            "WHERE n1.deletedAt IS NULL " +
            "OPTIONAL MATCH (n1)-[r]-(n2) " +
            "WHERE n2 IS NULL OR n2.deletedAt IS NULL " +
            "RETURN n1, collect(r), collect(n2)")
    Optional<CastingNode> findById(Long id);

    @Query("MATCH (n1:Cast{id: $sourceId})-[r:CONNECTION{uuid: $uuid}]->(n2:Cast{id: $targetId}) " +
            "WHERE n1.deletedAt IS NULL AND n2.deletedAt IS NULL " +
            "SET r.label = $label " +
            "RETURN r.uuid AS uuid, r.label AS label, r.sourceId AS sourceId, r.targetId AS targetId, " +
            "r.sourceHandle AS sourceHandle, r.targetHandle AS targetHandle")
    Optional<CastingRelationship> updateDirectionalConnectionName(String uuid, Long sourceId, Long targetId, String label);

    @Query("MATCH (n1:Cast{id: $sourceId})-[r:BI_CONNECTION{uuid: $uuid}]-(n2:Cast{id: $targetId}) " +
            "WHERE n1.deletedAt IS NULL AND n2.deletedAt IS NULL " +
            "SET r.label = $label " +
            "RETURN r.uuid AS uuid, r.label AS label, r.sourceId AS sourceId, r.targetId AS targetId, " +
            "r.sourceHandle AS sourceHandle, r.targetHandle AS targetHandle")
    Optional<CastingRelationship> updateBidirectionalConnectionName(String uuid, Long sourceId, Long targetId, String label);

    @Query("MATCH (n1:Cast)-[r:CONNECTION|BI_CONNECTION{uuid: 'string'}]-(n2:Cast) " +
            "DELETE r " +
            "RETURN count(DISTINCT r)")
    Integer deleteConnectionByUuid(String uuid);

    @Query("MATCH (n1:Project{id: $projectId})-[r1:INCLUDED]->(n2:StoryPlot)-[r2:APPEARED]-(n3:Cast) " +
            "WHERE n1.deletedAt IS NULL " +
                "AND n2.deletedAt IS NULL " +
                "AND n3.deletedAt IS NULL " +
            "RETURN n3")
    List<CastingNode> findAllByProjectId(Long projectId);

    @Query("MATCH (n1:Project{id: $projectId})-[r1:INCLUDED]->(n2:StoryPlot)-[r2:APPEARED]-(n3:Cast)-[r3]-(n4:Cast) " +
            "WHERE n1.deletedAt IS NULL " +
                "AND n2.deletedAt IS NULL " +
                "AND n3.deletedAt IS NULL " +
                "AND n4.deletedAt IS NULL " +
            "RETURN DISTINCT " +
                "type(r3) as type, r3.uuid AS uuid, r3.label AS label, r3.sourceId AS sourceId,  " +
                "r3.targetId AS targetId, r3.sourceHandle AS sourceHandle, r3.targetHandle AS targetHandle")
    List<CastingRelationshipInfoDto> findAllConnectionByProjectId(Long projectId);


}
