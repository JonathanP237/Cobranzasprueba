package com.ud.cobranzas.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "creditos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreditoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "credito_origen_id", unique = true)
    private Integer creditoOrigenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cobrador_id", nullable = false)
    private CobradorEntity cobrador;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @Column(name = "frecuencia_pago", nullable = false, length = 20)
    private String frecuenciaPago;

    @Column(name = "tasa_interes", nullable = false, precision = 5, scale = 2, insertable = false, updatable = false)
    private BigDecimal tasaInteres;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "AL_DIA";

    public CobradorEntity getCobrador() {
        return cobrador;
    }
}