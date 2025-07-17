package top.mrxiaom.sweetmail.paper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.bukkit.plugin.PluginDescriptionFile;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("UnstableApiUsage")
public class PaperPluginLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder builder) {
        PluginMeta configuration = builder.getContext().getConfiguration();
        if (!(configuration instanceof PluginDescriptionFile meta)) {
            applyFailed(builder);
            return;
        }
        List<String> dependencies = Lists.newArrayList(meta.getLibraries());
        try {
            // 清空原有依赖
            Field field = PluginDescriptionFile.class.getDeclaredField("libraries");
            field.setAccessible(true);
            field.set(meta, ImmutableList.of());
        } catch (Throwable t) {
            applyFailed(builder);
            return;
        }
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        if (Locale.getDefault().getCountry().equals("CN")) {
            resolver.addRepository(new RemoteRepository.Builder("huawei", "default", "https://mirrors.huaweicloud.com/repository/maven/").build());
            builder.getContext().getLogger().info("已应用镜像仓库地址");
        } else {
            resolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());
        }
        for (String s : dependencies) {
            resolver.addDependency(new Dependency(new DefaultArtifact(s), null));
        }
        builder.addLibrary(resolver);
    }

    private void applyFailed(PluginClasspathBuilder builder) {
        builder.getContext().getLogger().warn("无法应用镜像仓库地址，依赖下载可能较慢");
    }
}
