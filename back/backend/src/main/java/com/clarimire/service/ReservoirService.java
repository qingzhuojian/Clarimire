package com.clarimire.service;

import com.clarimire.entity.Reservoir;
import java.util.List;

public interface ReservoirService {
    List<Reservoir> getAllReservoirs();
    Reservoir getReservoirById(Integer id);
    Reservoir createReservoir(Reservoir reservoir);
    Reservoir updateReservoir(Reservoir reservoir);
    void deleteReservoir(Integer id);
    boolean isReservoirNameExists(String name);
}
