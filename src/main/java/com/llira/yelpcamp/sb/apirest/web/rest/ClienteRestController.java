package com.llira.yelpcamp.sb.apirest.web.rest;

import com.llira.yelpcamp.sb.apirest.entity.Cliente;
import com.llira.yelpcamp.sb.apirest.service.ClienteService;
import com.llira.yelpcamp.sb.apirest.web.rest.vm.ClienteVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ClienteRestController
 *
 * @author llira
 * @version 1.0
 * @since 22/01/2021
 * <p>
 * Clase controlador que contiene las rutas del endpoint
 */
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"})
@RestController
@RequestMapping("/api")
@SuppressWarnings("unused")
public class ClienteRestController {
    private final Logger log = LoggerFactory.getLogger(ClienteRestController.class);

    @Autowired
    private ClienteService clienteService;

    /**
     * Método para obtener una lista de registros sin paginación
     *
     * @return {@link ResponseEntity}
     */
    @GetMapping("/clientes")
    public ResponseEntity<?> findAll() {
        log.info("info +");
        log.error("error +");
        log.warn("warn +");

        Map<String, Object> params = new HashMap<>();
        List<ClienteVM> clientes;
        try {
            clientes = clienteService.findAll().stream().map(ClienteVM::new).collect(Collectors.toList());
        } catch (DataAccessException e) {
            params.put("message", "Se produjo un error al consultar en la base de datos.");
            params.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(params, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(clientes, HttpStatus.OK);
    }

    /**
     * Método para obtener una lista paginada de los registros
     *
     * @param page  página actual
     * @param limit limite de registros
     * @param sort  campo a ordenar
     * @param order forma ascendente o descendente
     * @return {@link ResponseEntity}
     */
    @GetMapping("/clientes/paginated")
    public ResponseEntity<?> findAll(@RequestParam(name = "page", defaultValue = "0") Integer page,
                                     @RequestParam(name = "limit", defaultValue = "5") Integer limit,
                                     @RequestParam(name = "sort", defaultValue = "id") String sort,
                                     @RequestParam(name = "order", defaultValue = "desc") String order) {
        log.info("Obtenemos la lista de los registros");
        Map<String, Object> params = new HashMap<>();
        Page<ClienteVM> clientes;
        try {
            Pageable pageable = order.equals("asc")
                    ? PageRequest.of(page, limit, Sort.by(sort))
                    : PageRequest.of(page, limit, Sort.by(sort).descending());
            clientes = clienteService.findAll(pageable).map(ClienteVM::new);
        } catch (DataAccessException e) {
            params.put("message", "Se produjo un error al consultar en la base de datos.");
            params.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(params, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(clientes, HttpStatus.OK);
    }

    /**
     * Método para obtener un registro en específico
     *
     * @param id identificador único del registro
     * @return {@link ResponseEntity}
     */
    @GetMapping("/clientes/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        Cliente cliente;
        Map<String, Object> params = new HashMap<>();
        try {
            cliente = clienteService.findById(id);
        } catch (DataAccessException e) {
            params.put("message", "Se produjo un error al consultar en la base de datos.");
            params.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(params, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (cliente == null) {
            params.put("message", "No se encontro el cliente.");
            return new ResponseEntity<>(params, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(cliente, HttpStatus.OK);
    }

    /**
     * Método para crear un nuevo objeto
     *
     * @param data objeto a insertar en la base de datos
     * @return {@link ResponseEntity}
     */
    @PostMapping("/clientes")
    public ResponseEntity<?> insert(@Valid @RequestBody Cliente data, BindingResult result) {
        Map<String, Object> params = new HashMap<>();
        if (result.hasErrors()) {
            return validateField(result);
        }
        try {
            clienteService.save(data);
        } catch (DataAccessException e) {
            params.put("message", "Se produjo un error al insertar en la base de datos.");
            params.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(params, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    /**
     * Método para editar un objeto
     *
     * @param data objeto a editar
     * @param id   identificador único del registro a editar
     * @return {@link ResponseEntity}
     */
    @PutMapping("/clientes/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Cliente data, BindingResult result, @PathVariable Long id) {
        Cliente cliente = clienteService.findById(id);
        Map<String, Object> params = new HashMap<>();
        if (result.hasErrors()) {
            return validateField(result);
        }
        if (cliente == null) {
            params.put("message", "No se encontro el cliente.");
            return new ResponseEntity<>(params, HttpStatus.NOT_FOUND);
        }
        try {
            cliente.setApellido(data.getApellido());
            cliente.setNombre(data.getNombre());
            cliente.setEmail(data.getEmail());
            cliente.setCreatedAt(data.getCreatedAt());
            clienteService.save(cliente);
        } catch (DataAccessException e) {
            params.put("message", "Se produjo un error al editar en la base de datos.");
            params.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(params, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    /**
     * Método para eliminar un registro
     *
     * @param id identificador único del registro a eliminar
     * @return {@link ResponseEntity}
     */
    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Map<String, Object> params = new HashMap<>();
        try {
            deleteImage(id);
            clienteService.delete(id);
        } catch (DataAccessException e) {
            params.put("message", "Se produjo un error al eliminar en la base de datos.");
            params.put("error", e.getMessage() + ": " + e.getMostSpecificCause().getMessage());
            return new ResponseEntity<>(params, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    /**
     * Método para subir una imagen
     *
     * @param image imagen del cliente
     * @param id    identificador único del registro
     * @return {@link ResponseEntity}
     */
    @PostMapping("/clientes/upload")
    public ResponseEntity<?> upload(@RequestParam("image") MultipartFile image, @RequestParam("id") Long id) {
        Map<String, Object> params = new HashMap<>();
        Cliente cliente = clienteService.findById(id);

        if (!image.isEmpty()) {
            String originalFilename = UUID.randomUUID().toString() + "_" +
                    Objects.requireNonNull(image.getOriginalFilename()).replace(" ", "");
            Path path = Paths.get("uploads").resolve(originalFilename).toAbsolutePath();
            try {
                Files.copy(image.getInputStream(), path);
            } catch (IOException e) {
                params.put("message", "Se produjo un error al subir la imagen.");
                params.put("error", e.getMessage() + ": " + e.getCause().getMessage());
                return new ResponseEntity<>(params, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            deleteImage(id);
            cliente.setImagen(originalFilename);
            clienteService.save(cliente);
            params.put("message", "Se ha subido correctamente la foto.");
        }
        return new ResponseEntity<>(params, HttpStatus.CREATED);
    }

    /**
     * Método para validar los campos obligatorios
     *
     * @param result objeto que contiene el resultado de las validaciones
     * @return {@link ResponseEntity}
     */
    private ResponseEntity<?> validateField(BindingResult result) {
        log.info("Validaciones generales");
        Map<String, Object> params = new HashMap<>();
        List<String> errors = result.getFieldErrors().stream()
                .map(e -> "El campo [" + e.getField().toUpperCase() + "] " + e.getDefaultMessage())
                .collect(Collectors.toList());
        params.put("errors", errors);
        params.put("message", "La petición contiene errores.");
        return new ResponseEntity<>(params, HttpStatus.BAD_REQUEST);
    }

    /**
     * Método para eliminar una foto del cliente
     *
     * @param id identificador único del registro
     */
    private void deleteImage(Long id) {
        log.info("Vamos a eliminar la foto.");
        Cliente cliente = clienteService.findById(id);
        String imagen = cliente.getImagen();
        if (imagen != null && imagen.length() > 0) {
            Path path = Paths.get("uploads").resolve(imagen).toAbsolutePath();
            File file = path.toFile();
            if (file.exists() && file.canRead()) {
                file.delete();
            }
        }
    }
}