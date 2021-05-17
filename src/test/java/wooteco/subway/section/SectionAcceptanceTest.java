package wooteco.subway.section;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import wooteco.subway.AcceptanceTest;
import wooteco.subway.line.dto.LineRequest;
import wooteco.subway.line.dto.LineResponse;
import wooteco.subway.station.dto.StationRequest;
import wooteco.subway.station.dto.StationResponse;

@DisplayName("Line API 인수테스트")
@Sql("classpath:tableInit.sql")
public class SectionAcceptanceTest extends AcceptanceTest {

    private LineResponse 테스트용_노선;
    private StationResponse A역;
    private StationResponse B역;
    private StationResponse C역;
    private StationResponse D역;

    @BeforeEach
    public void setUp() {
        super.setUp();
        A역 = 지하철_역_생성됨(new StationRequest("A"));
        B역 = 지하철_역_생성됨(new StationRequest("B"));
        C역 = 지하철_역_생성됨(new StationRequest("C"));
        D역 = 지하철_역_생성됨(new StationRequest("D"));

        LineRequest lineRequest = new LineRequest("테스트용 노선", "bg-green-600", B역.getId(), C역.getId(), 100);
        테스트용_노선 = 지하철_노선_생성됨(lineRequest);
    }

    @Test
    @DisplayName("노선에 상행 종점역을 추가한다. (B-C에 A-B 구간 추가시, A-B-C)")
    void addUpTerminalSection() {
        // given
        LineRequest addLineRequest = new LineRequest(A역.getId(), B역.getId(), 100);

        // when
        ExtractableResponse<Response> response = 지하철_구간_추가_요청(테스트용_노선.getId(), addLineRequest);

        // then
        지하철_구간_추가됨(response, Arrays.asList(A역, B역, C역));
    }

    @Test
    @DisplayName("노선에 하행 종점역을 추가한다. (B-C에 C-D 구간 추가시, B-C-D)")
    void addDownTerminalSection() {
        // given
        LineRequest addLineRequest = new LineRequest(C역.getId(), D역.getId(), 100);

        // when
        ExtractableResponse<Response> response = 지하철_구간_추가_요청(테스트용_노선.getId(), addLineRequest);

        // then
        지하철_구간_추가됨(response, Arrays.asList(B역, C역, D역));
    }

    @Test
    @DisplayName("노선 중간에 구간을 추가한다. (B-C에 B-D 구간 추가시, B-D-C)")
    void addDownInternalUpToDownSection() {
        // given
        LineRequest addLineRequest = new LineRequest(B역.getId(), D역.getId(), 50);

        // when
        ExtractableResponse<Response> response = 지하철_구간_추가_요청(테스트용_노선.getId(), addLineRequest);

        // then
        지하철_구간_추가됨(response, Arrays.asList(B역, D역, C역));
    }

    @Test
    @DisplayName("노선 중간에 구간을 추가한다. (B-C에 D-C 구간 추가시, B-D-C)")
    void addDownInternalDownToUpSection() {
        // given
        LineRequest addSectionRequest = new LineRequest(B역.getId(), D역.getId(), 50);

        // when
        ExtractableResponse<Response> response = 지하철_구간_추가_요청(테스트용_노선.getId(), addSectionRequest);

        // then
        지하철_구간_추가됨(response, Arrays.asList(B역, D역, C역));
    }

    @Test
    @DisplayName("노선에서 상행 종점역을 삭제한다. (A-B-C에서 A 삭제시, B-C)")
    void deleteUpTerminalSection() {
        // given
        LineRequest addSectionRequest = new LineRequest(A역.getId(), B역.getId(), 100);
        지하철_구간_추가_요청(테스트용_노선.getId(), addSectionRequest);

        // when
        ExtractableResponse<Response> response = 지하철_구간_삭제_요청(테스트용_노선.getId(), A역.getId());

        // then
        지하철_구간_삭제됨(response, Arrays.asList(B역, C역));
    }

    @Test
    @DisplayName("노선에서 하행 종점역을 삭제한다. (A-B-C에서 C 삭제시, A-B)")
    void deleteDownTerminalSection() {
        // given
        LineRequest addSectionRequest = new LineRequest(A역.getId(), B역.getId(), 100);
        지하철_구간_추가_요청(테스트용_노선.getId(), addSectionRequest);

        // when
        ExtractableResponse<Response> response = 지하철_구간_삭제_요청(테스트용_노선.getId(), C역.getId());

        // then
        지하철_구간_삭제됨(response, Arrays.asList(A역, B역));
    }

    @Test
    @DisplayName("노선에서 중간에 끼어있는 역을 삭제한다. (A-B-C에서 B 삭제시, A-C)")
    void deleteInternalSection() {
        // given
        LineRequest addSectionRequest = new LineRequest(A역.getId(), B역.getId(), 100);
        지하철_구간_추가_요청(테스트용_노선.getId(), addSectionRequest);

        // when
        ExtractableResponse<Response> response = 지하철_구간_삭제_요청(테스트용_노선.getId(), B역.getId());

        // then
        지하철_구간_삭제됨(response, Arrays.asList(A역, C역));
    }

    private ExtractableResponse<Response> 지하철_구간_삭제_요청(Long lineId, Long stationId) {
        return RestAssured.given().log().all()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .delete( "/lines/{lineId}/sections?stationId={stationId}", lineId, stationId)
            .then().log().all()
            .extract();
    }

    private void 지하철_구간_삭제됨(ExtractableResponse<Response> result,
        List<StationResponse> expectedStationResponses) {
        assertThat(result.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());

        LineResponse lineResponse = 지하철_노선_조회됨(테스트용_노선.getId());
        정렬된_역_목록이_반환됨(lineResponse, expectedStationResponses);
    }

    private ExtractableResponse<Response> 지하철_구간_추가_요청(Long lineId, LineRequest lineRequest) {
        return RestAssured.given().log().all()
            .body(lineRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines/{lineId}/sections", lineId)
            .then().log().all()
            .extract();
    }

    private void 지하철_구간_추가됨(ExtractableResponse<Response> result,
        List<StationResponse> expectedStationResponses) {
        assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.value());

        LineResponse lineResponse = 지하철_노선_조회됨(테스트용_노선.getId());
        정렬된_역_목록이_반환됨(lineResponse, expectedStationResponses);
    }

    private void 정렬된_역_목록이_반환됨(LineResponse lineResponse,
        List<StationResponse> stationResponses) {

        List<Long> actualStationIds = lineResponse.getStations().stream()
            .map(StationResponse::getId)
            .collect(Collectors.toList());

        List<Long> expectedStationIds = stationResponses.stream()
            .map(StationResponse::getId)
            .collect(Collectors.toList());

        assertThat(actualStationIds).containsExactlyElementsOf(expectedStationIds);
    }

    public static ExtractableResponse<Response> 지하철_노선_조회_요청(Long id) {
        return RestAssured
            .given().log().all()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when().get("/lines/{lineId}", id)
            .then().log().all()
            .extract();
    }

    private LineResponse 지하철_노선_조회됨(Long lineId){
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(lineId);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        return response.as(LineResponse.class);
    }

    private ExtractableResponse<Response> 지하철_노선_생성_요청(LineRequest lineRequest) {
        return RestAssured.given().log().all()
            .body(lineRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();
    }

    private LineResponse 지하철_노선_생성됨(LineRequest lineRequest){
        ExtractableResponse<Response> response = 지하철_노선_생성_요청(lineRequest);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        return response.as(LineResponse.class);
    }

    private ExtractableResponse<Response> 지하철_역_생성_요청(StationRequest stationRequest) {
        return RestAssured.given().log().all()
            .body(stationRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/stations")
            .then().log().all()
            .extract();
    }

    private StationResponse 지하철_역_생성됨(StationRequest stationRequest){
        ExtractableResponse<Response> response = 지하철_역_생성_요청(stationRequest);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        return response.as(StationResponse.class);
    }

}

 // 노선 인수 테스트에 노선 생성, 삭제 인수테스트 추가
 // Line이 sections 들고 있도록?!
