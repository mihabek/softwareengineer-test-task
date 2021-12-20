package com.klausapp.scoringapi.controller;

import com.klausapp.scoringapi.model.CategoryScoreWrapper;
import com.klausapp.scoringapi.model.ScoreChange;
import com.klausapp.scoringapi.model.TicketScore;
import com.klausapp.scoringapi.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/score")
@RequiredArgsConstructor
public class ScoringController{
    private final ScoringService scoringService;

    @GetMapping("/categories")
    public CategoryScoreWrapper getCategoryScores(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate
    ){
        return scoringService.findCategoryScores( startDate, endDate );
    }

    @GetMapping("/tickets")
    public List<TicketScore> getTicketScores(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate
    ){
        return scoringService.findTicketScores( startDate, endDate );
    }

    @GetMapping("/overall")
    public BigDecimal getOverallScore(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate
    ){
        return scoringService.getOverallScore( startDate, endDate );
    }

    @GetMapping("/overall-change")
    public ScoreChange getOverallScoreChange(
            @RequestParam("startDatePrevious") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDatePrevious,
            @RequestParam("endDatePrevious") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDatePrevious,
            @RequestParam("startDateSelected") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDateSelected,
            @RequestParam("endDateSelected") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDateSelected
    ){
        return scoringService.getOverallScoreChange( startDatePrevious, endDatePrevious, startDateSelected, endDateSelected );
    }
}
