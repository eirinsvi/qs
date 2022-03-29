package ntnu.karolisw.project_backend.model;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "students")

/**
 * This table extends the Person superClass
 *
 * This table has a many-to-many connection with course
 * This table has a many-to-many connection with assignment (this table = parent entity)
 */
public class Student extends Person {
    /**
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private long studentId; // autoIncrement

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;
    */

    // Many-to-many connection with assignment
    @ManyToMany(cascade = {CascadeType.ALL} )
    @JoinTable(
            name = "assignment_student",
            joinColumns = { @JoinColumn(name = "student_id") },
            inverseJoinColumns = { @JoinColumn(name = "assignment_id") }
    )
    private Set<Assignment> assignments = new HashSet<>();

    // Many-to-many connection with courses
    @ManyToMany(cascade = {CascadeType.ALL} )
    @JoinTable(
            name = "course_student",
            joinColumns = { @JoinColumn(name = "student_id") },
            inverseJoinColumns = { @JoinColumn(name = "course_id") }
    )
    private Set<Assignment> courses = new HashSet<>();

    // Creating the student assistant table as a many-to-many connection with course
    @ManyToMany(cascade = {CascadeType.ALL} )
    @JoinTable(
            name = "student_assistant",
            joinColumns = { @JoinColumn(name = "student_id") },
            inverseJoinColumns = { @JoinColumn(name = "course_id") }
    )
    private Set<Assignment> studentAssistants = new HashSet<>();

    // One-to-one relationship with student_in_queue
    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StudentInQueue studentInQueue;

}