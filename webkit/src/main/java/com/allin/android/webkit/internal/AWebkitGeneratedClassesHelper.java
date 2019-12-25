package com.allin.android.webkit.internal;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@Keep
public class AWebkitGeneratedClassesHelper {

    /**
     * get generated classes by AWebkit Annotation Compiler, which is implemented
     * {@link com.allin.android.webkit.api.JavascriptApiCollector}
     * or {@link com.allin.android.webkit.api.LifecycleRegistrant}
     * <p>
     * Note: method: getGeneratedClassesByAWebkit() generate by AWebkit Gradle Plugin
     */
    @Keep
    @Nullable
    @SuppressWarnings("JavaReflectionMemberAccess")
    public static Set<String> listClasses() {
        Class<AWebkitGeneratedClassesHelper> cls = AWebkitGeneratedClassesHelper.class;
        try {
            Method method = cls.getDeclaredMethod("getGeneratedClassesByAWebkit");
            method.setAccessible(true);
            Object obj = method.invoke(cls);
            if (obj instanceof String[]) {
                String[] src = (String[]) obj;
                String[] dest = new String[src.length];
                // replace / to . in path
                for (int i = 0; i < src.length; i++) {
                    dest[i] = src[i].replace("/", ".");
                }
                return new HashSet<>(Arrays.asList(dest));
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
