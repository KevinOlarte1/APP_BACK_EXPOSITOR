package com.gestorventas.deposito.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gestorventas.deposito.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entidad que representa el Vendedor/Usuario de nuestro programa
 * Estas entidades son las que se loggearan y tendran acceso a la app.
 */
@Entity
@Table(name = "vendedores")
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del vendedor.
     */
    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    /**
     * Contraseña del vendedor
     */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 512)
    private String refreshToken;
    private LocalDateTime refreshTokenExpiry;

    private String resetCode;
    private LocalDateTime resetCodeExpiry;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "vendedor_roles",
            joinColumns = @JoinColumn(name = "vendedor_id")
    )
    @Column(name = "role")
    private Set<Role> roles;


    /**
     * Lista de clientes que gestiona este vendedor.
     */
    @OneToMany(mappedBy = "vendedor", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Cliente> clientes = new LinkedHashSet<>();


    public Vendedor(String nombre, String apellido, String password,String email) {
        this.nombre = nombre;
        this.password = password;
        this.email = email;
        this.apellido = apellido;
    }

}
