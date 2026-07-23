package com.archops.asset.repository;

import com.archops.asset.domain.AssetGroupMember;
import com.archops.asset.domain.AssetGroupMember.AssetGroupMemberId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssetGroupMemberRepository extends JpaRepository<AssetGroupMember, AssetGroupMemberId> {

    List<AssetGroupMember> findByIdGroupId(Long groupId);

    List<AssetGroupMember> findByIdGroupIdIn(Collection<Long> groupIds);

    long countByIdGroupId(Long groupId);

    @Modifying(clearAutomatically = true)
    @Query("delete from AssetGroupMember m where m.id.groupId = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    @Modifying(clearAutomatically = true)
    @Query("delete from AssetGroupMember m where m.id.groupId = :groupId and m.id.assetId = :assetId")
    void deleteByGroupIdAndAssetId(@Param("groupId") Long groupId, @Param("assetId") Long assetId);
}
