package com.klausapp.scoringapi.service;
import com.google.protobuf.ByteString;
import com.klausapp.scoringapi.model.RatingCategory;
import com.klausapp.scoringapi.model.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import scoring.*;

import java.math.BigDecimal;
import java.util.List;

import static java.util.stream.Collectors.toList;

@GrpcService
@RequiredArgsConstructor
public class ScoringServiceImpl extends ScoringServiceGrpc.ScoringServiceImplBase{
    private final ScoringService scoringService;

    @Override
    public void findCategoryScores( ScoreRequest request, StreamObserver<CategoryScoreResponseWrapper> responseObserver ){
        CategoryScoreWrapper categoryScoreWrapper = scoringService.findCategoryScores( request.getPeriod().getStartDate(), request.getPeriod().getEndDate() );
        responseObserver.onNext( createCategoryScoreResponseWrapper( categoryScoreWrapper ) );
        responseObserver.onCompleted();
    }

    @Override
    public void findTicketScores( ScoreRequest request, StreamObserver<TicketScoreResponseWrapper> responseObserver ){
        List<TicketScore> ticketScores = scoringService.findTicketScores( request.getPeriod().getStartDate(), request.getPeriod().getEndDate() );
        responseObserver.onNext( createTicketScoreResponseWrapper( ticketScores ) );
        responseObserver.onCompleted();
    }

    @Override
    public void getOverallScore( ScoreRequest request, StreamObserver<OverallScoreResponse> responseObserver ){
        BigDecimal score = scoringService.getOverallScore( request.getPeriod().getStartDate(), request.getPeriod().getEndDate() );
        responseObserver.onNext( createOverallScoreResponse( score ) );
        responseObserver.onCompleted();
    }

    @Override
    public void getOverallScoreChange( ScoreChangeRequest request, StreamObserver<ScoreChangeResponse> responseObserver ){
        ScoreChange scoreChange = scoringService.getOverallScoreChange(
                request.getPreviousPeriod().getStartDate(),
                request.getPreviousPeriod().getEndDate(),
                request.getSelectedPeriod().getStartDate(),
                request.getSelectedPeriod().getEndDate()
        );

        responseObserver.onNext( createScoreChangeResponse( scoreChange ) );
        responseObserver.onCompleted();
    }

    private CategoryScoreResponseWrapper createCategoryScoreResponseWrapper( CategoryScoreWrapper wrapper ){
        List<CategoryScoreResponse> dailyScores = wrapper.getDailyScores().stream()
                .map( this::createCategoryScoreResponse )
                .collect( toList() );
        List<CategoryScoreResponse> weeklyScores = wrapper.getDailyScores().stream()
                .map( this::createCategoryScoreResponse )
                .collect( toList() );

        return CategoryScoreResponseWrapper.newBuilder()
                .addAllDailyScores( dailyScores )
                .addAllWeeklyScores( weeklyScores )
                .build();
    }

    private CategoryScoreResponse createCategoryScoreResponse( CategoryScore categoryScore ){
        List<CategoryRatingResponse> categoryRatingList = categoryScore.getCategoryRatingList().stream()
                .map( this::createCategoryRatingResponse )
                .collect( toList() );

        return CategoryScoreResponse.newBuilder()
                .setDate( categoryScore.getDate() )
                .setScore( serialize( categoryScore.getScore() ) )
                .addAllCategoryRatingList( categoryRatingList )
                .build();
    }

    private TicketScoreResponseWrapper createTicketScoreResponseWrapper( List<TicketScore> ticketScores ){
        List<TicketScoreResponse> tickets = ticketScores.stream()
                .map( this::createTicketScoreResponse )
                .collect( toList() );

        return TicketScoreResponseWrapper.newBuilder()
                .addAllWrapper( tickets )
                .build();
    }

    private TicketScoreResponse createTicketScoreResponse( TicketScore ticketScore ){
        List<CategoryRatingResponse> categoryRatingList = ticketScore.getCategoryRatingList().stream()
                .map( this::createCategoryRatingResponse )
                .collect( toList() );

        return TicketScoreResponse.newBuilder()
                .setId( ticketScore.getId() )
                .setScore( serialize( ticketScore.getScore() ) )
                .addAllCategoryRatingList( categoryRatingList )
                .build();
    }

    private CategoryRatingResponse createCategoryRatingResponse( CategoryRating categoryRating ){
        return CategoryRatingResponse.newBuilder()
                .setCategory( createRatingCategoryResponse( categoryRating.getCategory() ) )
                .setPercentageFromMax( serialize( categoryRating.getPercentageFromMax() ) )
                .setRatingsCount( categoryRating.getRatingsCount() )
                .build();
    }

    private RatingCategoryResponse createRatingCategoryResponse( RatingCategory ratingCategory ){
        return RatingCategoryResponse.newBuilder()
                .setId( ratingCategory.getId() )
                .setName( ratingCategory.getName() )
                .setWeight( ratingCategory.getWeight() )
                .build();
    }

    private ScoreChangeResponse createScoreChangeResponse( ScoreChange scoreChange ){
        return ScoreChangeResponse.newBuilder()
                .setPreviousScore( serialize( scoreChange.getPreviousScore() ) )
                .setSelectedScore( serialize( scoreChange.getSelectedScore() ) )
                .setDifference( serialize( scoreChange.getDifference() ) )
                .build();
    }

    private OverallScoreResponse createOverallScoreResponse( BigDecimal score ){
        return OverallScoreResponse.newBuilder()
                .setScore( serialize( score ) )
                .build();
    }

    private DecimalValue serialize( BigDecimal value ){
        return DecimalValue.newBuilder()
                .setScale( value.scale() )
                .setPrecision( value.precision() )
                .setValue( ByteString.copyFrom( value.unscaledValue().toByteArray() ) )
                .build();
    }
}
