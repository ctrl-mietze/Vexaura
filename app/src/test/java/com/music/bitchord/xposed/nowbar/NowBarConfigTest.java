package com.music.bitchord.xposed.nowbar;

import org.junit.Test;
import static org.junit.Assert.*;

public class NowBarConfigTest {
    @Test public void advancedEngineIsOptIn() {
        assertFalse(new ConfigManager.Config().masterEnabled);
        assertEquals(10, new ConfigManager.Config().categories.size());
    }

    @Test public void categoryOpacityWorksWithGlobalGradient() {
        ConfigManager.Config cfg = new ConfigManager.Config();
        cfg.colorMode = ConfigManager.MODE_GRADIENT;
        cfg.gradientStartColor = 0xFF102030;
        cfg.gradientEndColor = 0xFF405060;
        cfg.getCategory(ConfigManager.CAT_MEDIA).useCustomOpacity = true;
        cfg.getCategory(ConfigManager.CAT_MEDIA).customOpacity = 50;
        assertArrayEquals(new int[]{0x80102030, 0x80405060}, cfg.resolveGradientArgbForCategory(ConfigManager.CAT_MEDIA));
        assertEquals(50, cfg.resolveOpacityForCategory(ConfigManager.CAT_MEDIA));
    }

    @Test public void customGradientOverridesGlobalColor() {
        ConfigManager.Config cfg = new ConfigManager.Config();
        ConfigManager.CategoryConfig media = cfg.getCategory(ConfigManager.CAT_MEDIA);
        media.useCustomColor = true;
        media.customColorMode = ConfigManager.MODE_GRADIENT;
        media.customGradientStart = 0xFF000000;
        media.customGradientEnd = 0xFFFFFFFF;
        cfg.opacity = 100;
        assertArrayEquals(new int[]{0xFF000000, 0xFFFFFFFF}, cfg.resolveGradientArgbForCategory(ConfigManager.CAT_MEDIA));
        assertEquals(0xFF7F7F7F, cfg.resolveBaseRgbForCategory(ConfigManager.CAT_MEDIA, null));
        assertNull(cfg.resolveGradientArgbForCategory(ConfigManager.CAT_BATTERY));
    }

    @Test public void opacityIsClamped() {
        assertEquals(0x00123456, ConfigManager.applyOpacity(0xFF123456, -10));
        assertEquals(0xFF123456, ConfigManager.applyOpacity(0xFF123456, 200));
    }
}
