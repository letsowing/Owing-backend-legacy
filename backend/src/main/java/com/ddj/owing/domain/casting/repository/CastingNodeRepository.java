package com.ddj.owing.domain.casting.repository;

import com.ddj.owing.domain.casting.model.CastingNode;
import com.ddj.owing.domain.casting.model.CastingRelationship;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("neo4jRepository")
public interface CastingNodeRepository extends Neo4jRepository<CastingNode, Long> {

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

    @Query("MATCH (n:Cast{label: $label}) WHERE n.deletedAt IS NULL RETURN n")
    Optional<CastingNode> findOneByName(String name);

    @Query("MATCH (n1:Cast)-[r:CONNECTION|BI_CONNECTION{uuid: 'string'}]-(n2:Cast) " +
            "DELETE r " +
            "RETURN count(DISTINCT r)")
    Integer deleteConnectionByUuid(String uuid);
}
