# Taller GA — Implementación y comparativa ORM vs. SQL puro

**Universidad Técnica Estatal de Quevedo**
Facultad de Ciencias de la Computación y Diseño Digital · Ingeniería de Software
Asignatura: Aplicaciones Web (5.° semestre)
Docente: Dr. Gleiston Cicerón Guerrero Ulloa, Ph.D. · Período: PPA 2026–2027

**Autor:** Mariscal Cabrera Jaime Josué
**Fecha:** 13/7/2026

---

## Objetivo

Implementar el mismo CRUD de 3 operaciones (`listar`, `crear`, `eliminar`) dos veces
sobre la misma tabla `productos` de PostgreSQL 16:

1. Con **JDBC puro** y `PreparedStatement` (sin frameworks de acceso a datos).
2. Con **Spring Data JPA + Hibernate**.

Y comparar ambas implementaciones en 4 criterios: líneas de código, tiempo de la
consulta de listado con 100 registros, facilidad de mantenimiento y prevención de
inyección SQL.

## Estructura del repositorio

```
.
├── src/main/java/ec/edu/uteq/taller/   # Módulo 1: JDBC puro
│   ├── Conexion.java
│   ├── Producto.java
│   ├── ProductoRepositorioJdbc.java     # listar / crear / eliminar (PreparedStatement)
│   ├── ProductoRepositorioInseguro.java # contraejemplo: SQLi por concatenación
│   └── Main.java                        # benchmark + demo de inyección
├── pom.xml                              # pom del módulo JDBC puro
│
├── taller-jpa/                          # Módulo 2: Spring Data JPA + Hibernate
│   ├── src/main/java/ec/edu/uteq/tallerjpa/
│   │   ├── Producto.java                # @Entity
│   │   ├── ProductoRepository.java      # extends JpaRepository<Producto, Long>
│   │   └── TallerJpaApplication.java    # benchmark + demo de findByNombre
│   ├── src/main/resources/application.yml
│   ├── pom.xml                          # pom del módulo JPA
│   └── README.md                        # instrucciones específicas de este módulo
│
└── Criterio.pdf                         # portada del taller (nombre, fecha, link)
```

Cada módulo tiene su propio `pom.xml` y se abre/ejecuta como proyecto Maven
independiente en IntelliJ. Ambos apuntan a la misma base `taller_db` con la misma
tabla `productos` (100 registros sembrados con `generate_series`).

## Cómo ejecutar cada módulo

Requisitos previos: PostgreSQL 16 corriendo, base `taller_db`, usuario
`taller`/`taller`, tabla `productos` con 100 filas.

- **JDBC puro:** abre la raíz del repo como proyecto Maven, ejecuta
  `src/main/java/ec/edu/uteq/taller/Main.java`.
- **Spring Data JPA:** abre la carpeta `taller-jpa/` como proyecto Maven aparte,
  ejecuta `TallerJpaApplication.java` (más detalle en `taller-jpa/README.md`).

Ambos programas imprimen en consola la demo de inyección SQL y los tiempos de
`listar()` / `findAll()` medidos con `System.nanoTime()` y `StopWatch`.

## Tabla comparativa final

| Criterio | JDBC puro (`PreparedStatement`) | Spring Data JPA + Hibernate |
|---|---|---|
| **(1) Líneas de código** (repositorio + conexión, solo CRUD listar/crear/eliminar) | `Conexion.java` (16) + `ProductoRepositorioJdbc.java` CRUD (51) = **67 líneas** | `Producto.java` (37) + `ProductoRepository.java` (6) = **43 líneas** |
| **(2) Tiempo del listado (100 filas)** | nanoTime: **26.611 ms** / StopWatch: **29.441 ms** | nanoTime: **21.251 ms** / StopWatch: **6.170 ms** |
| **(3) Facilidad de mantenimiento** | El SQL está en cadenas Java: cambios de esquema exigen retocar cada consulta a mano; el mapeo `ResultSet`→objeto es boilerplate repetido en cada método. | El mapeo lo hace Hibernate vía anotaciones (`@Entity`, `@Column`); no se escribe SQL para el CRUD básico; los métodos de búsqueda se derivan del nombre (`findByNombre`). |
| **(4) Prevención de SQL Injection** | Se previene *si* el desarrollador usa `PreparedStatement` con `?`; si concatena strings (como en `ProductoRepositorioInseguro`), la vulnerabilidad reaparece — depende de la disciplina de quien programa. | Se previene **por defecto**: toda consulta generada por Hibernate (derivada o `@Query`) usa `PreparedStatement` parametrizado internamente; no hay forma fácil de romperlo por accidente. |

*Equipo de medición: los tiempos se tomaron descartando la primera ejecución
(warm-up de la JVM/Hibernate).*

## Conclusión

JDBC puro da control total sobre el SQL y un rendimiento competitivo una vez
"caliente", a costa de más código repetitivo y de que la seguridad ante
inyección SQL depende de que el desarrollador use `PreparedStatement`
consistentemente. Spring Data JPA reduce drásticamente las líneas de código y
garantiza consultas parametrizadas por defecto, aunque paga un costo de
arranque más alto en la primera consulta por la inicialización de Hibernate.
