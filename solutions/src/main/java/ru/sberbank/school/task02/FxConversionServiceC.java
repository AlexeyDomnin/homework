package ru.sberbank.school.task02;


import java.math.BigDecimal;
import java.util.List;

import ru.sberbank.school.task02.util.ClientOperation;
import ru.sberbank.school.task02.util.Quote;
import ru.sberbank.school.task02.util.Symbol;

public class FxConversionServiceC implements FxConversionService {
    /**
     * Возвращает значение цены единицы базовой валюты для указанного объема.
     *
     * @param operation вид операции
     * @param symbol    Инструмент
     * @param amount    Объем
     * @return Цена для указанного объема
     */
    private ExternalQuotesService exQuotes;

    public FxConversionServiceC(ExternalQuotesService externalQuotesService) {
        this.exQuotes = externalQuotesService;
    }

    @Override
    public BigDecimal convert(ClientOperation operation, Symbol symbol, BigDecimal amount) {
        if (operation == null || symbol == null || amount == null) {
            throw new NullPointerException("One of arguments is null");
        }

        if (amount.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("Amount is equal to ZERO");
        }

        List<Quote> quotes = exQuotes.getQuotes(symbol);
        Quote exRate = null;
        if (quotes.isEmpty()) {
            throw new NullPointerException("List of quotes is empty");
        }

        for (Quote q : quotes) {

            if (exRate == null && q.isInfinity()) {
                exRate = q;
            } else if (amount.compareTo(q.getVolumeSize()) < 0) {
                if ((exRate == null) || (exRate.isInfinity()) || (q.getVolumeSize().compareTo(exRate.getVolumeSize()) < 0)) {
                    exRate = q;
                }
            }
        }
        BigDecimal rate = (operation == ClientOperation.BUY ? exRate.getOffer() : exRate.getBid());
        return rate;
    }

}
