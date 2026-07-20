package com.launchwindow.service.weather;

import com.launchwindow.dto.WeatherSummaryResponse;
import com.launchwindow.model.Launch;
import com.launchwindow.model.ViewingCondition;
import com.launchwindow.model.WeatherSnapshot;
import com.launchwindow.repository.WeatherSnapshotRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WeatherSummaryQueryServiceTest {
    @Test
    void getByLaunchIds_returnsWeatherMappedByLaunchId() {
        WeatherSnapshotRepository repository = mock(WeatherSnapshotRepository.class);

        WeatherSummaryQueryService service = new WeatherSummaryQueryService(repository);
        WeatherSnapshot firstSnapshot = snapshot(1L, (short) 85, "2026-08-01T10:00:00Z");
        WeatherSnapshot secondSnapshot = snapshot(2L, (short) 55, "2026-08-02T14:00:00Z");

        when(repository.findAllByLaunch_IdIn(List.of(1L, 2L))).thenReturn(List.of(firstSnapshot, secondSnapshot));

        Map<Long, WeatherSummaryResponse> result = service.getByLaunchIds(List.of(1L, 2L));

        assertEquals(new WeatherSummaryResponse((short) 85, ViewingCondition.EXCELLENT,
                        Instant.parse("2026-08-01T10:00:00Z")), result.get(1L));

        assertEquals(new WeatherSummaryResponse((short) 55, ViewingCondition.FAIR,
                        Instant.parse("2026-08-02T14:00:00Z")), result.get(2L));

        verify(repository).findAllByLaunch_IdIn(List.of(1L, 2L));
    }

    @Test
    void getByLaunchIds_emptyCollectionSkipsRepository() {
        WeatherSnapshotRepository repository = mock(WeatherSnapshotRepository.class);

        WeatherSummaryQueryService service = new WeatherSummaryQueryService(repository);

        assertEquals(Map.of(), service.getByLaunchIds(List.of()));
        verifyNoInteractions(repository);
    }

    private WeatherSnapshot snapshot(Long launchId, short viewingScore, String forecastTime) {
        WeatherSnapshot snapshot = mock(WeatherSnapshot.class);
        Launch launch = mock(Launch.class);

        when(snapshot.getLaunch()).thenReturn(launch);
        when(launch.getId()).thenReturn(launchId);
        when(snapshot.getViewingScore()).thenReturn(viewingScore);
        when(snapshot.getForecastTime()).thenReturn(Instant.parse(forecastTime));

        return snapshot;
    }
}