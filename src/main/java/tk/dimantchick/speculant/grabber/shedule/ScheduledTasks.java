package tk.dimantchick.speculant.grabber.shedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tinkoff.invest.openapi.model.rest.Candle;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import tk.dimantchick.speculant.core.api.Api;
import tk.dimantchick.speculant.core.domain.candles.CandleWithEmaHA;
import tk.dimantchick.speculant.core.domain.candles.CandleWithHA;
import tk.dimantchick.speculant.core.domain.instrument.ActiveInstruments;
import tk.dimantchick.speculant.core.domain.instrument.Instrument;
import tk.dimantchick.speculant.core.domain.instrument.InstrumentStatus;
import tk.dimantchick.speculant.core.repository.Candles5minRepository;
import tk.dimantchick.speculant.core.repository.CandlesHourRepository;
import tk.dimantchick.speculant.grabber.model.GrabberModel;

import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Service
public class ScheduledTasks {

    private Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);;

    @Autowired
    private GrabberModel grabberModel;

    @Autowired
    private Candles5minRepository candles5minRepository;

    @Autowired
    private CandlesHourRepository candlesHourRepository;

    @Autowired
    private ActiveInstruments activeInstruments;

    @Autowired
    private Api api;

    @Scheduled(fixedDelay = 100)
    public void update5minCandles() {
        //System.out.println("update5minCandles " + OffsetDateTime.now());
        //logger.debug("update5minCandles.");
        Iterator<Instrument> it = activeInstruments.getInstruments().iterator();
        LinkedList<CandleWithHA> candles5minToSave = new LinkedList<>();
        while (it.hasNext()) {
            Instrument instrument = it.next();
            // Обычно мониторятся только инструменты, что готовы к покупке по какой-то стратегии
            // или которые уже куплены.
            if (instrument.getStatus() == InstrumentStatus.READY) {
                continue;
            }
            Pageable pageable = PageRequest.of(0, 5, Sort.Direction.DESC, "time");
            Slice<CandleWithHA> byInstrument = candles5minRepository.findByInstrument(instrument, pageable);
            LinkedList<CandleWithHA> candles5min = new LinkedList<>(byInstrument.getContent());
            OffsetDateTime lastCandleTime;
            //Отмечаем время последей свечи
            if (candles5min.size() > 2) {
                CandleWithHA last = candles5min.getFirst();
                lastCandleTime = last.time.minusMinutes(10);
            } else {
                candles5min.clear();
                lastCandleTime = OffsetDateTime.now().minusDays(1);
            }
            //Берем свечи с последней полной, или за сутки
            if (lastCandleTime.isBefore(OffsetDateTime.now().minusDays(1))) {
                lastCandleTime = OffsetDateTime.now().minusDays(1);
            }
            List<Candle> historicalCandles = api.getHistoricalCandles(instrument.getFigi(), lastCandleTime, OffsetDateTime.now(), CandleResolution._5MIN);

            //Переносим новые свечи в нашу коллекцию
            for (Candle candle : historicalCandles) {

                if (candles5min.size() == 0 || candle.getTime().isAfter(candles5min.getFirst().time.plusSeconds(1))) {
                    CandleWithHA newCandle;
                    if (candles5min.size() == 0) {
                        newCandle = new CandleWithHA(candle, instrument);
                    } else {
                        newCandle = new CandleWithHA(candle, candles5min.getFirst(), instrument);
                    }
                    candles5min.addFirst(newCandle);
                }
                //Если самая новая свеча, то просто её обновляем
                else if (candle.getTime().isEqual(candles5min.getFirst().time)) {
                    Long id = candles5min.getFirst().getId();
                    candles5min.removeFirst();
                    CandleWithHA newCandle = new CandleWithHA(candle, candles5min.getFirst(), instrument);
                    newCandle.setId(id);
                    candles5min.addFirst(newCandle);
                }
            }

            candles5minToSave.addAll(candles5min);
        }
        if (candles5minToSave.size() > 0) {
            candles5minRepository.saveAll(candles5minToSave);
        }
        //Отметка, что закончили цикл обработки 5 минут свечей
        grabberModel.setLast5minUpdate(OffsetDateTime.now());
    }

    @Scheduled(cron = "0 2-59 4-23 * * *")
    public void deleteOld5minCandles() {
        logger.debug("Delete old 5min candles");
        candles5minRepository.deleteAllByTimeLessThan(OffsetDateTime.now().minusHours(4));
    }

    @Scheduled(cron = "0 59 4-23 * * *")
    public void deleteOldHourCandles() {
        logger.debug("Delete old hour candles");
        candlesHourRepository.deleteByTimeLessThan(OffsetDateTime.now().minusDays(7));
    }

    @Scheduled(cron = "30 0 4-23 * * *")
    public void updateHourCandles() {
        //logger.debug("updateHourCandles " + OffsetDateTime.now());
        //Только активные инструменты
        Iterator<Instrument> it = activeInstruments.getInstruments().iterator();
        LinkedList<CandleWithHA> candlesHourToSave = new LinkedList<>();
        while (it.hasNext()) {
            Instrument instrument = it.next();
            //logger.debug(instrument.getTicker() + " updating hour candles");
            Pageable pageable = PageRequest.of(0, 5, Sort.Direction.DESC, "time");
            Slice<CandleWithEmaHA> byInstrument = candlesHourRepository.findByInstrument(instrument, pageable);
            LinkedList<CandleWithEmaHA> candlesHour = new LinkedList<>(byInstrument.getContent());
            OffsetDateTime lastCandleTime;
            //Отмечаем время последей полной свечи
            if (candlesHour.size() > 2) {
                CandleWithEmaHA last = candlesHour.getFirst();
                lastCandleTime = last.time.minusHours(2);
            } else {
                candlesHour.clear();
                lastCandleTime = OffsetDateTime.now().minusDays(7);
            }
            //Берем свечи с последней полной, или за неделю
            List<Candle> historicalCandles = api.getHistoricalCandles(instrument.getFigi(), lastCandleTime, OffsetDateTime.now(), CandleResolution.HOUR);

            //Переносим новые свечи в нашу коллекцию
            for (Candle candle : historicalCandles) {

                if (candlesHour.size() == 0 || candle.getTime().isAfter(candlesHour.getFirst().time.plusSeconds(1))) {
                    CandleWithEmaHA newCandle;
                    if (candlesHour.size() == 0) {
                        newCandle = new CandleWithEmaHA(candle, instrument);
                    } else {
                        newCandle = new CandleWithEmaHA(candle, candlesHour.getFirst(), instrument);
                    }
                    candlesHour.addFirst(newCandle);
                }
                //Если самая новая свеча, то просто её обновляем
                else if (candle.getTime().isEqual(candlesHour.getFirst().time)) {
                    Long id = candlesHour.getFirst().getId();
                    candlesHour.removeFirst();
                    CandleWithEmaHA newCandle = new CandleWithEmaHA(candle, candlesHour.getFirst(), instrument);
                    newCandle.setId(id);
                    candlesHour.addFirst(newCandle);
                }
            }
            candlesHourRepository.saveAll(candlesHour);
        }

        //Отметка, что закончили цикл обработки часовых свечей
        grabberModel.setLastHourUpdate(OffsetDateTime.now());
    }

    @Scheduled(cron = "0 2-59 4-23 * * *")
    public void updateInstruments() {
        logger.debug("Update instruments");
        activeInstruments.updateInstruments();
    }


}
