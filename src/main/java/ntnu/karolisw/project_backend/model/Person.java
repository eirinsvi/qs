package ntnu.karolisw.project_backend.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * Teacher and student inherits from Person
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@MappedSuperclass
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "pronouns", nullable = false)
    private String pronouns;


    // Timestamp for usage in frontend
    @Temporal( TemporalType.TIMESTAMP )
    @CreationTimestamp
    @Column(name = "created_on")
    private Date createdOn;
}