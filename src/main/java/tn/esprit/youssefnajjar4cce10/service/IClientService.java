package tn.esprit.youssefnajjar4cce10.service;

import tn.esprit.youssefnajjar4cce10.domain.Client;

import java.util.List;

public interface IClientService {
    Client create(Client client);
    Client findById(Long id);
    List<Client> findAll();
    Client update(Long id, Client client);
    void deleteById(Long id);
}
