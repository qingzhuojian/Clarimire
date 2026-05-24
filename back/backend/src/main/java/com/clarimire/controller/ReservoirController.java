package com.clarimire.controller;

import com.clarimire.entity.Reservoir;
import com.clarimire.service.ReservoirService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservoirs")
@CrossOrigin(origins = "*")
public class ReservoirController {

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

    @PostMapping
    public Reservoir create(@RequestBody Reservoir reservoir) {
        return reservoirService.createReservoir(reservoir);
    }

    @PutMapping("/{id}")
    public Reservoir update(@PathVariable Integer id, @RequestBody Reservoir reservoir) {
        reservoir.setId(id);
        return reservoirService.updateReservoir(reservoir);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        reservoirService.deleteReservoir(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    @GetMapping("/checkName")
    public boolean checkName(@RequestParam String name) {
        return reservoirService.isReservoirNameExists(name);
    }
}
