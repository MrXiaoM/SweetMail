package top.mrxiaom.sweetmail.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

public class ClassLoaderWrapper {
    @FunctionalInterface
    public interface DelegateAddURL {
        void run(URL url) throws Exception;
    }
    private final URLClassLoader classLoader;
    private final DelegateAddURL addURL;
    public ClassLoaderWrapper(URLClassLoader classLoader) {
        this.classLoader = classLoader;
        this.addURL = defineAddURLMethod();
    }

    @SuppressWarnings({"unchecked"})
    private DelegateAddURL defineAddURLMethod() {
        try {
            // 反射方法，直接调用 addURL
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            return url -> method.invoke(classLoader, url);
        } catch (Exception ignored) {
        }
        try {
            // unsafe 方法，拿到 URLClassPath 的 urls 和 path
            // 模仿 JDK 源码执行 addURL
            Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            Field fieldUCP = URLClassLoader.class.getDeclaredField("ucp");
            Object ucp = unsafe.getObject(classLoader, unsafe.objectFieldOffset(fieldUCP));
            Class<?> clazz = ucp.getClass();
            Field fieldUrls = defineUrlsField(clazz);
            Field fieldPath = clazz.getDeclaredField("path");
            if (fieldUrls != null) {
                Collection<URL> urls = (Collection<URL>) unsafe.getObject(ucp, unsafe.objectFieldOffset(fieldUrls));
                Collection<URL> path = (Collection<URL>) unsafe.getObject(ucp, unsafe.objectFieldOffset(fieldPath));
                return url -> {
                    synchronized (urls) {
                        urls.add(url);
                        path.add(url);
                    }
                };
            }
        } catch (Exception ignored) {
        }
        return url -> {
            throw new UnsupportedOperationException("当前环境不支持 addURL");
        };
    }
    private static Field defineUrlsField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals("unopenedUrls")) {
                return field;
            }
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals("urls")) {
                return field;
            }
        }
        return null;
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    public void addURL(URL url) throws Exception {
        addURL.run(url);
    }
}
