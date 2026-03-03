package com.gestorventas.deposito.repositories;

import com.gestorventas.deposito.enums.Role;
import com.gestorventas.deposito.models.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceder y gestionar entidades {@link Vendedor}
 * <p>
 *     Permite realizar operaciones CRUD
 * </p>
 * @author Kevin William Olarte Braun.
 */
@Repository
public interface VendedorRepository extends JpaRepository<Vendedor, Long> {

    Optional<Vendedor> findByEmail(String email);
    public Vendedor findById(long id);
    Optional<Vendedor> findByEmailIgnoreCase(String email);

    @Query("""
        SELECT v
        FROM Vendedor v
        JOIN v.roles r
        WHERE r = :role
    """)
    List<Vendedor> findByRole(@Param("role") Role role);


}
