package ntnu.karolisw.project_backend.service.classes;

import ntnu.karolisw.project_backend.dto.in.CourseIn;
import ntnu.karolisw.project_backend.dto.in.GroupIn;
import ntnu.karolisw.project_backend.dto.out.AssignmentOut;
import ntnu.karolisw.project_backend.dto.out.PersonOut;
import ntnu.karolisw.project_backend.model.*;
import ntnu.karolisw.project_backend.repository.*;
import ntnu.karolisw.project_backend.service.interfaces.CourseServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CourseService implements CourseServiceI {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GroupOfAssignmentRepository groupOfAssignmentRepository;

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    // Mark a specified course as archived
    @Override
    public ResponseEntity<Object> markAsArchived(long courseId) {
        Optional<Course> course = courseRepository.findById(courseId);

        // Archive the course if present
        if(course.isPresent()) {
            course.get().setArchived(true);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        // Else, the course was not found
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // setArchived --> when course is archived --> the queue must be deleted!
    @Override
    public ResponseEntity<Object> archiveCourse(long courseId) {
        // Get the course
        Optional<Course> course = courseRepository.findById(courseId);
        if (course.isPresent()) {
            // Archive the course
            course.get().setArchived(true);

            // Delete the queue
            long queueId = courseRepository.getQueueByCourseId(courseId).getQueueId();
            queueRepository.delete(queueRepository.getById(queueId)); //todo cascade needed!!
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private boolean validateCourse(CourseIn newCourse) {
        // If new course contains all arguments it should
        return newCourse.getCourseCode() != null && newCourse.getExpectedEndDate() != null &&
                newCourse.getStartDate() != null && newCourse.getMinApprovedAssignments() != 0 &&
                newCourse.getNumberOfAssignments() != 0 && newCourse.getName() != null;
    }


    @Override
    public ResponseEntity<Object> createCourse(CourseIn newCourse) {
        // If the dto contains all arguments it should, we can proceed
        if(validateCourse(newCourse)) {

            // We create a course object
            Course course = new Course();

            // Set all the variables needed
            course.setCourseCode(newCourse.getCourseCode());
            course.setArchived(false);
            course.setExpectedEndDate(newCourse.getExpectedEndDate());
            course.setStartDate(newCourse.getStartDate());
            course.setMinApprovedAssignments(newCourse.getMinApprovedAssignments());
            course.setNumberOfAssignments(newCourse.getNumberOfAssignments());
            course.setName(newCourse.getName());

            // Get all the assignments sent from frontend
            List<Set<Assignment>> assignmentList = newCourse.getGroupsOfAssignments();

            // For all groups of assignment, create a group of assignment object
            for(Set<Assignment> group : assignmentList) {
                GroupOfAssignment groupOfAssignment = new GroupOfAssignment();
                groupOfAssignment.setAssignments(group);

                // Add each individual group to the course object
                course.addGroupOfAssignment(groupOfAssignment);

                // Add each group to the GroupOfAssignmentRepository
                groupOfAssignmentRepository.save(groupOfAssignment);
            }

            // When all groups are added, the course object is finished and may be added to the db!
            courseRepository.save(course);

            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        // If the dto did not contain the proper attributes (parameters)
        else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // get all students in a course
    @Override
    public ResponseEntity<Object> getAllStudentsInCourse(long courseId) {
        Optional<Course> course = courseRepository.findById(courseId);
        if(course.isPresent()) {

            // Get students
            Set<Student> students = course.get().getStudents();
            ArrayList<PersonOut> students2 = new ArrayList<>(students.size());

            // Shape student entity-objects into correct data-transfer-objects (security)
            for(Student student: students) {
                PersonOut so = new PersonOut();
                so.setFirstName(student.getFirstName());
                so.setLastName(student.getLastName());
                try {
                    so.setApprovedAssignmentsInCourse(getAllApprovedAssignmentsForStudent(student.getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                students2.add(new PersonOut());
            }
            // Return the DTO
            return new ResponseEntity<>(students2, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // add a list of student assistant to a course
    @Override
    public ResponseEntity<Object> addStudentAssistant(long courseId, String studentEmail) {
        Optional<Course> course = courseRepository.findById(courseId);
        Optional<Student> student = studentRepository.findByEmail(studentEmail);
        if(course.isPresent() && student.isPresent()) {
            course.get().addStudentAssistant(student.get());
            courseRepository.save(course.get()); // todo necessary
            return new ResponseEntity<>( HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // add a teacher to a course
    @Override
    public ResponseEntity<Object> addTeacher(long courseId, String teacherEmail) {
        Optional<Course> course = courseRepository.findById(courseId);
        Optional<Teacher> teacher = teacherRepository.findByEmail(teacherEmail);
        if(course.isPresent() && teacher.isPresent()) {
            course.get().addTeacher(teacher.get());
            courseRepository.save(course.get());
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // add a student to a course
    @Override
    public ResponseEntity<Object> addStudent(long courseId, String studentEmail) {
        Optional<Course> course = courseRepository.findById(courseId);
        Optional<Student> student = studentRepository.findByEmail(studentEmail);
        if(course.isPresent() && student.isPresent()) {
            course.get().addStudent(student.get());
            courseRepository.save(course.get()); // TODO maybe it updates itself?
            return new ResponseEntity<>( HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Deletes a student from a course
    @Override
    public ResponseEntity<Object> removeStudent(long courseId, long studentId) {
        // if the course exists
        if(courseRepository.existsById(courseId)){
            // get all the students taking the course
            Set<Student> students = courseRepository.getStudentsByCourseId(courseId);

            // If there is a student with the specified student id, remove it
            students.removeIf(student -> student.getId() == studentId);

            // todo if this is necessary, remember to fix cascade
            for(Student student : students) {
                if(student.getId() == studentId) {
                    studentRepository.delete(student);
                }
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // update start date
    @Override
    public ResponseEntity<Object> updateStartDate(long courseId, Date startDate) {
        // Get the course
        Optional<Course> course = courseRepository.findById(courseId);

        if(course.isPresent()) {
            // Update its start date
            course.get().setStartDate(startDate);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // update end date
    @Override
    public ResponseEntity<Object> updateEndDate(long courseId, Date endDate) {
        // Get the course
        Optional<Course> course = courseRepository.findById(courseId);

        if(course.isPresent()) {
            // Update its end date
            course.get().setExpectedEndDate(endDate);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // update number of assignments
    @Override
    public ResponseEntity<Object> updateNumberOfAssignments(long courseId, int numberOfAssignments) {
        // Get the course
        Optional<Course> course = courseRepository.findById(courseId);
        if(course.isPresent()) {
            course.get().setNumberOfAssignments(numberOfAssignments);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // update min_approved_assignments
    @Override
    public ResponseEntity<Object> updateMinApprovedAssignments(long courseId, int numberOfApprovedAssignments) {
        // Get the course
        Optional<Course> course = courseRepository.findById(courseId);
        if(course.isPresent()) {
            course.get().setMinApprovedAssignments(numberOfApprovedAssignments);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // update number_parts_assignments
    @Override
    public ResponseEntity<Object> updateNumberPartsAssignments(long courseId, int numberOfParts) {
        // Get the course
        Optional<Course> course = courseRepository.findById(courseId);
        if(course.isPresent()) {
            course.get().setNumberPartsAssignments(numberOfParts);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // get number of approved assignments necessary for a certain course
    @Override
    public ResponseEntity<Object> getNumberOfApprovedAssignmentsByCourse(long courseId) {
        Optional<Course> course = courseRepository.findById(courseId);

        // If the course exists, retrieve how many (minimum) assignments must be approved
        if(course.isPresent()) {
            int minApprovedAssignments = course.get().getMinApprovedAssignments();
            return new ResponseEntity<>(minApprovedAssignments, HttpStatus.OK);
        }
        // If the course was not found
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // get all the assignments for a student with student id
    @Override
    public ResponseEntity<Object> getAllAssignmentsForStudent(long studentId) {
        // Create dto list
        ArrayList<AssignmentOut> assignments = new ArrayList<>();

        // Check that the student exists
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isPresent()) {

            Set<Assignment> allAssignments = studentRepository.getAssignmentsByStudentId(studentId);
            // Get all assignments with the correct course id and add them to assignments list
            for(Assignment assignment: allAssignments) {
                if(assignment.isApproved()) {
                    AssignmentOut ao = new AssignmentOut();
                    ao.setApproved(true);
                    ao.setAssignmentNumber(assignment.getAssignmentNumber());
                    assignments.add(ao);
                }
            }
            // Return the dto
            return new ResponseEntity<>(assignments, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // getAssignmentsByStudentId
    @Override
    public ResponseEntity<Object> getAllAssignmentsForStudentInCourse(long studentId, long courseId) {
        // List of assignments that are from the specified course
        ArrayList<AssignmentOut> assignments = new ArrayList<>();

        // Check that the student exists
        Optional<Student> student = studentRepository.findById(studentId);

        if (student.isPresent()) {

            // Get all assignments the student has
            Set<Assignment> allAssignments = studentRepository.getAssignmentsByStudentId(studentId);

            // Get all assignments with the correct course id and add them to assignments list
            for(Assignment assignment: allAssignments) {
                // Get assignment group id
                long groupId = assignmentRepository.getGroupIdOfAssignment(assignment.getAssignmentId());
                // Get course id
                long courseId2 = groupOfAssignmentRepository.getCourseIdOfGroup(groupId);

                // If the assignment has the correct course id
                if(courseId2 == courseId) {
                    // See if approved
                    if(assignment.isApproved()) {
                        AssignmentOut ao = new AssignmentOut();
                        ao.setApproved(true);
                        ao.setAssignmentNumber(assignment.getAssignmentNumber());
                        assignments.add(ao);
                    }
                }
            }
            // Return the dto
            return new ResponseEntity<>(assignments, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // get number of approved assignments for a student with student id
    @Override
    public int getAllApprovedAssignmentsForStudent(long studentId) throws Exception {
        // Create a counter
        int numberOfApprovedAssignments = 0;

        // Check that the student exists
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isPresent()) {
            Set<Assignment> allAssignments = studentRepository.getAssignmentsByStudentId(studentId);
            for(Assignment assignment : allAssignments) {
                if(assignment.isApproved()) {
                    numberOfApprovedAssignments ++;
                }
            }
            return numberOfApprovedAssignments;
        }
        else {
            throw new Exception("Could not find student with id: " + studentId);
        }
    }

    // get all courses for student with student id (whole object)
    @Override
    public ResponseEntity<Object> getAllCoursesForStudent(long studentId) {
        // Check that the student exists
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isPresent()) {
            Set<Course> allCourses = studentRepository.getCoursesByStudentId(studentId);
            return new ResponseEntity<>(allCourses, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<Object> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> getAllCoursesByTeacherId(long teacherId) {
        Set<Course> courses = teacherRepository.getCoursesByTeacherId(teacherId);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @Override
    public List<GroupOfAssignment> getAllGroupsOfAssignmentByCourseId(long courseId) {
        // If course exists...
        Optional<Course> course = courseRepository.findById(courseId);
        if(course.isPresent()) {
            return courseRepository.getAllGroupsOfAssignmentByCourseId
                    (courseId);
        }
        else {
            return null;
        }
    }

    @Override
    public boolean addGroupOfAssignmentToCourse(GroupIn dto) throws Exception {
        // Get all groups of assignments in the course
        List<GroupOfAssignment> groups = getAllGroupsOfAssignmentByCourseId(dto.getCourseId());

        if(groups != null) {

            // Get the groups from dto
            Set<Assignment> newGroup = dto.getGroupOfAssignments();

            // For all groups (could be only one group, but works either way)
            for(GroupOfAssignment group : groups) {
                // For all assignments in the group
                for(Assignment assignment : group.getAssignments()) {
                    // For all assignments in the new group
                    for(Assignment a: newGroup) {
                        // If newGroup contains an assignment with assignment id == assignment id of previous assignment
                        if (Objects.equals(assignment.getAssignmentId(), a.getAssignmentId())) {
                            // Remove the assignment that was there from the start in the course
                            group.removeAssignment(assignment);
                        }
                    }
                }
            }
            // When all assignments that had to be removed are removed, the new group is added to the course
            GroupOfAssignment goa = new GroupOfAssignment();
            goa.setApprovedAssignments(0);
            goa.setNumberOfAssignment(dto.getNumOfPractices());
            goa.setOrderNr(dto.getOrderNumber());
            goa.setMinApprovedAssignmentsInGroup(dto.getMinimumNumApproved());
            goa.setAssignments(newGroup);

            // Set group
            Optional<Course> course = courseRepository.findById(dto.getCourseId());

            // If the course id was present in db
            if(course.isPresent()){
                // Add the course as foreign key
                goa.setCourse(course.get());
                // Add the group to the course as foreign key
                course.get().addGroupOfAssignment(goa);
            }
            else {
                throw new Exception("The course id: " + dto.getCourseId() + " did not exist.");
            }
        }
        return false;
    }

    /**
     * Deletes the course (not archive)
     *
     * @param courseId is the course to delete
     */
    @Override
    public ResponseEntity<Object> deleteCourse(long courseId) {
        Optional<Course> course = courseRepository.findById(courseId);
        if(course.isPresent()) {
            courseRepository.delete(course.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}