package wooteco.subway.ui;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.Station;
import wooteco.subway.dto.StationRequest;
import wooteco.subway.dto.StationResponse;

@RestController
public class StationController {

    @PostMapping("/stations")
    public ResponseEntity<StationResponse> createStation(@RequestBody final StationRequest stationRequest) {
        final Station station = new Station(stationRequest.getName());
        Station newStation;
        try {
            newStation = StationDao.save(station);
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        final StationResponse stationResponse = new StationResponse(newStation.getId(), newStation.getName());
        return ResponseEntity.created(URI.create("/stations/" + newStation.getId())).body(stationResponse);
    }

    @GetMapping(value = "/stations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StationResponse>> showStations() {
        final List<Station> stations = StationDao.findAll();
        final List<StationResponse> stationResponses = stations.stream()
                .map(it -> new StationResponse(it.getId(), it.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(stationResponses);
    }

    @DeleteMapping("/stations/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable final Long id) {
        try {
            StationDao.delete(id);
        } catch (final IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.noContent().build();
    }
}
