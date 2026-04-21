package com.manyakakkar.DataFlowX.repository;
import com.manyakakkar.DataFlowX.entity.RegUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegUsersRepository extends JpaRepository<RegUsers,Long> {
    Optional<RegUsers> findByEmail(String email);


    boolean existsByEmail(String email);

}
