package com.reactive.dc.client.services;

import com.reactive.dc.client.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
public class ProductServiceImpl implements ProductService {

    @Qualifier("registerWebClient")
    @Autowired
    private WebClient.Builder client;

    @Override
    public Flux<ProductDTO> findAll() {
        return client.build().get()
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .flatMapMany(response -> response.bodyToFlux(ProductDTO.class));
    }

    @Override
    public Mono<ProductDTO> findById(String id) {
        return client.build().get()
                .uri("{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ProductDTO.class);
                //.exchange()
                //.flatMap(response -> response.bodyToMono(ProductDTO.class));
    }

    @Override
    public Mono<ProductDTO> save(ProductDTO productDTO) {
        return client.build().post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(productDTO))
                .retrieve()
                .bodyToMono(ProductDTO.class);
    }

    @Override
    public Mono<ProductDTO> update(ProductDTO productDTO, String id) {
        return client.build().put()
                .uri("{id}", Collections.singletonMap("id", id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(productDTO))
                .retrieve()
                .bodyToMono(ProductDTO.class);
    }

    @Override
    public Mono<Void> delete(String id) {
        return client.build().delete()
                .uri("{id}", Collections.singletonMap("id", id))
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<ProductDTO> upload(FilePart file, String id) {
        MultipartBodyBuilder part = new MultipartBodyBuilder();
        part.asyncPart("file", file.content(), DataBuffer.class).headers(h -> {
            h.setContentDispositionFormData("file", file.filename());
        });
        return client.build().post()
                .uri("upload/{id}", Collections.singletonMap("id", id))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromValue(part.build()))
                .retrieve()
                .bodyToMono(ProductDTO.class);
    }
}
