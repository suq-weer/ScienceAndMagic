package team.xcmt.sam.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.NonNull;

import team.xcmt.sam.api.wood.WoodSet;

/**
 * 通用「模板展开」数据生成器：把一份与木种无关的 JSON 模板，替换 {@code %NS%} / {@code %WOOD%}
 * 后为每个木种输出一份文件。
 * <p>
 * blockstate / 配方 / 战利品表这些「形状与木种无关」的数据只需要一份模板，靠占位符区分布局与路径即可。
 */
public class TemplatedJsonProvider implements DataProvider {

    private final PackOutput.PathProvider pathProvider;
    private final Map<String, String> templates;
    private final String displayName;

    /**
     * @param pathProvider 输出路径提供器
     * @param templates    键为名称模板（{@code %s} 代表木种名），值为 JSON 模板
     * @param displayName  数据生成器名称
     */
    public TemplatedJsonProvider(PackOutput.PathProvider pathProvider, Map<String, String> templates,
            String displayName) {
        this.pathProvider = pathProvider;
        this.templates = templates;
        this.displayName = displayName;
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (WoodSet set : WoodSet.all()) {
            String namespace = set.material().namespace();
            for (Map.Entry<String, String> entry : this.templates.entrySet()) {
                String name = entry.getKey().replace("%s", set.material().name());
                JsonElement json = JsonParser.parseString(expand(entry.getValue(), set));
                futures.add(DataProvider.saveStable(cache, json,
                        this.pathProvider.json(Identifier.fromNamespaceAndPath(namespace, name))));
            }
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /** 替换模板中的命名空间与木种占位符。 */
    protected static String expand(String template, WoodSet set) {
        return template
                .replace("%NS%", set.material().namespace())
                .replace("%WOOD%", set.material().name());
    }

    @Override
    public @NonNull String getName() {
        return this.displayName;
    }
}
