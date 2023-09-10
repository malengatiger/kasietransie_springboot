package com.boha.kasietransie.helpermodels;

import com.boha.kasietransie.data.dto.Vehicle;
import lombok.Data;

import java.util.List;

@Data
public class VehicleList {
    String routeId;
    String created;
    int intervalInSeconds;
    List<Vehicle> vehicles;
}
