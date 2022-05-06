package wooteco.subway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.Station;
import wooteco.subway.dto.StationRequest;
import wooteco.subway.dto.StationResponse;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @InjectMocks
    private StationService stationService;

    @Mock
    private StationDao stationDao;

    @Test
    @DisplayName("역을 등록한다.")
    void createStation() {
        // given
        final Long id = 1L;
        final String name = "한성대입구역";
        final StationRequest request = new StationRequest(name);

        // mocking
        given(stationDao.save(any())).willReturn(id);

        // when
        final StationResponse response = stationService.createStation(request);

        // then
        assertThat(response.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("모든 역을 조회한다.")
    void showStations() {
        // given
        final Station savedStation1 = new Station("한성대입구역");
        final Station savedStation2 = new Station("신대방역");

        // mocking
        given(stationDao.findAll()).willReturn(Arrays.asList(savedStation1, savedStation2));

        // when
        final List<StationResponse> responses = stationService.showStations();

        // then
        assertAll(() -> {
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getName()).isEqualTo(savedStation1.getName());
            assertThat(responses.get(1).getName()).isEqualTo(savedStation2.getName());
        });
    }

    @Test
    @DisplayName("역을 삭제한다.")
    void deleteStation() {
        // given
        final long id = 1L;
        final Station station = new Station(id, "신대방역");

        // mocking
        given(stationDao.find(id)).willReturn(station);

        // when
        stationService.deleteStation(id);

        // then
        verify(stationDao).delete(id);
    }
}
