package com.example.exercise.lucexer.demo;

import javax.annotation.Resource;

import com.example.exercise.lucexer.dal.lucene.dao.StudentTranscriptLuceneDAO;
import com.example.exercise.lucexer.demo.biz.DemoBizService;
import com.example.exercise.lucexer.sync.IndexDataSyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * DemoController
 *
 * @author Deven
 * @version : DemoController, v 0.1 2020-03-08 22:17 Deven Exp$
 */
@RequestMapping("/demo")
@Controller
public class DemoController {

    private final Logger               logger = LoggerFactory.getLogger(getClass());

    @Resource
    private DemoBizService             demoBizService;

    @Resource
    private IndexDataSyncManager       indexDataSyncManager;

    @Resource
    private StudentTranscriptLuceneDAO studentTranscriptLuceneDAO;

    @RequestMapping("/student/{id}")
    public @ResponseBody Object findStudentById(@PathVariable("id") Long id) {
        logger.info("http searchById: {}", id);
        return demoBizService.findStudentById(id);
    }

    @RequestMapping("/student")
    public @ResponseBody Object findStudentsByName(@RequestParam("name") String name) {
        logger.info("http searchByName: {}", name);
        return demoBizService.findStudentsByName(name, false);
    }

    @RequestMapping("/student/list")
    public @ResponseBody Object findStudentsByRange(@RequestParam("from") Long fromStudentId, @RequestParam("to") Long toStudentId) {
        return demoBizService.findStudentsByRange(fromStudentId, toStudentId);
    }

    @RequestMapping("/student/stat")
    public @ResponseBody Object queryStudentMaxMinIdInfo() {
        return demoBizService.queryStudentMaxMinIdInfo();
    }

    @RequestMapping("/course/list")
    public @ResponseBody Object listCourses() {
        return demoBizService.listAllCourses();
    }

    @RequestMapping("/student/transcripts")
    public @ResponseBody Object queryStudentTranscripts(@RequestParam("from") Long fromStudentId, @RequestParam("to") Long toStudentId) {
        return demoBizService.queryStudentTranscripts(fromStudentId, toStudentId);
    }

    @RequestMapping("/sync/all")
    public @ResponseBody Object syncAll() {
        indexDataSyncManager.syncAll();
        return "OK";
    }

    @RequestMapping("/sync/range")
    public @ResponseBody Object syncRange(@RequestParam("from") Long fromStudentId, @RequestParam("to") Long toStudentId) {
        indexDataSyncManager.syncRange(fromStudentId, toStudentId);
        return "OK";
    }

    @RequestMapping("/search/familyName/{familyName}")
    public @ResponseBody Object searchByFamilyName(@PathVariable("familyName") String familyName) {
        return studentTranscriptLuceneDAO.queryByFamilyName(familyName);
    }

    @RequestMapping("/search/age")
    public @ResponseBody Object searchByAgeRange(@RequestParam("from") Integer fromAge, @RequestParam("to") Integer toAge) {
        return studentTranscriptLuceneDAO.queryByAgeRange(fromAge, toAge);
    }

    @RequestMapping("/search/byCityAreaAndScoreLimit")
    public @ResponseBody Object searchCityAreaAndScoreLimit(@RequestParam("cityArea") String cityArea, @RequestParam("scoreLimit") Integer scoreLimit) {
        return studentTranscriptLuceneDAO.queryByCityAreaAndScoreLimit(cityArea, scoreLimit);
    }

    @RequestMapping("/search/queryTop100ByProvinceAndSexAndHouseNumber")
    public @ResponseBody Object searchCityAreaAndScoreLimit(@RequestParam("province") String province, @RequestParam("sex") String sex, @RequestParam("houseNumber") String houseNumber) {
        return studentTranscriptLuceneDAO.queryTop100ByProvinceAndSexAndHouseNumber(province, sex, houseNumber);
    }

    @RequestMapping("/search/summaryByFamilyName")
    public @ResponseBody Object summaryByFamilyName() {
        return studentTranscriptLuceneDAO.summaryByFamilyName();
    }

    @RequestMapping("/search/summaryByCityAndHuaxueFail")
    public @ResponseBody Object summaryByCityAndHuaxueFail() {
        return studentTranscriptLuceneDAO.summaryByCityAndHuaxueFail();
    }

    @RequestMapping("/search/summaryScorePerformanceByVillaHouse")
    public @ResponseBody Object summaryScorePerformanceByVillaHouse() {
        return studentTranscriptLuceneDAO.summaryScorePerformanceByVillaHouse();
    }
}
