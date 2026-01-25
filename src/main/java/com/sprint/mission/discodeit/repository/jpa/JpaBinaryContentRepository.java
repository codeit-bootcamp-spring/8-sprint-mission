package com.sprint.mission.discodeit.repository.jpa;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

public interface JpaBinaryContentRepositoryInterface extends JpaRepository<BinaryContent, UUID> {
}

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jpa")
public class JpaBinaryContentRepository implements BinaryContentRepository {
    private final JpaBinaryContentRepositoryInterface jpaRepository;

    public JpaBinaryContentRepository(JpaBinaryContentRepositoryInterface jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        return jpaRepository.save(binaryContent);
    }

    @Override
    public java.util.Optional<BinaryContent> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public java.util.List<BinaryContent> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
