package com.pagos.pagoservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pagos.pagoservice.models.Pagos;

public interface PagosRepository extends MongoRepository<Pagos, String>{
    
    List<Pagos> findByOrdenid(String ordenid);
}
