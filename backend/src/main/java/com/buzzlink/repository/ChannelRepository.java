package com.buzzlink.repository;

import com.buzzlink.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    /**
     * Find channel by name
     */
    Optional<Channel> findByName(String name);
}
