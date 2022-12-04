package com.whoIsLeader.GooDoggy.subscription.controller;

import com.whoIsLeader.GooDoggy.subscription.DTO.PersonalReq;
import com.whoIsLeader.GooDoggy.subscription.service.PersonalService;
import com.whoIsLeader.GooDoggy.subscription.service.StatisticsService;
import com.whoIsLeader.GooDoggy.user.DTO.StatisticsRes;
import com.whoIsLeader.GooDoggy.util.BaseException;
import com.whoIsLeader.GooDoggy.util.BaseResponse;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = "/statistics")
public class StatisticsController {

    private StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService){
        this.statisticsService = statisticsService;
    }

    @ResponseBody
    @GetMapping("/category")
    public BaseResponse<List<StatisticsRes.category>> getCategory(HttpServletRequest request) {
        try {
            List<StatisticsRes.category> categoryList = this.statisticsService.getCategoryStatistics(request);
            return new BaseResponse<>(categoryList);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }
}
