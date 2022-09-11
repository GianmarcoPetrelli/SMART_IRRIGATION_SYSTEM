package iet.progetto.iot.resources;

public class Actuator extends BoardResource {

    private boolean isActive = false;

    public Actuator(String nodeAddress, String resourceId) {
        super(nodeAddress, resourceId);
    }

    public boolean GetState() {
        return isActive;
    }

    public void SetState(boolean state) {
        isActive = state;
    }
}
