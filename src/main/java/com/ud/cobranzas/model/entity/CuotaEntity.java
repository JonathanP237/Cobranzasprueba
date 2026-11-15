package com.ud.cobranzas.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cuotas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CuotaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credito_id", nullable = false)
    private CreditoEntity credito;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "monto_cuota", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoCuota;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";
}