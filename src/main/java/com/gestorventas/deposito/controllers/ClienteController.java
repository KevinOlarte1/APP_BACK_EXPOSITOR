package com.gestorventas.deposito.controllers;

import com.gestorventas.deposito.dto.in.ClienteDto;
import com.gestorventas.deposito.dto.out.ClienteResponseDto;
import com.gestorventas.deposito.enums.Role;
import com.gestorventas.deposito.models.Vendedor;
import com.gestorventas.deposito.repositories.VendedorRepository;
import com.gestorventas.deposito.services.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


import java.util.List;
import java.util.Map;


/**
 * Controlador REST encargado de exponer los endpoints relacionados con los Clientes,
 * anidados bajo el recurso Vendedor.
 *
 * @author Kevin William Olarte Braun
 */
@RestController
//@RequestMapping("/api/vendedor/{idVendedor}/cliente")
@RequestMapping("/api/cliente")
@AllArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final VendedorRepository vendedorRepository;

    /**
     * Crear un nuevo cliente.
     *
     * @return DTO con los datos del cliente creado
     */
    @PostMapping
    @Operation(summary = "Crear un nuevo cliente", description = "Crea un nuevo cliente en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interlo", content = @Content) //TODO: CAMBIAR ESTO, FASE PRUIEBA
    })
    public ResponseEntity<ClienteResponseDto> add(
            Authentication auth,
            @RequestBody ClienteDto clienteDto

    ) {
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.add(clienteDto.getNombre(), clienteDto.getCif(),u.getId()));
    }


    /**
     * Listar todos los clientes de ese vendedor.
     * @return lista de DTOs con todos los vendedores
     */
    @GetMapping
    @Operation(summary = "Listar todos los clientes de un vendedor", description = "Listar todos los clientes de un vendedor")
    @ApiResponse(responseCode = "200", description = "Lista de clientes encontrados")
    public ResponseEntity<List<ClienteResponseDto>> getAll(
            Authentication auth
    ) {
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(clienteService.getAll(u.getId()));
    }

    @GetMapping("/admin")
    @Operation(summary = "Listar todos los clientes de un vendedor", description = "Listar todos los clientes de un vendedor")
    @ApiResponse(responseCode = "200", description = "Lista de clientes encontrados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClienteResponseDto>> getAll(
    ) {
        return ResponseEntity.ok(clienteService.getAll());
    }

    /**
     * Listar todos los clientes de ese vendedor.
     * @param idVendedor identificador del vendedor
     * @return lista de DTOs con todos los vendedores
     */
    @GetMapping("/vendedor/{idVendedor}")
    @Operation(summary = "Listar todos los clientes de un vendedor", description = "Listar todos los clientes de un vendedor")
    @ApiResponse(responseCode = "200", description = "Lista de clientes encontrados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClienteResponseDto>> getAllAdmin(@PathVariable Long idVendedor){
        return ResponseEntity.ok(clienteService.getAll(idVendedor));
    }

    /**
     * Obtener un cliente por su ID.
     *
     * @param idCliente identificador del cliente
     * @return DTO con los datos del vendedor
     */
    @GetMapping("/{idCliente}")
    @Operation(summary = "Obtener un cliente por su ID", description = "Obtener un cliente por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    public ResponseEntity<ClienteResponseDto> get(
            Authentication auth,
            @PathVariable Long idCliente) {

        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        ClienteResponseDto cliente;
        if(u.getRoles().contains(Role.ADMIN)){
            cliente = clienteService.get(idCliente);
        }else {
            cliente = clienteService.get(u.getId(), idCliente);

        }
        if (cliente == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cliente);
    }


    /**
     * Actualizar los datos de un cliente existente.
     *
     * @param idCliente identificador del cliente
     * @param dto datos actualizados
     * @return DTO con los datos del vendedor actualizado
     */
    @Operation(summary = "Actualizar los datos de un cliente", description = "Actualiza los datos de un cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado/ datos incorrectos a actualizar", content = @Content)
    })
    @PutMapping("/{idCliente}")
    public ResponseEntity<ClienteResponseDto> update(
            Authentication auth,
            @PathVariable Long idCliente,
            @RequestBody ClienteDto dto
    ) {
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        ClienteResponseDto cliente = clienteService.update(idCliente, dto.getNombre(), dto.getCif(), u.getId());
        if (cliente == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(cliente);
    }



    /**
     * Eliminar un cliente por su ID.
     *
     * @param idCliente identificador del cliente
     * @return código 204 si se eliminó correctamente
     */
    @Operation(summary = "Eliminar un cliente", description = "Elimina un cliente del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente eliminado")
    })
    @DeleteMapping("/{idCliente}")
    public ResponseEntity<Void> delete(
            Authentication auth,
            @PathVariable Long idCliente
    ) {
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        clienteService.delete(idCliente, u.getId());
        return ResponseEntity.noContent().build();
    }


    /**
     * Sacar un maepo con los gastos de un cliente de forma anual.
     * @param auth credenciales del usuario actual
     * @param idCliente identificador del cliente
     * @return Map<String, Double> con los gastos anuales.
     */
    @GetMapping("/{idCliente}/stats")
    @Operation(summary = "Obtener las estadisticas de un cliente", description = "listado anual de los gastos de un cliente")
    @ApiResponse(responseCode = "200", description = "Lista de clientes encontrados")
    public ResponseEntity<Map<String, Double>> getStats(Authentication auth,
                                                        @PathVariable Long idCliente){
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        if(u.getRoles().contains(Role.ADMIN))
            return ResponseEntity.ok(clienteService.getStats(idCliente));
        return ResponseEntity.ok(clienteService.getStats(idCliente, u.getId()));
    }







}

