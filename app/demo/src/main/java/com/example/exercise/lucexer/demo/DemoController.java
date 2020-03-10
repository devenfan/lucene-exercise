package com.example.exercise.lucexer.demo;

import javax.annotation.Resource;

import com.example.exercise.lucexer.dal.mybatis.domain.StudentTranscriptDO;
import com.example.exercise.lucexer.demo.biz.DemoBizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * DemoController
 *
 * @author Deven
 * @version : DemoController, v 0.1 2020-03-08 22:17 Deven Exp$
 */
@RequestMapping("/demo")
@Controller
public class DemoController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private DemoBizService demoBizService;

    @RequestMapping("/search/{id}")
    public @ResponseBody Object searchById(@PathVariable("id") Long id) {
        logger.info("http searchById: {}", id);
        return demoBizService.findStudentById(id);
    }

    @RequestMapping("/search")
    public @ResponseBody Object searchByName(@RequestParam("name") String name) {
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

}
