package com.gestorventas.deposito.controllers;

import com.gestorventas.deposito.dto.in.LineaPedidoDto;
import com.gestorventas.deposito.dto.out.LineaPedidoResponseDto;
import com.gestorventas.deposito.enums.Role;
import com.gestorventas.deposito.models.Vendedor;
import com.gestorventas.deposito.repositories.VendedorRepository;
import com.gestorventas.deposito.services.LineaPedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cliente/{idCliente}/pedido/{idPedido}/linea")
@AllArgsConstructor
public class LineaPedidoController {

    private final LineaPedidoService lineaPedidoService;
    private final VendedorRepository vendedorRepository;

    /**
     * Crear una nueva linea de pedido.
     * @param idCliente identificador del cliente que va a realizar el pedido.
     * @param idPedido identificador del pedido que va a realizar el pedido.
     * @param lineaDto datos de la linea de pedido.
     * @return DTO con los datos guardados visibles.
     */
    @PostMapping
    @Operation(summary = "Crear una nueva linea de pedido", description = "Crea una nueva linea de pedido en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Linea de pedido creada correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno", content = @Content) //TODO: CAMBIAR ESTO, FASE PRUIEBA
    })
    public ResponseEntity<LineaPedidoResponseDto> addLinea(
            Authentication auth,
            @PathVariable Long idCliente,
            @PathVariable Long idPedido,
            @RequestBody LineaPedidoDto lineaDto){
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        Long idVendedor = u.getId();
        if (u.getRoles().contains(Role.ADMIN)){
            return ResponseEntity.status(HttpStatus.CREATED).body(lineaPedidoService.add(idCliente, idPedido, lineaDto.getIdProducto(), lineaDto.getCantidad(), lineaDto.getPrecio(), lineaDto.getGrupo()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(lineaPedidoService.add(idVendedor, idCliente, idPedido, lineaDto.getIdProducto(), lineaDto.getCantidad(), lineaDto.getPrecio(), lineaDto.getGrupo()));
    }

    /**
     * Obtener una linea de pedido por su id.
     * @param idCliente identificador del cliente
     * @param idPedido identificador del pedido
     * @param idLinea identificador de la linea de pedido
     * @return DTO con los datos guardados visibles.
     */
    @GetMapping("/{idLinea}")
    @Operation(summary = "Obtener una linea de pedido por su id", description = "Obtener una linea de pedido por su id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Linea de pedido encontrada"),
            @ApiResponse(responseCode = "404", description = "Linea de pedido no encontrada", content = @Content)
    })
    public ResponseEntity<LineaPedidoResponseDto> getLinea(
            Authentication auth,
            @PathVariable Long idCliente,
            @PathVariable Long idPedido,
            @PathVariable Long idLinea){
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        Long idVendedor = u.getId();
        List<LineaPedidoResponseDto>  list= lineaPedidoService.get(idLinea, idPedido, idVendedor, idCliente);
        if(list.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list.get(0));
    }

    /**
     * Obtener una linea de pedido por su id.
     * @param idCliente identificador del cliente
     * @param idPedido identificador del pedido
     * @param idLinea identificador de la linea de pedido
     * @return DTO con los datos guardados visibles.
     */
    @GetMapping("/{idLinea}/admin")
    @Operation(summary = "Obtener una linea de pedido por su id", description = "Obtener una linea de pedido por su id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Linea de pedido encontrada"),
            @ApiResponse(responseCode = "404", description = "Linea de pedido no encontrada", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LineaPedidoResponseDto> getLineaAdmin(
            @PathVariable Long idCliente,
            @PathVariable Long idPedido,
            @PathVariable Long idLinea){
        List<LineaPedidoResponseDto>  list= lineaPedidoService.get(idLinea, idPedido, idCliente);
        if(list.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list.get(0));
    }

    /**
     * Obtener todas las lineas de un pedido.
     * @param idCliente identificador del cliente
     * @param idPedido identificador del pedido
     * @return Listado DTO con todos los clientes.
     */
    @GetMapping
    @Operation(summary = "Obtener todas las lineas de un pedido", description = "Obtener todas las lineas de un pedido")
    @ApiResponse(responseCode = "200", description = "Lista de lineas encontradas")
    public ResponseEntity<List<LineaPedidoResponseDto>> getAllLineas(
            Authentication auth,
            @PathVariable Long idCliente,
            @PathVariable Long idPedido){
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        if(u.getRoles().contains(Role.ADMIN)){
            return ResponseEntity.ok(lineaPedidoService.get(null,idPedido,null,idCliente));
        }
        Long idVendedor = u.getId();
        return ResponseEntity.ok(lineaPedidoService.get(null,idPedido,idVendedor,idCliente));
    }

    /**
     * Obtener todas las lineas de un pedido.
     * @param idCliente identificador del cliente
     * @param idPedido identificador del pedido
     * @return Listado DTO con todos los clientes.
     */
    @GetMapping("/admin")
    @Operation(summary = "Obtener todas las lineas de un pedido", description = "Obtener todas las lineas de un pedido")
    @ApiResponse(responseCode = "200", description = "Lista de lineas encontradas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LineaPedidoResponseDto>> getAllLineas(
            @PathVariable Long idCliente,
            @PathVariable Long idPedido){
        return ResponseEntity.ok(lineaPedidoService.get(null,idPedido,null,idCliente));
    }

    @PutMapping("/{idLinea}")
    public ResponseEntity<LineaPedidoResponseDto> updateLinea(
            Authentication auth,
            @PathVariable Long idCliente,
            @PathVariable Long idPedido,
            @PathVariable Long idLinea,
            @RequestBody LineaPedidoDto lineaDto){
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        Long idVendedor = u.getId();
        if (u.getRoles().contains(Role.ADMIN)) {
            return ResponseEntity.ok(lineaPedidoService.update(idLinea, lineaDto.getCantidad(), lineaDto.getPrecio(), null));
        }
        return ResponseEntity.ok(lineaPedidoService.update(idLinea, lineaDto.getCantidad(), lineaDto.getPrecio(), idVendedor));
    }

    @PutMapping("/{idLinea}/stock")
    public ResponseEntity<LineaPedidoResponseDto> putStockFinal(
            Authentication auth,
            @PathVariable Long idCliente,
            @PathVariable Long idPedido,
            @PathVariable Long idLinea,
            @RequestBody LineaPedidoDto lineaDto){
        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        Long idVendedor = u.getId();
        if (u.getRoles().contains(Role.ADMIN)) {
            return ResponseEntity.ok(lineaPedidoService.putStockFinal(idLinea, lineaDto.getStockFinal(), null));
        }
        return ResponseEntity.ok(lineaPedidoService.putStockFinal(idLinea, lineaDto.getStockFinal(), idVendedor));

    }


    /**
     * Eliminar una linea de un pedido.
     * @param idCliente identificador del cliente
     * @param idPedido identificador del pedido
     * @param idLinea identificador de la linea de pedido
     * @return DTO con los datos guardados visibles.
     */

    @DeleteMapping("/{idLinea}")
    @Operation(summary = "Eliminar una linea de un pedido", description = "Elimina una linea de un pedido")
    @ApiResponse(responseCode = "204", description = "Linea de pedido eliminada", content = @Content)
    public ResponseEntity<Void> deleteAllLineas(
            Authentication auth,
            @PathVariable Long idCliente,
            @PathVariable Long idPedido,
            @PathVariable Long idLinea){

        var email = auth.getName();
        Vendedor u = vendedorRepository.findByEmail(email).orElseThrow();
        Long idVendedor = u.getId();
        if (u.getRoles().contains(Role.ADMIN)) {
            lineaPedidoService.delete(idCliente, idPedido, idLinea);
        }
        else
            lineaPedidoService.delete(idVendedor, idCliente, idPedido, idLinea);
        return ResponseEntity.noContent().build();
    }







}
