package top.hendrixshen.magiclib.mixin.malilib.panel;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerButton;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.*;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.*;
import fi.dy.masa.malilib.hotkeys.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.hendrixshen.magiclib.api.i18n.minecraft.I18n;
import top.hendrixshen.magiclib.api.malilib.config.gui.ConfigButtonOptionListHovering;
import top.hendrixshen.magiclib.api.malilib.config.option.HotkeyWithSwitch;
import top.hendrixshen.magiclib.api.malilib.config.option.MagicIConfigBase;
import top.hendrixshen.magiclib.api.malilib.config.option.OptionListHotkeyed;
import top.hendrixshen.magiclib.impl.malilib.config.GlobalConfigManager;
import top.hendrixshen.magiclib.impl.malilib.config.gui.ConfigButtonBooleanSwitch;
import top.hendrixshen.magiclib.impl.malilib.config.gui.HotkeyedBooleanResetListener;
import top.hendrixshen.magiclib.impl.malilib.config.gui.MagicConfigGui;
import top.hendrixshen.magiclib.mixin.malilib.accessor.KeybindMultiAccessor;
import top.hendrixshen.magiclib.mixin.malilib.accessor.WidgetListConfigOptionsAccessor;

import java.util.Objects;

/**
 * Reference to <a href="https://github.com/Fallen-Breath/TweakerMore">TweakerMore</a>
 */
@Mixin(value = WidgetConfigOption.class, remap = false)
public abstract class WidgetConfigOptionMixin extends WidgetConfigOptionBase<GuiConfigsBase.ConfigOptionWrapper> {
    @Shadow
    @Final
    protected GuiConfigsBase.ConfigOptionWrapper wrapper;
    @Mutable
    @Shadow
    @Final
    @Nullable
    protected KeybindSettings initialKeybindSettings;
    @Shadow
    @Final
    protected IKeybindConfigGui host;

    @Shadow
    protected abstract void addKeybindResetButton(int x, int y, IKeybind keybind, ConfigButtonKeybind buttonHotkey);

    @Unique
    private boolean magiclib$initialBoolean;

    public WidgetConfigOptionMixin(int x, int y, int width, int height, WidgetListConfigOptionsBase<?, ?> parent,
                                   GuiConfigsBase.ConfigOptionWrapper entry, int listIndex) {
        super(x, y, width, height, parent, entry, listIndex);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Unique
    private boolean magiclib$isMagicGui() {
        return this.parent instanceof WidgetListConfigOptions &&
                ((WidgetListConfigOptionsAccessor) this.parent).magiclib$getParent() instanceof MagicConfigGui;
    }

    @Unique
    private void magiclib$addHotkeyTogglableButtons(int x, int y, int configWidth, IHotkeyTogglable config) {
        int booleanBtnWidth = (configWidth - 24) / 2;
        ButtonGeneric booleanButton = new ConfigButtonBoolean(x, y, booleanBtnWidth, 20, config);
        this.magiclib$addValueWithKeybindWidgets(x, y, configWidth, config, booleanButton);
    }

    @Unique
    private void magiclib$addHotkeyWithSwitchButtons(int x, int y, int configWidth, HotkeyWithSwitch config) {
        int booleanBtnWidth = (configWidth - 24) / 2;
        ButtonGeneric booleanButton = new ConfigButtonBooleanSwitch(x, y, booleanBtnWidth, 20, config);
        this.magiclib$addValueWithKeybindWidgets(x, y, configWidth, config, booleanButton);
    }

    @Unique
    private void magiclib$addOptionListWithHotkey(int x, int y, int configWidth, OptionListHotkeyed config) {
        int optionBtnWidth = (configWidth - 24) / 2;
        ConfigButtonOptionList optionButton = new ConfigButtonOptionList(x, y, optionBtnWidth, 20, config);
        ((ConfigButtonOptionListHovering) optionButton).magiclib$setEnableValueHovering();
        this.magiclib$addValueWithKeybindWidgets(x, y, configWidth, config, optionButton);
    }

    @Unique
    private void magiclib$addButtonAndHotkeyWidgets(int x, int y, int configWidth, @NotNull IHotkey config) {
        IKeybind keybind = config.getKeybind();
        int triggerBtnWidth = (configWidth - 24) / 2;
        ButtonGeneric triggerButton = new ButtonGeneric(
                x, y, triggerBtnWidth, 20,
                I18n.tr("magiclib.config.gui.trigger_button.text"),
                I18n.tr("magiclib.config.gui.trigger_button.hover", config.getName()));
        this.addButton(triggerButton, ((buttonBase, i) -> {
            IHotkeyCallback callback = ((KeybindMultiAccessor) keybind).magiclib$getCallback();

            if (callback == null) {
                return;
            }

            KeyAction activateOn = keybind.getSettings().getActivateOn();

            if (activateOn == KeyAction.BOTH || activateOn == KeyAction.PRESS) {
                callback.onKeyAction(KeyAction.PRESS, keybind);
            }

            if (activateOn == KeyAction.BOTH || activateOn == KeyAction.RELEASE) {
                callback.onKeyAction(KeyAction.RELEASE, keybind);
            }
        }));
        x += triggerBtnWidth + 2;
        configWidth -= triggerBtnWidth + 24;
        ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(x, y, configWidth, 20, keybind, this.host);
        x += configWidth + 2;
        this.addWidget(new WidgetKeybindSettings(x, y, 20, 20, keybind, config.getName(),
                this.parent, this.host.getDialogHandler()));
        //#if MC > 11701
        //$$ x += 22;
        //#else
        x += 24;
        //#endif
        this.addButton(keybindButton, this.host.getButtonPressListener());
        this.addKeybindResetButton(x, y, keybind, keybindButton);
    }

    /**
     * Common logic used for IHotkeyTogglable, IHotkeyWithSwitch and IOptionListHotkeyed
     * whose layouts all are [some button] [keybind button] [reset button]
     */
    @Unique
    private void magiclib$addValueWithKeybindWidgets(int x, int y, int configWidth,
                                                     @NotNull IHotkey config, @NotNull ButtonGeneric valueButton) {
        IKeybind keybind = config.getKeybind();
        int booleanBtnWidth = valueButton.getWidth();
        x += booleanBtnWidth + 2;
        configWidth -= booleanBtnWidth + 24;
        ConfigButtonKeybind keybindButton = new ConfigButtonKeybind(x, y, configWidth, 20, keybind, this.host);
        x += configWidth + 2;
        this.addWidget(new WidgetKeybindSettings(x, y, 20, 20, keybind, config.getName(),
                this.parent, this.host.getDialogHandler()));
        //#if MC > 11701
        //$$ x += 22;
        //#else
        x += 24;
        //#endif
        ButtonGeneric resetButton = this.createResetButton(x, y, config);
        ConfigOptionChangeListenerButton booleanChangeListener = new ConfigOptionChangeListenerButton(config,
                resetButton, null);
        HotkeyedBooleanResetListener resetListener = new HotkeyedBooleanResetListener(config, valueButton,
                keybindButton, resetButton, this.host);
        this.host.addKeybindChangeListener(
                //#if MC >= 11800
                //$$ resetListener::updateButtons
                //#else
                resetListener
                //#endif
        );
        this.addButton(valueButton, booleanChangeListener);
        this.addButton(keybindButton, this.host.getButtonPressListener());
        this.addButton(resetButton, resetListener);
    }

    @Inject(
            method = "<init>",
            at = @At(
                    value = "TAIL"
            ),
            remap = false
    )
    private void postConstruct(CallbackInfo ci) {
        if (!this.magiclib$isMagicGui() || this.wrapper.getType() != GuiConfigsBase.ConfigOptionWrapper.Type.CONFIG) {
            return;
        }

        IConfigBase config = wrapper.getConfig();

        if (!(config instanceof IConfigBoolean && config instanceof IHotkey)) {
            return;
        }

        this.magiclib$initialBoolean = ((IConfigBoolean) config).getBooleanValue();
        this.initialStringValue = ((IHotkey) config).getKeybind().getStringValue();
        this.initialKeybindSettings = ((IHotkey) config).getKeybind().getSettings();
    }

    @Inject(
            method = "addConfigButtonEntry",
            at = @At(
                    "HEAD"
            ),
            remap = false
    )
    private void preAddConfigButtonEntry(int xReset, int yReset, IConfigResettable config,
                                         ButtonBase optionButton, CallbackInfo ci) {
        if (this.magiclib$isMagicGui() && optionButton instanceof ConfigButtonOptionListHovering) {
            ((ConfigButtonOptionListHovering) optionButton).magiclib$setEnableValueHovering();
        }
    }

    //#if MC > 11701
    //$$ @Inject(
    //$$         method = "addConfigOption",
    //$$         at = @At(
    //$$                 value = "INVOKE",
    //$$                 target = "Lfi/dy/masa/malilib/gui/button/ConfigButtonOptionList;<init>(IIIILfi/dy/masa/malilib/config/IConfigOptionList;)V"
    //$$         ),
    //$$         cancellable = true
    //$$ )
    //$$ private void preConfigButtonOptionListInit(int x, int y, float zLevel, int labelWidth,
    //$$                                            int configWidth, IConfigBase config, CallbackInfo ci) {
    //$$     if (!this.magiclib$isMagicGui()) {
    //$$         return;
    //$$     }
    //$$
    //$$     if (config instanceof OptionListHotkeyed) {
    //$$         this.magiclib$addOptionListWithHotkey(x, y, configWidth, (OptionListHotkeyed) config);
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //$$
    //$$ @Inject(
    //$$         method = "addConfigOption",
    //$$         at = @At(
    //$$                 value = "INVOKE",
    //$$                 target = "Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;addBooleanAndHotkeyWidgets(IIILfi/dy/masa/malilib/config/IConfigResettable;Lfi/dy/masa/malilib/config/IConfigBoolean;Lfi/dy/masa/malilib/hotkeys/IKeybind;)V"
    //$$         ),
    //$$         cancellable = true
    //$$ )
    //$$ private void preGetBooleanConfig(int x, int y, float zLevel, int labelWidth, int configWidth,
    //$$                                  IConfigBase config, CallbackInfo ci) {
    //$$     if (!this.magiclib$isMagicGui()) {
    //$$         return;
    //$$     }
    //$$
    //$$     if (config instanceof IHotkeyTogglable) {
    //$$         this.magiclib$addHotkeyTogglableButtons(x, y, configWidth, (IHotkeyTogglable) config);
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //$$
    //$$ @Inject(
    //$$         method = "addHotkeyConfigElements",
    //$$         at = @At(
    //$$                 value = "HEAD"
    //$$         ),
    //$$         cancellable = true
    //$$ )
    //$$ private void preAddHotkeyConfigElements(int x, int y, int configWidth, String configName,
    //$$                                         IHotkey config, CallbackInfo ci) {
    //$$     if (!this.magiclib$isMagicGui()) {
    //$$         return;
    //$$     }
    //$$
    //$$     if (config instanceof HotkeyWithSwitch) {
    //$$         this.magiclib$addHotkeyWithSwitchButtons(x, y, configWidth, (HotkeyWithSwitch) config);
    //$$         ci.cancel();
    //$$     } else if ((config).getKeybind() instanceof KeybindMulti) {
    //$$         this.magiclib$addButtonAndHotkeyWidgets(x, y, configWidth, config);
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //#else
    @Inject(
            method = "addConfigOption",
            at = @At(
                    value = "FIELD",
                    target = "Lfi/dy/masa/malilib/config/ConfigType;BOOLEAN:Lfi/dy/masa/malilib/config/ConfigType;",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void preAddConfigButtonEntry(int x, int y, float zLevel, int labelWidth, int configWidth,
                                         IConfigBase config, CallbackInfo ci) {
        if (!this.magiclib$isMagicGui() || !(config instanceof IHotkey)) {
            return;
        }

        boolean modified = true;

        if (config instanceof IHotkeyTogglable) {
            this.magiclib$addHotkeyTogglableButtons(x, y, configWidth, (IHotkeyTogglable) config);
        } else if (config instanceof HotkeyWithSwitch) {
            this.magiclib$addHotkeyWithSwitchButtons(x, y, configWidth, (HotkeyWithSwitch) config);
        } else if (config instanceof OptionListHotkeyed) {
            this.magiclib$addOptionListWithHotkey(x, y, configWidth, (OptionListHotkeyed) config);
        } else if (((IHotkey) config).getKeybind() instanceof KeybindMulti) {
            this.magiclib$addButtonAndHotkeyWidgets(x, y, configWidth, (IHotkey) config);
        } else {
            modified = false;
        }

        if (modified) {
            ci.cancel();
        }
    }
    //#endif

    @Inject(
            method = "wasConfigModified",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true
    )
    private void postWasConfigModified(CallbackInfoReturnable<Boolean> cir) {
        IConfigBase config = this.wrapper.getConfig();

        if (!(config instanceof IConfigBoolean) || !(config instanceof IHotkey) ||
                !(config instanceof MagicIConfigBase) ||
                !GlobalConfigManager.getInstance().hasConfig((MagicIConfigBase) config)) {
            return;
        }

        IKeybind keybind = ((IHotkey) config).getKeybind();
        cir.setReturnValue(this.magiclib$initialBoolean != ((IConfigBoolean) config).getBooleanValue() ||
                !Objects.equals(this.initialStringValue, keybind.getStringValue()) ||
                !Objects.equals(this.initialKeybindSettings, keybind.getSettings()));
    }
}