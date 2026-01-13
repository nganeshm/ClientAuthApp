package service;

import dto.ClientDTO;
import entity.ClientEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repo.ClientRepository;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository repository;
    private final EmailService emailService;

    @Autowired
    public ClientServiceImpl(ClientRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    @Override
    public boolean saveClientDetails(ClientDTO clientDTO) {

        if(clientDTO.getClientName() == null || clientDTO.getClientEmail() == null) {
            return false;
        }

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setClientName(clientDTO.getClientName());
        clientEntity.setClientEmail(clientDTO.getClientEmail());
        clientEntity.setClientSubject(clientDTO.getClientSubject());
        clientEntity.setClientMessage(clientDTO.getClientMessage());

        repository.save(clientEntity);

        // Send welcome email after successful registration
        emailService.sendWelcomeEmail(clientDTO.getClientEmail(), clientDTO.getClientName());

        return true;
    }
}
