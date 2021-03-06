package uk.codingbadgers.bUpload.image;

import net.minecraft.client.Minecraft;
import uk.codingbadgers.bUpload.handlers.MessageHandler;
import uk.codingbadgers.bUpload.manager.TranslationManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class HDImageSource extends ImageSource {

    private File file;

    public HDImageSource(File file) {
        super(ImageSourceType.HD);
        this.file = file;
    }

    @Override
    public String getDescription() {
        return TranslationManager.getTranslation("image.history.open");
    }

    public File getFile() {
        return file;
    }

    @Override
    public void onClick() {
        Desktop dt = Desktop.getDesktop();
        try {
            dt.open(file);
        } catch (IOException e) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            MessageHandler.sendChatMessage("image.history.open.fail.1");
            MessageHandler.sendChatMessage("image.history.open.fail.2");
            try {
                dt.open(file.getParentFile());
            } catch (IOException e1) {
                MessageHandler.sendChatMessage("image.history.open.fail.3");
            }
        }
    }
}
