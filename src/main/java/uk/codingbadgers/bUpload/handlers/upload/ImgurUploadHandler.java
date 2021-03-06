package uk.codingbadgers.bUpload.handlers.upload;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import uk.codingbadgers.bUpload.handlers.ConfigHandler;
import uk.codingbadgers.bUpload.handlers.HistoryHandler;
import uk.codingbadgers.bUpload.handlers.MessageHandler;
import uk.codingbadgers.bUpload.handlers.auth.ImgurAuthHandler;
import uk.codingbadgers.bUpload.image.ImgurImageSource;
import uk.codingbadgers.bUpload.image.Screenshot;
import uk.codingbadgers.bUpload.image.UploadedImage;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImgurUploadHandler extends UploadHandler {

    public ImgurUploadHandler(Screenshot screen) {
        super(screen);
    }

    @Override
    public boolean run(Screenshot screen) {
        try {
            String title = ConfigHandler.SAVE_DATE_FORMAT.format(new Date());
            String description = "A minecraft screenshot ";

            if (Minecraft.getMinecraft().isSingleplayer()) {
                description += "in " + Minecraft.getMinecraft().getIntegratedServer().getFolderName();
            } else {
                ServerData data = Minecraft.getMinecraft().func_147104_D();
                description += "on " + data.serverIP + (data.field_82821_f != 25565 ? ":" + data.field_82821_f : "");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screen.image, "png", baos);
            String data = Base64.encodeBase64String(baos.toByteArray());

            List<NameValuePair> arguments = new ArrayList<NameValuePair>(3);
            arguments.add(new BasicNameValuePair("client_id", ImgurAuthHandler.CLIENT_ID));
            arguments.add(new BasicNameValuePair("image", data));
            arguments.add(new BasicNameValuePair("type", "base64"));
            arguments.add(new BasicNameValuePair("title", title));
            arguments.add(new BasicNameValuePair("description", description));

            HttpPost hpost = new HttpPost("https://api.imgur.com/3/upload");
            hpost.setEntity(new UrlEncodedFormEntity(arguments));

            if (ImgurAuthHandler.getInstance().getAccessToken() != null) {
                hpost.addHeader("Authorization", "Bearer " + ImgurAuthHandler.getInstance().getAccessToken());
            } else {
                hpost.addHeader("Authorization", "Client-ID " + ImgurAuthHandler.CLIENT_ID);
            }

            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse resp = client.execute(hpost);
            String result = EntityUtils.toString(resp.getEntity());

            JsonObject responce = new JsonParser().parse(result).getAsJsonObject();
            JsonObject responceData = responce.get("data").getAsJsonObject();

            if (responce.has("success") && responce.get("success").getAsBoolean()) {
                final String uploadUrl = responceData.get("link").getAsString();

                HistoryHandler.addUploadedImage(new UploadedImage(title, uploadUrl, screen, new ImgurImageSource(uploadUrl)));

                IChatComponent message = new ChatComponentTranslation("image.upload.success");
                IChatComponent url = new ChatComponentText("Imgur");
                IChatComponent tooltip = new ChatComponentText(uploadUrl)
                        .setChatStyle(new ChatStyle()
                                .setColor(EnumChatFormatting.AQUA));

                url.setChatStyle(new ChatStyle()
                        .setColor(EnumChatFormatting.GOLD)
                        .setBold(true)
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, uploadUrl)));

                message.appendSibling(url);

                MessageHandler.sendChatMessage(message);

                if (ConfigHandler.COPY_URL_TO_CLIPBOARD) {
                    GuiScreen.setClipboardString(uploadUrl);
                    MessageHandler.sendChatMessage("image.upload.copy");
                }
            } else {
                MessageHandler.sendChatMessage("image.upload.fail", "Imgur", responce.get("status").getAsInt());
                MessageHandler.sendChatMessage(responceData.get("error").getAsString());
                return false;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageHandler.sendChatMessage("image.upload.fail", "Imgur", ex.getMessage());
            return false;
        }
    }

}
