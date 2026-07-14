package com.tato.motorepuestos.repository;
import com.tato.motorepuestos.model.Caja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface CajaRepository extends JpaRepository<Caja, Long> {
    Optional<Caja> findTopByUsuarioIdAndSucursalIdAndEstadoOrderByIdDesc(
            Long usuarioId, Long sucursalId, String estado);
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v " +
            "WHERE v.usuario.id = :usuarioId " +
            "AND v.sucursal.id = :sucursalId " +
            "AND v.metodoPago = 'Efectivo' " +
            "AND v.estadoVenta <> 'ANULADA' " +
            "AND v.fecha >= :desde")
    BigDecimal sumVentasEfectivoDesde(
            @Param("usuarioId") Long usuarioId,
            @Param("sucursalId") Long sucursalId,
            @Param("desde") LocalDateTime desde);
    @Query("SELECT c FROM Caja c " +
            "WHERE (:usuarioId IS NULL OR c.usuario.id = :usuarioId) " +
            "AND (:sucursalId IS NULL OR c.sucursal.id = :sucursalId) " +
            "AND (:estado IS NULL OR c.estado = :estado) " +
            "AND (:desde IS NULL OR c.fechaApertura >= :desde) " +
            "AND (:hasta IS NULL OR c.fechaApertura <= :hasta) " +
            "ORDER BY c.fechaApertura DESC")
    List<Caja> buscarConFiltros(
            @Param("usuarioId")  Long usuarioId,
            @Param("sucursalId") Long sucursalId,
            @Param("estado")     String estado,
            @Param("desde")      LocalDateTime desde,
            @Param("hasta")      LocalDateTime hasta);
    @Query("SELECT c FROM Caja c " +
            "WHERE c.usuario.id = :usuarioId " +
            "AND c.estado = 'CERRADA' " +
            "AND (:sucursalId IS NULL OR c.sucursal.id = :sucursalId) " +
            "AND (:desde IS NULL OR c.fechaApertura >= :desde) " +
            "AND (:hasta IS NULL OR c.fechaApertura <= :hasta) " +
            "ORDER BY c.fechaApertura ASC")
    List<Caja> findCerradasPorUsuarioEnRango(
            @Param("usuarioId")  Long usuarioId,
            @Param("sucursalId") Long sucursalId,
            @Param("desde")      LocalDateTime desde,
            @Param("hasta")      LocalDateTime hasta);
}