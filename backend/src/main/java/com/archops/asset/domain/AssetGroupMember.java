package com.archops.asset.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "asset_group_member")
public class AssetGroupMember {

    @EmbeddedId
    private AssetGroupMemberId id = new AssetGroupMemberId();

    public AssetGroupMember() {}

    public AssetGroupMember(Long groupId, Long assetId) {
        this.id = new AssetGroupMemberId(groupId, assetId);
    }

    public AssetGroupMemberId getId() { return id; }
    public void setId(AssetGroupMemberId id) { this.id = id; }
    public Long getGroupId() { return id != null ? id.getGroupId() : null; }
    public Long getAssetId() { return id != null ? id.getAssetId() : null; }

    @Embeddable
    public static class AssetGroupMemberId implements Serializable {

        @Column(name = "group_id", nullable = false)
        private Long groupId;

        @Column(name = "asset_id", nullable = false)
        private Long assetId;

        public AssetGroupMemberId() {}

        public AssetGroupMemberId(Long groupId, Long assetId) {
            this.groupId = groupId;
            this.assetId = assetId;
        }

        public Long getGroupId() { return groupId; }
        public void setGroupId(Long groupId) { this.groupId = groupId; }
        public Long getAssetId() { return assetId; }
        public void setAssetId(Long assetId) { this.assetId = assetId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AssetGroupMemberId that)) return false;
            return Objects.equals(groupId, that.groupId) && Objects.equals(assetId, that.assetId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, assetId);
        }
    }
}
