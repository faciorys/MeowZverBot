package com.zver.meowzverbot;

import com.zver.meowzverbot.entities.Currency;
import com.zver.meowzverbot.service.CurrencyConversionService;
import com.zver.meowzverbot.service.CurrencyModeService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class Meow extends TelegramLongPollingBot {

    private final CurrencyModeService currencyModeService = CurrencyModeService.getInstance();
    private final CurrencyConversionService currencyConversionService =
            CurrencyConversionService.getInstance();

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
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        } else if (message.hasText() && message.hasEntities()) {
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
    private void handleCallback(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        System.out.println(message);
        String[] param = callbackQuery.getData().split(":");
        String action = param[0];
        System.out.println(action);
        Currency newCurrency = Currency.valueOf(param[1]);
        System.out.println(newCurrency);
        switch (action) {
            case "ORIGINAL":
                currencyModeService.setOriginalCurrency(message.getChatId(), newCurrency);
                break;
            case "TARGET":
                currencyModeService.setTargetCurrency(message.getChatId(), newCurrency);
                break;
        }
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
        Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
        for (Currency currency : Currency.values()) {
            buttons.add(
                    Arrays.asList(
                            InlineKeyboardButton.builder()
                                    .text(getCurrencyButton(originalCurrency, currency))
                                    .callbackData("ORIGINAL:" + currency)
                                    .build(),
                            InlineKeyboardButton.builder()
                                    .text(getCurrencyButton(targetCurrency, currency))
                                    .callbackData("TARGET:" + currency)
                                    .build()));
        }
        execute(
                EditMessageReplyMarkup.builder()
                        .chatId(message.getChatId().toString())
                        .messageId(message.getMessageId())
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                        .build());
    }


    @SneakyThrows
    private void handleMessage(Message message) {
        if (message.hasText() && message.hasEntities()) {
            Optional<MessageEntity> commandEntity = message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
            if (commandEntity.isPresent()) {
                String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
                if ("/set_currency".equals(command)) {
                    List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                    for (Currency currency : Currency.values()) {
                        buttons.add(Arrays.asList(
                                InlineKeyboardButton.builder().text(currency.name()).callbackData("ORIGINAL:" + currency).build(),
                                InlineKeyboardButton.builder().text(currency.name()).callbackData("TARGET:" + currency).build()));
                    }
                    execute(SendMessage.builder()
                            .text("Please choose: ")
                            .chatId(message.getChatId().toString())
                            .replyMarkup(InlineKeyboardMarkup.builder()
                                    .keyboard(buttons)
                                    .build()).build());
                }
            }
        }
        if (message.hasText()) {
            String messageText = message.getText();
            Optional<Double> value = parseDouble(messageText);
            Currency originalCurrency = currencyModeService.getOriginalCurrency(message.getChatId());
            Currency targetCurrency = currencyModeService.getTargetCurrency(message.getChatId());
            double ratio = currencyConversionService.getConversionRatio(originalCurrency, targetCurrency);

            if (value.isPresent()) {
                execute(
                        SendMessage.builder()
                                .chatId(message.getChatId().toString())
                                .text(
                                        String.format(
                                                "%4.2f %s is %4.2f %s",
                                                value.get(), originalCurrency, (value.get() * ratio), targetCurrency))
                                .build());
                return;
            }
        }
    }

    private Optional<Double> parseDouble(String messageText) {
        try {
            return Optional.of(Double.parseDouble(messageText));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    public void main() {
        Meow meow = new Meow();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(meow);
    }

    private String getCurrencyButton(Currency saved, Currency current) {
        return saved == current ? current + " ✅" : current.name();
    }
}