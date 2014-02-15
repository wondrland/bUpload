package uk.codingbadgers.bUpload.factory;

import java.util.Set;

import uk.codingbadgers.bUpload.gui.SettingsGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.client.IModGuiFactory;

public class ConfigGuiFactory implements IModGuiFactory {

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return SettingsGui.class;
	}

	@Override
	public void initialize(Minecraft minecraftInstance) {
	}

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

}