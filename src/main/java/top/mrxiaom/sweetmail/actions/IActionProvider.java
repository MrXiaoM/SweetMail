package top.mrxiaom.sweetmail.actions;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@FunctionalInterface
public interface IActionProvider {
    /**
     * IAction 提供器，返回 null 代表字符串不匹配当前 Action
     */
    @Nullable
    IAction provide(String s);

    /**
     * 处理优先级，数字越小越先处理
     */
    default int priority() {
        return 1000;
    }
    
    static IActionProvider newProvider(int priority, Function<String, IAction> function) {
        return new IActionProvider() {
            @Override
            public @Nullable IAction provide(String s) {
                return function.apply(s);
            }

            @Override
            public int priority() {
                return priority;
            }
        };
    }
}
