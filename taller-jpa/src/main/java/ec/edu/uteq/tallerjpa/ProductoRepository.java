package ec.edu.uteq.tallerjpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Consulta derivada: Hibernate la traduce a un PreparedStatement parametrizado.
    // Imposible de inyectar por concatenación, aunque el valor venga de un atacante.
    List<Producto> findByNombre(String nombre);
}
