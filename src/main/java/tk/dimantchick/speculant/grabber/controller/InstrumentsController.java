package tk.dimantchick.speculant.grabber.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import tk.dimantchick.speculant.core.api.Api;
import tk.dimantchick.speculant.core.domain.instrument.Instrument;
import tk.dimantchick.speculant.core.domain.instrument.InstrumentStatus;
import tk.dimantchick.speculant.core.repository.InstrumentRepository;

import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/instruments")
public class InstrumentsController {
    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private Api api;

    @GetMapping("")
    public String showAll(Model model) {
        Iterable<Instrument> all = instrumentRepository.findAll(Sort.by(Sort.Order.asc("id")));
        model.addAttribute("instruments", all);
        return "instruments/all";
    }

    @GetMapping("/{id}/{status}")
    public String enable(@PathVariable("id") Integer id, @PathVariable("status") String status) {
        Optional<Instrument> instrument = instrumentRepository.findById(id);
        if (instrument.isPresent()) {
            instrument.get().setStatus(InstrumentStatus.valueOf(status));
            instrumentRepository.save(instrument.get());
        }
        return "redirect:/instruments";
    }


    @GetMapping("/grab")
    public String instrumentsGrab() {
        instrumentRepository.deleteAll();
        List<MarketInstrument> stocks = api.getStocks();
        for (MarketInstrument marketInstrument : stocks) {
            Instrument instrument = new Instrument();
            instrument.setFigi(marketInstrument.getFigi());
            instrument.setTicker(marketInstrument.getTicker());
            instrument.setCurrency(marketInstrument.getCurrency());
            instrument.setStatus(InstrumentStatus.DISABLED);
            instrumentRepository.save(instrument);
        }
        return "redirect:/instruments";
    }


}
