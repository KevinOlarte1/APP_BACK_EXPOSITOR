package com.gestorventas.deposito.specifications;

import com.gestorventas.deposito.models.producto.Producto;
import org.springframework.data.jpa.domain.Specification;

public class ProductosSpecifications {

    public static Specification<Producto> withFilter(Long idCategoria){

        return (root, query, cb) ->{
            var predicate = cb.conjunction();

            if (idCategoria != null) {
                predicate = cb.and(
                        predicate,
                        cb.equal(root.get("categoria").get("id"), idCategoria)
                );
            }
            assert query != null;
            query.orderBy(cb.asc(root.get("id")));

            return predicate;
        };
    }
}
