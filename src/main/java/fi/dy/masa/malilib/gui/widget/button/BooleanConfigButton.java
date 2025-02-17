package fi.dy.masa.malilib.gui.widget.button;

import fi.dy.masa.malilib.config.option.BooleanConfig;

public class BooleanConfigButton extends OnOffButton
{
    public BooleanConfigButton(int width, int height, BooleanConfig config)
    {
        this(width, height, config, OnOffStyle.SLIDER_ON_OFF);
    }

    public BooleanConfigButton(int width, int height, BooleanConfig config, OnOffStyle style)
    {
        super(width, height, style, config::getBooleanValue, null);

        this.setActionListener(() -> {
            config.toggleBooleanValue();
            this.updateDisplayString();
        });
    }
}
