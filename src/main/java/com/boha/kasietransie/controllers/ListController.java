package com.boha.kasietransie.controllers;

import com.boha.kasietransie.data.dto.*;
import com.boha.kasietransie.helpermodels.*;
import com.boha.kasietransie.services.*;
import com.boha.kasietransie.util.CustomResponse;
import com.boha.kasietransie.util.E;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ListController {
    private static final Gson G = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = LoggerFactory.getLogger(ListController.class);

    private final MongoService mongoService;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final AssociationService associationService;
    private final UserGeofenceService userGeofenceService;
    private final DispatchService dispatchService;
    private final CityService cityService;
    private final RouteService routeService;
    private final HeartbeatService heartbeatService;
    private final LandmarkService landmarkService;
    private final MediaService mediaService;
    final AmbassadorService ambassadorService;
    final CommuterService commuterService;
    final TimeSeriesService timeSeriesService;
    final DataFileService dataFileService;

    @GetMapping("/getRoutePointAggregate")
    public ResponseEntity<Object> getRoutePointAggregate() {
        try {
            List<Object> objects = routeService.getRoutePointAggregate();
            return ResponseEntity.ok(objects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getRoutePointAggregate failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getUserById")
    public ResponseEntity<Object> getUserById(@RequestParam String userId) {
        try {
            logger.info(E.BLUE_DOT + " find user by id: " + userId);
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getUserById failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getUserByEmail")
    public ResponseEntity<Object> getUserByEmail(@RequestParam String email) {
        try {
            User user = userService.getUserByEmail(email);
            if (user == null) {
                throw new Exception("We are not winning! User is null, Chief!");
            }
            logger.info(E.LEAF + " user found? " + user.getName());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getUserByEmail failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationById")
    public ResponseEntity<Object> getAssociationById(@RequestParam String associationId) {
        try {
            Association ass = associationService.getAssociationById(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationById failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationSettings")
    public ResponseEntity<Object> getAssociationSettingsModels(@RequestParam String associationId) {
        try {
            List<SettingsModel> ass = associationService
                    .getAssociationSettingsModels(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationSettingsModels failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationUsers")
    public ResponseEntity<Object> getAssociationUsers(@RequestParam String associationId) {
        try {
            List<User> ass = userService
                    .getAssociationUsers(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationRoutes failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationRoutes")
    public ResponseEntity<Object> getAssociationRoutes(@RequestParam String associationId) {
        try {
            List<Route> ass = routeService
                    .getAssociationRoutes(associationId);
            logger.info(E.DOG + E.DOG + " Association Routes found: " + ass.size());
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationRoutes failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationVehicles")
    public ResponseEntity<Object> getAssociationVehicles(@RequestParam String associationId, @RequestParam int page) {
        try {
            List<Vehicle> ass = vehicleService
                    .getAssociationVehicles(associationId, page);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationVehicles failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationHeartbeatTimeSeries")
    public byte[] getAssociationHeartbeatTimeSeries(@RequestParam String associationId, @RequestParam String startDate) throws Exception {
        try {
            File zippedFile = timeSeriesService
                    .aggregateAssociationHeartbeatData(associationId, startDate);
            byte[] bytes = java.nio.file.Files.readAllBytes(zippedFile.toPath());
            try {
                boolean deleted = zippedFile.delete();
                logger.info(E.PANDA + E.PANDA + " zipped AssociationHeartbeatTimeSeries file deleted : " + deleted);
            } catch (Exception e) {
                //ignore
            }
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    @GetMapping("/getVehicleHeartbeatTimeSeries")
    public ResponseEntity<Object> getVehicleHeartbeatTimeSeries(@RequestParam String vehicleId, @RequestParam String startDate) {
        try {
            List<VehicleHeartbeatAggregationResult> ass = timeSeriesService
                    .aggregateVehicleHeartbeatData(vehicleId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getVehicleHeartbeatTimeSeries failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociations")
    public ResponseEntity<Object> getAssociations() {
        try {
            List<Association> ass = associationService
                    .getAssociations();
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociations failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationVehicleHeartbeats")
    public ResponseEntity<Object> getAssociationVehicleHeartbeats(@RequestParam String associationId,
                                                                  @RequestParam String startDate) {
        try {
            List<VehicleHeartbeat> ass = heartbeatService
                    .getAssociationVehicleHeartbeats(associationId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationVehicleHeartbeats failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationBag")
    public ResponseEntity<Object> getAssociationBag(@RequestParam String associationId,
                                                    @RequestParam String startDate) {
        try {
            AssociationBag ass = dispatchService
                    .getAssociationBag(associationId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationBag failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/getAssociationCounts")
    public ResponseEntity<Object> getAssociationCounts(@RequestParam String associationId,
                                                    @RequestParam String startDate) {
        try {
            AssociationCounts ass = dispatchService
                    .getAssociationCounts(associationId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
    @GetMapping("/getAssociationBagZipped")
    public byte[] getAssociationBagZipped(@RequestParam String associationId,
                                                    @RequestParam String startDate) throws Exception {
        try {
            File ass = dispatchService
                    .getAssociationBagZippedFile(associationId, startDate);
            byte[] bytes = java.nio.file.Files.readAllBytes(ass.toPath());
            boolean deleted = ass.delete();
            logger.info(E.PANDA + E.PANDA + " getAssociationBagZipped file deleted : " + deleted);
            return bytes;
        } catch (Exception e) {
            throw new Exception("getAssociationBagZipped failed: " + e.getMessage());
        }
    }
    @GetMapping("/file")
    public ResponseEntity<FileSystemResource> getZippedFile() throws Exception {
        File zipFile = dataFileService.createZippedFile();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(zipFile));
    }

    @GetMapping("/byte-array")
    public ResponseEntity<ByteArrayResource> getZippedByteArray() throws IOException {
        byte[] byteArray = dataFileService.createZippedByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new ByteArrayResource(byteArray));
    }
    @GetMapping("/getVehicleHeartbeats")
    public ResponseEntity<Object> getVehicleHeartbeats(@RequestParam String associationId,
                                                       @RequestParam int cutoffHours) {
        try {
            List<VehicleHeartbeat> ass = heartbeatService
                    .getVehicleHeartbeats(associationId, cutoffHours);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getVehicleHeartbeats failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getExampleFiles")
    public ResponseEntity<Object> getExampleFiles() {
        try {
            List<ExampleFile> ass = associationService
                    .getExampleFiles();
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getExampleFiles failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getOwnerVehicleHeartbeats")
    public ResponseEntity<Object> getOwnerVehicleHeartbeats(@RequestParam String associationId,
                                                            @RequestParam int cutoffHours) {
        try {
            List<VehicleHeartbeat> ass = heartbeatService
                    .getOwnerVehicleHeartbeats(associationId, cutoffHours);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getOwnerVehicleHeartbeats failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationAppErrors")
    public ResponseEntity<Object> getAssociationAppErrors(@RequestParam String associationId) {
        try {
            List<AppError> ass = associationService
                    .getAssociationAppErrors(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationAppErrors failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getRoutePoints")
    public ResponseEntity<Object> getRoutePoints(@RequestParam String routeId, @RequestParam int page) {
        try {
            List<RoutePoint> ass = routeService
                    .getRoutePoints(routeId, page);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getRoutePoints failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationRoutePoints")
    public ResponseEntity<Object> getAssociationRoutePoints(@RequestParam String associationId) {
        try {
            List<RoutePoint> ass = routeService
                    .getAssociationRoutePoints(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationRoutePoints failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationRouteCities")
    public ResponseEntity<Object> getAssociationRouteCities(@RequestParam String associationId) {
        try {
            List<RouteCity> ass = routeService
                    .getAssociationRouteCities(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationRouteCities failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationRouteLandmarks")
    public ResponseEntity<Object> getAssociationRouteLandmarks(@RequestParam String associationId) {
        try {
            List<RouteLandmark> ass = routeService
                    .getAssociationRouteLandmarks(associationId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationRouteLandmarks failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getRouteCities")
    public ResponseEntity<Object> getRouteCities(@RequestParam String routeId) {
        try {
            List<RouteCity> ass = routeService
                    .getRouteCities(routeId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getRouteCities failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getMarshalDispatchRecords")
    public ResponseEntity<Object> getMarshalDispatchRecords(@RequestParam String userId, @RequestParam String startDate) {
        try {
            List<DispatchRecord> ass = dispatchService
                    .getMarshalDispatchRecords(userId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getMarshalDispatchRecords failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationAmbassadorCheckIn")
    public ResponseEntity<Object> getAssociationAmbassadorCheckIn(@RequestParam String associationId, @RequestParam String startDate) {
        try {
            List<AmbassadorCheckIn> ass = ambassadorService
                    .getAssociationAmbassadorCheckIn(associationId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationAmbassadorCheckIn failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getVehicleAmbassadorCheckIn")
    public ResponseEntity<Object> getVehicleAmbassadorCheckIn(@RequestParam String vehicleId, @RequestParam String startDate) {
        try {
            List<AmbassadorCheckIn> ass = ambassadorService
                    .getVehicleAmbassadorCheckIn(vehicleId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getVehicleAmbassadorCheckIn failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getUserAmbassadorCheckIn")
    public ResponseEntity<Object> getUserAmbassadorCheckIn(@RequestParam String userId, @RequestParam String startDate) {
        try {
            List<AmbassadorCheckIn> ass = ambassadorService
                    .getUserAmbassadorCheckIn(userId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getUserAmbassadorCheckIn failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationAmbassadorPassengerCounts")
    public ResponseEntity<Object> getAssociationAmbassadorPassengerCounts(@RequestParam String associationId, @RequestParam String startDate) {
        try {
            List<AmbassadorPassengerCount> ass = ambassadorService
                    .getAssociationAmbassadorPassengerCounts(associationId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationAmbassadorPassengerCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getRoutePassengerCounts")
    public ResponseEntity<Object> getRoutePassengerCounts(
            @RequestParam String routeId, @RequestParam String startDate) {
        try {
            List<AmbassadorPassengerCount> ass = ambassadorService
                    .getRoutePassengerCounts(routeId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getRoutePassengerCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationCommuterRequests")
    public ResponseEntity<Object> getAssociationCommuterRequests(
            @RequestParam String associationId, @RequestParam String startDate) {
        try {
            List<CommuterRequest> ass = commuterService
                    .getAssociationCommuterRequests(associationId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationCommuterRequests failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getRouteCommuterRequests")
    public ResponseEntity<Object> getRouteCommuterRequests(
            @RequestParam String routeId, @RequestParam String startDate) {
        try {
            List<CommuterRequest> ass = commuterService
                    .getRouteCommuterRequests(routeId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getRouteCommuterRequests failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getRouteDispatchRecords")
    public ResponseEntity<Object> getRouteDispatchRecords(
            @RequestParam String routeId, @RequestParam String startDate) {
        try {
            List<DispatchRecord> ass = dispatchService
                    .getRouteDispatchRecords(routeId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getRouteDispatchRecords failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationDispatchRecords")
    public ResponseEntity<Object> getAssociationDispatchRecords(
            @RequestParam String associationId, @RequestParam String startDate) {
        try {
            List<DispatchRecord> ass = dispatchService
                    .getAssociationDispatchRecords(associationId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationDispatchRecords failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationVehicleArrivals")
    public ResponseEntity<Object> getAssociationVehicleArrivals(
            @RequestParam String associationId, @RequestParam String startDate) {
        try {
            List<VehicleArrival> ass = dispatchService
                    .getAssociationVehicleArrivals(associationId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationVehicleArrivals failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getRouteVehicleArrivals")
    public ResponseEntity<Object> getRouteVehicleArrivals(
            @RequestParam String routeId, @RequestParam String startDate) {
        try {
            List<VehicleArrival> ass = dispatchService
                    .getRouteVehicleArrivals(routeId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getRouteVehicleArrivals failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getUserAmbassadorPassengerCounts")
    public ResponseEntity<Object> getUserAmbassadorPassengerCounts(@RequestParam String userId, @RequestParam String startDate) {
        try {
            List<AmbassadorPassengerCount> ass = ambassadorService
                    .getUserAmbassadorPassengerCounts(userId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getUserAmbassadorPassengerCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getVehicleAmbassadorPassengerCounts")
    public ResponseEntity<Object> getVehicleAmbassadorPassengerCounts(@RequestParam String vehicleId, @RequestParam String startDate) {
        try {
            List<AmbassadorPassengerCount> ass = ambassadorService
                    .getVehicleAmbassadorPassengerCounts(vehicleId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getVehicleAmbassadorPassengerCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getVehicleRouteAssignments")
    public ResponseEntity<Object> getVehicleRouteAssignments(
            @RequestParam String vehicleId) {
        try {
            List<RouteAssignment> ass = vehicleService
                    .getVehicleRouteAssignments(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getVehicleRouteAssignments failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping(value = "/getAssociationRouteZippedFile")
    public byte[] getAssociationRouteZippedFile(@RequestParam String associationId) throws Exception {
        logger.info(E.PANDA + E.PANDA + " ListController getAssociationRouteZippedFile ");

        File zippedFile = routeService.getAssociationRouteZippedFile(associationId);
        byte[] bytes = java.nio.file.Files.readAllBytes(zippedFile.toPath());
        boolean deleted = zippedFile.delete();

        logger.info(E.PANDA + E.PANDA + " zipped project file deleted : " + deleted);
        return bytes;
    }

    @GetMapping("/getRouteAssignments")
    public ResponseEntity<Object> getRouteAssignments(
            @RequestParam String routeId) {
        try {
            List<RouteAssignment> ass = vehicleService
                    .getRouteAssignments(routeId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getRouteAssignments failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getOwnersBag")
    public ResponseEntity<Object> getOwnersBag(@RequestParam String userId, @RequestParam String startDate) {
        try {
            BigBag ass = dispatchService
                    .getOwnersBag(userId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getOwnersBag failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }


    @GetMapping("/getVehicleCounts")
    public ResponseEntity<Object> getVehicleCounts(@RequestParam String vehicleId) {
        try {
            List<CounterBag> ass = dispatchService
                    .getVehicleCounts(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getVehicleCounts failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getVehicleCountsByDate")
    public ResponseEntity<Object> getVehicleCountsByDate(
            @RequestParam String vehicleId, @RequestParam String startDate) {
        try {
            List<CounterBag> ass = dispatchService
                    .getVehicleCountsByDate(vehicleId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getVehicleCountsByDate failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getVehicleBag")
    public ResponseEntity<Object> getVehicleBag(
            @RequestParam String vehicleId, @RequestParam String startDate) {
        try {
            VehicleBag ass = dispatchService
                    .getVehicleBag(vehicleId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getVehicleBag failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/countVehicleArrivals")
    public ResponseEntity<Object> countVehicleArrivals(@RequestParam String vehicleId) {
        try {
            long ass = dispatchService
                    .countVehicleArrivals(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "countVehicleArrivals failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/countVehicleDepartures")
    public ResponseEntity<Object> countVehicleDeparture(@RequestParam String vehicleId) {
        try {
            long ass = dispatchService
                    .countVehicleDepartures(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "countVehicleDepartures failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/countVehicleDispatches")
    public ResponseEntity<Object> countVehicleDispatches(@RequestParam String vehicleId) {
        try {
            long ass = dispatchService
                    .countVehicleDispatches(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "countVehicleDispatches failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/countVehicleHeartbeats")
    public ResponseEntity<Object> countVehicleHeartbeats(@RequestParam String vehicleId) {
        try {
            long ass = heartbeatService
                    .countVehicleHeartbeats(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "countVehicleHeartbeats failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getOwnerVehicles")
    public ResponseEntity<Object> getOwnerVehicles(@RequestParam String userId,
                                                   @RequestParam int page) {
        try {
            List<Vehicle> ass = vehicleService
                    .getOwnerVehicles(userId, page);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getOwnerVehicles failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/downloadExampleVehiclesFile")
    public ResponseEntity<Object> downloadExampleVehiclesFile(HttpServletRequest request, final HttpServletResponse response) {
        try {
            File file = associationService
                    .downloadExampleVehiclesFile();
            response.setHeader("Cache-Control", "must-revalidate");
            response.setHeader("Pragma", "public");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Content-disposition", "attachment; ");

            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(bufferedInputStream, response.getOutputStream());
            return ResponseEntity.ok(file);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "downloadExampleVehiclesFile failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/downloadExampleUsersFile")
    public ResponseEntity<Object> downloadExampleUsersFile(HttpServletRequest request, final HttpServletResponse response) {
        try {
            File file = associationService
                    .downloadExampleUsersFile();
            response.setHeader("Cache-Control", "must-revalidate");
            response.setHeader("Pragma", "public");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Content-disposition", "attachment; ");

            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(bufferedInputStream, response.getOutputStream());
            return ResponseEntity.ok(file);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "downloadExampleVehiclesFile failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getVehicleMediaRequests")
    public ResponseEntity<Object> getVehicleMediaRequests(@RequestParam String vehicleId) {
        try {
            List<VehicleMediaRequest> ass = mediaService
                    .getVehicleMediaRequests(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getVehicleMediaRequests failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getAssociationVehicleMediaRequests")
    public ResponseEntity<Object> getAssociationVehicleMediaRequests(
            @RequestParam String associationId, @RequestParam String startDate) {
        try {
            List<VehicleMediaRequest> ass = mediaService
                    .getAssociationVehicleMediaRequests(associationId, startDate);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getAssociationVehicleMediaRequests failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getVehiclePhotos")
    public ResponseEntity<Object> getVehiclePhotos(@RequestParam String vehicleId) {
        try {
            List<VehiclePhoto> ass = mediaService
                    .getVehiclePhotos(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getVehiclePhotos failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getVehicleVideos")
    public ResponseEntity<Object> getVehicleVideos(@RequestParam String vehicleId) {
        try {
            List<VehicleVideo> ass = mediaService
                    .getVehicleVideos(vehicleId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getVehicleVideos failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }


    @GetMapping("/getCalculatedDistances")
    public ResponseEntity<Object> getCalculatedDistances(@RequestParam String routeId) {
        try {
            List<CalculatedDistance> ass = routeService
                    .getCalculatedDistances(routeId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getCalculatedDistances failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/refreshRoute")
    public ResponseEntity<Object> refreshRoute(@RequestParam String routeId) {
        try {
            RouteBag bag = routeService
                    .refreshRoute(routeId);
            return ResponseEntity.ok(bag);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "refreshRoute failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getCountryStates")
    public ResponseEntity<Object> getCountryStates(@RequestParam String countryId) {
        try {
            List<State> ass = cityService
                    .getCountryStates(countryId);
            return ResponseEntity.ok(ass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getCountryStates failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findCitiesByLocation")
    public ResponseEntity<Object> findCitiesByLocation(@RequestParam double latitude,
                                                       @RequestParam double longitude,
                                                       @RequestParam int limit,
                                                       @RequestParam double radiusInKM) {
        try {
            List<City> cities = cityService.findCitiesByLocation(
                    latitude, longitude, radiusInKM, limit);
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(500,
                            "findCitiesByLocation failed: "
                                    + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findRoutesByLocation")
    public ResponseEntity<Object> findRoutesByLocation(@RequestParam double latitude,
                                                       @RequestParam double longitude,
                                                       @RequestParam double radiusInKM) {
        try {
            List<Route> r = routeService.findRoutesByLocation(latitude, longitude, radiusInKM);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "findRoutesByLocation failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findRouteLandmarksByLocation")
    public ResponseEntity<Object> findRouteLandmarksByLocation(@RequestParam double latitude,
                                                               @RequestParam double longitude,
                                                               @RequestParam double radiusInKM) {
        try {
            List<RouteLandmark> r = routeService.findRouteLandmarksByLocation(
                    latitude, longitude, radiusInKM);

            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "findRouteLandmarksByLocation failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findAssociationVehiclesByLocationAndTime")
    public ResponseEntity<Object> findAssociationVehiclesByLocationAndTime(
            @RequestParam String associationId,
            @RequestParam double latitude,

            @RequestParam double longitude,
            @RequestParam int minutes) {
        try {
            List<VehicleHeartbeat> r = vehicleService
                    .findAssociationVehiclesByLocationAndTime(
                            associationId, latitude, longitude, minutes);

            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "findAssociationVehiclesByLocationAndTime failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findOwnerVehiclesByLocationAndTime")
    public ResponseEntity<Object> findOwnerVehiclesByLocationAndTime(
            @RequestParam String userId,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam int minutes) {
        try {
            List<VehicleHeartbeat> r = vehicleService
                    .findOwnerVehiclesByLocationAndTime(
                            userId, latitude, longitude, minutes);

            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "findOwnerVehiclesByLocationAndTime failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findAssociationRouteLandmarksByLocation")
    public ResponseEntity<Object> findAssociationRouteLandmarksByLocation(@RequestParam String associationId, @RequestParam double latitude,
                                                                          @RequestParam double longitude,
                                                                          @RequestParam double radiusInKM) {
        try {
            List<RouteLandmark> r = routeService.findAssociationRouteLandmarksByLocation(
                    associationId,
                    latitude, longitude, radiusInKM);

            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "findAssociationRouteLandmarksByLocation failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findLandmarksByLocation")
    public ResponseEntity<Object> findLandmarksByLocation(

            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radiusInKM) {
        try {
            List<Landmark> r = landmarkService.findLandmarksByLocation(latitude, longitude, radiusInKM);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "findLandmarksByLocation failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getRouteLandmarks")
    public ResponseEntity<Object> getRouteLandmarks(@RequestParam String routeId) {
        try {
            List<RouteLandmark> r = routeService.getRouteLandmarks(routeId);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getRouteLandmarks failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getRouteUpdateRequests")
    public ResponseEntity<Object> getRouteUpdateRequests(@RequestParam String routeId) {
        try {
            List<RouteUpdateRequest> r = routeService.getRouteUpdateRequests(routeId);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getRouteUpdateRequests failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/findAssociationRoutesByLocation")
    public ResponseEntity<Object> findAssociationRoutesByLocation(@RequestParam String associationId, @RequestParam double latitude,
                                                                  @RequestParam double longitude,
                                                                  @RequestParam double radiusInKM) {
        try {
            List<Route> r = routeService.findAssociationRoutesByLocation(associationId, latitude, longitude, radiusInKM);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "findAssociationRoutesByLocation failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getCountryCitiesZippedFile")
    public byte[]  getCountryCitiesZippedFile(@RequestParam String countryId) throws Exception {

            File cities = cityService.getCountryCitiesZippedFile(countryId);
            byte[] bytes = java.nio.file.Files.readAllBytes(cities.toPath());
            cities.delete();
            return bytes;

    }
    @GetMapping("/getVehiclesZippedFile")
    public byte[]  getVehiclesZippedFile(@RequestParam String associationId) throws Exception {

        File zippedFile = vehicleService.getVehiclesZippedFile(associationId);
        byte[] bytes = java.nio.file.Files.readAllBytes(zippedFile.toPath());
        zippedFile.delete();
        return bytes;

    }
    @GetMapping("/getCountryCities")
    public ResponseEntity<Object> getCountryCities(@RequestParam String countryId, int page) {
        try {
            List<City> cities = cityService.getCountryCities(countryId, page);
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getCountryCities failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }

    @GetMapping("/getCountries")
    public ResponseEntity<Object> getCountries() {
        try {
            List<Country> cities = cityService.getCountries();
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new CustomResponse(400,
                            "getCountries failed: " + e.getMessage(),
                            new DateTime().toDateTimeISO().toString()));
        }
    }
}
