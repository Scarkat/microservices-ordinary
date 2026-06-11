package com.productos.productoservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productos.productoservice.dto.ProductKafkaDto;
import com.productos.productoservice.dto.ProductoDto;
import com.productos.productoservice.models.Products;
import com.productos.productoservice.response.GeneralResponse;
import com.productos.productoservice.services.CreateProductosService;
import com.productos.productoservice.services.DeleteProdcutosService;
import com.productos.productoservice.services.ReadProductosServices;
import com.productos.productoservice.services.UpdateProductosService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/productos")
@Log4j2
public class ProductController {

    private final ReadProductosServices readService;
    private final CreateProductosService createService;
    private final UpdateProductosService updateService;
    private final DeleteProdcutosService deleteService;

    public ProductController(ReadProductosServices readService, 
                             CreateProductosService createService,
                             UpdateProductosService updateService, 
                             DeleteProdcutosService deleteService) {
        this.readService = readService;
        this.createService = createService;
        this.updateService = updateService;
        this.deleteService = deleteService;
    }

    //1. Obtener todos los productos
    @GetMapping
    public ResponseEntity<GeneralResponse<List<Products>>> obtenerTodos() {
        log.info("Petición GET recibida: Solicitando la lista completa de productos");
        
        List<Products> lista = readService.obtenerTodos();
        
        log.info("Petición GET finalizada: Se devolvieron {} productos", lista.size());
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Productos obtenidos correctamente", lista));
    }

    // 2. Obtener por ID 
    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<Products>> obtenerPorId(@PathVariable String id) {
        
        log.info("Petición GET recibida: Buscando producto por ID [{}]", id);
        
        Products producto = readService.obtenerPorId(id);
        
        log.info("Petición GET exitosa: Producto encontrado con ID [{}]", producto.getId());
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Producto encontrado", producto));
    }

    // 3. Obtener por Nombre (Búsqueda por filtro)
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<GeneralResponse<Products>> obtenerPorNombre(@PathVariable String nombre) {
        
        log.info("Petición GET recibida: Buscando producto por nombre [{}]", nombre);
        
        Products producto = readService.obtenerPorNombre(nombre);
        
        log.info("Petición GET exitosa: Producto encontrado con nombre [{}]", producto.getNombre());
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Producto encontrado", producto));
    }

    // 3. Crear Producto
    @PostMapping
    public ResponseEntity<GeneralResponse<Products>> crearProducto(@Valid @RequestBody ProductoDto request) {
        // En seguridad, logueamos qué intentan crear (ej. el código), pero evitamos imprimir todo el objeto por si trae datos sensibles
        log.info("Petición POST recibida: Intentando crear un nuevo producto con nombre [{}]", request.getNombre());
        
        Products creado = createService.crearProducto(request);
        
        log.info("Petición POST exitosa: Producto creado y guardado en BD con el nuevo ID [{}]", creado.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GeneralResponse<>("SUCCESS", "Producto creado exitosamente", creado));
    }

    // 4. Actualizar Producto
    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<Products>> actualizarProducto(
            @PathVariable String id, 
            @Valid @RequestBody ProductoDto request) {
        
        log.info("Petición PUT recibida: Intentando actualizar el producto con ID [{}]", id);
        
        Products actualizado = updateService.actualizarProducto(id, request);
        
        log.info("Petición PUT exitosa: Producto con ID [{}] actualizado correctamente", id);
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Producto actualizado exitosamente", actualizado));
    }

    @PutMapping("/reducir/{nombre}/{cantidad}")
    public ResponseEntity<GeneralResponse<Products>> reducirInventario(
            @PathVariable String nombre,
            @PathVariable int cantidad) {
        log.info("Petición PUT recibida: Reduciendo inventario para [{}] en {} unidades", nombre, cantidad);
        Products actualizado = updateService.reducirInventario(nombre, cantidad);
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Inventario reducido correctamente", actualizado));
    }

    // 5. Eliminar Producto
    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<String>> eliminarProducto(@PathVariable String id) {
        // Usamos log.warn para operaciones destructivas, resalta visualmente en la consola y en CloudWatch
        log.warn("Petición DELETE recibida: Solicitud para eliminar el producto con ID [{}]", id);
        
        deleteService.eliminarProducto(id);
        
        log.info("Petición DELETE exitosa: Producto con ID [{}] eliminado de la base de datos", id);
        return ResponseEntity.ok(new GeneralResponse<>("SUCCESS", "Producto eliminado exitosamente", null));
    }

    @GetMapping("/validar-stock/{nombre}/{cantidad}")
    public ResponseEntity<Boolean> validarStock(@PathVariable String nombre, @PathVariable int cantidad) {
        log.info("Validando stock para producto [{}] con cantidad requerida [{}]", nombre, cantidad);
        Products p = readService.obtenerPorNombre(nombre);
        boolean hayStock = p.getCantidad() >= cantidad;
        return ResponseEntity.ok(hayStock);
    }

    @GetMapping("/validar-stock-id/{id}/{cantidad}")
    public ResponseEntity<Boolean> validarStockId(@PathVariable String id, @PathVariable int cantidad) {
        log.info("Validando stock para producto ID [{}] con cantidad requerida [{}]", id, cantidad);
        Products p = readService.obtenerPorId(id);
        boolean hayStock = p.getCantidad() >= cantidad;
        return ResponseEntity.ok(hayStock);
    }

    // Error forzado en Kafka
    @PostMapping("/fail")
    public ResponseEntity<GeneralResponse<String>> failProductCreation(@Valid @RequestBody ProductKafkaDto producto) {

        createService.crearProductoFail(producto); // Simulamos un error al crear el producto
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GeneralResponse<>("ERROR", "Error simulado al crear el producto", null));
    }
}
