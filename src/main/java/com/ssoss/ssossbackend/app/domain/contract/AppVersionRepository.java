package com.ssoss.ssossbackend.app.domain.contract;

import java.util.Optional;

import com.ssoss.ssossbackend.app.domain.model.AppOs;
import com.ssoss.ssossbackend.app.domain.model.AppVersion;

import org.springframework.data.repository.CrudRepository;

public interface AppVersionRepository extends CrudRepository<AppVersion, Long> {

    Optional<AppVersion> findByOs(AppOs os);
}
