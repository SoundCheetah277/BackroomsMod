package org.vfast.backrooms.client.gui;


import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class InvisiScreen extends Screen {
    private LevelLoadTracker loadTracker;

    public InvisiScreen(LevelLoadTracker levelLoadTracker) {
        super(Text.empty());
        this.loadTracker = levelLoadTracker;
    }

    @Override
    public void tick() {
        if (this.loadTracker.isLevelReady()) {
            this.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
