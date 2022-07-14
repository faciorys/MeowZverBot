package com.zver.meowzverbot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Optional;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class Meow extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "meow_zver_bot";
    }

    @Override
    public String getBotToken() {
        return "5497711352:AAGAjEnPlX6ew3BHQDbuihvLzsLN2VHmk7U";
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message.hasText() && message.hasEntities()) {
            handleMessage(message);
        } else if (update.hasMessage() && message.hasText()) {
            execute(SendMessage.builder().chatId(message.getChatId().toString()).text("Good").build());
        } else if (update.getMessage().hasSticker()) {
            Sticker sticker = new Sticker();
            sticker.setFileId("CAADBQADiQMAAukKyAPZH7wCI2BwFxYE");
            execute(SendSticker.builder().chatId(message.getChatId().toString()).sticker(new InputFile(sticker.getFileId())).build());
        }
    }

    @SneakyThrows
    private void handleMessage(Message message) {
        Optional<MessageEntity> commandEntity = message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
        if (commandEntity.isPresent()) {
            String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
            if ("/set_currency".equals(command)) {
                execute(SendMessage.builder().chatId(message.getChatId().toString()).text("Good query!!!").build());
            }
        }
    }

    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    public void main() {
        Meow meow = new Meow();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(meow);
    }
}
