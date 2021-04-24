package tk.dimantchick.speculant.grabber.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import tk.dimantchick.speculant.core.api.Api;
import tk.dimantchick.speculant.core.domain.instrument.Instrument;
import tk.dimantchick.speculant.core.domain.instrument.InstrumentStatus;
import tk.dimantchick.speculant.core.repository.InstrumentRepository;
import tk.dimantchick.speculant.grabber.model.GrabberModel;

import java.util.List;

@Controller
@RequestMapping("/grabber")
public class MainController {

    @Autowired
    private Api api;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private GrabberModel grabberModel;

    @GetMapping
    public String main(Model model) {
        model.addAttribute("last5minUpdate", grabberModel.getLast5minUpdate().toString());
        model.addAttribute("lastHourUpdate", grabberModel.getLastHourUpdate().toString());
        return "main";
    }

}
