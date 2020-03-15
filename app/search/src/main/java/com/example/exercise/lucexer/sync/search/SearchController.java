package com.example.exercise.lucexer.sync.search;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.exercise.lucexer.sync.search.service.SearchService;

/**
 * DemoController
 *
 * @author Deven
 * @version : DemoController, v 0.1 2020-03-08 22:17 Deven Exp$
 */
@RequestMapping("/search")
@Controller
public class SearchController {

    private final Logger               logger = LoggerFactory.getLogger(getClass());

    @Resource
    private SearchService searchService;

    @RequestMapping("/queryByFamilyName/{familyName}")
    public @ResponseBody Object queryByFamilyName(@PathVariable("familyName") String familyName) {
        return searchService.queryByFamilyName(familyName);
    }

    @RequestMapping("/queryByAge")
    public @ResponseBody Object queryByAgeRange(@RequestParam("from") Integer fromAge, @RequestParam("to") Integer toAge) {
        return searchService.queryByAgeRange(fromAge, toAge);
    }

    @RequestMapping("/queryByCityAreaAndScoreLimit")
    public @ResponseBody Object queryByCityAreaAndScoreLimit(@RequestParam("cityArea") String cityArea, @RequestParam("scoreLimit") Integer scoreLimit) {
        return searchService.queryByCityAreaAndScoreLimit(cityArea, scoreLimit);
    }

    @RequestMapping("/queryTop100ByProvinceAndSexAndHouseNumber")
    public @ResponseBody Object queryTop100ByProvinceAndSexAndHouseNumber(@RequestParam("province") String province, @RequestParam("sex") String sex,
                                                            @RequestParam("houseNumber") String houseNumber) {
        return searchService.queryTop100ByProvinceAndSexAndHouseNumber(province, sex, houseNumber);
    }

    @RequestMapping("/summaryByFamilyName")
    public @ResponseBody Object summaryByFamilyName() {
        return searchService.summaryByFamilyName();
    }

    @RequestMapping("/summaryByCityAndHuaxueFail")
    public @ResponseBody Object summaryByCityAndHuaxueFail() {
        return searchService.summaryByCityAndHuaxueFail();
    }

    @RequestMapping("/summaryScorePerformanceByVillaHouse")
    public @ResponseBody Object summaryScorePerformanceByVillaHouse() {
        return searchService.summaryScorePerformanceByVillaHouse();
    }
}
