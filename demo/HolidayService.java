package com.example.calendar.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class HolidayService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, Map<LocalDate, String>> cacheByYearCountry = new HashMap<>();

    public HolidayService() {
    }

    public Map<LocalDate, String> getHolidays(String country, int year) {
        String cacheKey = country + "-" + year;
        if (!cacheByYearCountry.containsKey(cacheKey)) {
            fetchHolidays(country, year);
        }
        return cacheByYearCountry.getOrDefault(cacheKey, new HashMap<>());
    }

    private void fetchHolidays(String country, int year) {
        try {
            String url = String.format(
                    "https://calendarific.com/api/v2/holidays?api_key=%s&country=%s&year=%d",
                    "oSxNViuPkrO6BFAY8F7fb1j4tSSvv7c1", country, year
            );

            CalendarificResponse response = restTemplate.getForObject(url, CalendarificResponse.class);

            Map<LocalDate, String> holidays = new HashMap<>();
            if (response != null && response.response != null && response.response.holidays != null) {
                for (Holiday holiday : response.response.holidays) {
                    try {

                        LocalDate date;
                        if (holiday.date.iso.contains("T")) {

                            date = OffsetDateTime.parse(holiday.date.iso).toLocalDate();
                        } else {
                            // If it's just a date, parse directly
                            date = LocalDate.parse(holiday.date.iso);
                        }
                        holidays.put(date, holiday.name); // Store the holiday
                    } catch (Exception e) {
                        System.err.println("Error parsing date for holiday " + holiday.name + ": " + e.getMessage());
                    }
                }
            }
            cacheByYearCountry.put(country + "-" + year, holidays);
        } catch (Exception e) {
            System.err.println("Error fetching holidays: " + e.getMessage());
        }
    }


    public static class CalendarificResponse {
        public Response response;
    }

    public static class Response {
        public Holiday[] holidays;
    }

    public static class Holiday {
        public String name;
        public DateInfo date;
    }

    public static class DateInfo {
        public String iso;
    }
}
