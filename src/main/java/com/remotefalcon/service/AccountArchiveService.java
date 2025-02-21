package com.remotefalcon.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.remotefalcon.library.quarkus.entity.Show;
import com.remotefalcon.repository.ShowRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.bson.Document;

import java.time.LocalDate;
import java.util.List;

@JBossLog
@ApplicationScoped
public class AccountArchiveService {
    @Inject
    ShowRepository showRepository;

    @Inject
    MongoClient mongoClient;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Scheduled(every = "24h")
    void runArchiveProcess() {
        log.info("Running archive process");
        this.archiveAccounts();
        log.info("Finished archive process");
    }

    private void archiveAccounts() {
        log.info("Getting shows with lastLoginDate older than 24 months (" + LocalDate.now().minusMonths(24).atStartOfDay() + ")");
        List<Show> showsOlderThan24Months = showRepository.getShowsOlderThan24Months();
        log.info("Found " + showsOlderThan24Months.size() + " shows with lastLoginDate older than 24 months");
        showsOlderThan24Months.forEach(show -> {
            if(this.backupAccount(show)) {
                showRepository.delete(show);
            }
        });
        log.info("Finished archiving accounts");
    }

    private boolean backupAccount(Show show) {
        MongoCollection<Document> collection = mongoClient.getDatabase("remote-falcon-archive").getCollection("show");
        try {
            String json = objectMapper.writeValueAsString(show);
            Document document = Document.parse(json);
            return collection.insertOne(document).wasAcknowledged();
        } catch (JsonProcessingException e) {
            log.error("Error while converting Show object to JSON: " + e.getMessage(), e);
            return false;
        }

    }
}
