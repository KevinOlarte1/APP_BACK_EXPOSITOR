package com.gestorventas.deposito.services;

import com.gestorventas.deposito.dto.out.ConfigDtoResponse;
import com.gestorventas.deposito.models.ParametrosGlobales;
import com.gestorventas.deposito.repositories.ParametrosGlobalesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ParametrosGlobalesService {

    private final ParametrosGlobalesRepository repo;

    public Integer getIva(){
        return  repo.findById(1L)
                .orElseThrow(() ->new RuntimeException("Config sin datos."))
                .getIva();
    }

    public Integer getDescuento(){
        return repo.findById(1L)
                .orElseThrow(()->  new RuntimeException("Config sin datos."))
                .getDescuento();
    }

    public Integer getGrupoMax(){
        return repo.findById(1L)
                .orElseThrow(()->new RuntimeException("Config sin datos."))
                .getGrupoMax();
    }

    public void set(Integer iva, Integer descuento, Integer grupoMax){
        ParametrosGlobales param = repo.findById(1L).orElse( new ParametrosGlobales());
        if (iva != null && iva >= 0)
            param.setIva(iva);

        if (descuento != null  && descuento >= 0)
            param.setDescuento(descuento);

        if (grupoMax != null  && grupoMax >= 0)
            param.setGrupoMax(grupoMax);

        repo.save(param);
    }


    public ConfigDtoResponse getAll() {
        ParametrosGlobales param = repo.findById(1L)
                .orElseThrow(() -> new RuntimeException("Config sin datos."));

        Integer iva = param.getIva() == null ? 0 : param.getIva();
        Integer descuento = param.getDescuento() == null ? 0 : param.getDescuento();
        Integer grupoMax = param.getGrupoMax() == null ? 1 : param.getGrupoMax();

        return new ConfigDtoResponse(iva, descuento, grupoMax);
    }
    @Transactional
    public void ensureDefaults() {
        if (repo.findById(1L).isEmpty()) {
            ParametrosGlobales p = new ParametrosGlobales();
            p.setId(1L);              // si tu entidad permite setId; si es @GeneratedValue, NO lo pongas
            p.setIva(21);
            p.setDescuento(0);
            p.setGrupoMax(4);
            repo.save(p);
        }
    }



}
