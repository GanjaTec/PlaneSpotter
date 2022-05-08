package planespotter.controller;

import planespotter.model.DBOut;
import planespotter.throwables.DataNotFoundException;

import java.io.IOException;
import java.util.List;

public class Test {

    // TEST-MAIN
    // FIXME: 04.05.2022 callsigns und planetypes sind beide noch in "" (Bsp: "A320" statt A320)
    // FIXME: 05.05.2022 planetypes werden in getAllPlanetypes doppelt ausgegeben!
    public static void main(String[] args) throws Exception {
        //var pts = new DBOut().getAllCallsignsLike("");
        var rs = new DBOut().querryDB("SELECT groundspeed FROM tracking LIMIT 50000");
        while (rs.next()) {
            System.out.println(rs.getInt("groundspeed"));
        }
        rs.close();
        int size = 0;
        /*for (var s : pts) {
            System.out.println(s);
            size++;
        }
        System.out.println();
        System.out.println(size);*/
    }
}
