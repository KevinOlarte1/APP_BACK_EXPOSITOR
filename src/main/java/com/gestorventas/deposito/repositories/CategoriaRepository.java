package com.gestorventas.deposito.repositories;

import com.gestorventas.deposito.models.producto.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria,Long> {

    Categoria findById(long id);

    Categoria findByNombre(String nombre);
}
