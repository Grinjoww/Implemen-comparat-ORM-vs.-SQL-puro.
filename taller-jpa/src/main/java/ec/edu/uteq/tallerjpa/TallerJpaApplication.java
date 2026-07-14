package ec.edu.uteq.tallerjpa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class TallerJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TallerJpaApplication.class, args);
    }

    /**
     * Ejecuta el mismo guion que Main.java del proyecto taller-jdbc-puro,
     * para que los resultados sean directamente comparables en la tabla.
     */
    @Component
    static class Runner implements CommandLineRunner {

        private final ProductoRepository repo;

        Runner(ProductoRepository repo) {
            this.repo = repo;
        }

        @Override
        public void run(String... args) {

            // ------------------------------------------------------------
            // 1) Demo: intento de inyección SQL contra la consulta derivada
            //    (Spring Data JPA la parametriza siempre; no hay versión
            //    "insegura" equivalente porque el framework no lo permite)
            // ------------------------------------------------------------
            String ataque = "' OR '1'='1";
            System.out.println("=== SPRING DATA JPA (consulta derivada parametrizada) ===");
            List<Producto> filasAtaque = repo.findByNombre(ataque);
            System.out.println("Filas devueltas al atacante: "
                    + filasAtaque.size()
                    + " (correcto: 0 — Hibernate nunca concatena el valor al SQL)");

            // ------------------------------------------------------------
            // 2) Medición de findAll() con System.nanoTime()
            // ------------------------------------------------------------
            long inicio = System.nanoTime();
            List<Producto> lista1 = repo.findAll();
            long fin = System.nanoTime();
            double ms1 = (fin - inicio) / 1_000_000.0;
            System.out.printf("nanoTime : %d filas en %.3f ms%n", lista1.size(), ms1);

            // ------------------------------------------------------------
            // 3) Medición de findAll() con Spring StopWatch
            // ------------------------------------------------------------
            StopWatch sw = new StopWatch("listar-jpa");
            sw.start("findAll");
            List<Producto> lista2 = repo.findAll();
            sw.stop();
            System.out.printf("StopWatch: %d filas en %.3f ms%n",
                    lista2.size(),
                    sw.getTotalTimeNanos() / 1_000_000.0);
            System.out.println(sw.prettyPrint());

            // ------------------------------------------------------------
            // 4) Crear un producto nuevo
            // ------------------------------------------------------------
            Producto nuevo = repo.save(
                    new Producto("Producto de prueba", new BigDecimal("99.99"), 5));
            System.out.println("Creado con id = " + nuevo.getId());

            // ------------------------------------------------------------
            // 5) Eliminarlo para dejar la base como estaba
            // ------------------------------------------------------------
            repo.deleteById(nuevo.getId());
            boolean sigueExistiendo = repo.existsById(nuevo.getId());
            System.out.println("Eliminado: " + !sigueExistiendo);
        }
    }
}
