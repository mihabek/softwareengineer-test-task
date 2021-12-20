package com.klausapp.scoringapi.repository;

import com.klausapp.scoringapi.model.Rating;
import com.klausapp.scoringapi.model.RatingCategory;
import com.klausapp.scoringapi.util.DateUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ScoringRepository{
    @Resource
    private JdbcTemplate jdbcTemplate;

    private final String findRatingsOverPeriodSql = getFileContent( "sql/findRatingsOverPeriod.sql" );
    private final String findRatingCategoriesSql = getFileContent( "sql/findRatingCategories.sql" );

    public List<Rating> findRatingsOverPeriod( LocalDateTime startDate, LocalDateTime endDate ){
        return jdbcTemplate.query(
                findRatingsOverPeriodSql,
                new Object[]{startDate, endDate},
                new RatingMapper() );
    }

    public List<RatingCategory> findRatingCategories(){
        return jdbcTemplate.query(
                findRatingCategoriesSql,
                new RatingCategoryMapper() );
    }

    static class RatingMapper implements RowMapper<Rating>{
        public Rating mapRow( ResultSet rs, int rowNum ) throws SQLException{
            return Rating.builder()
                    .id( rs.getLong( "id" ) )
                    .rating( rs.getInt( "rating" ) )
                    .ticketId( rs.getLong( "ticket_id" ) )
                    .ratingCategoryId( rs.getLong( "rating_category_id" ) )
                    .reviewerId( rs.getLong( "reviewer_id" ) )
                    .revieweeId( rs.getLong( "reviewee_id" ) )
                    .createdAt( DateUtil.toLocalDateTimeForDbDate( (rs.getString( "created_at" )) ) )
                    .build();
        }
    }

    static class RatingCategoryMapper implements RowMapper<RatingCategory>{
        public RatingCategory mapRow( ResultSet rs, int rowNum ) throws SQLException{
            return RatingCategory.builder()
                    .id( rs.getLong( "id" ) )
                    .name( rs.getString( "name" ) )
                    .weight( rs.getDouble( "weight" ) )
                    .build();
        }
    }

    private String getFileContent( String path ){
        try{
            InputStream inputStream = new ClassPathResource( path ).getInputStream();
            return new String( FileCopyUtils.copyToByteArray( inputStream ), StandardCharsets.UTF_8 );
        }
        catch( IOException ex ){
            return null;
        }
    }
}
