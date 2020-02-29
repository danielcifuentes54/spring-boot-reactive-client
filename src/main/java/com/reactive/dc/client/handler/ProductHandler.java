package com.reactive.dc.client.handler;

import com.reactive.dc.client.dto.ProductDTO;
import com.reactive.dc.client.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProductHandler {

    @Autowired
    ProductService productService;

    public Mono<ServerResponse> list(ServerRequest request){
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.findAll(), ProductDTO.class);
    }

    public Mono<ServerResponse> getById(ServerRequest request){
        String id = request.pathVariable("id");

         /*
         //No validate Empty
         return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.findById(id), ProductDTO.class);*/

       return productService.findById(id)
                .flatMap(p -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(p))
                        .switchIfEmpty(ServerResponse.notFound().build()))
                .onErrorResume(this::notFoundError);
    }

    public Mono<ServerResponse> create(ServerRequest request){
        Mono<ProductDTO> product = request.bodyToMono(ProductDTO.class);

        return product.flatMap(p -> productService.save(p))
                .flatMap(p -> ServerResponse
                        .created(URI.create("/api/client/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(p)))
                .onErrorResume(error -> {
                    WebClientResponseException errorResponse = (WebClientResponseException) error;
                    if(errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST)
                        return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(errorResponse.getResponseBodyAsString()));
                    return Mono.error(errorResponse);
                });
    }

    public Mono<ServerResponse> edit(ServerRequest request) {
        Mono<ProductDTO> product = request.bodyToMono(ProductDTO.class);
        String id = request.pathVariable("id");

        return product
                .flatMap(p -> productService.update(p, id))
                .flatMap(p -> ServerResponse
                        .created(URI.create("/api/client/".concat(id)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(p)))
                .onErrorResume(this::notFoundError);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return productService.delete(id).then(ServerResponse.noContent().build())
                .onErrorResume(this::notFoundError);

    }

    public Mono<ServerResponse> upload(ServerRequest request){
        String id = request.pathVariable("id");
        return request.multipartData()
                .map(multiPart -> multiPart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productService.upload(file, id))
                .flatMap(p -> ServerResponse
                        .created(URI.create("/api/client/".concat(id)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(p)))
                .onErrorResume(this::notFoundError);
    }

    private Mono<ServerResponse> notFoundError(Throwable error){
        WebClientResponseException errorResponse = (WebClientResponseException) error;
        if(errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
            Map<String, Object> body = new HashMap<>();
            body.put("Error", "Product not exist ".concat(errorResponse.getMessage()));
            body.put("Timestamp", LocalDateTime.now());
            body.put("status", errorResponse.getStatusCode().value());
            return ServerResponse.status(HttpStatus.NOT_FOUND).body(BodyInserters.fromValue(body));
        }
        return Mono.error(errorResponse);
    }
}
