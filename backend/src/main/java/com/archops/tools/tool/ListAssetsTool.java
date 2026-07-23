package com.archops.tools.tool;

import com.archops.asset.dto.AssetResponse;
import com.archops.asset.service.AssetService;
import com.archops.tools.AgentTool;
import com.archops.tools.ToolScope;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Lists managed assets; when conversation targets are set, only those assets are returned. */
@Component
public class ListAssetsTool implements AgentTool {

    private final AssetService assetService;

    public ListAssetsTool(AssetService assetService) {
        this.assetService = assetService;
    }

    @Override
    public String name() {
        return "list_assets";
    }

    @Override
    public String description() {
        return "List managed assets (servers, clusters, services) with IDs, names, hosts and kinds. "
                + "When the conversation has target assets/groups, only those assets are listed.";
    }

    @Override
    public String parametersJson() {
        return "{\"type\":\"object\",\"properties\":{}}";
    }

    @Override
    public String execute(Map<String, Object> arguments, ExecutionContext context) {
        List<AssetResponse> assets = assetService.list();
        Set<Long> allowed = ToolScope.allowedSet(context.targetAssetIds());
        if (!allowed.isEmpty()) {
            assets = assets.stream().filter(a -> allowed.contains(a.id())).toList();
        }
        if (assets.isEmpty()) {
            return allowed.isEmpty() ? "No assets registered." : "No assets in the current conversation target scope.";
        }
        return assets.stream()
                .map(a -> "- id=" + a.id() + " name=" + a.name() + " kind=" + a.kind()
                        + " host=" + (a.host() != null ? a.host() : "n/a"))
                .collect(Collectors.joining("\n"));
    }
}
