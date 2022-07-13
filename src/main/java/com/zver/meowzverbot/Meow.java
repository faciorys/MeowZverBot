package com.zver.meowzverbot;

import lombok.SneakyThrows;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            execute(SendMessage.builder().chatId(message.getChatId().toString()).text("sa ").build());
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
