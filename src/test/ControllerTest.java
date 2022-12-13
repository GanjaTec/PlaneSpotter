import org.junit.jupiter.api.Test;
import planespotter.controller.Controller;
import planespotter.model.Scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ControllerTest {

    private final Controller ctrl = Controller.getInstance();

    @Test
    void start(boolean exit) {
        assertDoesNotThrow(() -> {
            ctrl.start();
            Scheduler.sleepSec(1);
            if (exit) {
                System.exit(0);
            }
        });
    }

    @Test
    void shutdown() {
        start(false);
        assertDoesNotThrow(() -> ctrl.shutdown(false));
        assertDoesNotThrow(() -> ctrl.shutdown(true));
    }

    @Test
    void done() {
        assertDoesNotThrow(() -> ctrl.done(true));
    }

    @Test
    void show() {
        //
    }

    @Test
    void search() {
        //
    }

    @Test
    void confirmSettings() {
    }

    @Test
    void onLiveClick() {
        //
    }

    @Test
    void onClick_all() {
        //
    }

    @Test
    void onMarkerHit() {
    }

    @Test
    void onTrackingClick() {
    }

    @Test
    void saveFile() {
    }

    @Test
    void loadFile() {
    }

    @Test
    void handleException() {
    }

    @Test
    void getScheduler() {
    }

    @Test
    void getUI() {
    }

    @Test
    void getLiveLoader() {
    }

    @Test
    void isLoading() {
    }

    @Test
    void setLoading() {
    }

    @Test
    void isTerminated() {
    }
}