package com.examplo.pagoamigos.service;

import com.examplo.pagoamigos.model.Product;
import com.examplo.pagoamigos.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Optional;

@Service
public class ProductIdGeneratorService {

    private final ProductRepository productRepository;

    public ProductIdGeneratorService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public synchronized String generateId() {
        int year = Year.now().getValue();
        String prefix = "SL-" + year + "-";

        Page<Product> page = productRepository.findByIdStartingWith(prefix, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id")));

        int next = 1;
        if (page.hasContent()) {
            Product p = page.getContent().get(0);
            String lastId = p.getId();
            try {
                String[] parts = lastId.split("-");
                String lastSeq = parts[parts.length - 1];
                next = Integer.parseInt(lastSeq) + 1;
            } catch (Exception ignored) {
            }
        }

        return prefix + next;
    }
}
