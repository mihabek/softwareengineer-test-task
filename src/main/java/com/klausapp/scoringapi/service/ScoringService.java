package com.klausapp.scoringapi.service;

import com.klausapp.scoringapi.model.*;
import com.klausapp.scoringapi.repository.ScoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import static com.klausapp.scoringapi.util.DateUtil.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class ScoringService{
    private final static int MAX_RATING = 5;
    private final ScoringRepository scoringRepository;

    public ScoreChange getOverallScoreChange( String startDatePrevious, String endDatePrevious, String startDateSelected, String endDateSelected ){
        LocalDateTime startPrevious = toLocalDateTime( startDatePrevious );
        LocalDateTime endPrevious = toLocalDateTime( endDatePrevious );
        LocalDateTime startSelected = toLocalDateTime( startDateSelected );
        LocalDateTime endSelected = toLocalDateTime( endDateSelected );

        List<Rating> previousRatings = scoringRepository.findRatingsOverPeriod( startPrevious, endPrevious );
        List<Rating> selectedRatings = scoringRepository.findRatingsOverPeriod( startSelected, endSelected );
        List<RatingCategory> categories = scoringRepository.findRatingCategories();

        BigDecimal previousScore = calculateCategoryScore( findCategoryRatings( previousRatings, categories ) );
        BigDecimal selectedScore = calculateCategoryScore( findCategoryRatings( selectedRatings, categories ) );

        if( previousScore == null || selectedScore == null ){
            return null;
        }

        return ScoreChange.builder()
                .previousScore( previousScore )
                .selectedScore( selectedScore )
                .difference( selectedScore.subtract( previousScore ) )
                .build();
    }

    public BigDecimal getOverallScore( String startDate, String endDate ){
        LocalDateTime start = toLocalDateTime( startDate );
        LocalDateTime end = toLocalDateTime( endDate );
        List<Rating> ratings = scoringRepository.findRatingsOverPeriod( start, end );
        List<RatingCategory> categories = scoringRepository.findRatingCategories();

        return calculateCategoryScore( findCategoryRatings( ratings, categories ) );
    }

    public List<TicketScore> findTicketScores( String startDate, String endDate ){
        LocalDateTime start = toLocalDateTime( startDate );
        LocalDateTime end = toLocalDateTime( endDate );
        List<Rating> ratings = scoringRepository.findRatingsOverPeriod( start, end );
        List<RatingCategory> categories = scoringRepository.findRatingCategories();
        LinkedHashMap<Long, List<Rating>> ratingsPerTicket = getRatingsPerTicket( ratings );

        return calculateTicketScores( categories, ratingsPerTicket );
    }

    public CategoryScoreWrapper findCategoryScores( String startDate, String endDate ){
        LocalDateTime start = toLocalDateTime( startDate );
        LocalDateTime end = toLocalDateTime( endDate );
        List<Rating> ratings = scoringRepository.findRatingsOverPeriod( start, end );
        List<RatingCategory> categories = scoringRepository.findRatingCategories();
        List<LocalDateTime> days = findDaysByPeriod( start, end );
        LinkedHashMap<LocalDateTime, List<Rating>> ratingsPerDay = getRatingsPerDay( ratings, days );

        if( checkIfPeriodLongerThanOneMonth( toDate( startDate ), toDate( endDate ) ) ){
            return CategoryScoreWrapper
                    .builder()
                    .weeklyScores( calculateWeeklyCategoryScores( categories, ratingsPerDay ) )
                    .build();
        }

        return CategoryScoreWrapper.builder()
                .dailyScores( calculateDailyCategoryScores( categories, ratingsPerDay ) )
                .build();
    }

    private List<TicketScore> calculateTicketScores( List<RatingCategory> categories, Map<Long, List<Rating>> ratingsPerTicket ){
        return ratingsPerTicket.entrySet().stream()
                .map( entry -> getTicketScore( entry, categories ) )
                .collect( toList() );
    }

    private List<CategoryScore> calculateDailyCategoryScores( List<RatingCategory> categories, Map<LocalDateTime, List<Rating>> ratingsPerDay ){
        return ratingsPerDay.entrySet().stream()
                .map( entry -> getCategoryScore( entry, categories ) )
                .collect( toList() );
    }

    private List<CategoryScore> calculateWeeklyCategoryScores( List<RatingCategory> categories,
                                                               LinkedHashMap<LocalDateTime, List<Rating>> ratingsPerDay ){
        return getRatingsPerWeek( ratingsPerDay ).entrySet().stream()
                .map( entry -> getCategoryScore( entry, categories ) )
                .collect( toList() );
    }

    private TicketScore getTicketScore( Entry<Long, List<Rating>> entry, List<RatingCategory> categories ){
        TicketScore ticketScore = TicketScore.builder()
                .id( entry.getKey() )
                .build();
        List<Rating> ratings = entry.getValue();

        if( (ratings.size() == 0) ){
            return ticketScore;
        }

        List<CategoryRating> categoryRatings = findCategoryRatings( ratings, categories );

        return ticketScore.toBuilder()
                .score( calculateCategoryScore( categoryRatings ) )
                .categoryRatingList( categoryRatings )
                .build();
    }

    private CategoryScore getCategoryScore( Entry<LocalDateTime, List<Rating>> entry, List<RatingCategory> categories ){
        CategoryScore categoryScore = CategoryScore.builder()
                .date( entry.getKey().toString() )
                .build();
        List<Rating> ratings = entry.getValue();

        if( (ratings.size() == 0) ){
            return categoryScore;
        }

        List<CategoryRating> categoryRatings = findCategoryRatings( ratings, categories );

        return categoryScore.toBuilder()
                .score( calculateCategoryScore( categoryRatings ) )
                .categoryRatingList( categoryRatings )
                .build();
    }

    private BigDecimal calculateCategoryScore( List<CategoryRating> categoryRatings ){
        List<RatingCategory> categories = categoryRatings.stream()
                .map( CategoryRating::getCategory )
                .collect( toList() );
        BigDecimal maxResult = calculateCategoryMaxResult( categories );

        if( maxResult.compareTo( BigDecimal.ZERO ) == 0 ){
            return null;
        }

        BigDecimal actualResult = calculateCategoryActualResult( categoryRatings );

        return actualResult.divide( maxResult, 3, RoundingMode.UP );
    }

    private BigDecimal calculateCategoryActualResult( List<CategoryRating> categoryRatings ){
        BigDecimal score = BigDecimal.ZERO;
        for( CategoryRating categoryRating : categoryRatings ){
            BigDecimal categoryScore = categoryRating.getPercentageFromMax().multiply( BigDecimal.valueOf( MAX_RATING ) )
                    .multiply( BigDecimal.valueOf( categoryRating.getCategory().getWeight() ) );
            score = score.add( categoryScore );
        }

        return score;
    }

    private BigDecimal calculateCategoryMaxResult( List<RatingCategory> categories ){
        BigDecimal score = BigDecimal.ZERO;
        for( RatingCategory category : categories ){
            BigDecimal categoryMaxScore = BigDecimal.valueOf( MAX_RATING ).multiply( BigDecimal.valueOf( category.getWeight() ) );
            score = score.add( categoryMaxScore );
        }

        return score;
    }

    private List<CategoryRating> findCategoryRatings( List<Rating> ratings, List<RatingCategory> categories ){
        Map<Long, RatingCategory> ratingCategoryMap = categories.stream()
                .collect( toMap( RatingCategory::getId, Function.identity() ) );
        Map<Long, List<Rating>> map = new HashMap<>();

        categories.forEach( ratingCategory -> map.put( ratingCategory.getId(), new ArrayList<>() ) );
        ratings.forEach( rating -> {
            if( map.containsKey( rating.getRatingCategoryId() ) ){
                List<Rating> categoryRatings = map.get( rating.getRatingCategoryId() );
                categoryRatings.add( rating );
            }
        } );

        return map.entrySet().stream()
                .filter( entry -> !entry.getValue().isEmpty() )
                .map( entry -> getCategoryRating( entry.getValue(), ratingCategoryMap.get( entry.getKey() ) ) )
                .collect( toList() );
    }

    private CategoryRating getCategoryRating( List<Rating> ratings, RatingCategory ratingCategory ){
        int ratingsCount = ratings.size();
        BigDecimal categoryMaxRating = BigDecimal.valueOf( ratingsCount ).multiply( BigDecimal.valueOf( MAX_RATING ) );
        BigDecimal categoryActualRating = BigDecimal.ZERO;

        for( Rating rating : ratings ){
            categoryActualRating = categoryActualRating.add( BigDecimal.valueOf( rating.getRating() ) );
        }

        return CategoryRating.builder()
                .category( ratingCategory )
                .ratingsCount( ratingsCount )
                .percentageFromMax( categoryActualRating.divide( categoryMaxRating, 3, RoundingMode.UP ) )
                .build();
    }

    private LinkedHashMap<LocalDateTime, List<Rating>> getRatingsPerDay( List<Rating> ratings, List<LocalDateTime> days ){
        List<Rating> ratingsCopy = new ArrayList<>( ratings );
        LinkedHashMap<LocalDateTime, List<Rating>> map = new LinkedHashMap<>();

        for( LocalDateTime currentDay : days ){
            List<Rating> ratingsOfCurrentDay = new ArrayList<>();

            for( Rating rating : ratingsCopy ){
                if( isBetweenPreviousAndNextDay( currentDay, rating.getCreatedAt() ) ){
                    ratingsOfCurrentDay.add( rating );
                }
            }

            map.put( currentDay, ratingsOfCurrentDay );
            ratingsCopy.removeAll( ratingsOfCurrentDay );
        }

        return map;
    }

    private LinkedHashMap<Long, List<Rating>> getRatingsPerTicket( List<Rating> ratings ){
        List<Rating> ratingsCopy = new ArrayList<>( ratings );
        LinkedHashMap<Long, List<Rating>> map = new LinkedHashMap<>();
        List<Long> ticketIds = ratings.stream()
                .sorted( comparing( Rating::getTicketId ) )
                .map( Rating::getTicketId )
                .distinct()
                .collect( toList() );

        for( Long currentTicketId : ticketIds ){
            List<Rating> ratingsOfCurrentTicket = new ArrayList<>();

            for( Rating rating : ratingsCopy ){
                if( rating.getTicketId().equals( currentTicketId ) ){
                    ratingsOfCurrentTicket.add( rating );
                }
            }

            map.put( currentTicketId, ratingsOfCurrentTicket );
            ratingsCopy.removeAll( ratingsOfCurrentTicket );
        }

        return map;
    }

    private LinkedHashMap<LocalDateTime, List<Rating>> getRatingsPerWeek( LinkedHashMap<LocalDateTime, List<Rating>> ratingsPerDay ){
        List<LocalDateTime> periodOfDays = new ArrayList<>( ratingsPerDay.keySet() );

        LinkedHashMap<LocalDateTime, List<Rating>> ratingsPerWeek = new LinkedHashMap<>();

        List<Rating> weeklyRatings = new ArrayList<>();
        LocalDateTime dateOfFirstRatingOfTheWeek = null;

        for( LocalDateTime currentDay : periodOfDays ){
            if( isFirstRatingOrStartOfNewWeek( dateOfFirstRatingOfTheWeek, currentDay ) ){
                if( hasRatingsToPush( dateOfFirstRatingOfTheWeek, weeklyRatings ) ){
                    ratingsPerWeek.put( LocalDateTime.from( dateOfFirstRatingOfTheWeek ), new ArrayList<>( weeklyRatings ) );
                }

                dateOfFirstRatingOfTheWeek = currentDay;
                weeklyRatings = new ArrayList<>();
            }

            weeklyRatings.addAll( ratingsPerDay.get( currentDay ) );
        }

        if( hasRatingsToPush( dateOfFirstRatingOfTheWeek, weeklyRatings ) ){
            ratingsPerWeek.put( LocalDateTime.from( dateOfFirstRatingOfTheWeek ), new ArrayList<>( weeklyRatings ) );
        }

        return ratingsPerWeek;
    }

    private boolean isFirstRatingOrStartOfNewWeek( LocalDateTime dateOfFirstRatingOfTheWeek, LocalDateTime observedDay ){
        return dateOfFirstRatingOfTheWeek == null || isStartOfWeek( observedDay );
    }

    private boolean hasRatingsToPush( LocalDateTime dateOfFirstRatingOfTheWeek, List<Rating> weeklyRatings ){
        return dateOfFirstRatingOfTheWeek != null && !weeklyRatings.isEmpty();
    }
}
