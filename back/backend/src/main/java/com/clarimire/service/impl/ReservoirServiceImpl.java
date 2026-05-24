package com.clarimire.service.impl;

import com.clarimire.entity.Reservoir;
import com.clarimire.mapper.ReservoirMapper;
import com.clarimire.service.ReservoirService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservoirServiceImpl implements ReservoirService {

    @Autowired
    private ReservoirMapper reservoirMapper;

    @Override
    public List<Reservoir> getAllReservoirs() {
        return reservoirMapper.findAll();
    }

    @Override
    public Reservoir getReservoirById(Integer id) {
        return reservoirMapper.findById(id);
    }

    @Override
    public Reservoir createReservoir(Reservoir reservoir) {
        reservoir.setCreatedAt(LocalDateTime.now());
        reservoir.setUpdatedAt(LocalDateTime.now());
        if (reservoir.getStatus() == null) {
            reservoir.setStatus("normal");
        }
        reservoirMapper.insert(reservoir);
        return reservoir;
    }

    @Override
    public Reservoir updateReservoir(Reservoir reservoir) {
        Reservoir existing = reservoirMapper.findById(reservoir.getId());
        if (existing == null) {
            return null;
        }
        reservoir.setUpdatedAt(LocalDateTime.now());
        reservoirMapper.update(reservoir);
        return reservoir;
    }

    @Override
    public void deleteReservoir(Integer id) {
        reservoirMapper.deleteById(id);
    }

    @Override
    public boolean isReservoirNameExists(String name) {
        return reservoirMapper.findByName(name) != null;
    }
}
