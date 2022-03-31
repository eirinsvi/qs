package ntnu.karolisw.project_backend.model;

import lombok.*;
import org.apache.tomcat.jni.Address;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
/**
 * This table has a many-to-many relationship with teachers
 * This table has a many-to-many relationship with students
 * This table has a many-to-one relationship with administrator (teacher)
 * This table has a one-to-many relationship with queues
 */
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_code", nullable = false)
    private String courseCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "expected_end_date", nullable = false)
    private Date expectedEndDate;

    @Column(name = "number_of_assignments", nullable = false)
    private int numberOfAssignments;

    @Column(name = "min_approved_assignments", nullable = false)
    private int minApprovedAssignments;

    @Column(name = "number_parts_assignments", nullable = false)
    private int numberPartsAssignments;

    @Column(name = "archived", nullable = false)
    private boolean archived;

    // Many-to-many with student
    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();

    // Many-to-many with teacher
    @ManyToMany(mappedBy = "courses")
    private Set<Teacher> teachers = new HashSet<>();

    // Many-to-many with student in a student assistant relationship
    @ManyToMany(mappedBy = "courses")
    private Set<Student> studentAssistants = new HashSet<>();

    // One-to-many relationship with group of assignments
    @OneToMany(mappedBy = "course")
    private Set<GroupOfAssignment> groupsOfAssignments = new HashSet<>();

    // Queue is child, course is parent
    @OneToOne(mappedBy = "course")
    private Queue queue;

    // Add a group of assignment to the list of groups of assignments
    public void addGroupOfAssignment(GroupOfAssignment groupOfAssignments) {
        groupsOfAssignments.add(groupOfAssignments);
    }

    // Add a student assistant to a course
    public void addStudentAssistant(Student studentAssistant) {
        studentAssistants.add(studentAssistant);
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
    }
}
