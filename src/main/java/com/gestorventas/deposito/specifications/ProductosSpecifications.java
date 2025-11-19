package com.gestorventas.deposito.specifications;

import com.gestorventas.deposito.enums.CategoriaProducto;
import com.gestorventas.deposito.models.Producto;
import org.springframework.data.jpa.domain.Specification;

public class ProductosSpecifications {

    public static Specification<Producto> withFilter(CategoriaProducto categoriaProducto){

        return (root, query, cb) ->{
            var predicate = cb.conjunction();

            if (categoriaProducto != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("categoria"), categoriaProducto));
            }
            assert query != null;
            query.orderBy(cb.asc(root.get("id")));

            return predicate;
        };
    }
}
