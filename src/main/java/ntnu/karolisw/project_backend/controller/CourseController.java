package ntnu.karolisw.project_backend.controller;

import ntnu.karolisw.project_backend.dto.in.CourseIn;
import ntnu.karolisw.project_backend.dto.in.GroupIn;
import ntnu.karolisw.project_backend.dto.in.PersonIn;
import ntnu.karolisw.project_backend.service.interfaces.CourseServiceI;
import ntnu.karolisw.project_backend.service.interfaces.UserServiceI;
import ntnu.karolisw.project_backend.service.interfaces.QueueServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@CrossOrigin("http://192.168.1.80:8081/") // Vue address

public class CourseController {

    @Autowired
    CourseServiceI courseService;

    @Autowired
    UserServiceI userService;

    @Autowired
    QueueServiceI queueService;


    @GetMapping()
    public ResponseEntity<Object> getAllCourses() {
        return new ResponseEntity<>(courseService.getAllCourses(), HttpStatus.OK);
    }

    @GetMapping("/teachers/{teacherId}")
    public ResponseEntity<Object> getAllCoursesForTeacher(@PathVariable("teacherId") long id) {
        return courseService.getAllCoursesByTeacherId(id);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<Object> getAllCoursesForStudent(@PathVariable("studentId") long id) {
        return courseService.getAllCoursesForStudent(id);
    }

    @GetMapping("/students/{courseId}")
    public ResponseEntity<Object> getAllStudentsInCourse(@PathVariable("courseId") long courseId){
        return courseService.getAllStudentsInCourse(courseId);
    }

    @GetMapping("/studentAssistants/{studentId}")
    public ResponseEntity<Object> getAllCoursesForStudentAssistant(@PathVariable("studentId") long id) {
        return courseService.getAllCoursesForStudentAssistant(id);
    }

    // assignmentNr, approved
    @PostMapping("/assignments")
    public ResponseEntity<Object> getAllAssignmentsInCourseForStudentAndIfApproved(@RequestBody CourseIn dto){
        return courseService.getAllAssignmentsForStudentInCourse(dto.getPersonId(),dto.getCourseId());
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<Object> getCourseById(@PathVariable("courseId") long courseId){
        return courseService.getCourse(courseId);
    }


    @PostMapping("/addNew")
    public ResponseEntity<Object> postNewCourse(@RequestBody CourseIn dto) {
        return courseService.createCourse(dto);
    }


    /**
     * While it is possible to create underGroups upon creation of course, it is also possible
     * to add underGroups after a course is created
     *
     * @param dto contains the assignments that are to be placed in the group
     */
    @PostMapping("/newGroup")
    public ResponseEntity<Object> postUnderGroupCourse(@RequestBody GroupIn dto) {
        try {
            courseService.addGroupOfAssignmentToCourse(dto);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Deletes a course
     * @param courseId is the course to delete
     *
     * @return HttpStatus OK or NOT_FOUND
     */
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Object> deleteCourse(@PathVariable("courseId") long courseId){
        return courseService.deleteCourse(courseId);
    }

    /**
     * To be able to archive a course, there must be both a course and a queue created
     * @param dto
     * @return
     */
    @PostMapping("/archive")
    public ResponseEntity<Object> archiveCourse(@RequestBody CourseIn dto){
        return courseService.archiveCourse(dto.getCourseId());
    }
    @PostMapping("/addStudent")
    public ResponseEntity<Object> addStudentToCourse(@RequestBody PersonIn dto){
        return courseService.addStudentToCourse(dto);
    }

    @PostMapping("/addTeacher")
    public ResponseEntity<Object> addTeacherToCourse(@RequestBody PersonIn dto){
        return courseService.addTeacherToCourse(dto);
    }

    @PostMapping("/addStudentAssistant")
    public ResponseEntity<Object> addStudentAssistantToCourse(@RequestBody PersonIn dto){
        return courseService.addStudentAssistantToCourse(dto);
    }

    @DeleteMapping("/removeStudent")
    public ResponseEntity<Object> deleteStudentFromCourse(@RequestBody PersonIn student){
        return courseService.removeStudentFromCourse(student.getCourseId(), student.getEmail());
    }
}
