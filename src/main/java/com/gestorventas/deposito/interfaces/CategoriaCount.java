package com.gestorventas.deposito.interfaces;

import com.gestorventas.deposito.enums.CategoriaProducto;

public interface CategoriaCount {
    CategoriaProducto getCategoria();
    Long getTotal();
}
