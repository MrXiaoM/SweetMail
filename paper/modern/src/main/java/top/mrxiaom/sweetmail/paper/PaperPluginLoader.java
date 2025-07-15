package top.mrxiaom.sweetmail.paper;

import com.google.common.collect.Lists;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

@SuppressWarnings("UnstableApiUsage")
public class PaperPluginLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder builder) {
        List<String> dependencies = Lists.newArrayList(
                "net.kyori:adventure-api:4.22.0",
                "net.kyori:adventure-platform-bukkit:4.4.0",
                "net.kyori:adventure-text-serializer-gson:4.22.0",
                "net.kyori:adventure-text-minimessage:4.22.0"
        );
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        if (Locale.getDefault().getCountry().equals("CN")) {
            resolver.addRepository(new RemoteRepository.Builder("huawei", "default", "https://mirrors.huaweicloud.com/repository/maven/").build());
        } else {
            resolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());
        }
        for (String s : dependencies) {
            resolver.addDependency(new Dependency(new DefaultArtifact(s), null));
        }
        builder.addLibrary(resolver);
    }
}
