package com.santi.turnero.peluqueria.repository;

import com.santi.turnero.peluqueria.domain.Peluqueria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeluqueriaRepository extends JpaRepository<Peluqueria, Long> {
}
