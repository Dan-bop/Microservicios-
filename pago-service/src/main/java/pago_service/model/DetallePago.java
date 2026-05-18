package pago_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_pagos")
@Data
public class DetallePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    @Column(nullable = false, length = 20)
    private String mes;

    @Column(nullable = false)
    private Integer anio;

    @Column(name = "monto_mes", precision = 10, scale = 2)
    private BigDecimal montoMes;
}