package com.santi.turnero.peluquero.repository;

import com.santi.turnero.peluquero.domain.Peluquero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeluqueroRepository extends JpaRepository<Peluquero, Long> {

    List<Peluquero> findByPeluqueriaIdOrderByNombreAsc(Long peluqueriaId);

    java.util.Optional<Peluquero> findByUsuarioIgnoreCaseAndActivoTrue(String usuario);
}
