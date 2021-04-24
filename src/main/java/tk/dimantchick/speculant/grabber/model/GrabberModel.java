package tk.dimantchick.speculant.grabber.model;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class GrabberModel {

    private OffsetDateTime last5minUpdate = OffsetDateTime.MIN;
    private OffsetDateTime lastHourUpdate = OffsetDateTime.MIN;

    public GrabberModel() {
    }

    public OffsetDateTime getLast5minUpdate() {
        return last5minUpdate;
    }

    public void setLast5minUpdate(OffsetDateTime last5minUpdate) {
        this.last5minUpdate = last5minUpdate;
    }

    public OffsetDateTime getLastHourUpdate() {
        return lastHourUpdate;
    }

    public void setLastHourUpdate(OffsetDateTime lastHourUpdate) {
        this.lastHourUpdate = lastHourUpdate;
    }
}
