package com.remotefalcon.repository;

import com.remotefalcon.library.quarkus.entity.Show;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ShowRepository implements PanacheMongoRepository<Show> {
    public List<Show> getShowsOlderThan24Months() {
        return list("lastLoginDate < ?1 or lastLoginDate is null", LocalDate.now().minusMonths(24).atStartOfDay());
    }
}
