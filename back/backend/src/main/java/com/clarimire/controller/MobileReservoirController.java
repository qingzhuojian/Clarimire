package com.clarimire.controller;

import com.clarimire.entity.Reservoir;
import com.clarimire.service.ReservoirService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mobile/reservoirs")
@CrossOrigin(origins = "*")
public class MobileReservoirController {

    @Autowired
    private ReservoirService reservoirService;

    @GetMapping
    public List<Reservoir> getAll() {
        return reservoirService.getAllReservoirs();
    }

    @GetMapping("/{id}")
    public Reservoir getById(@PathVariable Integer id) {
        return reservoirService.getReservoirById(id);
    }
}
