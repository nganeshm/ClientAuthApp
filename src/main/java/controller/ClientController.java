package controller;

import dto.ClientDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.ClientService;

@CrossOrigin(origins = "http://mywebstack2025.s3-website.ap-south-1.amazonaws.com")
@RestController
@RequestMapping("/api/client")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping("/saveClient")
    public boolean saveClientDetails(@RequestBody ClientDTO clientDTO) {
        return clientService.saveClientDetails(clientDTO);
    }
}
