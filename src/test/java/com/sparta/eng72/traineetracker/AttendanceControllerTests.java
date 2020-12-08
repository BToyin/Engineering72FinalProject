package com.sparta.eng72.traineetracker;

import com.sparta.eng72.traineetracker.controllers.AttendanceController;
import com.sparta.eng72.traineetracker.entities.Trainee;
import com.sparta.eng72.traineetracker.entities.TraineeAttendance;
import com.sparta.eng72.traineetracker.entities.Trainer;
import com.sparta.eng72.traineetracker.services.TraineeService;
import com.sparta.eng72.traineetracker.services.TrainerService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class AttendanceControllerTests {
    @Autowired
    private AttendanceController attendanceController;

    @Autowired
    private TraineeService traineeService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private MockMvc mockMvc;

    private final String trainerName = "MGadhvi@sparta.com";
    private final String trainerPw = "startrek";
    private final String traineeName = "bbird@spartaglobal.com";
    private final String traineePw = "test";


    @Test
    @WithMockUser(username =trainerName , password = trainerPw,roles = "TRAINER")
    public void getTraineeListTest() throws Exception{
        Optional<Trainer> trainer = trainerService.getTrainerByUsername("MGadhvi@sparta.com");
        List<Trainee> trainees = traineeService.getTraineesByGroupId(trainer.get().getGroupId());
        this.mockMvc.perform(get("/trainer/home")).andDo(print()).andExpect(status().isOk()).andExpect(model().attribute(
                "traineeList",trainees));
    }

    @Test
    @WithMockUser(username =trainerName , password = trainerPw,roles = "TRAINER")
    public void displayAttendanceTest() throws Exception {
        this.mockMvc.perform(get("/trainer/traineeAttendance/5")).andDo(print()).andExpect(status().isOk()).andExpect(model().attributeExists(
                "attendanceReports","trainee"));
    }

    @Test
    @WithMockUser(username =trainerName , password = trainerPw,roles = "TRAINER")
    public void getCorrectTraineeTest() throws Exception {
        this.mockMvc.perform(get("/trainer/traineeAttendance/5")).andDo(print()).andExpect(status().isOk()).andExpect(model().attribute(
                "trainee", Matchers.hasProperty("traineeId", Matchers.equalTo(5))));
    }

    @Test
    @WithMockUser(username =trainerName , password = trainerPw,roles = "TRAINER")
    public void attendanceEntryTest() throws Exception {
        this.mockMvc.perform(get("/trainer/attendanceEntry")).andDo(print()).andExpect(status().isOk()).andExpect(model().attributeExists(
                "courseStartDate","trainees", "today", "traineeAttendance"));
    }

    @Test
    @WithMockUser(username = traineeName , password = traineePw,roles = "TRAINEE")
    public void traineeViewAttendanceTest() throws Exception {
        this.mockMvc.perform(get("/trainee/trainee-attendance")).andDo(print()).andExpect(status().isOk()).andExpect(model().attributeExists("attendanceReports", "trainee"));
    }

    @Test
    @WithMockUser(username = traineeName , password = traineePw,roles = "TRAINEE")
    public void traineePercentagesTest() throws Exception {
        this.mockMvc.perform(get("/trainee/profile-percentage")).andDo(print()).andExpect(status().isOk()).andExpect(model().attributeExists("onTimePercentage", "latePercentage", "excusedPercentage", "unexcusedPercentage"));
    }

    @Test
    @WithMockUser(username = trainerName, password = trainerPw, roles = "TRAINER")
    public void weeklyAttendanceTest() throws Exception {
        this.mockMvc.perform(get("/trainer/weekly-attendance")).andExpect(status().isOk()).andExpect(model().attributeExists("days", "currentWeek", "reports"));
    }

    @Test

    public void getWeeklyAttendanceReports() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Trainer trainer = trainerService.getTrainerByUsername("MGadhvi@sparta.com").get();
        int currentWeek = 10;
        Class<?>[] parameters = {Trainer.class, int.class};
        Method method = AttendanceController.class.getDeclaredMethod("getWeeklyAttendanceReports", parameters);
        method.setAccessible(true);
        Map<Integer, Map<Trainee, List<TraineeAttendance>>> results = (Map<Integer, Map<Trainee, List<TraineeAttendance>>>) method.invoke(attendanceController, trainer, currentWeek);
        Assertions.assertEquals(10, results.size());
    }
}
