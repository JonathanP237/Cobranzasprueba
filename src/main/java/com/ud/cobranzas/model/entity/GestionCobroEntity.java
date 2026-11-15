package com.ud.cobranzas.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "gestiones_cobro")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GestionCobroEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credito_id", nullable = false)
    private CreditoEntity credito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuota_id", nullable = false)
    private CuotaEntity cuota;

    @Column(name = "fecha_gestion", nullable = false)
    private LocalDateTime fechaGestion;

    @Column(name = "tipo_gestion", nullable = false, length = 30)
    private String tipoGestion;

    @Column(name = "resultado", length = 100)
    private String resultado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}