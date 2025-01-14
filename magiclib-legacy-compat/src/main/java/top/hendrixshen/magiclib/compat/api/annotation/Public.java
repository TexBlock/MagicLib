package top.hendrixshen.magiclib.compat.api.annotation;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MagicLibMixinPlugin auxiliary annotation.
 *
 * <p>Mixin restricts the methods/fields and must be decorated by private.
 *
 * <p>For some scenarios where we want to expose these methods/fields,
 * decorating them with this annotation will modify their access using ASM.
 */
@Deprecated
@ApiStatus.ScheduledForRemoval
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Public {
}
