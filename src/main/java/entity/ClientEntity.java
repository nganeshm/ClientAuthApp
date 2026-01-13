package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

@Data
@Entity
@Table(name = "client", uniqueConstraints = @UniqueConstraint(columnNames = { "clientName" }))
public class ClientEntity {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    private String clientId;

    @Column(nullable = false, unique = true)
    private String clientName;

    @Column(nullable = false, name = "client_email")
    private String clientEmail;

    @Column(name = "subject")
    private String clientSubject;

    @Column(name = "message")
    private String clientMessage;

}

