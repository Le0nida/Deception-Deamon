package io.swagger.log;

import io.swagger.log.logmodel.HttpRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<HttpRequestLog, Long> {

}

