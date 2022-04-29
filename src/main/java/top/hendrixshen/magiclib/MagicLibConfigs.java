package top.hendrixshen.magiclib;

import com.google.common.collect.Lists;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.util.ActiveMode;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import top.hendrixshen.magiclib.config.ConfigHandler;
import top.hendrixshen.magiclib.config.ConfigManager;
import top.hendrixshen.magiclib.config.annotation.Config;
import top.hendrixshen.magiclib.config.annotation.Hotkey;
import top.hendrixshen.magiclib.config.annotation.Numeric;
import top.hendrixshen.magiclib.dependency.Predicates;
import top.hendrixshen.magiclib.dependency.annotation.Dependencies;
import top.hendrixshen.magiclib.dependency.annotation.Dependency;
import top.hendrixshen.magiclib.language.MagicLanguageManager;

import java.util.ArrayList;

public class MagicLibConfigs {
    @Hotkey(hotkey = "M,A,G")
    @Config(category = ConfigCategory.GENERIC)
    public static ConfigHotkey openConfigGui;

    @Config(category = ConfigCategory.GENERIC)
    public static boolean debug = false;

    @Config(category = ConfigCategory.GENERIC)
    public static ArrayList<String> fallbackLanguageList = Lists.newArrayList(MagicLanguageManager.DEFAULT_CODE);

    @Numeric(maxValue = 500, minValue = 0, useSlider = true)
    @Config(category = ConfigCategory.TEST, predicate = Predicates.DebugOptionPredicate.class)
    public static int intConfig = 0;

    @Config(category = ConfigCategory.TEST, predicate = Predicates.DebugOptionPredicate.class)
    public static String stringConfig = "string";

    @Config(category = ConfigCategory.TEST, predicate = Predicates.DebugOptionPredicate.class)
    public static boolean booleanConfig = false;

    @Hotkey
    @Config(category = ConfigCategory.TEST, predicate = Predicates.DebugOptionPredicate.class)
    public static boolean booleanHotkeyConfig = false;

    @Numeric(maxValue = 0.9, minValue = 0.1)
    @Config(category = ConfigCategory.TEST, predicate = Predicates.DebugOptionPredicate.class)
    public static double doubleConfig = 0.1;

    @Config(category = ConfigCategory.TEST, predicate = Predicates.DebugOptionPredicate.class)
    public static Color4f colorConfig = Color4f.ZERO;

    @Config(category = ConfigCategory.TEST, predicate = Predicates.DebugOptionPredicate.class)
    public static ArrayList<String> stringListConfig = Lists.newArrayList("test1", "test2");

    @Config(category = ConfigCategory.TEST, predicate = Predicates.DebugOptionPredicate.class)
    public static IConfigOptionListEntry optionListConfig = ActiveMode.ALWAYS;

    @Config(category = ConfigCategory.TEST, predicate = Predicates.DebugOptionPredicate.class,
            dependencies = @Dependencies(and = @Dependency(value = "sodium", versionPredicate = ">=0.1")))
    public static boolean sodiumTest = false;


    public static void init(ConfigManager cm) {
        openConfigGui.getKeybind().setCallback((keyAction, iKeybind) -> {
            MagicLibConfigGui screen = MagicLibConfigGui.getInstance();
            screen.setParentGui(Minecraft.getInstance().screen);
            Minecraft.getInstance().setScreen(screen);
            return true;
        });

        cm.setValueChangeCallback("debug", option -> {
            if (debug) {
                Configurator.setLevel(MagicLibReference.getModId(), Level.toLevel("DEBUG"));
            } else {
                Configurator.setLevel(MagicLibReference.getModId(), Level.toLevel("INFO"));
            }
            MagicLibConfigGui.getInstance().reDraw();
        });
    }

    public static void postDeserialize(ConfigHandler configHandler) {
        if (debug) {
            Configurator.setLevel(MagicLibReference.getModId(), Level.toLevel("DEBUG"));
        }
        MagicLanguageManager.INSTANCE.initClient();
    }

    public static class ConfigCategory {
        public static final String GENERIC = "generic";
        public static final String TEST = "test";
    }
}
