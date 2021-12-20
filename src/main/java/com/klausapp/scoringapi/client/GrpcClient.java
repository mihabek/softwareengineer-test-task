package com.klausapp.scoringapi.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import scoring.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

@Slf4j
public class GrpcClient{
    public static void main( String[] args ){
        ManagedChannel channel = ManagedChannelBuilder.forAddress( "localhost", 9090 )
                .usePlaintext()
                .build();

        ScoringServiceGrpc.ScoringServiceBlockingStub stub = ScoringServiceGrpc.newBlockingStub( channel );

        CategoryScoreResponseWrapper categoryDailyScores = stub.findCategoryScores( createScoreRequest( "2020-01-01", "2020-02-01" ) );
        CategoryScoreResponseWrapper categoryWeeklyScores = stub.findCategoryScores( createScoreRequest( "2020-01-01", "2020-02-10" ) );
        TicketScoreResponseWrapper ticketScores = stub.findTicketScores( createScoreRequest( "2020-01-01", "2020-02-01" ) );
        OverallScoreResponse overallScore = stub.getOverallScore( createScoreRequest( "2021-01-01", "2021-02-01" ) );
        if( overallScore.hasScore() ){
            log.info( "Overall score received from server: " + deserialize( overallScore.getScore() ) );
        }
        else {
            log.info( "Overall score received from server is null" );
        }
        ScoreChangeResponse scoreChange = stub.getOverallScoreChange(
                createScoreChangeRequest(
                        createPeriod( "2020-01-01", "2020-02-01" ),
                        createPeriod( "2020-02-01", "2020-03-01" )
                )
        );
        log.info( "Score change received from server: " + deserialize( scoreChange.getDifference() ) );

        channel.shutdown();
    }

    private static BigDecimal deserialize( DecimalValue value ){
        return new BigDecimal(
                new BigInteger( value.getValue().toByteArray() ),
                value.getScale(),
                new MathContext( value.getPrecision() )
        );
    }

    private static ScoreRequest createScoreRequest( String startDate, String endDate ){
        return ScoreRequest.newBuilder()
                .setPeriod( createPeriod( startDate, endDate ) )
                .build();
    }

    private static ScoreChangeRequest createScoreChangeRequest( Period previous, Period selected ){
        return ScoreChangeRequest.newBuilder()
                .setPreviousPeriod( previous )
                .setSelectedPeriod( selected )
                .build();
    }

    private static Period createPeriod( String startDate, String endDate ){
        return Period.newBuilder()
                .setStartDate( startDate )
                .setEndDate( endDate )
                .build();
    }
}
