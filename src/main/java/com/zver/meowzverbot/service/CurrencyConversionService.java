package com.zver.meowzverbot.service;

import com.zver.meowzverbot.entities.Currency;
import com.zver.meowzverbot.service.impl.NbrbCurrencyConversionService;

public interface CurrencyConversionService {
    static CurrencyConversionService getInstance() {
        return new NbrbCurrencyConversionService();
    }

    double getConversionRatio(Currency original, Currency target);
}
