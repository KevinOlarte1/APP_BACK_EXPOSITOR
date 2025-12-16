package com.gestorventas.deposito.repositories;

import com.gestorventas.deposito.models.ParametrosGlobales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParametrosGlobalesRepository extends JpaRepository<ParametrosGlobales, Long> {

}

