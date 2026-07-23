package com.archops.asset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.archops.asset.domain.AssetGroup;
import com.archops.asset.dto.AssetGroupRequest;
import com.archops.asset.repository.AssetGroupMemberRepository;
import com.archops.asset.repository.AssetGroupRepository;
import com.archops.asset.repository.AssetRepository;
import com.archops.audit.service.AuditService;
import com.archops.common.exception.BusinessException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssetGroupServiceTest {

    @Mock
    private AssetGroupRepository assetGroupRepository;
    @Mock
    private AssetGroupMemberRepository memberRepository;
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private AuditService auditService;

    private AssetGroupService service;

    @BeforeEach
    void setUp() {
        service = new AssetGroupService(assetGroupRepository, memberRepository, assetRepository, auditService);
    }

    @Test
    void createRejectsDuplicateName() {
        when(assetGroupRepository.existsByNameIgnoreCase("Hadoop")).thenReturn(true);
        assertThatThrownBy(() -> service.create(new AssetGroupRequest("Hadoop", null, true), 1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo("ASSET_GROUP_NAME_EXISTS");
    }

    @Test
    void deleteGroupDoesNotDeleteAssets() {
        AssetGroup group = new AssetGroup();
        group.setId(9L);
        group.setName("Hadoop");
        when(assetGroupRepository.findById(9L)).thenReturn(Optional.of(group));

        service.delete(9L, 1L, "admin");

        verify(assetGroupRepository).delete(group);
        verify(assetRepository, never()).delete(any());
        verify(assetRepository, never()).deleteById(any());
        verify(assetRepository, never()).deleteAll(any());
    }

    @Test
    void replaceMembersRejectsMissingAsset() {
        AssetGroup group = new AssetGroup();
        group.setId(1L);
        group.setName("Hadoop");
        when(assetGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(assetRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.replaceMembers(1L, List.of(99L), 1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getCode())
                .isEqualTo("ASSET_NOT_FOUND");
        verify(memberRepository, never()).deleteByGroupId(1L);
    }

    @Test
    void createPersistsTrimmedName() {
        when(assetGroupRepository.existsByNameIgnoreCase("Hadoop")).thenReturn(false);
        when(assetGroupRepository.save(any(AssetGroup.class))).thenAnswer(inv -> {
            AssetGroup g = inv.getArgument(0);
            g.setId(3L);
            return g;
        });

        var response = service.create(new AssetGroupRequest("  Hadoop  ", "cluster", true), 1L, "admin");

        ArgumentCaptor<AssetGroup> captor = ArgumentCaptor.forClass(AssetGroup.class);
        verify(assetGroupRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Hadoop");
        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.memberCount()).isZero();
    }
}
