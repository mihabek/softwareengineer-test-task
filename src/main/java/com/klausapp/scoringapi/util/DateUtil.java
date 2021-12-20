package com.klausapp.scoringapi.util;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import static java.time.DayOfWeek.MONDAY;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class DateUtil{
    public static LocalDateTime toLocalDateTime( String dateString ){
        return toZonedDateTime( requireNonNull( toDate( dateString ) ) ).toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTimeForDbDate( String dateString ){
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss" );
        return LocalDateTime.parse( dateString, DATE_TIME_FORMATTER );
    }

    public static LocalDate toLocalDate( Date date ){
        return toZonedDateTime( date ).toLocalDate();
    }

    public static Date toDate( String dateString ){
        SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd" );
        try{
            return formatter.parse( dateString );
        }
        catch( ParseException e ){
            return null;
        }
    }

    public static boolean checkIfPeriodLongerThanOneMonth( Date startDate, Date endDate ){
        return toLocalDate( startDate ).isBefore( toLocalDate( endDate ).minusMonths( 1 ) );
    }

    public static Boolean isStartOfWeek( LocalDateTime date ){
        return date.toLocalDate().getDayOfWeek().equals( MONDAY );
    }

    private static ZonedDateTime toZonedDateTime( Date date ){
        return date.toInstant()
                .atZone( ZoneId.systemDefault() );
    }

    public static List<LocalDateTime> findDaysByPeriod( LocalDateTime start, LocalDateTime end ){
        return start.toLocalDate().datesUntil( end.toLocalDate() )
                .map( LocalDate::atStartOfDay )
                .collect( toList() );
    }

    public static boolean isBetweenPreviousAndNextDay( LocalDateTime currentDay, LocalDateTime observedDay ){
        return observedDay.isAfter( getPreviousDayEnd( currentDay ) ) && observedDay.isBefore( getNextDayStart( currentDay ) );
    }

    private static LocalDateTime getPreviousDayEnd( LocalDateTime currentDay ){
        return currentDay.minusDays( 1 ).toLocalDate().atTime( LocalTime.MAX );
    }

    private static LocalDateTime getNextDayStart( LocalDateTime currentDay ){
        return currentDay.plusDays( 1 );
    }
}
