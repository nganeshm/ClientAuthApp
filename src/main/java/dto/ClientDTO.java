package dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ClientDTO {

    private String clientName;
    private String clientEmail;
    private String clientSubject;
    private String clientMessage;

}
